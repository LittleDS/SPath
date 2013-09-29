import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class Graph implements Comparable<Graph> {
	//We require that each vertex only has one label in this work
	public HashMap<Integer, String> primaryAttribute = new HashMap<Integer, String>();
	
	//The adjacency list of each vertex
	public HashMap<Integer, List<Integer>> children = new HashMap<Integer, List<Integer>>();
	public HashMap<Integer, List<Integer>> parents = new HashMap<Integer, List<Integer>>();
	
	//The value will be set after loading the graph
	public int totalEdges = 0;
	
	public boolean graphLoaded = false;

	public HashMap<Integer, Integer> indegree = new HashMap<Integer, Integer>();
	public HashMap<Integer, Integer> outdegree = new HashMap<Integer, Integer>();
	
	/**
	 * Load the graph from a file in the disk
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public void loadGraphFromFile(String fileName) throws FileNotFoundException {
		//Initialize the value
		totalEdges = 0;
		
		File tFile = new File(fileName);		
		Scanner tScanner = new Scanner(tFile);
		
		//Read the original graph file and build the graph in the main memory
		while (tScanner.hasNext()) {
			//Read the graph file
			//Get the ID and attributes line
			String idLine = tScanner.nextLine();

			//Get the neighbors line
			String neighborLine = tScanner.nextLine();
			
			//Build the graph in the main memory
			//Process the ID and the attributes
			String[] strings = idLine.split(",");
			int ID = Integer.parseInt(strings[0]);

			primaryAttribute.put(ID, strings[1]);
						
			//Process the neighbors
			strings = neighborLine.split(",");
			if (!children.containsKey(ID))
				children.put(ID, new LinkedList<Integer>());
			for (int i = 0; i < strings.length; i++) {
				int tN = Integer.parseInt(strings[i]);
				//If the neighbor ID is equal to -1, it means the current vertex doesn't have a neighbor
				if (tN != -1) {
					children.get(ID).add(tN);
					//Calculate the total number of edges
					totalEdges++;
						
					//The parents of each node is also record
					if (!parents.containsKey(tN))
						parents.put(tN, new LinkedList<Integer>());
					parents.get(tN).add(ID);						
				} else {
					children.remove(ID);
					break;
				}				
			}
		}
		
		tScanner.close();			
		
		calculateDegree();
		 
		graphLoaded = true;
	}
	
	/**
	 * Add an edge to the graph
	 */
	public void addEdge(Integer s, String aS, Integer t, String aT) {
		//Check whether the verex has already been in the graph
		if (primaryAttribute.containsKey(s)) {
			primaryAttribute.put(s, aS);
		}
		
		if (primaryAttribute.containsKey(t)) {
			primaryAttribute.put(t, aT);
		}
		
		//Add t as a child of s
		if (!children.containsKey(s))
			children.put(s, new LinkedList<Integer>());
		
		if (!children.get(s).contains(t)) {
			children.get(s).add(t);
			totalEdges++;
		}
		
		//In the meanwhile, add s as a parent of t
		if (!parents.containsKey(t))
			parents.put(t,  new LinkedList<Integer>());
		if (!parents.get(t).contains(s))
			parents.get(t).add(s);
	}
	
	/**
	 * Remove an edge
	 * @param s
	 * @param t
	 */
	public void removeEdge(Integer s, Integer t) {
		if (children.containsKey(s) && children.get(s).contains(t)) {
			children.get(s).remove(t);
			if (children.get(s).size() == 0) {
				children.remove(s);
				outdegree.remove(s);
				if (!parents.containsKey(s)) {
					primaryAttribute.remove(s);
				}
			} else {
				outdegree.put(s, outdegree.get(s) - 1);				
			}
			totalEdges--;
		}
		
		if (parents.containsKey(t) && parents.get(t).contains(s)) {
			parents.get(t).remove(s);
			if (parents.get(t).size() == 0) {
				parents.remove(t);
				indegree.remove(t);
				if (!children.containsKey(t)) {
					primaryAttribute.remove(t);
				}
			} else {
				indegree.put(t, indegree.get(t) - 1);
			}
		}
		
		
	}
	
	/**
	 * Calculate the indegree and outdegree
	 */
	public void calculateDegree() {
		outdegree.clear();
		for (Integer i : children.keySet()) {
			outdegree.put(i, children.get(i).size());
		}
		
		indegree.clear();
		for (Integer i : parents.keySet()) {
			indegree.put(i, parents.get(i).size());			
		}		
	}
	
	/**
	 * Print the structure of the graph
	 */
	public void print() {
		for (Integer i : children.keySet()) {
			System.out.print(i + ": ");
			for (Integer j : children.get(i)) {
				System.out.print(j + " ");
			}
			System.out.println();
		}
	}
	
	
	/**
	 * Constructor
	 */
	public Graph() {
		
	}
	
	/**
	 * The copy constructor
	 * @param another
	 */
	public Graph(Graph another) {
		//Attributes
		this.primaryAttribute.putAll(another.primaryAttribute);
		
		//Primary Attribute
		for (Integer i : another.primaryAttribute.keySet()) {
			primaryAttribute.put(i, another.primaryAttribute.get(i));
		}
		
		totalEdges = 0;
		
		//Children		
		for (Integer i : another.children.keySet()) {
			children.put(i, new LinkedList<Integer>());
			for (Integer j : another.children.get(i)) {
				children.get(i).add(j);
				totalEdges++;
			}
		}
		
		//Parents
		for (Integer i : another.parents.keySet()) {
			parents.put(i, new LinkedList<Integer>());
			for (Integer j : another.parents.get(i))
				parents.get(i).add(j);
		}
		
		graphLoaded = true;
		
		calculateDegree();
	}

	@Override
	public int compareTo(Graph arg0) {
		if (this.totalEdges > arg0.totalEdges)
			return -1;
		else if (this.totalEdges < arg0.totalEdges)
			return 1;
		else
			return 0;
				
	}

	/**
	 * Combine this graph with another one
	 * @param another
	 */
	public void Combine(Graph another) {
		//Primary Attribute
		//This two graphs shouldn't share any common vertices
		for (Integer i : another.primaryAttribute.keySet()) {
			primaryAttribute.put(i, another.primaryAttribute.get(i));
		}
		
		totalEdges += another.totalEdges;
		
		//Children		
		for (Integer i : another.children.keySet()) {
			children.put(i, new LinkedList<Integer>());
			for (Integer j : another.children.get(i)) {
				children.get(i).add(j);
			}
		}
		
		//Parents
		for (Integer i : another.parents.keySet()) {
			parents.put(i, new LinkedList<Integer>());
			for (Integer j : another.parents.get(i))
				parents.get(i).add(j);
		}
		
		graphLoaded = true;
		
		indegree.clear();
		outdegree.clear();
		
		calculateDegree();		
	}
	
	public void outputGADDIForm(String fileName) throws IOException {
		//Open the file
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);

		out.write(primaryAttribute.size() + "\r\n");

		//Write the index into file
		for (Integer i : primaryAttribute.keySet()) {
				out.write(i + " " + primaryAttribute.get(i) + "\r\n");
		}

		for (Integer i : children.keySet()) {
			List<Integer> clist = children.get(i);
			for (Integer j : clist)
				out.write(i + " " + j + "\r\n");
		}
		//don't forget to close the stream
		out.close();
	}
}
