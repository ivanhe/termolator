package FuseJet.Terminology;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import AceJet.Ace;
import Jet.Lex.Tokenizer;
import Jet.Tipster.Annotation;
import Jet.Tipster.Document;
import Jet.Tipster.ExternalDocument;
import Jet.Tipster.Span;
import Jet.Zoner.SentenceSplitter;
import Jet.Zoner.SpecialZoner;


public class NGramExtractor {

	/**
	 * This class extract n-gram information, and write n-grams into a term file.
	 */
	
	String fileList;
	String inputDir;
	String outputDir;
	String inputSuffix;
	String outputSuffix;
	
	NGramExtractor(String fileList, String inputDir, String inputSuffix, String outputDir, String outputSuffix){
		this.inputDir =  inputDir;
		this.outputDir = outputDir;
		this.fileList = fileList;
		this.inputSuffix = inputSuffix;
		this.outputSuffix = outputSuffix;
	}

	public void extractNGram() throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		String fileID;
		while((fileID = reader.readLine()) != null){
			System.err.println(fileID);
			processDocument(inputDir+fileID+"."+inputSuffix, outputDir+fileID+"."+outputSuffix);			
		}
		reader.close();
	}

	//using n-gram to extract terminology
	public static void processDocument(String inputName, String outputName) throws IOException{
		Document doc = getDocument(inputName);
		Vector<Annotation> tokens = doc.annotationsOfType("token");
		if(tokens == null)
			return;
		List<String> words = new ArrayList<String>();
		Map<String,Integer> ngrams = new HashMap<String, Integer>();
		for(Annotation token:tokens){   
			String word = doc.text(token.span()).toLowerCase().replace("\n", " ").trim();
			words.add(word);
			addToMap(word, ngrams,1);
		}
		for(int i=0;i<words.size()-1;i++){
			String bigram = words.get(i)+" "+words.get(i+1);
			addToMap(bigram, ngrams,1);
		}
		for(int i=0;i<words.size()-2;i++){
			String trigram = words.get(i)+" "+words.get(i+1)+" "+words.get(i+2);
			addToMap(trigram, ngrams,1);
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputName));
		for(String word:ngrams.keySet())
			writer.write(word+":"+ngrams.get(word)+"\n");
		writer.close();
	}

	private static Document getDocument(String sgmfile)throws IOException{
		ExternalDocument doc = new ExternalDocument("sgml", sgmfile);
		doc.setAllTags(true);
		doc.open();
		// find text segment(s)
		doc.annotateWithTag ("TEXT");
		SpecialZoner.findSpecialZones (doc);
		Vector<Annotation> textSegments = doc.annotationsOfType ("TEXT");
		if (textSegments == null) {
			System.out.println ("No <TEXT> in " + doc.fileName() + ", skipped.");
			return doc;
			//System.exit(1);
		}
		// split into sentences
		for (Annotation ann : textSegments) {
			Span textSpan = ann.span ();
			Ace.monocase = Ace.allLowerCase(doc);
			SentenceSplitter.split (doc, textSpan);
		}
		Span sp = doc.fullSpan();
		Tokenizer.tokenize (doc, sp);
		return doc;
	}
	
	private static void addToMap(String word, Map<String, Integer> map, int freq){
		if(map.containsKey(word))
			freq += map.get(word);
		map.put(word, freq);
	}
	
	public static void main(String[] args) throws IOException{
		if(args.length != 5){
			System.err.println("Parameter not correct\n Please type: " +
					"FileList \t " +
					"inputDir \t inputSuffix \t" +
					"outputDir \t outputSuffix \n System exiting ...");
			return;
		}
        NGramExtractor extractor = new NGramExtractor(args[0], args[1], args[2],args[3],args[4]);
        extractor.extractNGram();
	}


}
