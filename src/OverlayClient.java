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
			ipHeader.setVersion("0100");	// this is always 4 since we are using ipv4 
			ipHeader.setIhl("0101"); // I believe this is 5 bytes as far as I can tell, so storing it in an integer as 5. 
			ipHeader.setTos("00000000");	// Not doing anything with this, so 0
			ipHeader.setTotalLength((Integer.parseInt(ipHeader.getVersion(), 2) * Integer.parseInt(ipHeader.getIhl(), 2)) 
					+ (message.getBytes().length * 8)); 	// If I'm understanding this right, total length is the header (20 bytes) + data (the message)
			ipHeader.setTtl("00000110");	// Set as 6 since there are 6 nodes total on our overlay network, so I'm guessing at most there would be 6 hops...right? Will need to decrement this value in our router class
			ipHeader.setProtocol("00010001");	// UDP - 17
			ipHeader.setSrcAddress(InetAddress.getLocalHost().getHostAddress().toString());
			ipHeader.setDstAddress(dstIP);
			ipHeader.setCheckSum(ipCheckSum());
			
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
	
	public static String ipCheckSum(){
		String result = "";
		String a = "", b = "";
		String temp = "";
		
		a += ipHeader.getVersion();
		a += ipHeader.getIhl();
		a += ipHeader.getTos();
		b += Integer.toBinaryString(ipHeader.getTotalLength());
		temp = Integer.toBinaryString(Integer.parseInt(a, 2) + Integer.parseInt(b, 2));
		
		a = temp;
		b = ipHeader.getTtl();
		b += ipHeader.getProtocol();
		temp = Integer.toBinaryString(Integer.parseInt(a, 2) + Integer.parseInt(b, 2));
		
		a = temp;
		b = addrToBinary(ipHeader.getSrcAddress());
		temp = Integer.toBinaryString(Integer.parseInt(a, 2) + Integer.parseInt(b, 2));
		
		a = temp;
		b = addrToBinary(ipHeader.getDstAddress());
		temp = Integer.toBinaryString(Integer.parseInt(a, 2) + Integer.parseInt(b, 2));
		
		result = complement(temp);
		return result;
	}
	
	public static String addrToBinary(String addr){
		String addrBits = "";
		String[] temp = addr.split("\\.");
	    int j;
		for(int i = 0; i < 4; i++){
			j = Integer.parseInt(temp[i]);
			temp[i] = ("00000000" + Integer.toBinaryString(j)).substring(Integer.toBinaryString(j).length());
			addrBits += temp[i];
		}
		
		return addrBits;
	}
	
	public static String complement(String comp){
		String result = "";
		String temp = comp;
		char[] c = null;
		
		for(int i = 0; i < temp.length(); i++){
			c[i] = temp.charAt(i);
			if(c[i] == '0')
				c[i] = '1';
			else
				c[i] = '0';
		}
		
		result = c.toString();
		return result;
	}
}
