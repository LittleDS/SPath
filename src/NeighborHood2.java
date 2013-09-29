import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * NeighborHood Information Index
 * @author Administrator
 *
 */
public class NeighborHood2 {

	/**
	 * @param args
	 * @throws IOException 
	 */

	Graph g;
	HashMap<Integer, HashMap<String, List<Integer>>> ChildHood;
	HashMap<Integer, HashMap<String, List<Integer>>> GrandchildHood;
	
	HashMap<Integer, HashMap<String, List<Integer>>> ParentHood; 
	HashMap<Integer, HashMap<String, List<Integer>>> GrandparentHood;
	
	public static void main(String[] args) throws IOException {
		NeighborHood2 nh2 = new NeighborHood2();
		nh2.Encode("P2P");
		nh2.OutputToFile("P2P");
	}
	
	/**
	 * 
	 * @param g
	 */
	public void Encode(Graph g) {
		ChildHood = new HashMap<Integer, HashMap<String, List<Integer>>>();
		GrandchildHood = new HashMap<Integer, HashMap<String, List<Integer>>>(); 
		ParentHood = new HashMap<Integer, HashMap<String, List<Integer>>>();
		GrandparentHood = new HashMap<Integer, HashMap<String, List<Integer>>>();
				
		//Build the index for all the vertices
		for (Integer i : g.primaryAttribute.keySet()) {
			//If the vertex has child
			if (g.children.containsKey(i)) {
				if (!ChildHood.containsKey(i))
					ChildHood.put(i, new HashMap<String, List<Integer>>());
				
				HashMap<String, List<Integer>> localInfo = ChildHood.get(i);
				
				List<Integer> c = g.children.get(i);

				for (Integer ci : c) {
					String theAttribute = g.primaryAttribute.get(ci);
					if (!localInfo.containsKey(theAttribute))
						localInfo.put(theAttribute, new LinkedList<Integer>());					
					localInfo.get(theAttribute).add(ci);					
				}
				
				//For the grand children not the direct children
				for (Integer ci : c) {
					//If the vertex has grandchild
					if (g.children.containsKey(ci)) {
						if (!GrandchildHood.containsKey(i))
							GrandchildHood.put(i, new HashMap<String, List<Integer>>());
						
						HashMap<String, List<Integer>> grandlocalInfo = GrandchildHood.get(i);
						
						List<Integer> gc = g.children.get(ci);
						
						for (Integer gci : gc)
							//Remove the direct children
							if (!c.contains(gci)) {
								String theAttribute = g.primaryAttribute.get(gci);
								if (!grandlocalInfo.containsKey(theAttribute))
									grandlocalInfo.put(theAttribute, new LinkedList<Integer>());
								if (!grandlocalInfo.get(theAttribute).contains(gci))
									grandlocalInfo.get(theAttribute).add(gci);
							}
						
					}
				}
				
				if (GrandchildHood.containsKey(i) && GrandchildHood.get(i).size() == 0)
					GrandchildHood.remove(i);
					
			}
			
			if (g.parents.containsKey(i)) {
				if (!ParentHood.containsKey(i)) 
					ParentHood.put(i, new HashMap<String, List<Integer>>());
				
				HashMap<String, List<Integer>> localInfo = ParentHood.get(i);
				
				List<Integer> p = g.parents.get(i);
				
				for (Integer pi : p) {
					String theAttribute = g.primaryAttribute.get(pi);
					if (!localInfo.containsKey(theAttribute))
						localInfo.put(theAttribute, new LinkedList<Integer>());
					localInfo.get(theAttribute).add(pi);
				}
				
				//For each parent
				for (Integer pi : p) {
					if (g.parents.containsKey(pi)) {
						if (!GrandparentHood.containsKey(i)) 
							GrandparentHood.put(i, new HashMap<String, List<Integer>>());
						
						HashMap<String, List<Integer>> grandlocalInfo = GrandparentHood.get(i);
					
						List<Integer> gp = g.parents.get(pi);
							
						//For each grand parent
						for (Integer gpi : gp) 
							if (!p.contains(gpi)) {
								String theAttribute = g.primaryAttribute.get(gpi);
								if (!grandlocalInfo.containsKey(theAttribute))
									grandlocalInfo.put(theAttribute, new LinkedList<Integer>());
								if (!grandlocalInfo.get(theAttribute).contains(gpi))
									grandlocalInfo.get(theAttribute).add(gpi);
							}
					}
				}
				
				if (GrandparentHood.containsKey(i) && GrandparentHood.get(i).size() == 0)
					GrandparentHood.remove(i);
			}
			
		}			
	}
	
	public void Encode(String fileName) throws IOException {
		//Only initialize the graph when we start to use it
		g = new Graph();
		g.loadGraphFromFile(fileName);
		Encode(g);
	}

	/**
	 * Output any of the hood index
	 * @param fileName
	 * @param nh
	 * @throws IOException
	 */
	public void OutputStructure(String fileName, HashMap<Integer, HashMap<String, List<Integer>>> nh) throws IOException {
		//Output to file		
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);
		
		//Write the index into file
		for (Integer i : nh.keySet()) {
			out.write(i + " ");
			
			HashMap<String, List<Integer>> temp = nh.get(i);
			
			out.write(temp.keySet().size() + "\r\n");
			
			for (String s : temp.keySet()) {
				out.write(s);
				for (Integer j : temp.get(s)) {
					out.write(" " + j);
				}
				out.write("\r\n");
			}			
		}
		
