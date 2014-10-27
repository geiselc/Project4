
public class ICMPHeader {
	private String data;
	private String type;
	private String code;
	private String checksum;
	private String rest;
	public ICMPHeader(String data, String type, String code, String checksum,
			String rest) {
		super();
		this.data = data;
		this.type = type;
		this.code = code;
		this.checksum = checksum;
		this.rest = rest;
	}
	public ICMPHeader() {
		super();
	}
	public byte[] getMessageData() {
		String toBytes = type + code + checksum + rest;
		
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
		for (int i = 0; i < otherData.length; i++) {
			fin[j++] = otherData[i];
		}
		return fin;
	}
	public byte[] getCheckData() {
		String toBytes = type + code + checksum + rest;
			
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
		for (int i = 0; i < otherData.length; i++) {
			fin[j++] = otherData[i];
		}
		return fin;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getChecksum() {
		return checksum;
	}
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
	public String getRest() {
		return rest;
	}
	public void setRest(String rest) {
		this.rest = rest;
	}
	
	
}
