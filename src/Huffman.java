import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Huffman {

    /**
     * Builds Huffman's Algorithm tree
     *
     * @param freqMap the frequency map
     * @return the root node of the tree
     */
    public static Node huffmanAlgorithm(Map<ChunkBytes, Integer> freqMap) {
        List<Node> alphabets = new ArrayList<>();
        for (Map.Entry<ChunkBytes, Integer> entry : freqMap.entrySet()) {
            alphabets.add(new Node(entry.getKey(), entry.getValue()));
        }
        PriorityQueue<Node> Q = new PriorityQueue<>(alphabets);
        for (int i = 0; i < alphabets.size() - 1; i++) {
            Node left = Q.poll();
            Node right = Q.poll();
            assert left != null;
            assert right != null;
            Node z = new Node(left.getFreq() + right.getFreq(), left, right);
            Q.add(z);
        }
        return Q.peek();
    }
}
