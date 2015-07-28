package FuseJet.Terminology;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TermStatistics {

	Map<String, WordFreq> wordfreqs = new HashMap<String, WordFreq>();
	NPParser parser;

	public TermStatistics(String fileListFile, String corpusDir, boolean useJet) throws IOException{
		if(useJet)
			parser = new JetNPParser();
		else
			parser = new GeniaNPParser();
		extract(fileListFile, corpusDir);
	}
	
	public TermStatistics(String statisFile)throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(statisFile),"utf-8"));
		String line;
		while((line = reader.readLine()) != null){
			WordFreq wf = WordFreq.read(line);
			wordfreqs.put(wf.word, wf);
		}
		reader.close();
	}

	private void extract(String fileListFile,  String corpusDir) throws IOException{
		List<String> fileList = TerminologyExtractor.readList(fileListFile);
		for(String fileID:fileList){
			System.err.println(fileID);
			processOneDocument(corpusDir+fileID+".chunk");
		}
	}

	public void sortAndWrite(String outputFile)throws IOException{
		List<WordFreq> freqs = new ArrayList<WordFreq>();
		for(WordFreq wf : wordfreqs.values()){
			freqs.add(wf);
		}		
		Collections.sort(freqs);
		BufferedWriter writer= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),"utf-8"));
		for(WordFreq wf: freqs){
			writer.write(wf.word+"\t"+wf.freq+"\n");
		}
		writer.close();
	}

	public void processOneDocument(String inputName)throws IOException{
		File f = new File(inputName);
		if(f.exists() == false)
			return;
		List<NounPhrase> chunks = parser.NPParse(inputName);
		for(NounPhrase chunk:chunks){
			int freq = 1;
			if(wordfreqs.containsKey(chunk)){
				wordfreqs.get(chunk).addFreq(freq);
			}
			else{
				WordFreq wf = new WordFreq(chunk.getSequence(), freq);
				wordfreqs.put(chunk.getSequence(), wf);
			}			
		}
	}

	public static void main(String[] args) throws IOException{
		if(args.length != 3){
			System.err.println("Parameter not correct\n Please type: " +
					"FileList \t inputFileDirectory  \t statisticFile");
			return;
		}
		TermStatistics stastic = new TermStatistics(args[0], args[1], true);
		stastic.sortAndWrite(args[2]);
	}

}

