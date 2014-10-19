import java.io.*;
import java.net.*;

public class OverlayClient {
	private Read r;
	private Write w;
	
	public OverlayClient() {
		r = new Read();
		r.start();
		w = new Write();
		w.start();
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
					userIp = readIn.readLine();

					System.out.print("Enter message to send: ");
					userMessage = readIn.readLine();

					if (userIp.equals("quit") || userMessage.equals("quit")) {
						readIn.close();
						break;
					}

					buildPacket(userMessage, userIp);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		
		public void buildPacket(String message, String dstIP) {
			try {
				/** Build the IP Header */
				ipHead.setVersion("0100"); // this is always 4 since we are using
												// ipv4
				ipHead.setIhl("0101"); // I believe this is 5 bytes as far as I
											// can tell, so storing it in an integer
											// as 5.
				ipHead.setTos("00000000"); // Not doing anything with this, so 0
				ipHead.setTotalLength((Integer.parseInt(ipHead.getVersion(), 2) * Integer
						.parseInt(ipHead.getIhl(), 2))
						+ (message.getBytes().length * 8)); // If I'm understanding
															// this right, total
															// length is the header
															// (20 bytes) + data
															// (the message)
				ipHead.setTtl("00000110"); // Set as 6 since there are 6 nodes
												// total on our overlay network, so
												// I'm guessing at most there would
												// be 6 hops...right? Will need to
												// decrement this value in our
												// router class
				ipHead.setProtocol("00010001"); // UDP - 17

				// TODO - Swap this to get the address of the node we are
				// transmitting from, instead of local host. May need to parse the
				// config file, or switch it to manual entry in main above
				// as we are doing with obtaining the dst address
				ipHead.setSrcAddress(InetAddress.getLocalHost().getHostAddress()
						.toString());
				ipHead.setDstAddress(dstIP);
				ipHead.setCheckSum(ipCheckSum());

				/** Build the UDP Header */
				udpHead.setSrcPort("0010011010010100"); // 9876
				udpHead.setDstPort("0010011010010100"); // 9876
				/* Need to get proper padded length of data */
				String zero8 = "00000000";
				String padString = "";
				int messageLength = message.getBytes().length * 8;
				int pad = messageLength % 2;
				if (pad == 1) {
					padString = zero8;
				}
				int udpLength = 12 + 8 + messageLength + pad;
				udpHead.setLength(udpLength);
				udpHead.setCheckSum(udpCheckSum());

			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		public  String ipCheckSum() {
			String result = "";
			String a = "", b = "";
			String temp = "";

			a += ipHead.getVersion();
			a += ipHead.getIhl();
			a += ipHead.getTos();
			b += Long.toBinaryString(ipHead.getTotalLength());
			temp = Long.toBinaryString(Long.parseLong(a, 2) + Long.parseLong(b, 2));

			a = temp;
			b = ipHead.getTtl();
			b += ipHead.getProtocol();
			temp = Long.toBinaryString(Long.parseLong(a, 2) + Long.parseLong(b, 2));

			a = temp;
			b = addrToBinary(ipHead.getSrcAddress());
			temp = Long.toBinaryString(Long.parseLong(a, 2) + Long.parseLong(b, 2));

			a = temp;
			b = addrToBinary(ipHead.getDstAddress());
			temp = Long.toBinaryString(Long.parseLong(a, 2) + Long.parseLong(b, 2));

			result = complement(temp);
			return result;
		}

		public  String udpCheckSum() {
			String result = "";
			String a = "", b = "";
			String temp = "";

			a = addrToBinary(ipHead.getSrcAddress());
			b = addrToBinary(ipHead.getDstAddress());
			temp = Long.toBinaryString(Long.parseLong(a, 2) + Long.parseLong(b, 2));

			a = temp;
			b = ipHead.getProtocol(); // UDP Protocol - 17
			temp = Long.toBinaryString(Long.parseLong(a, 2) + Long.parseLong(b, 2));

			a = temp;
			b = Long.toBinaryString(udpHead.getLength());
			temp = Long.toBinaryString(Long.parseLong(a, 2) + Long.parseLong(b, 2));

			result = complement(temp);
			return result;
		}

		public String addrToBinary(String addr) {
			String addrBits = "";
			String[] temp = addr.split("\\.");
			int j;
			for (int i = 0; i < 4; i++) {
				j = Integer.parseInt(temp[i]);
				temp[i] = ("00000000" + Integer.toBinaryString(j))
						.substring(Integer.toBinaryString(j).length());
				addrBits += temp[i];
			}

			return addrBits;
		}

		public  String complement(String comp) {
			String result = "";
			char[] temp = comp.toCharArray();
			char[] c = null;

			// flip each bit
			for (int i = 0; i < temp.length; i++) {
				if (temp[i] == '0')
					temp[i] = '1';
				else
					temp[i] = '0';
			}

			result = new String(temp);
			return result;
		}
	}

	private class Read extends Thread {
		public void run() {

		}
	}
}