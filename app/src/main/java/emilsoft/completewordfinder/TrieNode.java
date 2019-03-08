package emilsoft.completewordfinder;

import java.io.Serializable;

public class TrieNode implements Serializable {

    private static final long serialVersionUID = 1178908382300456422L;

    TrieNode[] children;
    boolean isWord;
    char character;
    //int outDegree; //Specifies character position in the word
    TrieNode parent; //Link to the parent node
    TrieNode failure; //Suffix(Failure) Link
    TrieNode output; //Output Link

    //Initialize your data structure here
    public TrieNode() {
        this.children = new TrieNode[Trie.ALPHABET_SIZE];
        this.isWord = false;
        //outDegree = 0;
    }

    public TrieNode(TrieNode parent, char character) {
        this.parent = parent;
        this.children = new TrieNode[Trie.ALPHABET_SIZE];
        this.isWord = false;
        this.character = character;
        //outDegree = 0;
    }

}
