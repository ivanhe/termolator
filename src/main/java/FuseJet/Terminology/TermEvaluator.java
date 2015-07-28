package FuseJet.Terminology;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TermEvaluator {

	Mesh2012 mesh = null;

	static int batch = 10;
	static int portion = 300;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
         TermEvaluator evaluator = new TermEvaluator();
         /*evaluator.readMesh("/Users/shashaliao/Research/FUSE/mesh_vocabulary.txt");
         evaluator.evaluate("/Users/shashaliao/Research/FUSE/bioMed_ranking_list.txt");*/
        //evaluator.evaluate("/Users/shashaliao/Research/FUSE/arthritis_ranking_DR_DC.txt");
         evaluator.readMesh(args[0]);
         evaluator.evaluate(args[1]);
	}

	void readMesh(String dicFile)throws IOException{
		mesh = new Mesh2012(dicFile);
	}
	
	void evaluate(String filename)throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"utf-8"));
		String line ;
		List<String> rankList = new ArrayList<String>();
		while((line = reader.readLine()) != null){
			String[] tokens = line.split("\t");
			rankList.add(tokens[0]);
		}
		reader.close();
		evaluate(rankList);
	}

	void evaluate(List<String> rankList){
		int total = match(rankList, rankList.size());
		System.err.println("total : "+total);
		for(int i=0;i< batch;i++){
			int size = (i+1)*portion;
			
			int match = match(rankList, size);
			double precision = (double)match/Math.min(size, rankList.size());
			double recall = (double)match/total;
			System.err.println("size:"+size+"\t match:"+match);
			System.err.println(precision+"\t"+recall+"\t"+2*precision*recall/(precision+recall));
		}
	}

	int match(List<String> rankList, int topRank){
		int match = 0;
		for(int i=0; i<Math.min(topRank, rankList.size()); i++){
			String term = rankList.get(i);
			if(mesh.containsTerm(term))
				match++;
		}
		return match;
	}


}


