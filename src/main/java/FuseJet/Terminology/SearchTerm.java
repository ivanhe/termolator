package FuseJet.Terminology;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class SearchTerm {

  public static void main(String[] args) throws IOException {
	  /*String input = "/Users/shashaliao/Research/FUSE/DNA_Giga_IDF_ranking.txt";
	  String output = "/Users/shashaliao/Research/FUSE/DNA_Giga_IDF_rankingList.txt";
	  rewrite(input,output);*/
	  searchATerm( args);
  }
 
  public static void searchATerm(String[] args)throws IOException{
		String termFile = args[0];
		List<Term> terms = Term.readTermsFromFile(termFile);
		String term = args[1];
		for(int i=0;i<terms.size();i++){
			Term t = terms.get(i);
			if(t.word.equals(term)){
				System.err.println("The "+i+"-th terminology is :\t"+t.toString());
				return;
			}				
		}
	}
  
  public static void rewrite(String inputFile, String outputFile)throws IOException{
	  List<Term> terms = Term.readTermsFromFile(inputFile);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile),"utf-8"));
	  for(Term t: terms){
		  writer.write(t.word+"\n");
	  }
	  writer.close();
  }
}
