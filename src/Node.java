public class Node implements Comparable<Node> {
    private ChunkBytes bytes;
    private final int freq;
    private Node left;
    private Node right;


    public ChunkBytes getBytes() {
        return bytes;
    }

    public int getFreq() {
        return freq;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public Node(int freq, Node left, Node right) {
        this.freq = freq;
        this.left = left;
        this.right = right;
    }

    public Node(ChunkBytes bytes, int freq) {
        this.bytes = bytes;
        this.freq = freq;
    }

    @Override
    public int compareTo(Node o) {
        return Integer.compare(this.freq, o.freq);
    }
}