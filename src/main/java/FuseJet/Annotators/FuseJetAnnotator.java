package FuseJet.Annotators;

import FuseJet.Models.FuseDocument;
import Jet.Tipster.Span;

/**
 * User: yhe
 * Date: 6/26/12
 * Time: 5:20 PM
 */
public interface FuseJetAnnotator {
    public void initialize(String[] resourceInformation);
    public void annotate(FuseDocument doc);
}
