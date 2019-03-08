package emilsoft.completewordfinder;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Queue;

public class Trie implements Serializable {

    public static final int ALPHABET_SIZE = 26;
    public static final char WILDCARD = '?';

    private static final long serialVersionUID = -5673760844731832849L;

    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public Trie(TrieNode root) {
        this.root = root;
    }

    public TrieNode getRoot() { return this.root; }

    //Inserts a word into the trie
    public void insert(String word){
        if(word == null)
            return;
        TrieNode current = root;
        //int depth = 0;
        for(char c : word.toCharArray()){
            int index = c - 'a';
            if(current.children[index] == null)//Create new node
                current.children[index] = new TrieNode(current, c);
            current = current.children[index];
            //current.outDegree = ++depth;
        }
        current.isWord = true;
    }

    //Returns if the word is in the trie
    public boolean contains(String word){
        TrieNode current = getTrieNode(word);
        //return (current == null) ? false : current.isWord;
        return current != null && current.isWord;
    }

    /*public boolean startsWith(String prefix){
        TrieNode current = getTrieNode(prefix);
        return (current == null);
    }*/

    public ArrayList<String> startsWith(String prefix){
        TrieNode current = getTrieNode(prefix);
        //ArrayList<String> words = new ArrayList<>();
        //DFS(current, prefix, words);
        return (ArrayList<String>) DFS(current, prefix);
        //return words;
    }

    public TrieNode getTrieNode(String s) {
        TrieNode current = root;
        for(char c : s.toCharArray()) {
            int index = c - 'a';
            if(current.children[index] == null)
                return null;
            current = current.children[index];
        }
        return (current == root) ? null : current;
    }

    //Recursive (elegance)
    private void DFS(TrieNode root, String prefix, List<String> words){
        if(root == null)
            return;
        if(root.isWord)
            words.add(prefix);
        for(int i = 0; i < ALPHABET_SIZE; i++)
            if(root.children[i] != null)
                DFS(root.children[i], prefix + (char) (i + 'a'), words);
    }

    //Iterative (performance)
    private List<String> DFS(TrieNode root, String prefix) {
        ArrayList<String> words = new ArrayList<>();
        ArrayDeque<TrieNode> nodesQueue = new ArrayDeque<>();
        ArrayDeque<String> prefixQueue = new ArrayDeque<>();
        if(root.isWord)
            words.add(prefix);
        nodesQueue.offer(root);
        prefixQueue.offer(prefix);
        while(!nodesQueue.isEmpty()){
            TrieNode current = nodesQueue.poll();
            String prefixTemp = prefixQueue.poll();
            for(int i = 0; i < ALPHABET_SIZE; i++){
                TrieNode temp = current.children[i];
                if(temp != null){
                    nodesQueue.offer(temp);
                    prefixQueue.offer(prefixTemp + (char)(i + 'a'));
                    if(temp.isWord)
                        words.add(prefixTemp + (char)(i + 'a'));
                }
            }
        }
        return words;
    }

    //Finds all occurrences of all array words in pattern
    //Suffix Links should already be built
    public List<String> match(String pattern) {
        ArrayList<String> words = new ArrayList<>();
        TrieNode current = root;
        char[] input = pattern.toCharArray();
        for(int i = 0; i < input.length; i++) {
            current = findNextNode(current, input[i]);
            boolean noMatch = false;
            //if match not found move to next node
            if(current.output == null)
                noMatch = true;
            if(noMatch && current.isWord)
                words.add(getStringWord(current));
            else if(!noMatch){
                TrieNode temp = current;
                while(temp != null){ // e != root ?
                    if(temp.isWord)
                        words.add(getStringWord(temp));
                    temp = temp.output;
                }
            }
        }
        return words;
    }

    //all valid words that are possible using Characters of Array
    public List<String> permute(char[] letters) {
        ArrayList<String> words = new ArrayList<>();
        //TrieNode current = root;
        Queue<Character> queue = new ArrayDeque<>(letters.length);
        for(char c : letters)
            queue.offer(c);
        //queue.add(c);
        permute(queue, "", words);
        return words;
    }

