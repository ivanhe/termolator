package FuseJet.Terminology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SuffixSearch {

	/**
	 *This class builds a suffix tree structure, to search strings that contains a substring a.
	 */

	TermNode root = new TermNode("");

	public static void main(String[] args) {
		Set<String> glossary = new HashSet<String>();
		glossary.add("book");
		glossary.add("great book");
		glossary.add("bad book");
		glossary.add("quite great book");
		glossary.add("cat");
		glossary.add("beautiful cat");
		SuffixSearch search = new SuffixSearch(glossary);
		
      for(String word:glossary){
    	  TermNode node = search.searchWord(word);
    	  node.print();
      }
	}

	public TermNode searchWord(String index){
		String[] tokens = index.split(" ");
		TermNode curNode = root;
		String str = tokens[tokens.length-1];
		if(curNode.children.containsKey(str) == false)
			return null;
		curNode = curNode.children.get(str);
		for(int i= tokens.length-2;i >= 0;i--){
			str = tokens[i]+" "+str;
			curNode = curNode.children.get(str);
			if(curNode == null)
				return null;
		}
		return curNode;
	}
	
	SuffixSearch(Set<String> glossary){
		for(String word:glossary){
			String[] tokens = word.split(" ");
			TermNode parent = root;
			String subStr = tokens[tokens.length-1];			
			TermNode curNode = searchWord(subStr);
			if(curNode == null){
				curNode = parent.addChildren(subStr);
			}
			for(int i = tokens.length-2;i >=0;i--){
				parent = curNode;
				subStr = tokens[i]+" "+subStr;
				curNode = searchWord(subStr);
				if(curNode == null){
					curNode = parent.addChildren(subStr);
				}
			}
		}
	}
	
	public Set<String> getStringsContainsCurrent(TermNode node){
		Set<String> words = new HashSet<String>();
		getSuccessor(node, words);
		return words;
	}
	
	private void getSuccessor(TermNode a, Set<String> words){
		words.add(a.word);
	    if(a.children.size() == 0)
	    	return;
	    for(TermNode node:a.children.values()){
	    	getSuccessor(node, words);
	    }
	}	

}


class TermNode {
	String word;

	Map<String, TermNode> children = new HashMap<String, TermNode>();

	TermNode(String term){
		word = term;
	}

	TermNode addChildren(String child){
		if(children.containsKey(child)){
			System.err.println("Error: term "+child+" already in trees.");
			System.exit(1);
		}
		TermNode node = new TermNode(child);
		children.put(child, node);
		return node;
	}

	 void print(){
		StringBuffer str = new StringBuffer(word);
		if(children.size() != 0){
			for(String child:children.keySet()){
				str.append("\t|\t"+child);
			}
		}
		System.err.println(str);
	}

}