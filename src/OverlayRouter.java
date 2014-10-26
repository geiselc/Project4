import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverlayRouter {
	private List<String> addresses;
	private Map<String, String> prefixes;
	private int port = 9875;
	private DatagramSocket routerGetSocket;
	private DatagramSocket routerSendSocket;

	public static void main(String[] args) {
		new OverlayRouter(args[0]);
	}

	public OverlayRouter(String file) {
		addresses = new ArrayList<String>();
		prefixes = new HashMap<String, String>();
		try {
			readFile(file);
			routerGetSocket = new DatagramSocket();
			routerSendSocket = new DatagramSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Read r = new Read();
		r.start();
		while (r.isAlive()) {

		}
		return;
	}

	private void readFile(String fileName) throws IOException {
		fileName = "router-10" + fileName + ".txt";

		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(" ");
			if (parts.length == 2) {
				addresses.add(parts[1]);
			} else if (parts.length == 3) {
				prefixes.put(parts[1], parts[2]);
			}
		}
		br.close();
	}

	private class Read extends Thread {
		public void run() {
			while (true) {
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				try {
					routerGetSocket.receive(receivePacket);
				} catch (IOException e) {
					System.out.println("Sorry, didn't get anything");
					return;
				}
				int length = 0;
				for (int i = 50; i < receiveData.length; i++) {
					byte b = receiveData[i];
					if ((int) b == 0) {
						length = i - 1;
						// do not want the first IP and UDP header
						length -= 28;
						break;
					}
				}

				// copy over the IP and UDP and data headers
				// that the OverlayClient added
				byte[] data = new byte[length];
				for (int i = 0; i < length; i++) {
					data[i] = receiveData[i + 28];
				}
				int pos = 0;

				// go through and create a ip and udp header based
				IPHeader ip = new IPHeader();
				UDPHeader udp = new UDPHeader();

				// IP header is bytes 0 through 19
				String s = byteToBitString(data[pos++]);
				ip.setVersion(s.substring(0, 4));
				ip.setIhl(s.substring(4));
				ip.setTos(byteToBitString(data[pos++]));
				ip.setTotalLength(byteToBitString(data[pos++])
						+ byteToBitString(data[pos++]));

				ip.setIden(byteToBitString(data[pos++])
						+ byteToBitString(data[pos++]));
				s = byteToBitString(data[pos++]) + byteToBitString(data[pos++]);
				ip.setFlags(s.substring(0, 3));
				ip.setOffset(s.substring(3));

				ip.setTtl(byteToBitString(data[pos++]));
				ip.setProtocol(byteToBitString(data[pos++]));
				ip.setCheckSum(byteToBitString(data[pos++])
						+ byteToBitString(data[pos++]));

				ip.setSrcAddress(byteToBitString(data[pos++])
						+ byteToBitString(data[pos++])
						+ byteToBitString(data[pos++])
						+ byteToBitString(data[pos++]));

				ip.setDstAddress(byteToBitString(data[pos++])
						+ byteToBitString(data[pos++])
						+ byteToBitString(data[pos++])
						+ byteToBitString(data[pos++]));

				// UDP header is bytes 20 through 27
				udp.setSrcPort(byteToBitString(data[pos++])
						+ byteToBitString(data[pos++]));
				udp.setDstPort(byteToBitString(data[pos++])
						+ byteToBitString(data[pos++]));
				udp.setLength(byteToBitString(data[pos++])
						+ byteToBitString(data[pos++]));
				udp.setCheckSum(byteToBitString(data[pos++])
						+ byteToBitString(data[pos++]));

				// data is the rest
				s = "";
				for (int i = pos; i < length; i++) {
					s = s + byteToBitString(data[i]);
				}
				udp.setData(s);

				String knowsPrefix = checkPrefix(ip.getDstAddress());

				if (knowsPrefix.equals("")) {
					doesnotKnowPrefix(ip, udp);
				} else if (ip.getTtl().equals("00000000")) {
					timeToLiveIsZero(ip, udp);
				} else {
					boolean validCheck = checkIPCheckSum(ip);
					if (!validCheck) {
						checkSumWasNotValid(ip, udp);
					} else {
						String t = ip.getTtl();
						int ttl = Integer.parseInt(t, 2);
						ttl-=1;
						t = Integer.toBinaryString(ttl);
						while(t.length() < 8) {
							t = "0"+t;
						}
						ip.setTtl(t);
						ip.setCheckSum(getCheckSum(ip.getCheckData()));
						normalPack(ip, udp, knowsPrefix);
					}
				}
			}
		}

		public void doesnotKnowPrefix(IPHeader ip, UDPHeader udp) {
			udp.setData("ERROR: UNKNOWN PREFIX");
			errorPacket(ip, udp);
		}

		public void timeToLiveIsZero(IPHeader ip, UDPHeader udp) {
			udp.setData("ERROR: TIME TO LIVE WAS ZERO");
			errorPacket(ip, udp);
		}

		public void checkSumWasNotValid(IPHeader ip, UDPHeader udp) {
			udp.setData("ERROR: IP CHECKSUM INVALID");
			errorPacket(ip, udp);
		}
		
		public void errorPacket(IPHeader ip, UDPHeader udp) {
			
			// update TTL to 2
			ip.setTtl("00000010");
			
			// change DEST to SRC
			ip.setDstAddress(ip.getSrcAddress());
			
			// look up prefix in table
			String dest = checkPrefix(ip.getDstAddress());
			
			// updates lengths
			int messageLength = udp.getData().getBytes().length;
			String udpLength = Integer.toBinaryString(messageLength + 8);
			while (udpLength.length() < 16) {
				udpLength = "0" + udpLength;
			}
			udp.setLength(udpLength);

			String twenty = "00010100";
			String temp = Long.toBinaryString(Long.parseLong(udpLength, 2)
					+ Long.parseLong(twenty, 2));
			while (temp.length() < 16) {
				temp = "0" + temp;
			}
			ip.setTotalLength(temp);
			
			// put in new checksums
			ip.setCheckSum(getCheckSum(ip.getCheckData()));
			udp.setCheckSum(uc(ip, udp));
			
			// send packet
			Write w = new Write(port, ip, udp, dest);
			w.start();
		}
		
		public void normalPack(IPHeader ip, UDPHeader udp, String dest) {
			Write w = new Write(port, ip, udp, dest);
			w.start();
		}

		public boolean checkIPCheckSum(IPHeader ip) {
			String check = ip.getCheckSum();
			String check2 = getCheckSum(ip.getCheckData());
			return check.equals(check2);
		}

		public String getCheckSum(byte[] input) {
			byte[] buf = input;
			int length = buf.length;
			int i = 0;
			long sum = 0;
			while (length > 0) {
				sum += (buf[i++] & 0xff) << 8;
				if ((--length) == 0)
					break;
				sum += (buf[i++] & 0xff);
				--length;
			}

			long x = (~((sum & 0xFFFF) + (sum >> 16))) & 0xFFFF;
			return Long.toBinaryString(x);
		}
		
		public String uc(IPHeader ipHead, UDPHeader udpHead) {

			String toBytes = ipHead.srcAddress + ipHead.dstAddress + "00000000"
					+ ipHead.protocol;
			toBytes += udpHead.length + udpHead.srcPort + udpHead.dstPort
					+ udpHead.length + udpHead.checkSum;

			byte[] toSend = new byte[toBytes.length() / 8];

			String temp = "";
			int count = 0;

			for (int i = 0; i < toBytes.length(); i++) {
				temp += "" + toBytes.charAt(i);
				if ((i + 1) % 8 == 0) {
					toSend[count++] = (byte) Integer.parseInt(temp, 2);
					temp = "";
				}
			}

			byte[] otherData = udpHead.data.getBytes();
			if (otherData.length % 2 == 1) {
				byte[] n = new byte[otherData.length + 1];
				for (int i = 0; i < otherData.length; i++) {
					n[i] = otherData[i];
					if (i + 1 == otherData.length) {
						n[i+1] = (byte) 0;
					}
				}
				otherData = n;
			}
			byte[] fin = new byte[toSend.length + otherData.length];
			for (int i = 0; i < toSend.length; i++) {
				fin[i] = toSend[i];
			}
			int j = toSend.length;
			for (int i = 0; i < otherData.length; i++) {
				fin[j++] = otherData[i];
			}
			byte[] buf = fin;
			int length = buf.length;
			int i = 0;
			long sum = 0;
			while (length > 0) {
				sum += (buf[i++] & 0xff) << 8;
				if ((--length) == 0)
					break;
				sum += (buf[i++] & 0xff);
				--length;
			}

			long x = (~((sum & 0xFFFF) + (sum >> 16))) & 0xFFFF;
			System.out.println(x+"");
			String y = Long.toBinaryString(x);
			System.out.println(y);
			while(y.length() < 16) {
				y = "0" + y;
			}
			return y;
		}
	}

	private String byteToBitString(byte b) {
		return ("0000000" + Integer.toBinaryString(0xFF & b)).replaceAll(
				".*(.{8})$", "$1");
	}

	private String checkPrefix(String prefix) {
		String ret = "";
		int one = Integer.parseInt(prefix.substring(0, 8), 2);
		int two = Integer.parseInt(prefix.substring(8, 16), 2);
		int three = Integer.parseInt(prefix.substring(16, 24), 2);
		int four = Integer.parseInt(prefix.substring(32), 2);
		String pre1 = one + "." + two + "." + three + "." + four;
		String[] input = pre1.split("\\.");

		String[] keys = prefixes.keySet().toArray(new String[0]);
		for (String key : keys) {
			int value = Integer.parseInt(key.substring(1 + key.indexOf('\\')));
			String pre2 = key.substring(key.indexOf('\\'));
			String[] known = pre2.split("\\.");

			boolean[] b = new boolean[4];
			for (int i = 0; i < 4; i++) {
				b[i] = input[i].equals(known[i]);
			}
			if (value == 8) {
				if (b[0]) {
					ret = prefixes.get(key);
					break;
				}
			} else if (value == 16) {
				if (b[0] && b[1]) {
					ret = prefixes.get(key);
					break;
				}
			} else if (value == 24) {
				if (b[0] && b[1] && b[2]) {
					ret = prefixes.get(key);
					break;
				}
			} else { // value == 32
				if (b[0] && b[1] && b[2] && b[3]) {
					ret = prefixes.get(key);
					break;
				}
			}
		}
		return ret;
	}

	private class Write extends Thread {
		private int port;
		private IPHeader ip;
		private UDPHeader udp;
		private String dest;

		public Write(int port, IPHeader ip, UDPHeader udp, String dest) {
			super();
			this.port = port;
			this.ip = ip;
			this.udp = udp;
			this.dest = dest;
		}
		
		public void run() {
			byte[] send1 = ip.getMessageData();
			byte[] send2 = udp.getMessageData();
			byte[] sendData = new byte[send1.length + send2.length];
			int count = 0;
			for (int i = 0; i < send1.length; i++) {
				sendData[count++] = send1[i];
			}
			for (int i = 0; i < send2.length; i++) {
				sendData[count++] = send2[i];
			}

			try {
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, InetAddress.getByName(dest), port);
				routerSendSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}