import java.io.*;
import java.net.*;


public class OverlayClient {

	public static void main(String[] args) {
		InetAddress IP;
		BufferedReader readIn = new BufferedReader(new InputStreamReader(System.in));
		
		String userIp;
		String userMessage;
		
		try {
			System.out.print("Enter IP address to send to: ");
			userIp = readIn.readLine();
		
		
			System.out.print("Enter message to send: ");
			userMessage = readIn.readLine();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/* Constructor for the packet to send to the OverlayRouter */
	public static void buildPacket(String message, String dstIP){
		try {
			String zero8 = "00000000";
			String protocol = "00010001";
			String src = InetAddress.getLocalHost().toString();
			String dst = dstIP;
			String padString = "";
			
			/* src & dst ports */
			String port = "0010011010010100";
			int messageLength = message.getBytes().length * 8;
			int pad = messageLength % 2;
			
			if (pad == 1) {
				padString = zero8;
			}
			
			
			int udpLength = 12 + 8 + messageLength + pad;
			String udpCheckSum = "";
			String data = message;
			
			byte[] by = src.getBytes();
			
			StringBuilder binary = new StringBuilder();
			  for (byte b : by)
			  {
			     int val = b;
			     for (int i = 0; i < 8; i++)
			     {
			        binary.append((val & 128) == 0 ? 0 : 1);
			        val <<= 1;
			     }
			  }
			String srcBits = binary.toString();
			
			by = dst.getBytes();
			
			binary = new StringBuilder();
			  for (byte b : by)
			  {
			     int val = b;
			     for (int i = 0; i < 8; i++)
			     {
			        binary.append((val & 128) == 0 ? 0 : 1);
			        val <<= 1;
			     }
			  }
			String destBits = binary.toString();
			
			while(srcBits.length() < 32) {
				srcBits = "0" + srcBits;
			}
			
			while(destBits.length() < 32) {
				destBits = "0" + destBits;
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
