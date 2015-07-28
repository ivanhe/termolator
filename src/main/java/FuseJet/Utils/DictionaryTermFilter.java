package FuseJet.Utils;
import FuseJet.Models.FuseAnnotation;
import FuseJet.Models.FuseDocument;
import FuseJet.Models.FuseRelation;
import Jet.Lisp.FeatureSet;
import Jet.Tipster.Annotation;
import Jet.Tipster.Span;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: llinda
 * Date: 7/15/13
 * Time: 12:57 PM
 * To change this template use File | Settings | File Templates.
 * Used to convert and filter Zach's words into the proper format.
 */
public class DictionaryTermFilter {
    private Set<String> stopWords;
    public DictionaryTermFilter() {
        try {
            stopWords = new HashSet<String>();
            BufferedReader r = new BufferedReader(new FileReader("/home/llinda/workspace/fuse/Linda/Models/en.freq"));
            String l = null;
            while ((l = r.readLine()) != null) {
                String[] parts = l.split("\t");
                if (Integer.valueOf(parts[1]) < 10000) {
                    break;
                }
                stopWords.add(parts[0].trim().toLowerCase());
            }
            r.close();
            stopWords.add("reference");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void convertToDictFormat(){
        try {
            BufferedReader r = new BufferedReader(new FileReader("/home/llinda/workspace/fuse/speech.out"));
            BufferedWriter w = new BufferedWriter(new FileWriter("/home/llinda/workspace/fuse/dict.out"));

            String l = null;
            while ((l = r.readLine()) != null) {
                String[] parts = l.split("\\|");
                int lengthMinusFreq = parts.length - 1;
                for (int i=0; i<lengthMinusFreq; i++) {
                    String p = parts[i].trim();
                    if (Pattern.matches("[a-zA-Z]+", p) && !stopWords.contains(p.toLowerCase()))    {
                        w.write(p + "|||term\n");
                    }
                }
            }
            r.close();
            w.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args){
        DictionaryTermFilter d = new DictionaryTermFilter();
        d.convertToDictFormat();

    }

}
