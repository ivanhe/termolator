package FuseJet.Annotators;

import FuseJet.Models.FuseDocument;
import Jet.HMM.HMMTagger;
import Jet.Tipster.Annotation;

import java.util.List;

/**
 * Created by yhe on 5/25/14.
 */
public class HMMPOSAnnotator implements FuseJetAnnotator {
    private HMMTagger tagger = null;

    @Override
    public void initialize(String[] resourceInformation) {
        try {
            tagger = new HMMTagger();
            tagger.load(resourceInformation[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void annotate(FuseDocument doc) {
        List<Annotation> sentences = doc.annotationsOfType("sentence");
        if (sentences != null) {
            for (Annotation sentence : sentences) {
                tagger.annotate(doc, sentence.span(), "constit");
                tagger.annotate(doc, sentence.span(), "tagger");
            }
        }
    }
}
