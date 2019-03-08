package emilsoft.completewordfinder;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class TrieNodeSerializer extends Serializer<TrieNode> {


    @Override
    public void write(Kryo kryo, Output output, TrieNode object) {
        output.writeChar(object.character);
        output.writeBoolean(object.isWord);
        //kryo.reference(object);
        kryo.writeObjectOrNull(output, object.parent, TrieNode.class);
        kryo.writeObjectOrNull(output, object.failure, TrieNode.class);
        kryo.writeObjectOrNull(output, object.output, TrieNode.class);
        for(TrieNode t : object.children){
            if(t != null)
                write(kryo, output, t);
            else kryo.writeObjectOrNull(output, null, TrieNode.class);
        }
    }

    @Override
    public TrieNode read(Kryo kryo, Input input, Class<? extends TrieNode> type) {
        TrieNode trieNode = new TrieNode();
        kryo.reference(trieNode);
        trieNode.character = input.readChar();
        trieNode.isWord = input.readBoolean();
        trieNode.parent = kryo.readObjectOrNull(input, TrieNode.class);
        trieNode.failure = kryo.readObjectOrNull(input, TrieNode.class);
        trieNode.output = kryo.readObjectOrNull(input, TrieNode.class);
        for(int i = 0; i < Trie.ALPHABET_SIZE; i++) {
            trieNode.children[i] = read(kryo, input, TrieNode.class);
        }
        return trieNode;
    }
}
