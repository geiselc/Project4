import java.io.*;
import java.net.*;


public class OverlayClient {
	
	public static IPHeader ipHeader;
	public static UDPHeader udpHeader;

	public static void main(String[] args) {
		InetAddress IP;
		BufferedReader readIn = new BufferedReader(new InputStreamReader(System.in));
		
		String userIp;
		String userMessage;
		
		ipHeader = new IPHeader();
		udpHeader = new UDPHeader();
		
		try {
			System.out.print("Enter IP address to send to: ");
			userIp = readIn.readLine();
		
			System.out.print("Enter message to send: ");
			userMessage = readIn.readLine();
			
			buildPacket(userMessage, userIp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/* Constructor for the packet to send to the OverlayRouter */
	public static void buildPacket(String message, String dstIP){
		try {
			/** Build the IP Header */
			ipHeader.setVersion(4);	// this is always 4 since we are using ipv4 
			ipHeader.setIhl(20); // I believe this is 20 bytes as far as I can tell, so storing it in an integer as 20. 
			ipHeader.setTos(0);	// Not doing anything with this, so ... 0?
			ipHeader.setTotalLength(20 + (message.getBytes().length * 8)); 	// If I'm understanding this right, total length is the header (20 bytes) + data (the message)
			ipHeader.setTtl(6);	// Set as 6 since there are 6 nodes total on our overlay network, so I'm guessing at most there would be 6 hops...right? Will need to decrement this value in our router class
			ipHeader.setProtocol("00010001");
			//checksum
			ipHeader.setSrcAddress(InetAddress.getLocalHost().toString());
			ipHeader.setDstAddress(dstIP);
			
			/** Build the UDP Header */
			udpHeader.setSrcPort("0010011010010100");	// 9876
			udpHeader.setDstPort("0010011010010100");	// 9876
				/* Need to get proper padded length of data */
				String zero8 = "00000000";
				String padString = "";
				int messageLength = message.getBytes().length * 8;
				int pad = messageLength % 2;
				
				if (pad == 1) {
					padString = zero8;
				}
				
				
				int udpLength = 12 + 8 + messageLength + pad;
			udpHeader.setLength(udpLength);
			// checksum
			
/** The old code is listed below: **/
//			String zero8 = "00000000";
//			String protocol = "00010001";
//			String src = InetAddress.getLocalHost().toString();
//			String dst = dstIP;
//			String padString = "";
//			
//			/* src & dst ports */
//			String port = "0010011010010100";
//			int messageLength = message.getBytes().length * 8;
//			int pad = messageLength % 2;
//			
//			if (pad == 1) {
//				padString = zero8;
//			}
//			
//			
//			int udpLength = 12 + 8 + messageLength + pad;
//			String udpCheckSum = "";
//			String data = message;
//			
//			byte[] by = src.getBytes();
//			
//			StringBuilder binary = new StringBuilder();
//			  for (byte b : by)
//			  {
//			     int val = b;
//			     for (int i = 0; i < 8; i++)
//			     {
//			        binary.append((val & 128) == 0 ? 0 : 1);
//			        val <<= 1;
//			     }
//			  }
//			String srcBits = binary.toString();
//			
//			by = dst.getBytes();
//			
//			binary = new StringBuilder();
//			  for (byte b : by)
//			  {
//			     int val = b;
//			     for (int i = 0; i < 8; i++)
//			     {
//			        binary.append((val & 128) == 0 ? 0 : 1);
//			        val <<= 1;
//			     }
//			  }
//			String destBits = binary.toString();
//			
//			while(srcBits.length() < 32) {
//				srcBits = "0" + srcBits;
//			}
//			
//			while(destBits.length() < 32) {
//				destBits = "0" + destBits;
//			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 
	}
}
