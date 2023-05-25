package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
    public void makeSortedList() {
        StdIn.setFile(fileName);

	/* Your code goes here */
    double fileLength = 0.0;
    int[] asciiFrequencyArray = new int[128]; // frequency array for all ASCII characters
    boolean[] presentCArray = new boolean[128]; // to keep track of which characters are present in the input file
    int distinctC = 0;
    // read from file character by character and update frequency array - keep track of the number of occurrences of each character in an array of size 128
    while (StdIn.hasNextChar()) {
        Character c = StdIn.readChar();
        asciiFrequencyArray [(int) c]++;
        presentCArray[(int) c] = true;
        fileLength++;
        }
    this.sortedCharFreqList = new ArrayList<>();
    for (int i = 0; i < 128; i++) {
        if (asciiFrequencyArray [i] > 0) {
            CharFreq charFreq = new CharFreq((char) i, (double) asciiFrequencyArray [i] / fileLength);
            sortedCharFreqList.add(charFreq);
            distinctC++;
        }
    }

    if (distinctC == 1) {
        Character specialChar;
        // wrap around to ASCII 0 if highest present character is 127
        if (sortedCharFreqList.get(0).getCharacter() == 127) specialChar = 0;         
        // add the character with ASCII value one more than the distinct character
        else specialChar = (char) (sortedCharFreqList.get(0).getCharacter() + 1);
        sortedCharFreqList.add(new CharFreq(specialChar, 0));
        presentCArray[specialChar] = true;
    }

    // sort the ArrayList by frequency using Collections.sort(list)
    Collections.sort(sortedCharFreqList);

    // remove any characters that did not appear in the input file i.e. characters that do not appear in the input file will not appear in ArrayList.
    for (int i = sortedCharFreqList.size() - 1; i >= 0; i--) {
        if (!presentCArray[sortedCharFreqList.get(i).getCharacter()]) sortedCharFreqList.remove(i);
    }
    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {
	/* Your code goes here */
    //Create queue for initial and target
    Queue<TreeNode> source = new Queue<TreeNode>();
    Queue<TreeNode> target = new Queue<TreeNode>();
    //put items in queue
    for(int i = 0;i<sortedCharFreqList.size();i++){
        source.enqueue(new TreeNode(sortedCharFreqList.get(i),null,null));
    }
    //dequeue from TreeNode
    TreeNode x = source.dequeue();
    TreeNode y = source.dequeue();
    //enqueue into target queue
    target.enqueue(new TreeNode(new CharFreq(null,x.getData().getProbOcc()+y.getData().getProbOcc()),x,y));
    //while loop to make tree
    while(source.size()>0 || target.size()>1){
        if(!source.isEmpty() && (target.isEmpty() || source.peek().getData().getProbOcc() <= target.peek().getData().getProbOcc())) x = source.dequeue();
        else x = target.dequeue();
        if(!source.isEmpty() && (target.isEmpty() || source.peek().getData().getProbOcc() <= target.peek().getData().getProbOcc())) y = source.dequeue();
        else y = target.dequeue();
        target.enqueue(new TreeNode(new CharFreq(null,x.getData().getProbOcc()+y.getData().getProbOcc()),x,y));
    }
    huffmanRoot = target.dequeue();
    }

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    //helper method to traverse through a tree

    private boolean traverseTree(TreeNode root, CharFreq target, String enc){
        if(target.getCharacter() != null && root.getData().getCharacter() != null &&(root.getData().getCharacter() == target.getCharacter())){
            int asciiConvert = (int) target.getCharacter();
            encodings[asciiConvert] = enc;
            return true;
        }
        if(root.getLeft() != null){
            if(traverseTree(root.getLeft(), target, enc+"0")) return true;
        }
        if(root.getRight() != null){
            if(traverseTree(root.getRight(), target, enc+"1")) return true;
        }
            return false;
    }
    
     public void makeEncodings() {
	/* Your code goes here */
    encodings = new String [128];
    for(CharFreq target : sortedCharFreqList) traverseTree(huffmanRoot, target, "");
    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
    StdIn.setFile(fileName);
    /* Your code goes here */
    String encode = "";
    while(StdIn.hasNextChar()) {
        char character = StdIn.readChar();
        int asciiConvert = (int) character;
        encode += encodings[asciiConvert];
    }
    writeBitString(encodedFile, encode);
    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
    StdOut.setFile(decodedFile);
	/* Your code goes here */
    String decode = readBitString(encodedFile);
    String target = "";
    TreeNode root = huffmanRoot;
    for(int i = 0;i<decode.length();i++){
        if(decode.charAt(i) == '0') root = root.getLeft();
        else root = root.getRight();
        if(root.getLeft() == null && root.getRight() == null){
            target += root.getData().getCharacter();
            root = huffmanRoot;
        }
    }   
    StdOut.print(target);
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}