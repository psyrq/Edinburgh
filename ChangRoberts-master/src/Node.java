import java.util.*;
import java.io.*;


public class Node extends Thread {

	public int id;
	private int period = 20;
	private boolean participant = false;
	private boolean leader = false;
	private Network network;
	public Node leftHandNode;
	public Node rightHandNode;
	private Object lock1 = new Object();
	public ArrayList<Integer> tempNeighbours;


	// Neighbouring nodes
	public ArrayList<Node> myNeighbours;

	// Queues for the incoming and outgoing messages
	public ArrayList<String> incomingMsg;
	public ArrayList<String> outgoingMsg;
	
	// file output 
	BufferedWriter bw;

	//constructer
	public Node(int id){
	
		this.id = id;
		this.network = network;
		
		myNeighbours = new ArrayList<Node>();
		tempNeighbours = new ArrayList<Integer>();
		incomingMsg = new ArrayList<String>();
		outgoingMsg = new ArrayList<String>();
		leftHandNode = null;
		rightHandNode = null;
	}
	


	//thread run method
	public void run(){
		synchronized(lock1){
			if(!this.getIncomingMsg().isEmpty()){
				for(int i = 0; i<this.getIncomingMsg().size(); i++){
					this.receiveMsg(this.getIncomingMsg().get(i));
				}
			}
		}
		try{
			Thread.sleep(period);
		}catch (InterruptedException e) {
            System.out.println("Exception in thread: "+e.getMessage());
        }		
	}
	
	// Basic methods for the Node class
	public Node getRightHandNode() {
		return rightHandNode;
	}

	public void setRightHandNode(Node rightHandNode) {
		this.rightHandNode = rightHandNode;
	}
	
	public Node getLeftHandNode() {
		return leftHandNode;
	}

	public void setLeftHandNode(Node leftHandNode) {
		this.leftHandNode = leftHandNode;
	}
	
	public int getNodeId() {
		return this.id;
		}
	
	public ArrayList<String> getIncomingMsg() {
		return incomingMsg;
	}

	public ArrayList<String> getOutgoingMsg() {
		return outgoingMsg;
	}
			
	public boolean isNodeLeader() {
		return leader;
		}
		
	public List<Node> getNeighbors() {
		return myNeighbours;
		}
		
	public void addNeighbour(Node n) {
		myNeighbours.add(n);
		}
			
	//method that receives a message and applies the Chang-Roberts Algorithm 
	//and also outputs to file 
	public synchronized void receiveMsg(String m) {
		if(m.startsWith("election")){
			int senderId = Integer.parseInt(m.substring(8));
			if(senderId > this.getNodeId()){
				outgoingMsg.add(m);
				this.participant = true;
			} else if (senderId < this.getNodeId() && this.participant == false){
				this.participant = true;
				outgoingMsg.add("election"+String.valueOf(this.getNodeId()));
			} else if (senderId == this.getNodeId() && this.participant == true){
				this.leader = true;
				this.participant = false;
				System.out.println("Node " + this.getNodeId() + " has declared itself leader!");
				outgoingMsg.add("leader"+String.valueOf(this.getNodeId()));
				try {
					bw = new BufferedWriter(new FileWriter("log.txt", true));
					bw.write("leader "+String.valueOf(this.getNodeId()));
					bw.newLine();
					bw.flush();
				}
				catch(IOException e){
					System.out.println(e.getMessage());
				}
			}
		}
		
		else if(m.startsWith("leader")){
			int senderId = Integer.parseInt(m.substring(6));
			if(senderId != this.getNodeId()){
				this.participant = false;
				outgoingMsg.add(m);
				System.out.println("Node " + senderId + " is our leader");
			}
			
			
		}
		
		}
		
	//sending a message from a string
	public void sendMsg(String m) {
		if (participant == false){
			participant = true;
		}
		this.outgoingMsg.add(m);
		System.out.println("message added to outgoing for node " + this.getNodeId());
		}
	
}