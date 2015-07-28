package FuseJet.Terminology;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class CopyFiles {

	/**
	 * extract text (abstract or body) from patent articles in FUSENET
	 */
	static List<String> fileList = new ArrayList<String>();
	String fileListFile;
	String journalListFile;
	String inputDir;
	String outputDir;

	public static void main(String[] args) throws IOException, InterruptedException{
		if(args.length ==3 ){	
			for(int i=1;i<3;i++){
				File dir = new File(args[i]);
				if(!dir.isDirectory()){
					System.err.println("Error:"+args[i]+" must be a directory...");
					return;
				}
			}
			CopyFiles extractor  = new CopyFiles(args[0], args[1],args[2]);
			extractor.extractData();
		}
		else {
			System.err.println("Please put correct parameters:");
			System.err.println("fileListFile \t inputDir \t outputDir \t ");
			return;
		}
		/*TextExtractor extractor  = new TextExtractor();
		extractor.processDocument(new File("/Users/shashaliao/Research/FUSE/f3ff0960-bd40-11e0-9557-52c9fc93ebe0-001-1758-5996-1-12.nxml"), 
				"/Users/shashaliao/Research/FUSE/");*/
	}

	public CopyFiles(){
	}

	public CopyFiles(String fileListFile, String inputDir, String outputDir){
		this.fileListFile = fileListFile;
		this.journalListFile = fileListFile+".journal";
		this.inputDir = inputDir;
		this.outputDir = outputDir;
	}

	public void extractData() throws IOException{
		recursiveProcessOneDirectory(new File(inputDir),outputDir);
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileListFile));
		for(String file:fileList){
			writer.write(file+"\n");
		}
		writer.close();
	}

	public  void recursiveProcessOneDirectory(File dir, String outputDir) throws IOException{
		File[] files = dir.listFiles();
		for(File f:files){
			if(f.isDirectory()){
				recursiveProcessOneDirectory(f,outputDir);
			}
			else if(f.isFile()){
				processDocument(f, outputDir);
			}
		}		
	}

	public void processDocument(File f, String outputDir)throws IOException{
		String docID = f.getName();
		if(docID.endsWith("nxml") == false)
			return;
		docID = docID.substring(0, docID.lastIndexOf("."));
		copy(f, docID, outputDir);         
	}


	public String  copy(File f, String docID, String outputDir) throws IOException{
		String text = readFromOneDocument(f);
		writeToFile(text, outputDir+docID+".nxml" );
		System.err.println(f.getAbsolutePath());
		fileList.add(docID);
		return docID;
	}

	public void writeToFile(String text, String outputFile)throws IOException{
		BufferedWriter writer= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),"utf-8"));
		writer.write(text+"\n");
		writer.close();
	}

	public String readFromOneDocument(File f) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
		StringBuffer strbuf = new StringBuffer("");
		String line;
		while((line = reader.readLine()) != null){
			strbuf.append(line);
		}
		reader.close();
		String text = strbuf.toString();
		return text;
	}	

}
