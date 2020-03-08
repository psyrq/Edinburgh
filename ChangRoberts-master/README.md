Chang Roberts – Readme

Implementation choices
Part A
The nodes are read in from the file and held in an ArrayList which provides the structure for the ring. The neighbours are taken in as Integer values until all the nodes have been instanciated, at which point the myNeighbours ArrayList for each node is populated with the relevant nodes. The election lines are parsed and added to a HashMap where the key is the round in which the election takes place and the value is an ArrayList of nodes that will start an election in that round. The nodes to fail are stored in an ArrayList and a will be triggered in an appropriate round.

The clockwise neighbour for each node is determined in the getNeighbours method. This sets up the structure and dictates where the node will send messages to once an election takes place.

In the relevant rounds the elections are triggered. The method addMessages goes through all the nodes and checks if they have any messages to send. If they do they are added to the network class’s msgToDeliver hashMap and then delivered to the correct nodes by the deliverMessages method. 

The chang-roberts algorithm is implemented in the node class on the receiving of a message as is the file output. 

Part B
The first failure is triggered 30 rounds after the last election has taken place to give the network plenty of time to handle everything and each successive failure is triggered 30 rounds after the last.

The network uses a Breadth first search to find a new route to the node on the other side of the failed node, . An assumption is made that the network knows what this node is. This is implemented in the findPath(Node start, Node finish) method. It does this by examining the neighbours of the sender and in turn their neighbours and so on. This keeps all the nodes active on the failure of a node. See figure 1.