		//don't forget to close the stream
		out.close();		
	}
	
	/**
	 * 
	 * @param fileName
	 * @throws IOException 
	 */
	public void OutputToFile(String fileName) throws IOException {
		//Output to file		
		OutputStructure(fileName + "Child", ChildHood);
		OutputStructure(fileName + "Grandchild", GrandchildHood);
		OutputStructure(fileName + "Parent", ParentHood);
		OutputStructure(fileName + "Grandparent", GrandparentHood);		
	}
	
	public void LoadStructure(String fileName, HashMap<Integer, HashMap<String, List<Integer>>> nh) throws FileNotFoundException {
		//Child Part
		File cFile = new File(fileName);
		
		Scanner cScanner = new Scanner(cFile);		
		while (cScanner.hasNext()) {
			String IDString = cScanner.nextLine();
			String[] ids = IDString.split(" ");
			Integer ID = Integer.parseInt(ids[0]);
			int lineNumber = Integer.parseInt(ids[1]);
			
			nh.put(ID, new HashMap<String, List<Integer>>());
			
			for (int i = 0; i < lineNumber; i++) {
				String neigh = cScanner.nextLine();
				String[] slist = neigh.split(" ");
				HashMap<String, List<Integer>> localInfo = nh.get(ID);			
				localInfo.put(slist[0], new LinkedList<Integer>());
				for (int j = 1; j < slist.length; j++)
					localInfo.get(slist[0]).add(Integer.parseInt(slist[j]));
			}
			
		}
		cScanner.close();
		
	}
	
	/**
	 * 
	 * @param fileName
	 * @throws FileNotFoundException 
	 */
	public void loadIndexFromFile(String fileName) throws FileNotFoundException {
		ChildHood = new HashMap<Integer, HashMap<String, List<Integer>>>();
		ParentHood = new HashMap<Integer, HashMap<String, List<Integer>>>();		
		
		LoadStructure(fileName + "Child", ChildHood);
		
		LoadStructure(fileName + "Parent", ParentHood);

		GrandchildHood = new HashMap<Integer, HashMap<String, List<Integer>>>();
		GrandparentHood = new HashMap<Integer, HashMap<String, List<Integer>>>();		

		LoadStructure(fileName + "Grandchild", GrandchildHood);
		
		LoadStructure(fileName + "Grandparent", GrandparentHood);		
	}	
	

	
	public boolean Check(HashMap<Integer, HashMap<String, List<Integer>>> qstructure, Integer s, Integer t, HashMap<Integer, HashMap<String, List<Integer>>> structure) {
		if (qstructure.containsKey(s)) {
			HashMap<String, List<Integer>> nhQ = qstructure.get(s);
			
			if (!structure.containsKey(t))
				return false;
			
			HashMap<String, List<Integer>> nhD = structure.get(t);
			
			//First check the childhood
			for (String skey : nhQ.keySet())
				if (!nhD.containsKey(skey))
					return false;
				else if (nhD.get(skey).size() < nhQ.get(skey).size()) {
					return false;
				}
		}
		
		return true;
	}
	
	/**
	 * Check the containment of neighborhood index
	 * @param query is the neighborhood of the query pattern
	 * @param s is the vertex in the query pattern
	 * @param t is the corresponding matching vertex in the data graph
	 * @return
	 */
	public boolean Check(NeighborHood2 query, Integer s, Integer t) {
		if (query.ChildHood.containsKey(s)) {
			HashMap<String, List<Integer>> nhQ = query.ChildHood.get(s);
			
			if (!ChildHood.containsKey(t))
				return false;
			
			HashMap<String, List<Integer>> nhD = ChildHood.get(t);
			
			//First check the childhood
			for (String skey : nhQ.keySet())
				if (!nhD.containsKey(skey))
					return false;
				else if (nhD.get(skey).size() < nhQ.get(skey).size()) {
					return false;
				}
		}
		
		//Second check the parenthood		
		if (query.ParentHood.containsKey(s)) {
			HashMap<String, List<Integer>> nhQ = query.ParentHood.get(s);
			
			if (!ParentHood.containsKey(t))
				return false;

			HashMap<String, List<Integer>> nhD = ParentHood.get(t);
			
			for (String skey : nhQ.keySet())
				if (!nhD.containsKey(skey))
					return false;
				else if (nhD.get(skey).size() < nhQ.get(skey).size()) {
					return false;
				}
		}
		
		if (query.GrandchildHood.containsKey(s)) {
			HashMap<String, List<Integer>> nhQ = query.GrandchildHood.get(s);
			
			if (!GrandchildHood.containsKey(t))
				return false;
			
			HashMap<String,  List<Integer>> nhD = GrandchildHood.get(t);
			
			for (String skey : nhQ.keySet())
				if (!nhD.containsKey(skey))
					return false;
				else if (nhD.get(skey).size() < nhQ.get(skey).size()) {
					return false;
				}					
		}
		
		if (query.GrandparentHood.containsKey(s)) {
			HashMap<String, List<Integer>> nhQ = query.GrandparentHood.get(s);
			
			if (!GrandparentHood.containsKey(t))
				return false;

			HashMap<String, List<Integer>> nhD = GrandparentHood.get(t);
			
			for (String skey : nhQ.keySet())
				if (!nhD.containsKey(skey))
					return false;
				else if (nhD.get(skey).size() < nhQ.get(skey).size()) {
					return false;
				}
		}
		
		return true;
	}	
}
