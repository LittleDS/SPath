import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class VertexIndex {
	Graph graph;
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		VertexIndex vi = new VertexIndex();
		vi.Encode("P2P");

	}

	public void Encode(String fileName) throws IOException {
		graph = new Graph();
		graph.loadGraphFromFile(fileName);
		
		IndexVertices(fileName + "Vertices");
		System.out.println("Finish");		
	}
	
	HashMap<String, List<Integer>> verticesIndex = new HashMap<String,List<Integer>>();
	
	/**
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void IndexVertices(String fileName) throws IOException {
		for (Integer i : graph.primaryAttribute.keySet()) {
			String sAttribute = graph.primaryAttribute.get(i);

			if (!verticesIndex.containsKey(sAttribute))
				verticesIndex.put(sAttribute, new LinkedList<Integer>());
			
			verticesIndex.get(sAttribute).add(i);
		}
		
		//Open the file
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);

		//Write the index into file
		for (String s : verticesIndex.keySet()) {

			out.write(s);

			//All the vertices that contains the two attributes
			for (Integer i : verticesIndex.get(s)) {
				out.write("," + i);
			}

			//Write into the file
			out.write("\r\n");
		}

		//don't forget to close the stream
		out.close();		
	}
	
	/**
	 * Load the vertices index from file
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public void loadVerticesIndexFromFile(String fileName) throws FileNotFoundException {
		
		verticesIndex.clear();
		
		File tFile = new File(fileName);
		
		Scanner tScanner = new Scanner(tFile);
		
		//Read the original graph file and build the graph in the main memory
		while (tScanner.hasNext()) {
			String idLine = tScanner.nextLine();			
			String[] strings = idLine.split(",");
			
			String attribute = strings[0];
			if (!verticesIndex.containsKey(attribute))
				verticesIndex.put(attribute, new LinkedList<Integer>());
						
			List<Integer> toInsert = verticesIndex.get(attribute);
			for (int i = 1; i < strings.length; i++) {
				toInsert.add(Integer.parseInt(strings[i]));
			}			
		}
		
		tScanner.close();			
	}
}
