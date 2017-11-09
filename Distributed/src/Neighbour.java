class Neighbour{
	private String ip;
	private int port;
	private String username;
	private int noOfPeers;

	public Neighbour(String ip, int port, String username){
		this.ip = ip;
		this.port = port;
		this.username = username;
	}	
	
	public Neighbour(String ip, int port, int noOfPeers){
		this.ip = ip;
		this.port = port;
		this.noOfPeers = noOfPeers;
	}

	public String getIp(){
		return this.ip;
	}

	public String getUsername(){
		return this.username;
	}

	public int getPort(){
		return this.port;
	}
	
	public int getNoOfPeers() {
		return noOfPeers;
	}

	public void setNoOfPeers(int noOfPeers) {
		this.noOfPeers = noOfPeers;
	}
}
