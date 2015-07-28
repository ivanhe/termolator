package FuseJet.Utils;

import AceJet.Gazetteer;
import FuseJet.Annotators.FuseJetAnnotator;
import FuseJet.IO.FuseDocumentReader;
import FuseJet.IO.FuseDocumentWriter;
import FuseJet.IO.InitializableFuseDocumentWriter;
import FuseJet.Models.FuseDocument;
import Jet.Lex.EnglishLex;
import Jet.Lex.Lexicon;
import Jet.Pat.Pat;
import Jet.Pat.PatternCollection;
import Jet.Tipster.Annotation;
import Jet.Tipster.Span;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: yhe
 * Date: 6/22/12
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Console {
    private static Properties props = new Properties();

//    @Deprecated
//    private static MENameTagger nameTagger = null;

    private static String configFileName;

    private static List<FuseJetAnnotator> annotators = new ArrayList<FuseJetAnnotator>();

    private static String[] inputFiles;

    private static String[] outputFiles;

    private static FuseDocumentReader reader;

    private static FuseDocumentWriter writer;

//    private static String[] spanNamesToAnnotate;
//
//    private static String[] tagsToOutput;

    private static Gazetteer gazetteer;

    private static int maxAnnotationInput = Integer.MAX_VALUE;

    public static Gazetteer getGazetteer() {
        return gazetteer;
    }

    public static PatternCollection pc = null;

    public static String getGazetteerFile() {
        return props.getProperty("Gazetteer.fileName");
    }

    public static String[] getLexFiles() {
        int i = 1;
        List<String> result = new ArrayList<String>();
        while (props.containsKey(
                String.format("EnglishLex.fileName%d", i))) {
            result.add(props.getProperty(String.format("EnglishLex.fileName%d", i)));
            i++;
        }
        return result.toArray(new String[result.size()]);
    }

    public static String getNameTagsMEFile() {
        return props.getProperty("NameTags.ME.fileName");
    }

    public static String getWordClustersFile() {
        return props.getProperty("WordClusters.fileName");

    }

//    @Deprecated
//    public static MENameTagger getNameTagger() {
//        return nameTagger;
//    }

    public static void initialize() {
        try {
            props.load(new FileInputStream(configFileName));
        } catch (IOException e) {
            System.err.println("Error reading props file: " + configFileName);
            e.printStackTrace();
        }

        updatePropVariables();

        boolean shouldLoadBasicResource = true;
        // Prepare Reader and Writer
        if (props.getProperty("FuseJet.loadBasicResource") != null && props.getProperty("FuseJet.loadBasicResource")
                .toLowerCase().trim().equals("false")) {
            shouldLoadBasicResource = false;
        }

        try {
            if (!props.getProperty("FuseJet.IO.FuseDocumentReader").contains(".")) {
                reader =
                        (FuseDocumentReader) Class.forName("FuseJet.IO." + props.getProperty("FuseJet.IO.FuseDocumentReader")).newInstance();
            }
            else {
                reader =
                        (FuseDocumentReader) Class.forName(props.getProperty("FuseJet.IO.FuseDocumentReader")).newInstance();
            }

        } catch (Exception e) {
            System.err.println("Error creating FuseDocumentReader. Please check props file.");
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            if (!props.getProperty("FuseJet.IO.FuseDocumentWriter").contains(".")) {
                writer =
                        (FuseDocumentWriter) Class.forName("FuseJet.IO." + props.getProperty("FuseJet.IO.FuseDocumentWriter")).newInstance();
            }
            else {
                writer =
                        (FuseDocumentWriter) Class.forName(props.getProperty("FuseJet.IO.FuseDocumentWriter")).newInstance();
            }

            if (writer instanceof InitializableFuseDocumentWriter) {
                String[] resourceInfos = props.getProperty(
                        props.getProperty("FuseJet.IO.FuseDocumentWriter").trim()
                                + "." + "ResourceInformation").trim().split(";");
                ((InitializableFuseDocumentWriter)writer).initialize(resourceInfos);

            }
        } catch (Exception e) {
            System.err.println("Error creating FuseDocumentWriter. Please check props file.");
            e.printStackTrace();
            System.exit(-1);
        }

        // determine max input file size
        if (props.getProperty("FuseJet.MaxAnnotationInput") != null) {
            maxAnnotationInput = Integer.valueOf(props.getProperty("FuseJet.MaxAnnotationInput"));
        }

        if (shouldLoadBasicResource) {
            // Load Lexicon
            Lexicon.clear();
            String[] lexFiles = getLexFiles();
            for (String lexFile : lexFiles) {
                try {
                    EnglishLex.readLexicon(lexFile);
                } catch (Exception e) {
                    System.err.println("Error reading LEX file: " + lexFile + "\nPlease check your props file.");
                }
            }

            String gazetteerFileName = getGazetteerFile();
            gazetteer = new Gazetteer();
            try {
                gazetteer.load(gazetteerFileName);
            } catch (IOException e) {
                System.err.println("Error reading Gazetteer file: " + gazetteerFileName);
                System.exit(-1);
            }
        }
        // Prepare MaxEnt NameTagger
        //nameTagger = new MENameTagger();

//        try {
//            nameTagger.mene.loadWordClusters(getWordClustersFile());
//        } catch (IOException e) {
//            System.err.println("Error reading MENameTagger word cluster file: " + getWordClustersFile());
//        }
//
//        try {
//            nameTagger.load(getNameTagsMEFile());
//        } catch (IOException e) {
//            System.err.println("Error reading MENameTagger model file: " + getNameTagsMEFile());
//        }

//        // Find spans to be annotate
//        if (!props.containsKey("FuseJet.SpansToAnnotate")) {
//            spanNamesToAnnotate = null;
//        } else {
//            spanNamesToAnnotate = props.getProperty("FuseJet.SpansToAnnotate").split(",");
//        }

//        // Find tags to output
//        try {
//            tagsToOutput = props.getProperty("FuseJet.AnnotationsToOutput").trim().split(",");
//        } catch (Exception e) {
//            System.err.println("WARNING: Unable to parse annotations to output. Tags set to be empty");
//            tagsToOutput = new String[]{};
//        }

        //readPatterns
        readPatterns();

        // LoadAnnotators
        loadAnnotators();
    }

    public static void readPatterns() {
        pc = new PatternCollection();
        // read in patterns
        int i = 1;
        while (props.containsKey("Pattern.fileName" + i)) {
            String fileName = props.getProperty("Pattern.fileName" + i);
            try {
                File patternFile = new File(fileName);
                pc.readPatternCollection(new BufferedReader(new FileReader(patternFile)));
            } catch (IOException ioe) {
                System.err.println("Error: reading pattern file " + fileName + ", "
                        + ioe.getMessage());
            }
            i++;
        }

        pc.makePatternGraph();
        String trace = props.getProperty("Pattern.trace");
        if (trace != null) {
            if (trace.equals("on"))
                Pat.trace = true;
            else if (trace.equals("off"))
                Pat.trace = false;
            else
                System.err.println("*** Invalid value " + trace + " for Pattern.trace "
                        + "(should be 'on' or 'off')");
        }
    }

    private static void updatePropVariables() {
        int i = 1;
        while (true) {
            if (props.containsKey("FuseJet.path" + i)) {
                String variableName = Pattern.quote("${FuseJet.path" + i + "}");
                //System.err.println(variableName);
                String variableValue = props.getProperty("FuseJet.path" + i);
                for (String key : props.stringPropertyNames()) {
                    props.put(key, props.getProperty(key).replaceAll(variableName, variableValue));
                }
                i++;
            } else {
                break;
            }
        }
    }

    private static void loadAnnotators() {
        annotators.clear();
        int i = 1;
        while (true) {
            String classKey = "[Unknown ClassName]";
            if (props.containsKey("FuseJet.Annotator.FuseJetAnnotator" + i)) {
                try {
                    classKey = "FuseJet.Annotator.FuseJetAnnotator" + i;
                    String className = props.getProperty(classKey).trim();
                    FuseJetAnnotator annotator = null;
                    if (className.startsWith("PatternSet(")) {
//                        annotator = new GeneralPatternAnnotator();
//                        String patternSetName = className.substring(11, className.length() - 1);
//                        if (!pc.patternSetNames.contains(patternSetName)) {
//                            System.err.println("Pattern set not found: " + patternSetName);
//                            System.exit(-1);
//                        }
//                        annotator.initialize(new String[]{patternSetName});
                    } else {
                        if (!className.contains(".")) {
                            annotator =
                                    (FuseJetAnnotator) Class.forName("FuseJet.Annotators." +
                                            className).newInstance();
                        } else {
                            annotator =
                                    (FuseJetAnnotator) Class.forName(className).newInstance();
                        }
                        if (props.containsKey(className + ".ResourceInformation")) {
                            String[] resourceInformations = props.getProperty(className + ".ResourceInformation").split(";");
                            if ((resourceInformations.length > 1) || (resourceInformations[0].trim().length() > 0)) {
                                annotator.initialize(resourceInformations);
                            }
                        }
                    }
                    annotators.add(annotator);
                } catch (Exception e) {
                    System.err.println("Error creating " + classKey + ". Please check props file.");
                    System.exit(-1);
                }
                i++;
            } else {
                break;
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("FuseJet.FuseJet.Sentiment.Utils.Console configFile inputFiles outputFiles");
        }
        configFileName = args[0];
        initialize();
        boolean multiFileMode = true;
        if (props.getProperty("FuseJet.multiFileMode") != null
                && props.getProperty("FuseJet.multiFileMode").toLowerCase().trim().equals("false")) {
            multiFileMode = false;
        }
        if (multiFileMode) {
            try {
                inputFiles = FuseUtils.readLines(args[1]);
                outputFiles = FuseUtils.readLines(args[2]);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.print("Error reading input/output file list.");
                System.exit(-1);
            }
            if (inputFiles.length != outputFiles.length) {
                System.err.println("Number of input/output files should equal.");
                System.exit(-1);
            }

            processFiles(inputFiles, outputFiles);
        } else {
            processFile(args[1], args[2]);
        }


        //System.err.println("Initialization finished.");

        //BAE2NESentProcessor processor = new BAE2NESentProcessor();
        //processor.processFiles(inputFiles, outputFiles);
    }

    public static void processFiles(String[] inputFiles, String[] outputFiles) {
        for (int i = 0; i < inputFiles.length; i++) {
            try {
                processFile(inputFiles[i].trim(), outputFiles[i].trim());
            }
            catch (Exception e) {
                System.err.println("Error processing file:" + inputFiles[i]);
                e.printStackTrace();

            }
        }
    }

    private static Span[] spansToAnnotateFromAnnotations(Vector<Annotation> anns, FuseDocument doc) {
        boolean[] covered = new boolean[doc.length()];
        List<Span> result = new ArrayList<Span>();
        if (anns != null) {
            for (Annotation ann : anns) {
                for (int i = ann.start(); i < ann.end(); i++) {
                    covered[i] = true;
                }
            }
        }

        int start = -1;
        for (int i = 0; i < covered.length; i++) {
            if (start > -1) {
                if (!covered[i]) {
                    Span span = new Span(start, i);
                    span.setDocument(doc);
                    result.add(span);
                    start = -1;
                }
            } else {
                if (covered[i]) {
                    start = i;
                }
            }
        }
        if (start > -1) {
            Span span = new Span(start, doc.length());
            span.setDocument(doc);
            result.add(span);
        }
        return result.toArray(new Span[result.size()]);
    }

    public static void processFile(String inputFile, String outputFile) {
        System.err.print("Processing " + inputFile + "\t");

        long start = System.currentTimeMillis();

        FuseDocument doc = reader.read(inputFile);
        doc.setName(outputFile);
        // find spans to annotate and output, if no span is set in props, use the full span
//        Span[] spansToAnnotate = new Span[0];
//        Annotation[] annsToAnnotate;
//        if (spanNamesToAnnotate == null) {
//            Annotation ann = AnnotationFactory.createAnnotation("fulldoc", doc, doc.fullSpan().start(), doc.fullSpan().end());
//            spansToAnnotate = new Span[]{doc.fullSpan()};
//            annsToAnnotate = new Annotation[]{ann};
//        } else {
//            List<Span> spanListToAnnotate = new ArrayList<Span>();
//            List<Annotation> annListToAnnotate = new ArrayList<Annotation>();
//            for (String spanNameToAnnotate : spanNamesToAnnotate) {
//                Vector<Annotation> anns = doc.annotationsOfType(spanNameToAnnotate);
//                if (anns != null) {
//                    for (Annotation ann : anns) {
////                        spanListToAnnotate.add(ann.span());
//                        annListToAnnotate.add(ann);
//                    }
//                    spansToAnnotate = spansToAnnotateFromAnnotations(anns, doc);
//                }
//            }
//            //spansToAnnotate =IMAGE DATA PROCESSING OR GENERATION, IN GENERAL spanListToAnnotate.toArray(new Span[spanListToAnnotate.size()]);
//            annsToAnnotate = annListToAnnotate.toArray(new Annotation[annListToAnnotate.size()]);
//        }

        long readEnd = System.currentTimeMillis();
        System.err.print("read: " +(readEnd-start) + " ms\t");
        long length = new File(inputFile.split(";")[0]).length();
        System.err.print("length: " +length + " bytes\t");

        long annotateEnd;
        if (length > maxAnnotationInput) {
            annotateEnd = System.currentTimeMillis();
            System.err.print("Gave up annotating big file. File size:" + length + " > Max size:" + maxAnnotationInput + "\t");
        }
        else {
            for (FuseJetAnnotator annotator : annotators) {
                annotator.annotate(doc);
            }
            annotateEnd = System.currentTimeMillis();
            System.err.print("annotate: " + (annotateEnd-readEnd) + " ms\t");
        }

        writer.write(doc, outputFile);
        long writeEnd = System.currentTimeMillis();
        System.err.println("write: " + (writeEnd-annotateEnd) + " ms");
        doc.clearAnnotations();
        doc = null;
    }

    public static String[] getInputFiles() {
        return inputFiles;
    }

    public static String[] getOutputFiles() {
        return outputFiles;
    }

    public static FuseDocumentWriter getWriter() {
        return writer;
    }
}
