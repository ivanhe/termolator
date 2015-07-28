package FuseJet.IO;

import FuseJet.Models.FuseDocument;

/**
 * User: yhe
 * Date: 9/14/12
 * Time: 3:40 PM
 */
abstract public class InitializableFuseDocumentWriter implements FuseDocumentWriter {
    abstract public void write(FuseDocument doc, String outputFileName);

    abstract public void initialize(String[] resourceInformation);

}
