package FuseJet.Terminology;

import FuseJet.Utils.FuseUtils;
import com.google.common.collect.ImmutableSet;
import org.apache.lucene.analysis.de.GermanStemmer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: yhe
 * Date: 10/3/12
 * Time: 12:25 PM
 */
public class GermanTermFilter implements TermFilter {

    private Set<String> stopWordList = new HashSet<String>();
    private Set<String> usedStems = new HashSet<String>();
    private int minWordLength = 8;
    private PublicGermanStemmer stemmer = new PublicGermanStemmer();
    private boolean useStemmer = false;

    private Set<String> forbiddenStems = new HashSet<String>();
    private Set<String> forbiddenPrefixes = new HashSet<String>();
    private Set<String> forbiddenSuffixes = new HashSet<String>();


    public String stem(String input) {
        return stemmer.doStem(input);
    }

    private static final ImmutableSet<Character> PUNCT_CHARS = new ImmutableSet.Builder<Character>()
            .add('„')
            .add('\'')
            .add('«')
            .add('(')
            .add('[')
            .add('{')
            .add('`')
            .add(':')
            .add('“')
            .add('\'')
            .add('"')
            .add('»')
            .add(')')
            .add(']')
            .add('}')
            .add('…')
            .add('!')
            .add('’')
            .add('-')
            .add('?')
            .add('—')
            .add(',')
            .add('.')
            .add(';')
            .add(':')
            .add('$')
            .add('­')
            .add('€')
            .build();

    @Override
    public void initialize(String[] resourceInformation) {
        if (resourceInformation.length != 5) {
            System.err.println("GermanTermFilter initialization error: unable to obtain correct path.");
            System.exit(-1);
        }
        String stopWordListName = resourceInformation[0];
        int stopThreshold = 0;
        try {
            stopThreshold = Integer.valueOf(resourceInformation[1]);
            minWordLength = Integer.valueOf(resourceInformation[2]);
            useStemmer = Boolean.valueOf(resourceInformation[3]);
            loadFlexStopFile(resourceInformation[4]);
        }
        catch (Exception e) {
            System.err.println("The second parameter for GermanTermFilter should be a number");
            System.exit(-1);
        }
        try {
            stopWordList = FuseUtils.readLCWordListWithThreshold(stopWordListName, stopThreshold);
        }
        catch (IOException e) {
            System.err.println("GermanTermFilter error loading files...");
            System.err.println(-1);
        }
    }

    @Override
    public List<Term> filterTerm(List<Term> termList) {
        List<Term> result = new ArrayList<Term>();
        for (Term term : termList) {
            if (stopWordList.contains(term.word.toLowerCase())) {
                continue;
            }
            if (term.word.endsWith("s")) {
                continue;
            }
            if (term.word.length() < minWordLength) {
                continue;
            }
            boolean forbidden = false;
            char[] chars = term.word.toCharArray();
            for (char c : chars) {
                if (Character.isDigit(c)) {
                    forbidden = true;
                    break;
                }
            }
            for (char c : chars) {
                if (PUNCT_CHARS.contains(c)) {
                    forbidden = true;
                    break;
                }
            }
            if (forbidden) {
                continue;
            }
            if (usedStems.contains(stem(term.word.trim()).toLowerCase())) {
                continue;
            }
            else {
                if (useStemmer) {
                    usedStems.add(stem(term.word.trim()).toLowerCase());
                }
            }
            if (!isValidFlex(term.word)) {
                continue;
            }
            result.add(term);
        }
        return result;
    }

    private void loadFlexStopFile(String flexStopFile) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(flexStopFile));
        String line = null;
        forbiddenStems.clear();
        forbiddenSuffixes.clear();
        forbiddenPrefixes.clear();
        while ((line = r.readLine()) != null) {
            if (line.startsWith("*")) {
                line = line.substring(1);
                forbiddenSuffixes.add(stem(line.toLowerCase().trim()));
                continue;
            }
            if (line.endsWith("*")) {
                line = line.substring(0, line.length()-1);
                forbiddenPrefixes.add(stem(line.toLowerCase().trim()));
                continue;
            }
            forbiddenStems.add(stem(line.trim().toLowerCase()));
        }

    }

    private boolean isValidFlex(String word) {
        String stem = stem(word.trim()).toLowerCase();
        if (forbiddenStems.contains(stem)) return false;
        for (String forbiddenPrefix : forbiddenPrefixes) {
             if (stem.startsWith(forbiddenPrefix)) return false;
        }
        for (String forbiddenSuffix : forbiddenSuffixes) {
            if (stem.endsWith(forbiddenSuffix)) return false;
        }
        return true;
    }
}
