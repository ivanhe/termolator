package FuseJet.Terminology;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JournalSorter {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
		//sortJournal();
		String inputFile = "/Users/shashaliao/Research/FUSE/MITRE_list_withJournal.txt";
		String journalName = "Arthritis Research & Therapy";
		String relFile = "/Users/shashaliao/Research/FUSE/MITRE_"+journalName+"_pos.txt";
		String negFile = "/Users/shashaliao/Research/FUSE/MITRE_"+journalName+"_neg.txt";
		getRelList(inputFile, journalName, relFile, negFile);
	}
	
	public static void getRelList(String inputFile, String journalName, String relFile, String negFile)throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer1 = new BufferedWriter(new FileWriter(relFile));
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(negFile));
		String fileID;
		while((fileID = reader.readLine()) != null){
			if(fileID.startsWith(journalName+"/")){
				writer1.write(fileID+"\n");
			}
			else
				writer2.write(fileID+"\n");
		}
		reader.close();
		writer1.close();
		writer2.close();
	}
	
	
	public static void sortJournal()throws IOException{
		String jFile = "/Users/shashaliao/Research/FUSE/FUSE_MITRE_list.txt.journal";
		String oFile = "/Users/shashaliao/Research/FUSE/FUSE_MITRE_sorted_journal.txt";
		int total = 0;
		double kept = 0;
		String line;
		List<Journal> js = new ArrayList<Journal>();
		BufferedReader reader = new BufferedReader(new FileReader(jFile));
		while((line = reader.readLine()) != null){
			//System.err.println(line);
			Journal j = Journal.buildJournal(line);
			total += j.num;
			js.add(j);			
		}
		reader.close();
		Collections.sort(js);
		BufferedWriter writer = new BufferedWriter(new FileWriter(oFile));
		for(Journal j:js){
			if(j.num < 1000)
				break;
			writer.write(j.name+"\t"+j.num+"\n");
			System.err.println(j.name);
			kept += j.num;
		}
		writer.close();
		System.err.println("There are total "+total +" journals, and "+kept+" are saved.");
		System.err.println("The saved portion is : "+kept/total);
	}

}

class Journal implements Comparable<Journal>{
	String name;
	int num = 0;

	Journal(String n, int f){
		name = n;
		num = f;
	}
	
	static Journal  buildJournal(String line){
		int pos = line.lastIndexOf("\t");
		return new Journal(line.substring(0,pos), Integer.parseInt(line.substring(pos+1)));
	}

	@Override
	public int compareTo(Journal o) {
		if(num > o.num)
			return -1;
		else if(num < o.num)
			return 1;
		else	
			return 0;
	}

}
