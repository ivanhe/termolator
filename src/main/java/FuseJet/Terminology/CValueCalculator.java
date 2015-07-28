package FuseJet.Terminology;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CValueCalculator {

	private SuffixSearch search;
	private Map<String, Term> terms = new HashMap<String, Term>();
	
	public static void main(String[] args) throws IOException {
		String termFile = args[0];
		List<Term> terms = Term.readTermsFromFile(termFile);
		CValueCalculator values = new CValueCalculator(terms);
		values.calCValues();
		Term.writeTermsToFile(terms, args[1]);
	}
	
	CValueCalculator(List<Term> ts) throws IOException{
		for(Term t:ts)
			terms.put(t.word, t);
		search = new SuffixSearch(terms.keySet());
	}
	
	public void calCValues(){
		search = new SuffixSearch(terms.keySet());
		for(Term t:terms.values()){
			double cvalue = calRelCValue(t);
			t.cvalue = cvalue;
		}
	}

	private double calRelCValue(Term t){
		TermNode node = search.searchWord(t.word);
		//for all strings a with maximum length, the C-value(a) = log2|a|*freq(a)
		double	cvalue =  t.getPosFreq();
		//for all smaller string a, C-value(a) = log2|a|*[freq(a) - (1/P(Ta))*Sigma(freq(b))]
		//where b is the string that contains a, and Ta is the number of such strings
		//NOTE: Since we use different method for counting, the formula seems to be a little different from the description
		//In my program, the frequent of "book" is only counted without the occurrence  of "great book", while in Katerina's paper
		//they include such occurrence.
		Set<String> longerStrs = search.getStringsContainsCurrent(node);
		double fb = 0.0;
		int num = 0;
		for(String str:longerStrs){
			Term tb = terms.get(str);
			if(tb != null){
			  fb += tb.getPosFreq();
			  num++;
			}
		}
		if(num > 0)
		    cvalue -= fb/num;
		return Math.log(t.length+2.0)*cvalue;
	}


}
