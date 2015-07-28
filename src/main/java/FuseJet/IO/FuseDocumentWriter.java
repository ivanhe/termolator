package FuseJet.IO;

import FuseJet.Models.FuseDocument;
import Jet.Tipster.Annotation;
import Jet.Tipster.Span;

/**
 * User: yhe
 * Date: 6/26/12
 * Time: 2:44 PM
 */
public interface FuseDocumentWriter {
    public void write(FuseDocument doc, String outputFileName);
}