    private void permute(Queue<Character> letters, String current, List<String> words) {
        if(!current.equals("")){
            TrieNode node = getTrieNode(current);
            if(node != null && node.isWord) //TODO And if word length > 1
                words.add(current);
        }
        if(!letters.isEmpty()){
            //char c = letters.poll();
            Character[] array = new Character[letters.size()];
            array = letters.toArray(array);
            for(Character ch : array){
                Queue<Character> temp = new ArrayDeque<>(array.length);
                temp.addAll(letters);
                temp.remove(ch);
                permute(temp, current + ch, words);
            }
        }
    }

    public List<String> query(String expression) {
        ArrayList<String> words = new ArrayList<>();
        query(expression.toCharArray(),0, root, "", words);
        return words;
    }

    //Speed up with Regex ?
    //METTERE IL LIMITE ALLA STRINGA EXPRESSION CON LA PAROLA PIU' LUNGA DEL DIZIONARIO
    //https://stackoverflow.com/questions/1953080/good-algorithm-and-data-structure-for-looking-up-words-with-missing-letters
    private void query(char[] expression, int index, TrieNode root, String current, List<String> words){
        if(root.isWord && current.length() == expression.length)
            words.add(current);
        if(index < expression.length) {
            char next = expression[index];
            if(next == WILDCARD) {
                int next_index = index + 1;
                for(int i = 0; i < ALPHABET_SIZE; i++)
                    if(root.children[i] != null)
                        query(expression, next_index, root.children[i], current + (char)(i + 'a'), words);
            } else {
                int idx = next - 'a';
                if(root.children[idx] == null)
                    return;
                query(expression, index + 1, root.children[idx], current + next, words);
            }
        }
    }

    //Returns the next node the machine will transition to using goto
    //and failure functions
    private TrieNode findNextNode(TrieNode current, char nextInput) {
        int index = nextInput - 'a';
        //if goto is not defined(there is no word), use failure function
        while(current.children[index] == null && current != root)
            current = current.failure;
        return current.children[index];
    }

    private String getStringWord(TrieNode end) {
        //end.isWord must be true
        StringBuilder sb = new StringBuilder();
        while(end != root){
            sb.append(end.character);
            end = end.parent;
        }
        return sb.reverse().toString();
    }

    public void buildSuffixLinks() {
        //Failure funcion is computed in BFS using a queue
        ArrayDeque<TrieNode> nodesQueue = new ArrayDeque<>();
        root.failure = root;
        //nodesQueue.offer(root);
        //All nodes of depth 1 has root as suffix link
        for(int i = 0; i < ALPHABET_SIZE; i++){
            TrieNode temp = root.children[i];
            if(temp != null){
                temp.failure = root;
                nodesQueue.offer(temp);
            }
        }
        while(!nodesQueue.isEmpty()) {
            TrieNode current = nodesQueue.poll();
            for(int i = 0; i < ALPHABET_SIZE; i++) {
                if(current.children[i] != null) {

                    //Find failure node of the current node
                    TrieNode fail = current.failure;

                    //Find the deepest node labeled by proper
                    //suffix of string from root to current node
                    while(fail.children[i] == null && fail != root)
                        fail = fail.failure;

                    fail = (fail.children[i] == null) ? root : fail.children[i];
                    current.children[i].failure = fail;

                    //Set output link
                    current.children[i].output = (fail.isWord) ? fail : fail.output;

                    nodesQueue.offer(current.children[i]);
                }
            }
        }
    }

//    public void write(final UnsafeMemory buffer) {
//        TrieNode temp = root.children[0];
//        Log.v(MainActivity.TAG, "Original - Character: "+temp.character);
//        TrieNode t = buffer.shallowCopy(temp);
//        Log.v(MainActivity.TAG, "Returned - Character: "+t.character);
////        buffer.putTrieNode(root);
////        ArrayDeque<TrieNode> nodesQueue = new ArrayDeque<>();
////        nodesQueue.offer(root);
////        while(!nodesQueue.isEmpty()){
////            TrieNode current = nodesQueue.poll();
////            for(int i = 0; i < ALPHABET_SIZE; i++){
////                TrieNode temp = current.children[i];
////                if(temp != null){
////                    nodesQueue.offer(temp);
////                    buffer.putTrieNode(temp);
////                }
////            }
////        }
//    }

//    public static Trie read(final UnsafeMemory buffer) {
//        final TrieNode root = buffer.getTrieNode();
//        return new Trie(root);
//    }

}
