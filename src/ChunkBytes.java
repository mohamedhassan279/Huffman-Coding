import java.util.Arrays;

public class ChunkBytes {
    public final byte[] bytes;

    public ChunkBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChunkBytes other) {
            return Arrays.equals(bytes, other.bytes);
        }
        return false;
    }
}
