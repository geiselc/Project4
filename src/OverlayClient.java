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
			// TODO: i don't know if it goes UDP header over IP header
			// of it is the other way;
			byte[] send1 = ipHead.getMessageData();
			byte[] send2 = udpHead.getMessageData();
			byte[] sendData = new byte[send1.length+send2.length];
			int count = 0;
			for(int i = 0; i < send1.length; i++) {
				sendData[count++] = send1[i];
			}
			for (int i = 0; i < send2.length; i++) {
				sendData[count++] = send2[i];
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
//			ipHead.setTotalLength((Integer.parseInt(ipHead.getVersion(), 2) * Integer
//					.parseInt(ipHead.getIhl(), 2))
//					+ (message.getBytes().length * 8)); // If I'm understanding
														// this right, total
														// length is the header
														// (20 bytes) + data
														// (the message)
			//ipHead.setTotalLength("0000000000000000");
			
			ipHead.setIden("0000000000000000");
			ipHead.setFlags("0000");
			ipHead.setOffset("000000000000");
			//ipHead.setTtl("00000110"); // Set as 6 since there are 6 nodes
											// total on our overlay network, so
											// I'm guessing at most there would
											// be 6 hops...right? Will need to
											// decrement this value in our
											// router class
			ipHead.setTtl("00000001");
			ipHead.setProtocol("00010001"); // UDP - 17
			

			ipHead.setSrcAddress(ipToBits(ip));
			ipHead.setDstAddress(ipToBits(dstIP));

			/** Build the UDP Header */
			udpHead.setSrcPort("0010011010010100"); // 9876
			udpHead.setDstPort("0010011010010100"); // 9876
			/* Need to get proper padded length of data */
			String zero8 = "00000000";
			String padString = "";
			int messageLength = message.getBytes().length;
			/*
			int pad = messageLength % 2;
			if (pad == 1) {
				padString = zero8;
				message += padString;
			}  */
			String udpLength = Integer.toBinaryString(messageLength + 8);
			while(udpLength.length() < 16) {
				udpLength = "0" + udpLength;
			}
			udpHead.setLength(udpLength);
			udpHead.setData(message);
			
			//TODO Verify this is correct
			String twenty = "00010100";
			String temp = Long.toBinaryString(Long.parseLong(udpLength,2) + Long.parseLong(twenty, 2));
			while(temp.length() < 16) {
				temp = "0" + temp;
			}
			ipHead.setTotalLength(temp);
			
			ipHead.setCheckSum(ipCheckSum());
			udpHead.setCheckSum(udpCheckSum());
		}
		public  String ipCheckSum() {
			byte[] buf = ipHead.getCheckData();
			int length = buf.length;
		    int i = 0;
		    long sum = 0;
		    while (length > 0) {
		        sum += (buf[i++]&0xff) << 8;
		        if ((--length)==0) break;
		        sum += (buf[i++]&0xff);
		        --length;
		    }

		    long x = (~((sum & 0xFFFF)+(sum >> 16)))&0xFFFF;
		    return Long.toBinaryString(x);
		}

		public  String udpCheckSum() {
			/*
			String result = "";
			String a = "", b = "";
			String temp = "";

			a = ipHead.getSrcAddress();
			b = ipHead.getDstAddress();
			
			temp = Long.toBinaryString(Long.parseLong(a, 2) + Long.parseLong(b, 2));

			a = temp;
			b = ipHead.getProtocol(); // UDP Protocol - 17
			temp = Long.toBinaryString(Long.parseLong(a, 2) + Long.parseLong(b, 2));

			a = temp;
			b = udpHead.getLength();
			temp = Long.toBinaryString(Long.parseLong(a, 2) + Long.parseLong(b, 2));

			result = complement(temp);
			
			while(result.length() < 16) {
				result = "0" + result;
			} */
			/*
			String temp = ipHead.getSrcAddress() + ipHead.getDstAddress();
			String temp2 = ipHead.getProtocol();
			while(temp2.length() < 16) {
				temp2 = "0" + temp2;
			}
			temp += temp2 + udpHead.getLength();
			
			byte[] data = new byte[temp.length() / 8];
			
			String t = "";
			int count = 0;
			
			for(int i = 0; i < temp.length(); i++){
				t += ""+ temp.charAt(i);
				if((i+1) % 8 == 0){
					data[count++] = (byte)Integer.parseInt(t,2);
					t = "";
				}
			}
			
			byte[] buf = data;
			int length = data.length;
		    int i = 0;
		    long sum = 0;
		    while (length > 0) {
		        sum += (buf[i++]&0xff) << 8;
		        if ((--length)==0) break;
		        sum += (buf[i++]&0xff);
		        --length;
		    }

		    long x = (~((sum & 0xFFFF)+(sum >> 16)))&0xFFFF;
		    return Long.toBinaryString(x);
		    */
			return udpC();
		}
		
		public String udpC() {
			String one = "0"+ipHead.getSrcAddress().substring(0,16);
			String two = "0"+ipHead.getSrcAddress().substring(16);
			String three = "0"+ipHead.getDstAddress().substring(0,16);
			String four ="0"+ ipHead.getDstAddress().substring(16);
			String five = "0"+"00000000"+ipHead.protocol;
			String six = "0"+ ipHead.totalLength;
			
			int a = Integer.parseInt(one, 2);
			int b = Integer.parseInt(two, 2);
			int c = Integer.parseInt(three, 2);
			int d = Integer.parseInt(four, 2);
			int e = Integer.parseInt(five, 2);
			int f = Integer.parseInt(six, 2);
			
			int z = a + b + c + d + e + f;
			String total = Integer.toBinaryString(z);
			total = complement(total);
			
			a = Integer.parseInt(total,2);
			b = Integer.parseInt(udpHead.getSrcPort(),2);
			c = Integer.parseInt(udpHead.getDstPort(),2);
			d = Integer.parseInt(udpHead.getLength(),2);
			
			z = a + b + c + d;
			
			/*
			String data = udpHead.getData();
			byte[] bytes = data.getBytes();
			
			String bits = "";
			for (int i = 0; i < bytes.length; i++) {
				byte current = bytes[i];
				
			}
			*/
			
			byte[] first = Integer.toBinaryString(z).getBytes();
			byte[] second = udpHead.getData().getBytes();
			byte[] full = new byte[first.length + second.length];
			int count = 0;
			for(int i = 0; i < first.length; i++) {
				full[count++] = first[i];
			}
			for(int i = 0; i < second.length; i++) {
				full[count++] = second[i];
			}
			
			byte[] buf = full;
			int length = full.length;
		    int i = 0;
		    long sum = 0;
		    while (length > 0) {
		        sum += (buf[i++]&0xff) << 8;
		        if ((--length)==0) break;
		        sum += (buf[i++]&0xff);
		        --length;
		    }

		    long x = (~((sum & 0xFFFF)+(sum >> 16)))&0xFFFF;
		    return Long.toBinaryString(x);
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
		
		public String ipToBits(String ipA) {
			String[] parts = ipA.split("\\.");
			String temp = "";
			
			for(int i = 0; i < parts.length; i++) {
				String current = parts[i];
				current = Integer.toBinaryString(Integer.parseInt(current));
				while(current.length() < 8) {
					current = "0" + current;
				}
				temp += current;
			}
			
			return temp;
		}
	}

	private class Read extends Thread {
		public void run() {
			while(true) {
				
				// receive the packet
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
				try{
				    clientSocket.receive(receivePacket);
				} catch(IOException e){
				    System.out.println("Sorry, didn't get anything");
				    return;
				}
				
				// verify IP and UDP checksum
				
				// print out message
				
			}
		}
	}
}
