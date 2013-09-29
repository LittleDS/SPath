import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SPath {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		String dataGraphName = "P2P";
		// TODO Auto-generated method stub
		VertexIndex vi = new VertexIndex();
		vi.loadVerticesIndexFromFile(dataGraphName + "Vertices");
		
		System.out.println("Finish Loading Index....");
		
		Graph d = new Graph();
		d.loadGraphFromFile(dataGraphName);

		System.out.println("Finish Loading Data Graph....");
		
		Graph t = new Graph();
		t.loadGraphFromFile("query0GBE");
		
		System.out.println("Finish Loading Query Pattern....");
		
		NeighborHood2 n = new NeighborHood2();
		n.loadIndexFromFile(dataGraphName);
		
		System.out.println("Finish Loading NeighborHood Index");
		
		SPath sp = new SPath();
		
		long startTime = System.nanoTime();

		LinkedList<MatchedCandidates> queryResult = null;
		
		for (int i = 0; i < 50; i++) { 
			queryResult = sp.Query(t, d, n, vi);
		}
		
		long endTime = System.nanoTime();
		long duration = endTime - startTime;		
		
		System.out.println("Total running time: " + duration / 50);
		for (MatchedCandidates mc : queryResult)
			mc.Print();
	}
	
	public LinkedList<MatchedCandidates> Query(Graph q, Graph data, NeighborHood2 nh, VertexIndex vi) {
		Graph qcopy = new Graph(q);
		
		//Initialize the neighborhood index for query graph first
		NeighborHood2 nq = new NeighborHood2();
		nq.Encode(q);
		
		HashMap<Integer, List<Integer>> reducedMatchingCandidates = new HashMap<Integer, List<Integer>>();
		
		//Determine the reduced candidates		
		for (Integer i: q.primaryAttribute.keySet()) {
			String attribute = q.primaryAttribute.get(i);
			List<Integer> potentialCandidates = vi.verticesIndex.get(attribute);
			List<Integer> realCandidates = new LinkedList<Integer>();
			
			for (Integer j : potentialCandidates) {
				if (nh.Check(nq, i, j))
					realCandidates.add(j);
			}
			
			reducedMatchingCandidates.put(i, realCandidates);
		}
		
	
		//Chose the start vertex
		Integer start = 0;
		int min = Integer.MAX_VALUE;
		for (Integer i : reducedMatchingCandidates.keySet()) {
			List<Integer> c = reducedMatchingCandidates.get(i);
			if (c.size() < min) {
				min = c.size();
				start = i;
			}
		}
		
		
		//Generate all the shortest paths from each vertex
		HashMap<Integer, List<List<Integer>>> forwardpaths = new HashMap<Integer, List<List<Integer>>>();
		HashMap<Integer, List<List<Integer>>> backwardpaths = new HashMap<Integer, List<List<Integer>>>();
		
		int kstar = 2;
		for (Integer i : q.children.keySet()) {
			forwardpaths.put(i, GeneratePathForward(q, i, kstar));
		}
		
		for (Integer i : q.parents.keySet()) {
			backwardpaths.put(i, GeneratePathBackward(q, i, kstar));
		}
		
		//Decompose the query graph into those shortest paths
		int totalEdges = q.totalEdges;
		
		HashSet<Edge> visited = new HashSet<Edge>();
		HashSet<Integer> coveredVertices = new HashSet<Integer>();
		
		List<List<Integer>> decomposedPaths = new LinkedList<List<Integer>>();
		
		while (totalEdges > 0) {
			double max = 0.0;
			List<Integer> chosenPaths = null;
			
			List<List<Integer>> fpaths = forwardpaths.get(start);
			if (fpaths != null && fpaths.size() > 0) {
				for (List<Integer> pi : fpaths) {
					boolean invalid = false;
					for (int i = 0; i < pi.size() - 1; i++) 
						if (visited.contains(new Edge(pi.get(i), pi.get(i + 1)))) {
							invalid = true;
							break;
						}
					
					if (invalid)
						continue;
					
					double spi = CalculateSelectivity(pi, reducedMatchingCandidates);
					if (spi > max || (spi == max && pi.size() > chosenPaths.size())) {
						max = spi;
						chosenPaths = pi;
					}
				}
			}
			
			List<List<Integer>> bpaths = backwardpaths.get(start);
			if (bpaths != null && bpaths.size() > 0) {
				for (List<Integer> pi : bpaths) {
					boolean invalid = false;
					for (int i = 0; i < pi.size() - 1; i++) 
						if (visited.contains(new Edge(pi.get(i), pi.get(i + 1)))) {
							invalid = true;
							break;
						}
					
					if (invalid)
						continue;
					
					double spi = CalculateSelectivity(pi, reducedMatchingCandidates);
					if (spi > max || (spi == max && pi.size() > chosenPaths.size())) {
						max = spi;
						chosenPaths = pi;
					}
				}
			}
			
			for (int i = 0; i < chosenPaths.size() - 1; i++) {
				visited.add(new Edge(chosenPaths.get(i), chosenPaths.get(i + 1)));
				qcopy.removeEdge(chosenPaths.get(i), chosenPaths.get(i + 1));
			}
			
			coveredVertices.addAll(chosenPaths);
			
			totalEdges -= chosenPaths.size() - 1;
			
			decomposedPaths.add(chosenPaths);
			
			start = 0;
			min = Integer.MAX_VALUE;
			//Choose next start vertices
			for (Integer i : coveredVertices) {
				if (!qcopy.outdegree.containsKey(i) && !qcopy.indegree.containsKey(i)) 
					continue;
				List<Integer> c = reducedMatchingCandidates.get(i);
				if (c.size() < min) {
					min = c.size();
					start = i;
				}				
			}
		}
		
//		//Get the decomposed shortest paths
//		for (Integer s : backwardpaths.keySet()) {
//			List<List<Integer>> ps = backwardpaths.get(s);
//				for (List<Integer> i : ps) {
//					for (Integer j : i)
//					System.out.print(j + " ");
//				}
//				System.out.println();
//		}

		

		LinkedList<MatchedCandidates> result = new LinkedList<MatchedCandidates>();

		//For each decomposed path, the next step is to instantiate them
		for (List<Integer> i : decomposedPaths) {
			//Using the ID-List to instantiate the paths
			List<List<Integer>> matchingPaths = InstantiatePaths(i, reducedMatchingCandidates, nh, q);
			if (matchingPaths != null) {
				//Join the matching candidates with the intermediate result
				if (result.size() == 0) {   //First time, initiailize the result set
					for (List<Integer> j : matchingPaths) {					
						MatchedCandidates mT = new MatchedCandidates(j, i);
						result.add(mT);					
					}
				}
				else {
					LinkedList<MatchedCandidates> newResult = new LinkedList<MatchedCandidates>();
					
					for (MatchedCandidates m : result) {
						for (List<Integer> j : matchingPaths) {
							if (m.CanJoin(j, i)) {
								//Make a copy of the current graph piece
								MatchedCandidates mT = new MatchedCandidates(m);
								mT.Join(j, i);
								newResult.add(mT);
							}						
						}
					}
					
					result.clear();
					result = newResult;
				}
			}
		}		

		return result;
	}

	/**
	 * According to the ID-List, we instantiate the possible matching for path p
	 * First, we generate all the possible combinations from ID-List
	 * Second, we verify each of them to prune the false positives
	 * @param p
	 * @param RDM
	 * @param nh
	 * @return
	 */
	public List<List<Integer>> InstantiatePaths(List<Integer> p, HashMap<Integer, List<Integer>> RDM, NeighborHood2 nh, Graph q) {
		List<List<Integer>> result = new LinkedList<List<Integer>>();
		
		//Paths of length 1
		List<List<Integer>> path = new LinkedList<List<Integer>>();		
		Integer firstVertex = p.get(0);
		List<Integer> matchesForStart = RDM.get(firstVertex);
		
		//Next in the ID-List of each matching candidates for the first vertex
		//Looking for the vertex satisfying the label contraint
		//And the vertex needs to be within the set of reduced matching candidates for the second vertex
		Integer secondVertex = p.get(1);
		String secondLabel = q.primaryAttribute.get(secondVertex);
		
		for (Integer i : matchesForStart) {
			List<Integer> temp = nh.ChildHood.get(i).get(secondLabel);
			for (Integer j : temp)
				if (RDM.get(secondVertex).contains(j)) {
					List<Integer> tempPath = new LinkedList<Integer>();
					tempPath.add(i);
					tempPath.add(j);
					path.add(tempPath);
				}
			}

		//Paths of length 2
		if (p.size() - 1 == 2) {
			//Go one step further
			Integer thirdVertex = p.get(2);
			String thirdLabel = q.primaryAttribute.get(thirdVertex);
			
			for (List<Integer> pi : path) {
				Integer targetVertex = pi.get(1);
				List<Integer> temp = nh.ChildHood.get(targetVertex).get(thirdLabel);
				for (Integer j : temp)
					if (RDM.get(thirdVertex).contains(j)) {
						List<Integer> tempPath = new LinkedList<Integer>();
						tempPath.addAll(pi);
						tempPath.add(j);
						result.add(tempPath);
					}				
			}
		}
		else 		
			result = path;
		
		return result;
	}
	
	/**
	 * Node in the BFS queue
	 * @author Administrator
	 *
	 */
	class QueueNode {
		Integer VertexID;
		int depth;
		
		LinkedList<Integer> path;
		
		QueueNode(Integer id, int d) {
			VertexID = id;
			depth = d;
			path = new LinkedList<Integer>();
		}		
	}
	
	/**
	 * Given a vertex in the query pattern,
	 * this method generates the paths starting from this vertex
	 * @param q
	 * @param v
	 * @param RDM
	 * @return
	 */
	public List<List<Integer>> GeneratePathForward(Graph q, Integer v, int depth) {
		List<List<Integer>> result = new LinkedList<List<Integer>>();
		
		//The source vertex
		QueueNode s = new QueueNode(v, 0);
		s.path.add(v);

		//The breadth first search queue
		Queue<QueueNode> forward = new LinkedList<QueueNode>();
		forward.add(s);

		HashMap<Integer, QueueNode> visited = new HashMap<Integer, QueueNode>();
		visited.put(s.VertexID, s);
		
		while (forward.size() > 0) {
			QueueNode current = forward.poll();
			
			if (current.depth + 1 <= depth && q.children.containsKey(current.VertexID)) {
				for (Integer i : q.children.get(current.VertexID)) 
					if (!visited.containsKey(i) || visited.get(i).depth == current.depth + 1) {
						//Initialize the node
						QueueNode temp = new QueueNode(i, current.depth + 1);
						//Add all the previous nodes
						temp.path.addAll(current.path);
						
						//Add the next node
						temp.path.add(i);
						
						result.add(temp.path);
						
						if (!visited.containsKey(i))
							visited.put(i, temp);
						
						forward.add(temp);
				}
			}
		}
		
		return result;		
	}
	

	/**
	 * 
)	 * @param q
	 * @param v
	 * @return
	 */
	public List<List<Integer>> GeneratePathBackward(Graph q, Integer v, int depth) {
		List<List<Integer>> result = new LinkedList<List<Integer>>();
		
		//The source vertex
		QueueNode s = new QueueNode(v, 0);
		s.path.add(v);

		//The breadth first search queue
		Queue<QueueNode> backward = new LinkedList<QueueNode>();
		backward.add(s);
		
		HashMap<Integer, QueueNode> visited = new HashMap<Integer, QueueNode>();
		visited.put(s.VertexID, s);
		
		while (backward.size() > 0) {
			QueueNode current = backward.poll();
			
			if (current.depth + 1 <= depth && q.parents.containsKey(current.VertexID)) {
				for (Integer i : q.parents.get(current.VertexID)) {
					if (!visited.containsKey(i) || visited.get(i).depth == current.depth + 1) {
						//Initialize the node
						QueueNode temp = new QueueNode(i, current.depth + 1);
	
						//Maintain the order of the node in the path
						temp.path.add(i);
	
						//Add all the previous nodes
						temp.path.addAll(current.path);
						
						//Add the every path
						result.add(temp.path);
						
						if (!visited.containsKey(i))
							visited.put(i, temp);
						
						backward.add(temp);
					}
				}				
			}
		}
		
		return result;		
	}

	/**
	 * 
	 * @param path
	 * @param RDM
	 * @return
	 */
	public double CalculateSelectivity(List<Integer> path, HashMap<Integer, List<Integer>> RDM) {
		double d = Math.pow(2, path.size() - 1);
		double n = 1.0;
		for (int i = 0; i < path.size(); i++) {
			n *= RDM.get(path.get(i)).size();
		}
		return d / n;
	}
}
