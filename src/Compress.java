import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Compress {
    private static final Map<ChunkBytes, String> codeMap = new HashMap<>();

    /**
     * Compress the given file according to a certain chunk size
     *
     * @param inputFile the original file
     * @param chunkSize the chunk size
     * @throws IOException when the file does not exist or IO exception occurs
     */
    public static void compress(File inputFile, int chunkSize) throws IOException {
        Map<ChunkBytes, Integer> freqMap = FileParsing.parseFile(inputFile, chunkSize); // frequency map
        String outputPath = generateCompressedPath(inputFile, chunkSize);

        if (freqMap.isEmpty()) { // empty file, just write the file size = 0, map entries = 0
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputPath))) {
                dos.writeLong(0);
                dos.writeInt(0);
            }
            return;
        }
        if (freqMap.size() == 1) { // only one chunk --> just give it 0
            codeMap.put(freqMap.keySet().iterator().next(), "0");
        } else {
            Node root = Huffman.huffmanAlgorithm(freqMap); // perform huffman algorithm
            setCodewords(root, ""); // build the codeword map
        }
        try {
            writeHeader(outputPath, inputFile.length()); // write the header to the compressed file
            writeFileData(inputFile, outputPath, chunkSize); // write the compressed data
        } catch (Exception e) {
            System.out.println("File not found");
            throw new FileNotFoundException();
        }
        // compression ratio = compressed file size / original file size
        System.out.println("Compression ratio: " + (double) new File(outputPath).length() / inputFile.length());
    }

    /**
     * This function writes the header of the compressed file
     *
     * @param outputPath the absolute path of the compressed file
     * @param fileSize   the size of the original file
     * @throws IOException when the file does not exist or IO exception occurs
     */
    private static void writeHeader(String outputPath, long fileSize) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputPath))) {
            dos.writeLong(fileSize);
            dos.writeInt(codeMap.size());
            for (Map.Entry<ChunkBytes, String> entry : codeMap.entrySet()) {
                byte[] bytes = entry.getKey().bytes;
                dos.writeInt(bytes.length); // the size of this chunk
                dos.write(bytes);
                String codeword = entry.getValue();
                dos.writeInt(codeword.length()); // the size of the codeword
                byte[] byteArray = new byte[(int) Math.ceil(codeword.length() / 8.0)];
                for (int i = 0; i < codeword.length(); i += 8) {
                    // add padding of zeros to the right
                    String byteToWrite8 = String.format("%-8s", codeword.substring(i, Math.min(codeword.length(), i + 8))).replace(' ', '0');
                    byte b = (byte) Integer.parseInt(byteToWrite8, 2);
                    byteArray[i / 8] = b;
                }
                dos.write(byteArray);
            }
        }
    }

    /**
     * This function write the compressed data to the file
     *
     * @param inputFile  the original file
     * @param outputPath the absolute path of the compressed file
     * @param chunkSize  the chunk size (n)
     * @throws IOException when the file does not exist or IO exception occurs
     */
    private static void writeFileData(File inputFile, String outputPath, int chunkSize) throws IOException {
        StringBuilder byteToWrite = new StringBuilder();
        int bufferSize = 128 * 1024 * 1024;
        bufferSize = bufferSize - bufferSize % chunkSize; // make it divisible by the chunk size
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputPath, true))) {

            byte[] buffer = new byte[bufferSize];
            byte[] outputBuffer = new byte[bufferSize];
            int bytesRead, bufferIndex = 0;
            while ((bytesRead = bis.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i += chunkSize) {
                    byte[] chunkBytes = Arrays.copyOfRange(buffer, i, Math.min(bytesRead, i + chunkSize));
                    String codeword = codeMap.get(new ChunkBytes(chunkBytes));
                    byteToWrite.append(codeword);
                    while (byteToWrite.length() >= 8) {
                        String byteToWrite8 = byteToWrite.substring(0, 8);
                        byteToWrite = new StringBuilder(byteToWrite.substring(8));
                        byte b = (byte) Integer.parseInt(byteToWrite8, 2);
                        outputBuffer[bufferIndex++] = b;
                        if (bufferIndex == bufferSize) {
                            bos.write(outputBuffer);
                            bufferIndex = 0;
                        }
                    }
                }
            }
            if (!byteToWrite.isEmpty()) {
                String byteToWrite8 = String.format("%-8s", byteToWrite).replace(' ', '0');
                byte b = (byte) Integer.parseInt(byteToWrite8, 2);
                outputBuffer[bufferIndex++] = b;
            }
            if (bufferIndex > 0) {
                bos.write(outputBuffer, 0, bufferIndex);
            }
        }
    }

    /**
     * This function builds the codeword map recursively
     *
     * @param root     the root of the subtree
     * @param codeword the current codeword
     */
    private static void setCodewords(Node root, String codeword) {
        if (root.getLeft() == null && root.getRight() == null) {
            codeMap.put(root.getBytes(), codeword);
        }
        if (root.getLeft() != null) {
            setCodewords(root.getLeft(), codeword + "0");
        }
        if (root.getRight() != null) {
            setCodewords(root.getRight(), codeword + "1");
        }
    }

    /**
     * @param inputFile the original file
     * @param chunkSize the chunk size (n)
     * @return the absolute path of the compressed file
     */
    private static String generateCompressedPath(File inputFile, int chunkSize) {
        // file name and path
        String inputFileName = inputFile.getName();
        String dir = inputFile.getParent();
        String outputFileName = "20011539." + chunkSize + "." + inputFileName + ".hc";
        return dir + "/" + outputFileName;
    }
}
