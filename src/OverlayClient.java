import java.io.*;
import java.net.*;
import java.util.zip.Adler32;

public class OverlayClient {
	private Read r;
	private Write w;
	private String ip;
	private String pre;
	private static String file = "host-10A.txt";
	private DatagramSocket clientSocket;
	private int port = 9875;

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
		ipHead.setTtl("00000001");
		ipHead.setProtocol("00010001"); // UDP - 17
		ipHead.setSrcAddress(ipToBits(ip));
		ipHead.setDstAddress(ipToBits(dstIP));
		/** Build the UDP Header */
		udpHead.setSrcPort("0010011010010100"); // 9876
		udpHead.setDstPort("0010011010010100"); // 9876
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
		/*
		* String result = ""; String a = "", b = ""; String temp = "";
		*
		* a = ipHead.getSrcAddress(); b = ipHead.getDstAddress();
		*
		* temp = Long.toBinaryString(Long.parseLong(a, 2) +
		* Long.parseLong(b, 2));
		*
		* a = temp; b = ipHead.getProtocol(); // UDP Protocol - 17 temp =
		* Long.toBinaryString(Long.parseLong(a, 2) + Long.parseLong(b, 2));
		*
		* a = temp; b = udpHead.getLength(); temp =
		* Long.toBinaryString(Long.parseLong(a, 2) + Long.parseLong(b, 2));
		*
		* result = complement(temp);
		*
		* while(result.length() < 16) { result = "0" + result; }
		*/
		/*
		* String temp = ipHead.getSrcAddress() + ipHead.getDstAddress();
		* String temp2 = ipHead.getProtocol(); while(temp2.length() < 16) {
		* temp2 = "0" + temp2; } temp += temp2 + udpHead.getLength();
		*
		* byte[] data = new byte[temp.length() / 8];
		*
		* String t = ""; int count = 0;
		*
		* for(int i = 0; i < temp.length(); i++){ t += ""+ temp.charAt(i);
		* if((i+1) % 8 == 0){ data[count++] = (byte)Integer.parseInt(t,2);
		* t = ""; } }
		*
		* byte[] buf = data; int length = data.length; int i = 0; long sum
		* = 0; while (length > 0) { sum += (buf[i++]&0xff) << 8; if
		* ((--length)==0) break; sum += (buf[i++]&0xff); --length; }
		*
		* long x = (~((sum & 0xFFFF)+(sum >> 16)))&0xFFFF; return
		* Long.toBinaryString(x);
		*/
		return uc();
		}
		public String udpC() {
		return "";
		/*
		* String one = "0"+ipHead.getSrcAddress().substring(0,16); String
		* two = "0"+ipHead.getSrcAddress().substring(16); String three =
		* "0"+ipHead.getDstAddress().substring(0,16); String four ="0"+
		* ipHead.getDstAddress().substring(16); String five =
		* "0"+"00000000"+ipHead.protocol; String six = "0"+
		* ipHead.totalLength;
		*
		* int a = Integer.parseInt(one, 2); int b = Integer.parseInt(two,
		* 2); int c = Integer.parseInt(three, 2); int d =
		* Integer.parseInt(four, 2); int e = Integer.parseInt(five, 2); int
		* f = Integer.parseInt(six, 2);
		*
		* int z = a + b + c + d + e + f; String total =
		* Integer.toBinaryString(z); total = complement(total);
		*
		* a = Integer.parseInt(total,2); b =
		* Integer.parseInt(udpHead.getSrcPort(),2); c =
		* Integer.parseInt(udpHead.getDstPort(),2); d =
		* Integer.parseInt(udpHead.getLength(),2);
		*
		* z = a + b + c + d;
		*/
		/*
		* String data = udpHead.getData(); byte[] bytes = data.getBytes();
		*
		* String bits = ""; for (int i = 0; i < bytes.length; i++) { byte
		* current = bytes[i];
		*
		* }
		*/
		/*
		* byte[] first = Integer.toBinaryString(z).getBytes(); byte[]
		* second = udpHead.getData().getBytes(); byte[] full = new
		* byte[first.length + second.length]; int count = 0; for(int i = 0;
		* i < first.length; i++) { full[count++] = first[i]; } for(int i =
		* 0; i < second.length; i++) { full[count++] = second[i]; }
		*
		* byte[] buf = full; int length = full.length; int i = 0; long sum
		* = 0; while (length > 0) { sum += (buf[i++]&0xff) << 8; if
		* ((--length)==0) break; sum += (buf[i++]&0xff); --length; }
		*
		* long x = (~((sum & 0xFFFF)+(sum >> 16)))&0xFFFF; return
		* Long.toBinaryString(x);
		*/
		}
		private String uc() {
		/*
		* long sum = 0;
		*
		* sum += (Integer.parseInt(ipHead.srcAddress, 2)>>>16)&0xFFFF; sum
		* += (Integer.parseInt(ipHead.srcAddress, 2))&0xFFFF; sum +=
		* (Integer.parseInt(ipHead.dstAddress, 2)>>>16)&0xFFFF; sum +=
		* (Integer.parseInt(ipHead.dstAddress, 2))&0xFFFF; sum +=
		* (Integer.parseInt(ipHead.protocol, 2)); sum +=
		* (Integer.parseInt(udpHead.length, 2)); sum +=
		* (Integer.parseInt(udpHead.srcPort, 2)); sum +=
		* (Integer.parseInt(udpHead.dstPort, 2)); sum +=
		* (Integer.parseInt(ipHead.totalLength,2));
		*
		* System.out.println(sum+"");
		*
		* byte[] c = udpHead.data.getBytes(); boolean pad = false;
		* if(c.length%2 == 1) { pad = true; } for (int i = 0; i+1 <
		* c.length; i+=2) { byte one = c[i]; byte two = c[i+1]; int a =
		* (int)one; int b = (int)two; String x =
		* Integer.toBinaryString(a)+""+Integer.toBinaryString(b); sum +=
		* (Integer.parseInt(x,2)); } if (pad) { byte one = c[c.length-1];
		* int a = (int)one; String x =
		* Integer.toBinaryString(a)+"00000000"; sum +=
		* (Integer.parseInt(x,2)); } System.out.println(sum+"");
		*
		* sum = ~sum;
		*
		* System.out.println(sum+"");
		*
		* String x = Long.toBinaryString(sum); System.out.println(x);
		*
		* return Long.toBinaryString(sum);
		*/
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
		private String udpCSum() {
		String result = "";
		String add = "0000000000000000";
		int a;
		String temp2;
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
		n[i] = (byte) 0;
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
		/*
		for (int i = 0; i < fin.length; i++) {
		String str = Byte.toString(fin[i]);
		}*/
		byte[] inputData = fin;
		long FF00 = 0xff00;
		long FF = 0xff;
		int length = inputData.length;
		int i = 0;
		long sum = 0;
		long data;
		// Handle all pairs
		while (length > 1) {
		data = (((inputData[i] << 8) & FF00) | ((inputData[i + 1]) & FF));
		sum += data;
		// 1's complement carry bit correction in 16-bits (detecting sign
		// extension)
		if ((sum & 0xFFFF0000) > 0) {
		sum = sum & 0xFFFF;
		sum += 1;
		}
		i += 2;
		length -= 2;
		}
		// Handle remaining byte in odd length inputDatafers
		if (length > 0) {
		sum += (inputData[i] << 8 & 0xFF00);
		// 1's complement carry bit correction in 16-bits (detecting sign
		// extension)
		if ((sum & 0xFFFF0000) > 0) {
		sum = sum & 0xFFFF;
		sum += 1;
		}
		}
		// Final 1's complement value correction to 16-bits
		long inverted = ~sum;
		inverted = inverted & 0xFFFF;
		String x = Long.toBinaryString(inverted);
		while (x.length() < 16) {
		x = "0"+x;
		}
		return x;
		/*
		a = (Integer.parseInt(ipHead.srcAddress, 2) >>> 16) & 0xFFFF;
		temp = "" + Integer.toBinaryString(a);
		add = binaryAdd(add, temp);
		System.out.println(temp);
		a = (Integer.parseInt(ipHead.srcAddress, 2)) & 0xFFFF;
		temp = "" + Integer.toBinaryString(a);
		add = binaryAdd(add, temp);
		System.out.println(temp);
		a = (Integer.parseInt(ipHead.dstAddress, 2) >>> 16) & 0xFFFF;
		temp = "" + Integer.toBinaryString(a);
		add = binaryAdd(add, temp);
		System.out.println(temp);
		a = (Integer.parseInt(ipHead.dstAddress, 2)) & 0xFFFF;
		temp = "" + Integer.toBinaryString(a);
		add = binaryAdd(add, temp);
		System.out.println(temp);
		a = (Integer.parseInt("00000000"+ipHead.protocol, 2));
		temp = "" + Integer.toBinaryString(a);
		add = binaryAdd(add, temp);
		System.out.println(temp);
		a = (Integer.parseInt(udpHead.length, 2));
		temp = "" + Integer.toBinaryString(a);
		add = binaryAdd(add, temp);
		System.out.println(temp);
		a = (Integer.parseInt(udpHead.srcPort, 2));
		temp = "" + Integer.toBinaryString(a);
		add = binaryAdd(add, temp);
		System.out.println(temp);
		a = (Integer.parseInt(udpHead.dstPort, 2));
		temp = "" + Integer.toBinaryString(a);
		add = binaryAdd(add, temp);
		System.out.println(temp);
		a = (Integer.parseInt(udpHead.length, 2));
		temp = "" + Integer.toBinaryString(a);
		add = binaryAdd(add, temp);
		System.out.println(temp);
		while (add.length() > 16) {
		String low = add.substring(add.length()-16);
		String high = add.substring(0, add.length()-16);
		add = binaryAdd(low, high);
		}
		while (add.length() < 16) {
		add = "0" + add;
		}
		for (int j = 0; j < add.length(); j++) {
		if (add.charAt(j) == '0') {
		result += "1";
		} else {
		result += "0";
		}
		}
		*/
		//return result;
		}
		private String binaryAdd(String a, String b) {
		int avalue = Integer.parseInt("0" + a, 2);
		int bvalue = Integer.parseInt("0" + b, 2);
		return Integer.toBinaryString(avalue + bvalue);
		}
		public String complement(String comp) {
		String result = "";
		char[] temp = comp.toCharArray();
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
		private DatagramPacket receivePacket;

		public Read() {
			ipHead = new IPHeader();
			udpHead = new UDPHeader();
		}
		
		public Read(DatagramPacket receive){
			receivePacket = receive;
			ipHead = new IPHeader();
			udpHead = new UDPHeader();
			icHead = new ICMPHeader();
		}
		
		public void run() {
			while (true) {
				
				// receive the packet
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				try {
					clientSocket.receive(receivePacket);
					
				} catch (IOException e) {
					System.out.println("Sorry, didn't get anything");
					return;
				}
				
				// get received packet into byte arrays
				byte[] packet = receivePacket.getData();
				byte[] ip = new byte[20];
				
				for(int i = 28; i <= 48; i++)
					ip[i-28] = packet[i];	
				buildIP(ip);
				
				if(ipHead.protocol.equals("00010001")){ // udp
					byte[] udp = new byte[8];
					byte[] temp = new byte[1024];
				
					for(int i = 49; i < packet.length; i++){
						if(i > 48 && i <= 56){
							udp[i-49] = packet[i];
						} else{
							temp[i-57] = packet[i];
						}
					}
					
					// verify IP Checksum
					String recvrIPCheckSum = ipCheckSum();
					if(ipHead.checkSum.equals(recvrIPCheckSum)){
						// verify UDP Checksum
						buildUDP(udp);
						String s = new String(temp);
						udpHead.setData(s);
						String recvrUDPCheckSum = uc();
						if(udpHead.checkSum.equals(recvrUDPCheckSum)){
							System.out.println("Message Recieved: " + udpHead.getData());
						} else {
							// UDP Checksum mismatch
							System.out.println("Could not receive message: UDP Checksum mismatch");
						}
					} else {
						// IP Checksum mismatch
						System.out.println("Could not receive message: IP Checksum mismatch");
					}
				} else if(ipHead.protocol.equals("00000001")) { // icmp
					// else packet is type ICMP, so parse differently
					byte[] icmp = new byte[8];
					byte[] temp = new byte[1024];
					
					for(int i = 49; i < packet.length; i++){
						if(i > 48 && i <= 56){
							icmp[i-49] = packet[i];
						} else{
							temp[i-57] = packet[i];
						}
					}
					buildICMP(icmp);
					String s = new String(temp);
					icHead.setData(s);
					System.out.println(icHead.getData());
					
				}	
			}
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
		
		private String uc() {
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
			String y = Long.toBinaryString(x);
			while(y.length() < 16) {
				y = "0" + y;
			}
			return y;
		}
		
		public void buildIP(byte[] data){
			byte[] toParse = data;
			
			String byteOne = Integer.toBinaryString(toParse[0]);
			ipHead.setVersion(byteOne.substring(0, 3));
			ipHead.setIhl(byteOne.substring(3, 7));
			
			ipHead.setTos(Integer.toBinaryString(toParse[1]));
			
			String byteThree = Integer.toBinaryString(toParse[2]);
			String byteFour = Integer.toBinaryString(toParse[3]);
			ipHead.setTotalLength(byteThree+byteFour);
			
			String byteFive = Integer.toBinaryString(toParse[4]);
			String byteSix = Integer.toBinaryString(toParse[5]);
			ipHead.setIden(byteFive+byteSix);
			
			String byteSeven = Integer.toBinaryString(toParse[6]);
			String byteEight = Integer.toBinaryString(toParse[7]);
			ipHead.setFlags(byteSeven.substring(0, 2));
			ipHead.setOffset(byteSeven.substring(2, 7)+byteEight);
			
			ipHead.setTtl(Integer.toBinaryString(toParse[8]));
			
			ipHead.setProtocol(Integer.toBinaryString(toParse[9]));
			
			String byteEleven = Integer.toBinaryString(toParse[10]);
			String byteTwelve = Integer.toBinaryString(toParse[11]);
			ipHead.setCheckSum(byteEleven + byteTwelve);
			
			String byteThirteen = Integer.toBinaryString(toParse[12]);
			String byteFourteen = Integer.toBinaryString(toParse[13]);
			String byteFifteen = Integer.toBinaryString(toParse[14]);
			String byteSixteen = Integer.toBinaryString(toParse[15]);
			ipHead.setSrcAddress(byteThirteen+byteFourteen+byteFifteen+byteSixteen);
			
			String byteSeventeen = Integer.toBinaryString(toParse[16]);
			String byteEighteen = Integer.toBinaryString(toParse[17]);
			String byteNineteen = Integer.toBinaryString(toParse[18]);
			String byteTwenty = Integer.toBinaryString(toParse[19]);
			ipHead.setDstAddress(byteSeventeen+byteEighteen+byteNineteen+byteTwenty);
		}
		
		public void buildUDP(byte[] data){
			byte[] toParse = data;
			
			String byteOne = Integer.toBinaryString(toParse[0]);
			String byteTwo = Integer.toBinaryString(toParse[1]);
			udpHead.setSrcPort(byteOne+byteTwo);
			
			String byteThree = Integer.toBinaryString(toParse[2]);
			String byteFour = Integer.toBinaryString(toParse[3]);
			udpHead.setDstPort(byteThree+byteFour);
			
			String byteFive = Integer.toBinaryString(toParse[4]);
			String byteSix = Integer.toBinaryString(toParse[5]);
			udpHead.setLength(byteFive+byteSix);
			
			String byteSeven = Integer.toBinaryString(toParse[6]);
			String byteEight = Integer.toBinaryString(toParse[7]);
			udpHead.setSrcPort(byteSeven+byteEight);
		}
		
		public void buildICMP(byte[] data){
			byte[] toParse = data;
			
			icHead.setType(Integer.toBinaryString(toParse[0]));
			icHead.setCode(Integer.toBinaryString(toParse[1]));
			
			String byteThree = Integer.toBinaryString(toParse[2]);
			String byteFour = Integer.toBinaryString(toParse[3]);
			icHead.setChecksum(byteThree+byteFour);
			
			String byteFive = Integer.toBinaryString(toParse[4]);
			String byteSix = Integer.toBinaryString(toParse[5]);
			String byteSeven = Integer.toBinaryString(toParse[6]);
			String byteEight = Integer.toBinaryString(toParse[7]);
			icHead.setRest(byteFive+byteSix+byteSeven+byteEight);
		}
	}
}
