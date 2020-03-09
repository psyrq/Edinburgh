import java.util.*;
import java.io.*;
//import org.apache.commons.lang.math.NumberUtils;

/* 
Class to simulate the network. System design directions:

- Synchronous communication: each round lasts for 20ms
- At each round the network receives the messages that the nodes want to send and delivers them
- The network should make sure that:
	- A node can only send messages to its neighbours
	- A node can only send one message per neighbour per round
- When a node fails, the network must inform all the node's neighbours about the failure
*/

public class Network {

	public List<Node> nodes;
	private int period = 20;
	private static int round = 0;
	private static HashMap<Node, String> msgToDeliver; //Integer for the id of the sender and String for the message
	private static HashMap<Integer, ArrayList<Node>> elections;
	private static ArrayList<Integer> failures;
	private int safeForFailures = 0;
	String[] args = new String[] {"src/input.txt"};
	//String[] args;
	
	
	public void NetSimulator() {
		
        	msgToDeliver = new HashMap<Node, String>();
        	nodes = new ArrayList<Node>();
        	elections = new HashMap<Integer, ArrayList<Node>>();
        	failures = new ArrayList<Integer>();
        	/*
        	Code to call methods for parsing the input file, initiating the system and producing the log can be added here.
        	*/
        	
        	//parsing the file
        	try {
				parseFile(args[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
        	//setting up the ring structure 
        	getNeighbours();
        	
        	//utility
        	getRightNeighbours();

        	
        	//starting the rounds
        	while(round<150){
        		System.out.println("**************   round: " + round + "   ******************");
        		
        		// check if there are any elections in this round and if so there are then get the starters to send the message
        		Set<Integer> keySet = elections.keySet();
        		if (keySet.contains(round)){
        			ArrayList<Node> starters = elections.get(round);
        			for (int i = 0; i<starters.size(); i++){
        				//startNode.sendMsg("election" + String.valueOf(startNode.getNodeId()));
        				starters.get(i).outgoingMsg.add("election" + String.valueOf(starters.get(i).getNodeId()));
        				//msgToDeliver.put(starters.get(i), "election" + String.valueOf(starters.get(i).getNodeId()));
        				System.out.println("Node " + starters.get(i).getNodeId() + " has started an election");
        			}
        			elections.remove(round);
        			
        		}
        		
        		//get all the outgoing messages for all the nodes and add them to the network hashmap
        		addMessages();
        		
        		//deliver the messages to the correct recipient
        		deliverMessages();
        		
        		//failure handling 
        		if(round == safeForFailures){
        			Iterator<Node> it = nodes.iterator();
        			while(it.hasNext()){
        				Node node = it.next();
        				if(node.getNodeId() == failures.get(0)){
        					System.out.println("Node " + node.getNodeId()+ " has failed");
        					for(Node n : node.myNeighbours){
        						n.myNeighbours.remove(node);
        					}
            				findPath(node.rightHandNode, node.leftHandNode);
        					if(node.isNodeLeader()){
        						System.out.println("Node " + node.getNodeId() +  " was the leader");
        						System.out.println("A new election will be held next round by the node closest to the failure");
        						ArrayList<Node> temp = new ArrayList<Node>();
            					temp.add(node.getLeftHandNode());
            					elections.put(round+1, temp);
            					it.remove();
        					}else{
        						System.out.println("Node " + node.getNodeId() + " was not the leader");
        						System.out.println("Reconstructing the ring network");
        						it.remove();
        					}
        				}

        				
        			}
    				failures.remove(0);
        			safeForFailures+=30;
        			getNeighbours();
        		}
        		
        		
        		//incrementing the round 
        		round+=1;
        		
        		//adding the sleep period
        		try {
        		    Thread.sleep(period);               
        		} catch(InterruptedException ex) {
        		    Thread.currentThread().interrupt();
        		}

        		
        	}
        	
        	
   		}
   		
	
	// File parsing 
   	private void parseFile(String fileName) throws IOException {
   		String line;
   		int iteration = 0;
   		try(BufferedReader br = new BufferedReader(new FileReader(fileName))){
   			//skip first line
   			while ((line = br.readLine()) != null) {
   			    if(iteration == 0) {
   			        iteration++;  
   			        continue;
   			    }
   			    String[] parts = line.split(" ");
   			    //nodes and neighbours
   			    if(isNumeric(parts[0])){
   			    	Node node = new Node(Integer.parseInt(parts[0]));
   			    	nodes.add(node);
   			    	System.out.println("adding node " + node.getNodeId());
   			    	for (int i = 1; i < parts.length; i++){
   			    		int neighbour = Integer.parseInt(parts[i]);
   			    		node.tempNeighbours.add(neighbour);
   			    	}
   			    }
   			    //elections 
   			    else if (parts[0].equals("ELECT")){
   			    	int round = Integer.parseInt(parts[1]);
   			    	ArrayList<Node> starters = new ArrayList<Node>();
   			    	for( int i = 2 ; i<parts.length; i++){
   			    		for(int j = 0; j<nodes.size();j++){
   			    		//Node starter = new Node(Integer.valueOf(parts[i]));
   			    		if ((Integer.parseInt(parts[i]))==(nodes.get(j).getNodeId()))
   			    			starters.add(nodes.get(j));
   			    		}
   			    	}
   			    	elections.put(round, starters);
   			    }
   			    //failures
   			    else if (parts[0].equals("FAIL")){
   			    	int failNode = Integer.parseInt(parts[1]);
   			    	failures.add(failNode);
   			    }
   			    
   		}
   			// populating node neighbours with the other node instances
   			for(Node node1 : nodes){
   				for(Node node2 : nodes){
   					for(int i = 0; i<node1.tempNeighbours.size(); i++){
   						if(node1.tempNeighbours.get(i) == node2.getNodeId()){
   							node1.myNeighbours.add(node2);
   						}
   					}
   				}
   			}
   			//set a round when the thread is safe for failures
   			int max = 0;
   			for (int i : elections.keySet()){
   				if(max == 0 || i > max){
   					max = i;
   				}
   			}
   			safeForFailures = max + 30;
   			
   			
   		}catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}

   		
   	}
   	//utility method
   	public boolean isNumeric(String s) {  
   	    return s.matches("[-+]?\\d*\\.?\\d+");  
   	}  
   	
   	/*failure path finding method*/
   	public synchronized void findPath(Node s , Node f){
   		Queue<Node> q = new LinkedList<Node>();
   		q.add(s);
   		while(!q.isEmpty()){
   			Node n = (Node)q.remove();
   			if(n.myNeighbours.contains(f)){
   				System.out.println("New route found from " + s.getNodeId() + " to " + f.getNodeId() + " via node " + n.getNodeId());
   				q.clear();
   			}
   			else{
   				q.addAll(n.myNeighbours);
   			}
   		}
   	}
   	
   	//assigning each node a closest neighbour
   	public synchronized void getNeighbours(){
   		for ( int i = 0; i < nodes.size(); i++){
   			if(i == nodes.size()-1){
   				nodes.get(i).leftHandNode = nodes.get(0);
   				//System.out.println("node" + nodes.get(i).getNodeId() + "has neighbour" + nodes.get(0).getNodeId());
   			}else{
   				nodes.get(i).leftHandNode = nodes.get(i+1);
   				//System.out.println("node" + nodes.get(i).getNodeId() + "has neighbour" + nodes.get(i+1).getNodeId());
   			}
   		}
   	}
   	
   	// anti-clockwise neighbour for use in event of failure
   	public synchronized void getRightNeighbours(){
   		for ( int i = 0; i < nodes.size(); i++){
   			if(i == 0){
   				nodes.get(i).rightHandNode = nodes.get(nodes.size()-1);
   				//System.out.println("node" + nodes.get(i).getNodeId() + "has neighbour" + nodes.get(0).getNodeId());
   			}else{
   				nodes.get(i).rightHandNode = nodes.get(i-1);
   				//System.out.println("node" + nodes.get(i).getNodeId() + "has neighbour" + nodes.get(i+1).getNodeId());
   			}
   		}
   	}
	
	// go through the nodes and check if they have outgoing messages.
	// if they do then add them to the hashmap 
	public synchronized void addMessages() {

		for( int i = 0; i<nodes.size(); i++){
			ArrayList<String>  outgoing = nodes.get(i).getOutgoingMsg();
			//System.out.println(outgoing.size());
			for( int j = 0; j<outgoing.size(); j++){
				msgToDeliver.put(nodes.get(i), outgoing.get(j));
				System.out.println("Node" + nodes.get(i).getNodeId() + " has sent a message: " + outgoing.get(j));
				outgoing.remove(j);
			}
		}
		}

	//deliver the messages to the correct nodes
	public synchronized void deliverMessages() {	
		for (Node node : nodes){
			String message = msgToDeliver.get(node);
			if (message != null){
				node.getLeftHandNode().receiveMsg(message);
				//node.getLeftHandNode().start();
				msgToDeliver.remove(node);
				
			}	
		}
		}
		
	public synchronized void informNodeFailure(int id) {
		/*
		Method to inform the neighbours of a failed node about the event.
		*/
		}
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		Network net = new Network();
		net.NetSimulator();
		
		
		}
	
	
	
	
	
	
	
}