package FuseJet.Terminology;

public class WordFreq implements Comparable<WordFreq>{
	String word;
	double freq;

	WordFreq(String word, double freq){
		this.word = word;
		this.freq = freq;
	}

	void addFreq(double f){
		freq += f;
	}

	@Override
	public int compareTo(WordFreq arg0) {
		if(freq > arg0.freq)
			return -1;
		else if(freq < arg0.freq)
			return 1;
		else
			return 0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return word+"\t"+freq;
	}

	public static WordFreq read(String line){
		int pos = line.lastIndexOf("\t");
		String word = line.substring(0,pos);
		double freq = Double.parseDouble(line.substring(pos+1));
		return new WordFreq(word, freq);
	}

}