import java.util.*;
import java.io.*;

/* Class to represent a node. Each node must run on its own thread.*/

public class Node extends Thread {

	private int id;
	private int period = 20;

	private boolean participant = false;
	private boolean leader = false;

	private Network network;

	private final Object lockObj = new Object();

	public ArrayList<Integer> tempNeighbours;
	
	// Neighbouring nodes
	public ArrayList<Node> myNeighbours;

	// Queues for the incoming and outgoing messages
	public ArrayList<String> incomingMsg;
	public ArrayList<String> outgoingMsg;

	public Node leftNode;
	public Node rightNode;

	BufferedWriter bw;

	private String logFile = "log.txt";
	
	public Node(int id) {
	
		this.id = id;

		myNeighbours = new ArrayList<>();
		tempNeighbours = new ArrayList<>();

		incomingMsg = new ArrayList<>();
		outgoingMsg = new ArrayList<>();

		leftNode = null;
		rightNode = null;

		File log = new File(logFile);

		try {
			if (log.exists()) {
				log.delete();
			}
			log.createNewFile();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public Node(int id, Network network) {

		this.id = id;
		this.network = network;

		myNeighbours = new ArrayList<>();
		tempNeighbours = new ArrayList<>();

		incomingMsg = new ArrayList<>();
		outgoingMsg = new ArrayList<>();

		leftNode = null;
		rightNode = null;

		File log = new File(logFile);

		try {
			if (log.exists()) {
				log.delete();
			}
			log.createNewFile();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public void run() {

		synchronized (lockObj) {

			if (!getIncomingMsg().isEmpty()) {
				for (int i = 0; i < getIncomingMsg().size(); i++) {
					receiveMsg(getIncomingMsg().get(i));
				}
			}

			try {
				Thread.sleep(period);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}
	
	// Basic methods for the Node class


	public Node getLeftNode() {
		return leftNode;
	}

	public void setLeftNode(Node leftNode) {
		this.leftNode = leftNode;
	}

	public Node getRightNode() {
		return rightNode;
	}

	public void setRightNode(Node rightNode) {
		this.rightNode = rightNode;
	}

	public int getNodeId() {
		/*
		Method to get the Id of a node instance
		*/
		return id;
	}
			
	public boolean isNodeLeader() {
		/*
		Method to return true if the node is currently a leader
		*/
		return leader;
	}
		
	public List<Node> getNeighbors() {
		/*
		Method to get the neighbours of the node
		*/
		return myNeighbours;
	}
		
	public void addNeighbour(Node n) {
		/*
		Method to add a neighbour to a node
		*/
		myNeighbours.add(n);
	}

	public ArrayList<String> getIncomingMsg() {
		return incomingMsg;
	}

	public ArrayList<String> getOutgoingMsg() {
		return outgoingMsg;
	}

	public synchronized void receiveMsg(String m) {
		/*
		Method that implements the reception of an incoming message by a node
		*/
		if (m.startsWith("election")) {
			int senderID = Integer.parseInt(m.substring(9));
//			System.out.println("the sender id is: " + senderID);

			if (senderID > getNodeId()) {
				outgoingMsg.add(m);
				participant = true;
			}

			else if (senderID < getNodeId() && !participant) {
				participant = true;
				outgoingMsg.add("election " + getNodeId());
			}

			else if (senderID == getNodeId() && participant) {
				leader = true;
				participant = false;
				System.out.println("Node " + this.id + " has become leader");
				outgoingMsg.add("leader " + this.id);
				try {
					bw = new BufferedWriter(new FileWriter(logFile, true));
					bw.write("leader " + this.id);
					bw.newLine();
					bw.flush();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}

		else if (m.startsWith("leader")) {
			int senderID = Integer.parseInt(m.substring(7));
			if (senderID != this.id) {
				this.participant = false;
				outgoingMsg.add(m);
				System.out.println("Node " + senderID + " is leader");
			}
		}
	}
		
	public void sendMsg(String m) {
		/*
		Method that implements the sending of a message by a node. 
		The message must be delivered to its recepients through the network.
		This method need only implement the logic of the network receiving an outgoing message from a node.
		The remainder of the logic will be implemented in the network class.
		*/
		if (!participant) {
			participant = true;
		}
		this.outgoingMsg.add(m);
		System.out.println("message added to outgoing for node " + this.id);
	}
	
}
