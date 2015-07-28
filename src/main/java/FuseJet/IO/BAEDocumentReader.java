package FuseJet.IO;

import FuseJet.Models.AnnotationFactory;
import FuseJet.Models.FuseDocument;
import FuseJet.Utils.FuseUtils;
import Jet.Lisp.FeatureSet;
import Jet.Tipster.Annotation;
import Jet.Tipster.Span;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * User: yhe
 * Date: 6/20/12
 * Time: 12:04 PM
 */
public class BAEDocumentReader implements FuseDocumentReader {
    public FuseDocument read(String textFileName, String factsFileName) {
        FuseDocument doc = new FuseDocument();
        try {
            doc.setText(FuseUtils.readFileAsString(textFileName));
            String text = doc.text();
            // record bio for <text> chunks, for each character
            // 1=B -1=I 0=O
            int[] textRecord = new int[text.length()+1];
            BufferedReader r = new BufferedReader(new FileReader(factsFileName));
            String line;
            while ((line = r.readLine()) != null) {
                Annotation ann =  null;
                try {
                    //System.err.println(line.trim());
                    ann = annotationFromString(line.trim());
                }
                catch (Exception e) {
                    System.err.print("Unable to parse annotation. Stacktrace: ");
                    e.printStackTrace();
                }

                if (ann != null) {
                    int start = ann.start();
                    while ((start < text.length()) &&
                            (Character.isWhitespace(text.charAt(start))))  {
                        start++;
                    }
                    int end = ann.end();
                    while ((end < text.length()) &&
                            (Character.isWhitespace(text.charAt(end))))  {
                        end++;
                    }
                    ann.span().setStart(start);
                    ann.span().setEnd(end);
                    ann.span().setDocument(doc);
                    // 1: B -1:I 0:O
                    if (ann.type().equals("text")) {
                        for (int i = ann.start(); i < ann.end(); i++) {
                            if (textRecord[i] == 0) {
                                if (i == ann.start()) {
                                    textRecord[i] = 1;
                                }
                                else {
                                    textRecord[i] = -1;
                                }
                            }
                            if (textRecord[i] == -1) {
                                if (i == ann.start()) {
                                    textRecord[i] = 1;
                                }
                            }
                        }
                    }
                    else {
                        doc.addAnnotation(ann);
                    }

//                    if ((ann.get("TITLE") != null) &&
//                            (((String)ann.get("TITLE"))).toLowerCase().startsWith("acknowledg")) {
//                        Annotation ackAnn = AnnotationFactory.createAnnotation("acknowledgements",
//                                doc,
//                                ann.span().start(),
//                                ann.span().end());
//                        doc.addAnnotation(ackAnn);
//                    }
                }
            }

            List<Annotation> tables = doc.annotationsOfType("table");
            if (tables != null) {
                for (Annotation table : tables) {
                    int end = table.end();
                    for (int i = table.start(); i < table.end(); i++) {
                        if (i > -1 && i < textRecord.length) {
                            textRecord[i] = 0;
                        }
                    }
                    if (end < textRecord.length && textRecord[end] == -1) {
                        textRecord[end] = 1;
                    }
                }
            }

            int start = -1;
            for (int i = 0; i < textRecord.length; i++) {
                if (textRecord[i] == 1) {
                    if (start >= 0) {
                        Annotation textAnn = AnnotationFactory.createAnnotation("text", doc, start, i);
                        doc.addAnnotation(textAnn);
                    }
                    start = i;
                }
                if (textRecord[i] == 0) {
                    if (start >= 0) {
                        Annotation textAnn = AnnotationFactory.createAnnotation("text", doc, start, i);
                        doc.addAnnotation(textAnn);
                    }
                    start = -1;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        /* set the whole text as body */
        // Annotation body = AnnotationFactory.createAnnotation("body", doc, 0, doc.text().length());
        // doc.addAnnotation(body);
        /* should be removed when BAE supports body (and abstract) */


        int textRegionStart = 0;
        int textRegionEnd = doc.length();
        Vector<Annotation> textRegions = doc.annotationsOfType("text_chunk");
        if (textRegions != null) {
            for (Annotation textRegion : textRegions) {
                if (textRegion.start() > textRegionStart) textRegionStart =  textRegion.start();
                if (textRegion.end() < textRegionEnd) textRegionEnd = textRegion.end();
            }
        }
        Annotation textRegionAnn = AnnotationFactory.createAnnotation("text_region", doc, textRegionStart, textRegionEnd);
        doc.addAnnotation(textRegionAnn);
        doc.splitAndTokenize();
        return doc;
    }

    public static Annotation annotationFromString(String s) {
        String[] parts = s.split(" ");
        int start = -1;
        int end   = -1;
        if (parts.length < 3) return null;
        String type = parts[0].trim().toLowerCase();
        FeatureSet fs = new FeatureSet();
        boolean quoted = false;
        StringBuilder valueBuffer = new StringBuilder(100);
        String key = "";
        for (int i = 1; i < parts.length; i++) {
            if (!quoted) {
                String[] kv = parts[i].split("=");
                if (kv.length < 2) {
                    valueBuffer.setLength(0);
                }
                else {
                    key = kv[0];
                    valueBuffer.setLength(0);
                    if (kv[1].startsWith("\"")) {
                        if ((kv[1].length() < 2)|| kv[1].endsWith("\"")) {
                            fs.put(key.toLowerCase(), kv[1].replaceAll("\"", ""));
                        }
                        else {
                            valueBuffer.append(kv[1].substring(1));
                            quoted = true;
                        }
                    }
                    else {
                        if (key.equals("START") || key.equals("END")) {
                            if (key.equals("START")) {
                                start = Integer.valueOf(kv[1]);
                            }
                            else {
                                end = Integer.valueOf(kv[1]);
                            }
                        }
                        else {
                            fs.put(key.toLowerCase(), kv[1]);
                        }
                    }
                }
            }
            else {
                if (parts[i].endsWith("\"")) {
                    valueBuffer.append(" ").append(parts[i].substring(0, parts[i].length() - 1));
                    fs.put(key.toLowerCase(), valueBuffer.toString());
                    valueBuffer.setLength(0);
                    quoted = false;
                }
                else {
                    valueBuffer.append(" ").append(parts[i]);
                }
            }
        }
        if ((start < 0) || (end < 0)) return null;
        if (type.equals("structure")) {
            type = ((String)fs.get("type")).trim().toLowerCase();
            fs.remove("type");
        }
        return new Annotation(type, new Span(start, end), fs);
    }

    @Override
    public FuseDocument read(String fileIdentifier) {
        String[] parts = fileIdentifier.split(";");
        if (parts.length != 2) {
            System.err.println("A BAE file identifier should contain the txt filename and the fact filename," +
                    "separated by ;");
            return null;
        }
        else {
            String txtFileName = parts[0];
            String factFileName = parts[1];
            return read(txtFileName, factFileName);
        }
    }
}
