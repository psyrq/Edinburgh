import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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

	private static List<Node> nodes;
	private int round = 1;
	private int period = 20;
	private Map<Node, String> msgToDeliver; //Integer for the id of the sender and String for the message

	private Map<Integer, ArrayList<Node>> elections;
	private Map<Integer, Integer> failures;

	private void NetSimulator(String[] args) {

		msgToDeliver = new HashMap<>();
		nodes = new ArrayList<>();
		elections = new HashMap<>();
		failures = new HashMap<>();

		parseFile(args[0], args[1]);

		getNeighbours();

		while(round <= 250) {
			System.out.println("#####  round: " + round + " #####");
			Set<Integer> keyset = elections.keySet();

			if (keyset.contains(round)) {
				ArrayList<Node> starters = elections.get(round);
				for (Node starter : starters) {
					starter.outgoingMsg.add("election " + starter.getNodeId());
					System.out.println("Node " + starter.getNodeId() + " start an election");
				}
				elections.remove(round);
			}

			addMessage();

			deliverMessages();

			if (failures.containsKey(round)) {

				Iterator<Node> iter = nodes.iterator();

				while (iter.hasNext()) {

					Node node = iter.next();

					if (node.getNodeId() == failures.get(round)) {

						System.out.println("Node " + node.getNodeId() + " has failed");
						for (Node n : node.getNeighbors()) {
							n.deleteNeighbour(node);
						}

						findNewPath(node.getRightNode(), node.getLeftNode());
						if (node.isNodeLeader()) {
							System.out.println("Current leader: " + node.getNodeId() + " Node " + node.getNodeId() + " has failed");
							System.out.println("A new election will be held next round by the node closest to the " +
									"failure");
							ArrayList<Node> temp = new ArrayList<>();
							temp.add(node.getLeftNode());
							elections.put(round+1, temp);
						} else {
							System.out.println("Node " + node.getNodeId() + " is not leader");
							System.out.println("Reconstruct network");
						}
						iter.remove();
					}
				}

				failures.remove(round);
				getNeighbours();
			}

			round += 1;

			try {
				Thread.sleep(period);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}

		/*
		Code to call methods for parsing the input file, initiating the system and producing the log can be added here.
		*/
	}
		
	private void parseFile(String graph, String event) {
		/*
		Code to parse the file can be added here. Notice that the method's descriptor must be defined.
		*/
		String line;

		try {

			BufferedReader graphReader = new BufferedReader(new FileReader(graph));
			BufferedReader eventReader = new BufferedReader(new FileReader(event));

			// read the graph of network from text
			while ((line = graphReader.readLine()) != null) {

				String[] parts = line.split(" ");

				// nodes and neighbours
				Node node = new Node(Integer.parseInt(parts[0]));
				nodes.add(node);

				System.out.println("add node: " + node.getNodeId());
				System.out.print("temp neighbours for node " + node.getNodeId() + ": ");

				ArrayList<Integer> tempNeighbours = new ArrayList<>();
				for (int i = 1; i < parts.length; i++) {
					int neighbour = Integer.parseInt(parts[i]);
					tempNeighbours.add(neighbour);
					System.out.print(neighbour + " ");
				}
				node.setTempNeighbours(tempNeighbours);
				System.out.println();
			}

			// read the events (election or failure) from the text
			while ((line = eventReader.readLine()) != null) {

				String[] parts = line.split(" ");

				// elections
				if (parts[0].equals("ELECT")) {
					int round = Integer.parseInt(parts[1]);
					ArrayList<Node> starters = new ArrayList<>();
					for (int i = 2; i < parts.length; i++) {
						for (Node node : nodes) {
							if ((Integer.parseInt(parts[i])) == (node.getNodeId())) {
								starters.add(node);
							}
						}
					}
					elections.put(round, starters);
				}

				// failures
				else if(parts[0].equals("FAIL")) {
					int failRound = Integer.parseInt(parts[1]);
					int failNode = Integer.parseInt(parts[2]);
					failures.put(failRound, failNode);
				}
			}

			// populate neighbours
			for (Node node1 : nodes) {
				for (Node node2 : nodes) {
					for (int i = 0; i < node1.getTempNeighbours().size(); i++) {
						if (node1.getTempNeighbours().get(i) == node2.getNodeId()) {
//							node1.myNeighbours.add(node2);
							node1.addNeighbour(node2);
						}
					}
				}
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private synchronized void addMessage() {
		/*
		At each round, the network collects all the messages that the nodes want to send to their neighbours. 
		Implement this logic here.
		*/
		for (Node node : nodes) {
			ArrayList<String> outgoingMsg = node.getOutgoingMsg();
			for (int j = outgoingMsg.size() - 1; j >= 0; j--) {
				msgToDeliver.put(node, outgoingMsg.get(j));
				System.out.println("Node " + node.getNodeId() + " sent a message: " + outgoingMsg.get(j));
				outgoingMsg.remove(j);
			}
		}
	}
	
	private synchronized void deliverMessages() {
		/*
		At each round, the network delivers all the messages that it has collected from the nodes.
		Implement this logic here.
		The network must ensure that a node can send only to its neighbours, one message per round per neighbour.
		*/
		for (Node node : nodes) {
			String msg = msgToDeliver.get(node);
			if (msg != null) {
				node.getLeftNode().receiveMsg(msg);
				msgToDeliver.remove(node);
			}
		}
	}
		
	public synchronized void informNodeFailure(int id) {
		/*
		Method to inform the neighbours of a failed node about the event.
		*/
	}

	private synchronized void getNeighbours() {

		for (int i = 0; i < nodes.size(); i++) {
			if (i == nodes.size() - 1) {
				nodes.get(i).setLeftNode(nodes.get(0));
			} else {
				nodes.get(i).setLeftNode(nodes.get(i + 1));
			}
		}

		for (int i = 0; i < nodes.size(); i++) {
			if (i == 0) {
				nodes.get(i).setRightNode(nodes.get(nodes.size() - 1));
			} else {
				nodes.get(i).setRightNode(nodes.get(i - 1));
			}
		}
	}

	private synchronized void findNewPath(Node start, Node finish) {

		Queue<Node> queue = new LinkedList<>();
		queue.add(start);
		while (!queue.isEmpty()) {
			Node n = queue.remove();
			if (n.getNeighbors().contains(finish)) {
				System.out.println("New route found from " + start.getNodeId() + " to " + finish.getNodeId() + "via node " + n.getNodeId());
				queue.clear();
			} else {
				queue.addAll(n.getNeighbors());
			}
		}
	}

	public static void main(String[] args) {
		/*
		Your main must get the input file as input.
		*/
		Network network = new Network();
		network.NetSimulator(args);
	}
}
