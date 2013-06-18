package bdagent.util;



import java.util.ArrayList;
import java.util.List;

public class PortMap {
    private final static String KEY_PORT_MAPPING = "port_mapping";
	private int destinationPort, localPort;
	private String destinationHost;
	public int getDestinationPort() {
		return destinationPort;
	}
	public void setDestinationPort(int destinationPort) {
		this.destinationPort = destinationPort;
	}
	public int getLocalPort() {
		return localPort;
	}
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}
	public String getDestinationHost() {
		return destinationHost;
	}
	public void setDestinationHost(String destinationHost) {
		this.destinationHost = destinationHost;
	}
	
	
	public static List<PortMap> getPortMaps(){
		List<PortMap> portMaps=new ArrayList<PortMap> ();
		String line;
		int i=0;
		while(null!=(line= ConfigHelp.getConifg(KEY_PORT_MAPPING + (i++), null))){
			//192.168.9.31:22:22
			PortMap portMap=new PortMap();
			String [] splt=line.split(":");
			portMap.setDestinationHost(splt[0]);
			portMap.setDestinationPort(Integer.parseInt(splt[1]));
			portMap.setLocalPort(Integer.parseInt(splt[2]));
			portMaps.add(portMap);
		}
		
		
		return portMaps;
	}
}
