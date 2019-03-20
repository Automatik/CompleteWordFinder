package emilsoft.completewordfinder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrieNode trieNode = (TrieNode) o;
        return isWord == trieNode.isWord &&
                character == trieNode.character &&
                Arrays.equals(children, trieNode.children) &&
                Objects.equals(parent, trieNode.parent) &&
                Objects.equals(failure, trieNode.failure) &&
                Objects.equals(output, trieNode.output);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(isWord, character, parent, failure, output);
        result = 31 * result + Arrays.hashCode(children);
        return result;
    }
}
