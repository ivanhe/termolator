package FuseJet.Utils;

import Jet.Tipster.Annotation;

import java.util.Comparator;

/**
 * User: yhe
 * Date: 8/11/12
 * Time: 1:04 PM
 */
public class AnnotationStartComparator implements Comparator<Annotation> {
    @Override
    public int compare(Annotation annotation1, Annotation annotation2) {
        return annotation1.start() - annotation2.start();
    }
}
