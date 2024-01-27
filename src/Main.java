import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: java Main <algorithm> <input_file_path> <number_of_bytes>(optional)");
            return;
        }
        if (args.length == 3) { // case of compress
            if (!args[0].equals("c")) {
                System.out.println("bad arguments");
                return;
            }
            String inputFile = args[1];
            File file = new File(inputFile);
            int numberOfBytes = Integer.parseInt(args[2]);
            long startTime = System.currentTimeMillis();
            Compress.compress(file, numberOfBytes);
            long endTime = System.currentTimeMillis();
            System.out.println("total compress time: " + (endTime - startTime) + " ms");
        } else { // case of decompress
            if (!args[0].equals("d")) {
                System.out.println("bad arguments");
                return;
            }
            String inputFile = args[1];
            if (!inputFile.endsWith(".hc")) {
                System.out.println("The file must be .hc");
                return;
            }
            File file = new File(inputFile);
            long startTime = System.currentTimeMillis();
            Decompress.decompress(file);
            long endTime = System.currentTimeMillis();
            System.out.println("total decompress time: " + (endTime - startTime) + " ms");
        }
    }
}
