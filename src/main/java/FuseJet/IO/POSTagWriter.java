package FuseJet.IO;

import FuseJet.Models.FuseAnnotation;
import FuseJet.Models.FuseDocument;
import Jet.Tipster.Annotation;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yhe
 * Date: 8/6/13
 * Time: 10:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class POSTagWriter implements FuseDocumentWriter {
    @Override
    public void write(FuseDocument doc, String outputFileName) {
        try {
            List<Annotation> tokens = doc.annotationsOfType("token");
            PrintWriter o = new PrintWriter(outputFileName);
            if (tokens == null) return;
            FuseAnnotation.sortByStartPosition(tokens);
            int prevStart = -1;
            for (Annotation token : tokens) {
                if (token.start() == prevStart) continue;
                prevStart = token.start();
                List<Annotation> constitAnns = doc.annotationsAt(token.start(), "constit");
                if (constitAnns != null) {
                    for (Annotation c : constitAnns) {
                        if (c.start() == token.start() &&
                                c.end() == token.end()) {
                            if (c.get("cat") != null) {
                                o.println(String.format("%s ||| S:%d E:%d ||| %s",
                                        doc.text(token).replaceAll("\\s+", " "),
                                        c.start(), c.end(),
                                        ((String)c.get("cat")).toUpperCase()));
                                break;
                            }
                        }
                    }
                }
            }
            o.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
