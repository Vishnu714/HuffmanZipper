import java.io.*;
import java.util.*;

class HuffmanNode {
    int frequency;
    char data;
    HuffmanNode left, right;

    public HuffmanNode(char data, int frequency) {
        this.data = data;
        this.frequency = frequency;
        left = right = null;
    }
}

class HuffmanComparator implements Comparator<HuffmanNode> {
    public int compare(HuffmanNode x, HuffmanNode y) {
        return x.frequency - y.frequency;
    }
}

public class HuffmanZipper {
    private static final int BUFFER_SIZE = 1024;

    public static void compressFile(String sourceFile, String compressedFile) {
        try {
            FileInputStream fis = new FileInputStream(sourceFile);
            FileOutputStream fos = new FileOutputStream(compressedFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            // Count frequency of each character
            int[] frequency = new int[256];
            int bytesRead;
            while ((bytesRead = fis.read()) != -1) {
                frequency[bytesRead]++;
            }
            fis.close();

            // Build Huffman Tree
            PriorityQueue<HuffmanNode> pq = new PriorityQueue<>(256, new HuffmanComparator());
            for (int i = 0; i < 256; i++) {
                if (frequency[i] > 0) {
                    pq.add(new HuffmanNode((char) i, frequency[i]));
                }
            }
            while (pq.size() > 1) {
                HuffmanNode left = pq.poll();
                HuffmanNode right = pq.poll();
                HuffmanNode parent = new HuffmanNode('\0', left.frequency + right.frequency);
                parent.left = left;
                parent.right = right;
                pq.add(parent);
            }
            HuffmanNode root = pq.peek();

            // Generate Huffman Codes
            Map<Character, String> huffmanCodes = new HashMap<>();
            generateCodes(root, "", huffmanCodes);

            // Write Huffman Codes to the compressed file
            oos.writeObject(huffmanCodes);

            // Compress the file content using Huffman Codes
            fis = new FileInputStream(sourceFile);
            StringBuilder encodedContent = new StringBuilder();
            while ((bytesRead = fis.read()) != -1) {
                encodedContent.append(huffmanCodes.get((char) bytesRead));
            }
            fis.close();

            // Convert binary string to byte array and write to file
            String encodedContentStr = encodedContent.toString();
            int index = 0;
            while (index < encodedContentStr.length()) {
                String chunk = encodedContentStr.substring(index, Math.min(index + 8, encodedContentStr.length()));
                int byteValue = Integer.parseInt(chunk, 2);
                fos.write(byteValue);
                index += 8;
            }
            fos.close();
            oos.close();
            System.out.println("File compressed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decompressFile(String compressedFile, String decompressedFile) {
        try {
            FileInputStream fis = new FileInputStream(compressedFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            // Read Huffman Codes from the compressed file
            Map<Character, String> huffmanCodes = (Map<Character, String>) ois.readObject();

            // Reconstruct Huffman Tree
            HuffmanNode root = reconstructHuffmanTree(huffmanCodes);

            // Decompress the file content using Huffman Tree
            FileOutputStream fos = new FileOutputStream(decompressedFile);
            StringBuilder decodedContent = new StringBuilder();
            int bit;
            HuffmanNode current = root;
            while ((bit = fis.read()) != -1) {
                if (bit == '0') {
                    current = current.left;
                } else {
                    current = current.right;
                }
                if (current.left == null && current.right == null) {
                    decodedContent.append(current.data);
                    current = root;
                }
            }
            fis.close();

            // Write decompressed content to file
            fos.write(decodedContent.toString().getBytes());
            fos.close();
            ois.close();
            System.out.println("File decompressed successfully.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static HuffmanNode reconstructHuffmanTree(Map<Character, String> huffmanCodes) {
        HuffmanNode root = new HuffmanNode('\0', 0);
        for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
            char character = entry.getKey();
            String code = entry.getValue();
            HuffmanNode current = root;
            for (int i = 0; i < code.length(); i++) {
                if (code.charAt(i) == '0') {
                    if (current.left == null) {
                        current.left = new HuffmanNode('\0', 0);
                    }
                    current = current.left;
                } else {
                    if (current.right == null) {
                        current.right = new HuffmanNode('\0', 0);
                    }
                    current = current.right;
                }
            }
            current.data = character;
        }
        return root;
    }

    private static void generateCodes(HuffmanNode root, String code, Map<Character, String> huffmanCodes) {
        if (root == null)
            return;
        if (root.left == null && root.right == null) {
            huffmanCodes.put(root.data, code);
        }
        generateCodes(root.left, code + "0", huffmanCodes);
        generateCodes(root.right, code + "1", huffmanCodes);
    }

    public static void main(String[] args) {
        String sourceFile = "./input.txt";
        String compressedFile = "./compressed.bin";
        String decompressedFile = "./decompressed.txt";
        // Compress the file
        compressFile(sourceFile, compressedFile);

        // Decompress the file
        decompressFile(compressedFile, decompressedFile);
    }
}

