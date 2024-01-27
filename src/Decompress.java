import java.io.*;
import java.util.*;

public class Decompress {
    private static final Map<String, ChunkBytes> decMap = new HashMap<>();

    /**
     * Extract a compressed file
     * @param compressedFile the compressed file
     * @throws IOException when the file does not exist or IO exception occurs
     */
    public static void decompress(File compressedFile) throws IOException {
        String decompressedPath = generateDecompressedPath(compressedFile);
        File decompressedFile = new File(decompressedPath);

        int bufferSize = 128 * 1024 * 1024;
        int chunkSize = 0;

        byte[] inputBuffer = new byte[bufferSize];
        byte[] outputBuffer = new byte[bufferSize];
        byte[] readBytes, codeWordBytes;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(compressedFile));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(decompressedFile))) {
            // first extract the header and build the dictionary
            long fileSize = dis.readLong(); // the original file size
            int mapEntries = dis.readInt(); // the number of map entries
            for (int i = 0; i < mapEntries; i++) {
                int currentChunkSize = dis.readInt();
                chunkSize = Math.max(chunkSize, currentChunkSize);
                readBytes = dis.readNBytes(currentChunkSize);
                ChunkBytes chunk = new ChunkBytes(readBytes);
                int codewordSize = dis.readInt();
                codeWordBytes = dis.readNBytes((int) Math.ceil(codewordSize / 8.0));
                char[] codeword = new char[codewordSize];
                int x = 0;
                char[] binaryString;
                if (codeWordBytes.length > 0) {
                    for (int j = 0; j < codeWordBytes.length - 1; j++) {
                        binaryString = byteToBinaryString(codeWordBytes[j]);
                        for (int k = 0; k < 8; k++) {
                            codeword[x++] = binaryString[k];
                        }
                        codewordSize -= 8;
                    }
                    int idx = 0;
                    binaryString = byteToBinaryString(codeWordBytes[codeWordBytes.length - 1]);
                    while (codewordSize > 0 && idx < 8) {
                        codeword[x++] = binaryString[idx++];
                        codewordSize--;
                    }
                    decMap.put(new String(codeword), chunk);
                }
            }

            // second, Extract the data itself and write it in a buffer
            // when the buffer is full, write it to the extracted file
            int bytesRead;
            StringBuilder str = new StringBuilder();
            while ((bytesRead = dis.read(inputBuffer)) != -1) {
                int idx = 0;
                for (int i = 0; i < bytesRead; i++) {
                    int c = 0;
                    char[] binaryString = byteToBinaryString(inputBuffer[i]);
                    while (c < 8 && fileSize > 0) {
                        while (c < 8 && !decMap.containsKey(str.toString())) {
                            str.append(binaryString[c++]);
                        }
                        ChunkBytes chunk = decMap.get(str.toString());
                        if (chunk != null) {
                            for (byte b : chunk.bytes) {
                                outputBuffer[idx++] = b;
                                if (idx == bufferSize) {
                                    bos.write(outputBuffer);
                                    idx = 0;
                                }
                            }
                            fileSize -= chunkSize;
                            str = new StringBuilder();
                        }
                    }
                }
                if (idx > 0) {
                    bos.write(outputBuffer, 0, idx);
                }
            }
        } catch (Exception e) {
            System.out.println("File not found");
            throw new IOException();
        }
    }

    /**
     *
     * @param compressedFile the compressed file
     * @return the absolute path of the extracted file
     */
    private static String generateDecompressedPath(File compressedFile) {
        String compressedFileName = compressedFile.getName();
        String dir = compressedFile.getParent();
        String decompressedFileName = "extracted." + compressedFileName.substring(0, compressedFileName.length() - 3);
        return dir + "/" + decompressedFileName;
    }

    /**
     * This function is for optimization purpose (using the char[] instead of String)
     * @param value the byte to convert to binary
     * @return char[] of size 8, containing the binary representation of this byte
     */
    private static char[] byteToBinaryString(byte value) {
        char[] binaryCharArray = new char[8];
        for (int i = 7; i >= 0; i--) {
            byte bit = (byte) ((value >> i) & 1);
            binaryCharArray[7 - i] = (char) ('0' + bit);
        }
        return binaryCharArray;
    }
}

