import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OverlayRouter {
	private List<String> addresses;
	private Map<String, String> prefixes;
	
	public static void main(String[] args) {
		new OverlayRouter(args[0]);
	}
	
	public OverlayRouter(String file) {
		addresses = new ArrayList<String>();
		prefixes = new HashMap<String, String>();
		try {
			readFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Read r = new Read();
		r.start();
	}
	
	private void readFile(String fileName) throws IOException {
		fileName = "router-10"+fileName+".txt";
		
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;
		while((line = br.readLine()) != null) {
			String[] parts = line.split(" ");
			if (parts.length == 2) {
				addresses.add(parts[1]);
			} else if (parts.length == 3) {
				prefixes.put(parts[1], parts[2]);
			}
		}
		br.close();
	}
	
	private class Read extends Thread {
		public void run() {
			
		}
	}
	
	private class Write extends Thread {
		public Write() {
			
		}
		public void run() {
			
		}
	}
}
