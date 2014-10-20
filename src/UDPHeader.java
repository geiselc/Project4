
public class UDPHeader {
	
	protected String srcPort;
	protected String dstPort;
	protected String length;
	protected String checkSum;
	protected String data;
	
	public UDPHeader(){
		
	}
	
	public UDPHeader(String srcPort, String dstPort, String length, String checkSum, String data){
		this.srcPort = srcPort;
		this.dstPort = dstPort;
		this.length = length;
		this.checkSum = checkSum;
		this.data = data;
	}
	
	public byte[] getMessageData() {
		String toBytes = srcPort + dstPort + length + checkSum;
		
		
		byte[] toSend = new byte[toBytes.length() / 8];
		
		String temp = "";
		int count = 0;
		
		for(int i = 0; i < toBytes.length(); i++){
			temp += ""+ toBytes.charAt(i);
			if((i+1) % 8 == 0){
				toSend[count++] = (byte)Integer.parseInt(temp,2);
				temp = "";
			}
		}
		
		byte[] otherData = data.getBytes();
		byte[] fin = new byte[toSend.length + otherData.length];
		for (int i = 0; i < toSend.length; i++) {
			fin[i] = toSend[i];
		}
		int j = toSend.length;
		for (int i = 0; i < toSend.length; i++, j++) {
			fin[j] = toSend[i];
		}
		return fin;
	}
	
	public String getSrcPort(){
		return srcPort;
	}
	
	public void setSrcPort(String srcPort){
		this.srcPort = srcPort;
	}
	
	public String getDstPort(){
		return dstPort;
	}
	
	public void setDstPort(String dstPort){
		this.dstPort = dstPort;
	}
	
	public String getLength(){
		return length;
	}
	
	public void setLength(String length){
		this.length = length;
	}
	
	public String getCheckSum(){
		return checkSum;
	}
	
	public void setCheckSum(String checkSum){
		this.checkSum = checkSum;
	}
	
	public String getData(){
		return data;
	}
	
	public void setData(String data){
		this.data = data;
	}
}
