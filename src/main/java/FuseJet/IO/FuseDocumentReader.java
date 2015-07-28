package FuseJet.IO;

import FuseJet.Models.FuseDocument;

/**
 * User: yhe
 * Date: 6/26/12
 * Time: 1:41 PM
 */
public interface FuseDocumentReader {
    public FuseDocument read(String fileIdentifier);
}
