
public class IPHeader {
	protected int version;
	protected int ihl;
	protected String tos;
	protected int totalLength;
	//skipping id, flags, and frag-offset fields since we don't need to do fragmentation for this project
	protected int ttl;
	protected String protocol;
	protected String checkSum;
	protected String srcAddress;
	protected String dstAddress;
	
	public IPHeader(){
		
	}
	
	public IPHeader(int version, int ihl, String tos, int totalLength, int ttl, String protocol, 
			String checkSum, String srcAddress, String dstAddress){
		this.version = version;
		this.ihl = ihl;
		this.tos = tos;
		this.totalLength = totalLength;
		this.ttl = ttl;
		this.protocol = protocol;
		this.checkSum = checkSum;
		this.srcAddress = srcAddress;
		this.dstAddress = dstAddress;
	}
	
	public int getVersion(){
		return this.version;
	}
	
	public void setVersion(int version){
		this.version = version;
	}
	
	public int getIhl(){
		return this.ihl;
	}
	
	public void setIhl(int ihl){
		this.ihl = ihl;
	}
	
	public String getTos(){
		return this.tos;
	}
	
	public void setTos(String tos){
		this.tos = tos;
	}
	
	public int getTotalLength(){
		return this.totalLength;
	}
	
	public void setTotalLength(int totalLength){
		this.totalLength = totalLength;
	}
	
	public int getTtl(){
		return this.ttl;
	}
	
	public void setTtl(int ttl){
		this.ttl = ttl;
	}
	
	public String getProtocol(){
		return this.protocol;
	}
	
	public void setProtocol(String protocol){
		this.protocol = protocol;
	}
	
	public String getCheckSum(){
		return this.checkSum;
	}
	
	public void setCheckSum(String checkSum){
		this.checkSum = checkSum;
	}
	
	public String getSrcAddress(){
		return this.srcAddress;
	}
	
	public void setSrcAddress(String srcAddress){
		this.srcAddress = srcAddress;
	}
	
	public String getDstAddress(){
		return this.dstAddress;
	}
	
	public void setDstAddress(String dstAddress){
		this.dstAddress = dstAddress;
	}
}