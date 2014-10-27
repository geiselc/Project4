import java.io.*;
import java.net.*;

public class OverlayClient {
	private Read r;
	private Write w;
	private String ip;
	private String pre;
	private static String file = "host-10A.txt";
	private DatagramSocket clientSocket;
	private int port = 9876;

	public static void main(String[] args) {
		new OverlayClient(args[0]);
	}

	public OverlayClient(String number) {

		// read in from correct configuration file
		file = file.replaceAll("A", number);
		String line;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			line = br.readLine();
			String[] parts = line.split(" ");
			ip = parts[1];

			line = br.readLine();
			parts = line.split(" ");
			pre = parts[2];
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			clientSocket = new DatagramSocket();
		} catch (IOException e) {
			e.printStackTrace();
			clientSocket.close();
			return;
		}

		// start threads
		r = new Read();
		r.start();
		w = new Write();
		w.start();
		while (true) {
			if (r.isAlive() || w.isAlive()) {

			} else {
				clientSocket.close();
				break;
			}
		}
	}

	private class Write extends Thread {
		private IPHeader ipHead;
		private UDPHeader udpHead;

		public Write() {
			ipHead = new IPHeader();
			udpHead = new UDPHeader();
		}

		public void run() {
			BufferedReader readIn = new BufferedReader(new InputStreamReader(
					System.in));
			String userIp;
			String userMessage;
			while (true) {
				try {
					System.out.print("Enter IP address to send to: ");
					userIp = readIn.readLine().trim();
					System.out.print("Enter message to send: ");
					userMessage = readIn.readLine().trim();
					if (userIp.equals("quit") || userMessage.equals("quit")) {
						readIn.close();
						break;
					}
					buildPacket(userMessage, userIp);
					sendPacket();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		public void sendPacket() {
			byte[] send1 = ipHead.getMessageData();
			byte[] send2 = udpHead.getMessageData();
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
						sendData.length, InetAddress.getByName(pre), port);
				clientSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void buildPacket(String message, String dstIP) {
			/** Build the IP Header */
			ipHead.setVersion("0100"); // this is always 4 since we are using
			// ipv4
			ipHead.setIhl("0101"); // I believe this is 5 bytes as far as I
			// can tell, so storing it in an integer
			// as 5.
			ipHead.setTos("00000000"); // Not doing anything with this, so 0
			// ipHead.setTotalLength((Integer.parseInt(ipHead.getVersion(), 2) *
			// Integer
			// .parseInt(ipHead.getIhl(), 2))
			// + (message.getBytes().length * 8)); // If I'm understanding
			// this right, total
			// length is the header
			// (20 bytes) + data
			// (the message)
			// ipHead.setTotalLength("0000000000000000");
			ipHead.setIden("0000000000000000");
			ipHead.setFlags("0000");
			ipHead.setOffset("000000000000");
			// ipHead.setTtl("00000110"); // Set as 6 since there are 6 nodes
			// total on our overlay network, so
			// I'm guessing at most there would
			// be 6 hops...right? Will need to
			// decrement this value in our
			// router class
			ipHead.setTtl("00000011");
			ipHead.setProtocol("00010001"); // UDP - 17
			ipHead.setSrcAddress(ipToBits(ip));
			ipHead.setDstAddress(ipToBits(dstIP));
			/** Build the UDP Header */
			udpHead.setSrcPort("0010011010010000"); // 9872
			udpHead.setDstPort("0010011010010000"); // 9872
			int messageLength = message.getBytes().length;
			String udpLength = Integer.toBinaryString(messageLength + 8);
			while (udpLength.length() < 16) {
				udpLength = "0" + udpLength;
			}
			udpHead.setLength(udpLength);
			udpHead.setData(message);
			String twenty = "00010100";
			String temp = Long.toBinaryString(Long.parseLong(udpLength, 2)
					+ Long.parseLong(twenty, 2));
			while (temp.length() < 16) {
				temp = "0" + temp;
			}
			ipHead.setTotalLength(temp);
			ipHead.setCheckSum(ipCheckSum());
			udpHead.setCheckSum(udpCheckSum());
		}

		public String ipCheckSum() {
			byte[] buf = ipHead.getCheckData();
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

		public String udpCheckSum() {
			return uc();
		}

		private String uc() {
			String toBytes = ipHead.srcAddress + ipHead.dstAddress + "00000000"
					+ ipHead.protocol;
			toBytes += udpHead.length + udpHead.srcPort + udpHead.dstPort
					+ udpHead.length;
			
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
						n[i + 1] = (byte) 0;
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

			String y = Long.toBinaryString(x);

			while (y.length() < 16) {
				y = "0" + y;
			}
			return y;
		}
	}

	public String ipToBits(String ipA) {
		String[] parts = ipA.split("\\.");
		String temp = "";
		for (int i = 0; i < parts.length; i++) {
			String current = parts[i];
			current = Integer.toBinaryString(Integer.parseInt(current));
			while (current.length() < 8) {
				current = "0" + current;
			}
			temp += current;
		}
		return temp;
	}

	private class Read extends Thread {
		private IPHeader ipHead;
		private UDPHeader udpHead;
		private ICMPHeader icHead;
		private DatagramSocket sendSocket;

		public Read() {
			ipHead = new IPHeader();
			udpHead = new UDPHeader();
			icHead = new ICMPHeader();
		}

		private void buildIP(byte[] data) {
			int pos = 0;
			String s = byteToBitString(data[pos++]);
			ipHead.setVersion(s.substring(0, 4));
			ipHead.setIhl(s.substring(4));
			ipHead.setTos(byteToBitString(data[pos++]));
			ipHead.setTotalLength(byteToBitString(data[pos++])
					+ byteToBitString(data[pos++]));
			ipHead.setIden(byteToBitString(data[pos++])
					+ byteToBitString(data[pos++]));
			s = byteToBitString(data[pos++]) + byteToBitString(data[pos++]);
			ipHead.setFlags(s.substring(0, 3));
			ipHead.setOffset(s.substring(3));
			ipHead.setTtl(byteToBitString(data[pos++]));
			ipHead.setProtocol(byteToBitString(data[pos++]));
			ipHead.setCheckSum(byteToBitString(data[pos++])
					+ byteToBitString(data[pos++]));
			ipHead.setSrcAddress(byteToBitString(data[pos++])
					+ byteToBitString(data[pos++])
					+ byteToBitString(data[pos++])
					+ byteToBitString(data[pos++]));
			ipHead.setDstAddress(byteToBitString(data[pos++])
					+ byteToBitString(data[pos++])
					+ byteToBitString(data[pos++])
					+ byteToBitString(data[pos++]));
		}

		private void buildUDP(byte[] data) {
			int pos = 0;
			udpHead.setSrcPort(byteToBitString(data[pos++])
					+ byteToBitString(data[pos++]));
			udpHead.setDstPort(byteToBitString(data[pos++])
					+ byteToBitString(data[pos++]));
			udpHead.setLength(byteToBitString(data[pos++])
					+ byteToBitString(data[pos++]));
			udpHead.setCheckSum(byteToBitString(data[pos++])
					+ byteToBitString(data[pos++]));
		}

		private void buildICMP(byte[] data) {
			int pos = 0;
			icHead.setType(byteToBitString(data[pos++]));
			icHead.setCode(byteToBitString(data[pos++]));
			icHead.setChecksum(byteToBitString(data[pos++])
					+ byteToBitString(data[pos++]));
			icHead.setRest(byteToBitString(data[pos++])
					+ byteToBitString(data[pos++])
					+ byteToBitString(data[pos++])
					+ byteToBitString(data[pos++]));
		}

		private String ipCheckSum(byte[] data) {
			byte[] buf = data;
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

		private String uc2(IPHeader ip, UDPHeader udp) {
			String toBytes = ip.srcAddress + ip.dstAddress + "00000000"
					+ ip.protocol;
			toBytes += udp.length + udp.srcPort + udp.dstPort
					+ udp.length;
			
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
			byte[] otherData = udp.data.getBytes();
			if (otherData.length % 2 == 1) {
				byte[] n = new byte[otherData.length + 1];
				for (int i = 0; i < otherData.length; i++) {
					n[i] = otherData[i];
					if (i + 1 == otherData.length) {
						n[i + 1] = (byte) 0;
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

			String y = Long.toBinaryString(x);

			while (y.length() < 16) {
				y = "0" + y;
			}
			return y;
		}

		public void run() {
			try {
				sendSocket = new DatagramSocket(9875);
			} catch (SocketException e1) {
				e1.printStackTrace();
			}
			while (true) {

				// receive the packet
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				try {

					sendSocket.receive(receivePacket);
				} catch (IOException e) {
					System.out.println("Sorry, didn't get anything");
					e.printStackTrace();
					return;
				}

				// get received packet into byte arrays
				byte[] packet = receivePacket.getData();
				byte[] ip = new byte[20];

				for (int i = 0; i < 20; i++) {
					ip[i] = packet[i];
				}
				buildIP(ip);
				udpHead.setData("");
				if (ipHead.protocol.equals("00010001")) { // udp
					byte[] udp = new byte[8];
					byte[] temp = new byte[1024];

					for (int i = 20, j = 0; i < packet.length; j++, i++) {
						if (i >= 20 && i <= 27) {
							udp[j] = packet[i];
						} else {
							String str = byteToBitString(packet[i]);
							char next = (char)Integer.parseInt(str, 2);
							udpHead.setData(udpHead.getData()+next);
						}
					}

					// verify IP Checksum
					String recvrIPCheckSum = ipCheckSum(ipHead.getCheckData());
					if (ipHead.checkSum.equals(recvrIPCheckSum)) {
						// verify UDP Checksum
						buildUDP(udp);
						String recvrUDPCheckSum = uc2(ipHead, udpHead);
						if (recvrUDPCheckSum.equals(udpHead.getCheckSum())) {
							System.out.println("\nMessage Recieved: "
									+ udpHead.getData());
						} else {
							// UDP Checksum mismatch
							System.out
									.println("Could not receive message: UDP Checksum mismatch");
						}
					} else {
						// IP Checksum mismatch
						System.out
								.println("Could not receive message: IP Checksum mismatch");
					}
				} else if (ipHead.protocol.equals("00000001")) { // icmp
					// else packet is type ICMP, so parse differently
					byte[] icmp = new byte[8];
					byte[] temp = new byte[1024];
					icHead.setData("");
					for (int i = 20, j = 0; i < packet.length; j++, i++) {
						if (i >= 20 && i <= 27) {
							icmp[j] = packet[i];
						} else {
							String str = byteToBitString(packet[i]);
							char next = (char)Integer.parseInt(str, 2);
							icHead.setData(icHead.getData()+next);
						}
					}
					
					
					buildICMP(icmp);
					System.out.println(icHead.getData());
				}
			}
		}

		private String byteToBitString(byte b) {
			return ("0000000" + Integer.toBinaryString(0xFF & b)).replaceAll(
					".*(.{8})$", "$1");
		}
	}
}
