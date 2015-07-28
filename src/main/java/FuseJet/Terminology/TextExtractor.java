package FuseJet.Terminology;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class TextExtractor {

	/**
	 * extract text (abstract or body) from patent articles in FUSENET
	 */
	static List<String> fileList = new ArrayList<String>();	

	static String unicode = "utf-8";
	String fileListFile;
	String inputDir;
	String outputDir;

	public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException, SAXException{
		if(args.length >= 3){	
			TextExtractor extractor  = new TextExtractor(args[0], args[1],args[2]);
			extractor.extractData();
		}
		else if(args.length == 2){
			TextExtractor extractor  = new TextExtractor();
			extractor.processDocument(args[0], args[0].substring(args[0].lastIndexOf("/")+1),args[1]);
		}
		else {
			System.err.println("Please put correct parameters:");
			System.err.println("fileListFile \t inputDir \t outputDir \t ");
			System.err.println("OR inputFile \t outputFile ");
			return;
		}
		/*TextExtractor extractor  = new TextExtractor();
		extractor.processDocument("/Users/shashaliao/Research/FUSE/02886ee3-bd41-11e0-9557-52c9fc93ebe0-001-gkh936.nxml", 
				"02886ee3-bd41-11e0-9557-52c9fc93ebe0-001-gkh936","/Users/shashaliao/Research/FUSE/");*/
	}

	public TextExtractor(){
	}

	public TextExtractor(String fileListFile, String inputDir, String outputDir){
		this.fileListFile = fileListFile;
		this.inputDir = inputDir;
		this.outputDir = outputDir;
	}

	public void extractData() throws IOException, ParserConfigurationException, SAXException{
		processOneDirectory(fileListFile, inputDir,outputDir);
		//recursiveProcessOneDirectory(new File(inputDir),outputDir);
		BufferedWriter writer= new BufferedWriter(new FileWriter(fileListFile.substring(0, fileListFile.lastIndexOf("."))+".txt"));
		for(String file:fileList){
			writer.write(file+"\n");
		}
		writer.close();
	}

	public void processOneDirectory(String listFile, String inputDir,String outputDir)throws IOException, ParserConfigurationException, SAXException{
		BufferedReader reader = new BufferedReader(new FileReader(listFile));
		String docID;
		while((docID = reader.readLine()) != null){
			if(docID.endsWith(".nxml")){
				docID = docID.substring(0, docID.length()-5);
			}
			processDocument(inputDir+docID+".nxml", docID, outputDir);
		}
		reader.close();
	}

	public void processDocument(String fileName, String docID, String outputDir)throws IOException, ParserConfigurationException, SAXException{
		String text = readFromOneDocument(fileName);
		if(text == null)
			return;
		writeToFile(text, outputDir+docID+".sgm" );
		System.err.println(docID);
		fileList.add(docID);       
	}


	public void writeToFile(String text, String outputFile)throws IOException{

		BufferedWriter writer= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),unicode));
		writer.write("<DOC>\n<TEXT>\n"+text+"\n</TEXT>\n</DOC>\n");
		writer.close();
	}

	public String readFromOneDocument(String filename) throws IOException, ParserConfigurationException, SAXException{
		String tmp = readFile(filename);
		if(tmp.length() == 0)return null;
		ByteArrayInputStream stream = new ByteArrayInputStream(tmp.getBytes());
		InputStream is = stream;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();	
		Document document = builder.parse(is);
		Element rootElement = document.getDocumentElement();	
		NodeList jnodes = rootElement.getElementsByTagName("journal-title");
		if(jnodes.getLength() == 0 )
			return null;
		//journal_title = jnodes.item(0).getTextContent().replace("/", "-");

		NodeList titlenodes = rootElement.getElementsByTagName("article-title");
		if(titlenodes.getLength() == 0)
			return null;
		String title = extractSectionText((Element)titlenodes.item(0));	

		NodeList abstractnodes = rootElement.getElementsByTagName("abstract");
		if(abstractnodes.getLength() == 0)
			return null;
		String abstractText = extractSectionText((Element)abstractnodes.item(0));			

		NodeList bodynodes = rootElement.getElementsByTagName("body");
		String bodyText ="";
		if(bodynodes.getLength() != 0){
			bodyText = extractSectionText((Element)bodynodes.item(0));
		}
		//System.err.println(title+"\n"+abstractText+"\n"+bodyText);
		return title+abstractText+"\n"+bodyText;
	}

	public String extractSectionText(Element textNode){
		StringBuffer buffer = new StringBuffer();
		NodeList abstractnodes = textNode.getElementsByTagName("p");
		if(abstractnodes.getLength() > 0){
			for(int i=0;i < abstractnodes.getLength();i++){
				Node node= abstractnodes.item(i);
				buffer.append("<P>"+node.getTextContent().replace("<", "&lt")+"\n</P>\n");
			}
		}
		else{
			buffer.append("<P>"+textNode.getTextContent()+"\n</P>\n");
		}
		return buffer.toString();
	}



	private String readFile(String filename) {
		String templetContent = "";
		try {
			FileInputStream fileinputstream = new FileInputStream(filename);
			int length = fileinputstream.available();
			byte[] bytes = new byte[length];
			fileinputstream.read(bytes);
			fileinputstream.close();
			templetContent = new String(bytes, unicode);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return templetContent;
	}

}
