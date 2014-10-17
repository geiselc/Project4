
public class UDPHeader {
	
	protected String srcPort;
	protected String dstPort;
	protected int length;
	protected String checkSum;
	protected String data;
	
	public UDPHeader(){
		
	}
	
	public UDPHeader(String srcPort, String dstPort, int length, String checkSum, String data){
		this.srcPort = srcPort;
		this.dstPort = dstPort;
		this.length = length;
		this.checkSum = checkSum;
		this.data = data;
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
	
	public int getLength(){
		return length;
	}
	
	public void setLength(int length){
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
