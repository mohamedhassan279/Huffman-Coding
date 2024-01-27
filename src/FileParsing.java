import java.io.*;
import java.util.*;

public class FileParsing {
    public static Map<ChunkBytes, Integer> parseFile(File inputFile, int n) throws IOException {
        Map<ChunkBytes, Integer> freqMap = new HashMap<>();
        int bufferSize = 128 * 1024 * 1024; // 64 MB
        bufferSize = bufferSize - bufferSize % n; // make it divisible by n

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile))) {
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i += n) {
                    byte[] chunkBytes = Arrays.copyOfRange(buffer, i, Math.min(bytesRead, i + n));
                    ChunkBytes chunk = new ChunkBytes(chunkBytes);
                    freqMap.merge(chunk, 1, (a, b) -> (int) a + b);
                }
            }
        } catch (Exception e) {
            System.out.println("File not found");
            throw new IOException();
        }
        return freqMap;
    }
}
