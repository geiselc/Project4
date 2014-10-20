
public class IPHeader {
	protected String version;
	protected String ihl;
	protected String tos;
	protected String totalLength;
	//skipping id, flags, and frag-offset fields since we don't need to do fragmentation for this project
	protected String ttl;
	protected String protocol;
	protected String checkSum;
	protected String srcAddress;
	protected String dstAddress;
	protected String iden;
	protected String flags;
	protected String offset;
	
	public IPHeader(){
		
	}
	
	public IPHeader(String version, String ihl, String tos, String totalLength,
			String ttl, String protocol, String checkSum, String srcAddress,
			String dstAddress, String iden, String flags, String offset) {
		super();
		this.version = version;
		this.ihl = ihl;
		this.tos = tos;
		this.totalLength = totalLength;
		this.ttl = ttl;
		this.protocol = protocol;
		this.checkSum = checkSum;
		this.srcAddress = srcAddress;
		this.dstAddress = dstAddress;
		this.iden = iden;
		this.flags = flags;
		this.offset = offset;
	}

	public byte[] getMessageData() {
		String toBytes = version + ihl + tos + totalLength;
		toBytes += iden + flags + offset;
		toBytes += ttl + protocol + checkSum + srcAddress + dstAddress;
		
		for(int i = 0; i < toBytes.length(); i++){
			System.out.print(toBytes.charAt(i));
			if((i+1) % 8 == 0){
				System.out.println("");
			}
		}
		
		return toBytes.getBytes();
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
	
	public String getTotalLength(){
		return this.totalLength;
	}
	
	public void setTotalLength(String totalLength){
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

	public String getIden() {
		return iden;
	}

	public void setIden(String iden) {
		this.iden = iden;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}
	
	
}