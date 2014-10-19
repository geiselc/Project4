
public class IPHeader {
	protected String version;
	protected String ihl;
	protected String tos;
	protected int totalLength;
	//skipping id, flags, and frag-offset fields since we don't need to do fragmentation for this project
	protected String ttl;
	protected String protocol;
	protected String checkSum;
	protected String srcAddress;
	protected String dstAddress;
	
	public IPHeader(){
		
	}
	
	public IPHeader(String version, String ihl, String tos, int totalLength, String ttl, String protocol, 
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
	
	public String getVersion(){
		return this.version;
	}
	
	public void setVersion(String version){
		this.version = version;
	}
	
	public String getIhl(){
		return this.ihl;
	}
	
	public void setIhl(String ihl){
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
	
	public String getTtl(){
		return this.ttl;
	}
	
	public void setTtl(String ttl){
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