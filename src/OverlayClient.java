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
			ip = parts[0];
			
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
			if(r.isAlive() || w.isAlive()) {
				
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
					userIp = readIn.readLine();

					System.out.print("Enter message to send: ");
					userMessage = readIn.readLine();

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
			// TODO: i don't know if it goes UDP header over IP header
			// of it is the other way;
			byte[] send1 = "";
			byte[] send2 = "";
			byte[] sendData = new byte[send1.length+send2.length];
			for(int i = 0; i < send1.length; i++) {
				sendData[i] = send1[i];
			}
			for(int i = 0; i < send2.length; i++) {
				sendData[i] = send2[i];
			}
			try {
				DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,InetAddress.getByName(pre),port);
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

			ipHead.setSrcAddress(ip);
			ipHead.setDstAddress(dstIP);
			ipHead.setCheckSum(ipCheckSum());
			
			// TODO
			ipHead.setIden();
			ipHead.setFlags();
			ipHead.setOffset();

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