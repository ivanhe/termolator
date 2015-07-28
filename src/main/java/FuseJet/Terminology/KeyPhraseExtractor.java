package FuseJet.Terminology;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KeyPhraseExtractor {

	int maxSize = 10;
	String iDirectory;
	String oDirectory;
	String fileList;
	TermStatistics statis;

	static boolean trace = false;

	public static void main(String[] args) throws IOException{

		if(args.length == 5){
			trace = false;
			KeyPhraseExtractor extractor = new KeyPhraseExtractor(args[0], args[1], args[2], args[3], Integer.parseInt(args[4]));
			extractor.extract();
		}
		else if(args.length == 3){
			trace = true;
			KeyPhraseExtractor extractor = new KeyPhraseExtractor(args[2]);
			extractor.processOneDocument(args[0], args[1]);
		}
		else {
			System.err.println("Parameter not correct\n Please type: " +
					"FileList \t  inputFileDirectory  \t outputDirectory \t + statisFile \t +maxSize");
			return;
		}
	}

	KeyPhraseExtractor(String sFile)throws IOException{
		statis = new TermStatistics(sFile);
	}

	KeyPhraseExtractor(String fList, String inputDirectory, String outputDirectory, String sFile, int num) throws IOException{
		iDirectory = inputDirectory;
		oDirectory = outputDirectory;
		fileList = fList;
		statis = new TermStatistics(sFile);
		maxSize = num;
	}


	private void extract()throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		String fileID;
		while((fileID = reader.readLine()) != null){
			processOneDocument(iDirectory+fileID+".chunk", oDirectory+fileID+".key");
		}
		reader.close();
	}

	void processOneDocument(String fileName, String outputFile)throws IOException{
		File f = new File(fileName);
		if(f.exists() == false)
			return;
		NPExtractor extractor = new NPExtractor(true);
		Map<String, Integer> chunks = extractor.extractNPFromDocument(fileName);
		List<WordFreq> words = new ArrayList<WordFreq>();
		for(String chunk:chunks.keySet()){
			double weight = chunks.get(chunk)/statis.wordfreqs.get(chunk).freq;
			WordFreq wf = new WordFreq(chunk, weight);
			words.add(wf);
			if(trace)
				System.err.println(chunk+"\t"+chunks.get(chunk)+"\t"+statis.wordfreqs.get(chunk).freq+"\t"+weight);
		}
		Collections.sort(words);
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		for(int i=0;i<Math.min(maxSize, words.size());i++){
			writer.write(words.get(i).toString()+"\n");
		}
		writer.close();
	}
}
