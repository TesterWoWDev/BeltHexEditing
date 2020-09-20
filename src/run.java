import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class run {
    public static String fileName = "";
    public static byte[] fileContent;

    public static void main(String[] args) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("BeltListfile.csv"))) {
            String line;
            br.readLine();//skip header
            while ((line = br.readLine()) != null) {
                String[] displayRow = line.split(";");
                fileName = displayRow[0];
                System.out.println(displayRow[1]);
                proofOfConcept(displayRow[1]);


            }
        }
    }

    public static void proofOfConcept(String filePath) throws IOException{
        String skinFile = filePath.replace(".m2","00.skin");
        readInFile(skinFile);
        saveFile("00.skin");
        readInFile(filePath);
        String textureOffset = getAtAddress("55",1) + getAtAddress("54",1);
        String texture = "item/objectcomponents/waist/" + fileName + ".blp";
        String textureNameLength = intToHex(texture.length());
        String textureLengthOffset = intToHex(Integer.parseInt(textureOffset,16) + 8);
        int textureOffsetOffsetInt = Integer.parseInt(textureOffset,16) + 12;
        replaceBytesAtAddress(textureOffset,"00");//change flag 2 to 0
        replaceBytesAtAddress(textureLengthOffset,textureNameLength);//add length of it
        printAtAddress(textureOffset,16);
        insertBytesAtEndOfFile("00");//add single null
        String lengthOfFile = intToHex(fileContent.length);
        if(lengthOfFile.length() == 4) {
            lengthOfFile = lengthOfFile.substring(2, 4) + lengthOfFile.substring(0, 2);
            insertStringAtEndOfFile(texture.replace("/","\\"));

            byte[] val = replaceWithValue(lengthOfFile);

            fileContent[textureOffsetOffsetInt] = val[0];
            fileContent[textureOffsetOffsetInt + 1] = val[1];

            saveFile(".m2");
        }
    }

    public static void proofOfConceptOld(String arg1) throws IOException {
        readInFile(arg1);
        //either works
        String hex = intToHex(304);
        //String hex = "193";//dont include the 0x or the h ex.0x193h -> 193
        printAtAddress(hex, 5);
        replaceStringAtAddress(hex, "trash");
        printAtAddress(hex, 5);
        String value = getAtAddress(hex,5);
        System.out.println(value);//gets direct value, so you get get and set with it
        insertBytesAtEndOfFile(value);
        replaceBytesAtAddress(hex,"885533");
        printAtAddress(hex, 5);
        insertStringAtEndOfFile("trash");
        insertBytesAtEndOfFile("772129");//no spaces for the bytes
        printAtAddress(intToHex(fileContent.length-8), 8);
        saveFile(".m2");
    }

    public static byte[] replaceWithValue(String stuff){
        byte[] val = new byte[stuff.length() / 2];
        for (int i = 0; i < val.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(stuff.substring(index, index + 2), 16);
            val[i] = (byte) j;
        }
        return val;
    }

    public static String intToHex(int intake){
        return Integer.toHexString(intake);
    }

    public static String returnString(String input){
        StringBuilder result = new StringBuilder();
        char[] charArray = input.toCharArray();
        for(int i = 0; i < charArray.length; i=i+2) {
            String st = ""+charArray[i]+""+charArray[i+1];
            char ch = (char)Integer.parseInt(st, 16);
            result.append(ch);
        }
        return result.toString();
    }

    public static void readInFile(String name) throws IOException {
        fileContent = Files.readAllBytes(Paths.get(name));
    }

    public static void replaceBytesAtAddress(String address,String replacement){
        replaceStringAtAddress(address,returnString(replacement));
    }

    public static void insertBytesAtEndOfFile(String replacement){
        insertStringAtEndOfFile(returnString(replacement));
    }

    public static void replaceStringAtAddress(String address,String replacement){
        byte[] replace = replacement.getBytes();
        int size = replace.length;
        int hex = Integer.parseInt(address,16);
        System.arraycopy(replace, 0, fileContent, hex, size);
    }

    public static void insertStringAtEndOfFile(String replacement){
        byte[] replace = replacement.getBytes();
        int size = replace.length;
        byte[] holder = new byte[fileContent.length + size];
        System.arraycopy(fileContent, 0, holder, 0, fileContent.length);
        System.arraycopy(replace, 0, holder, fileContent.length, size);
        fileContent = holder;
    }

    public static void printAtAddress(String address, int size) {
        int hexToInt = Integer.parseInt(address, 16);
        for (int i=0;i<size;i++) {
            System.out.printf("%02x ", fileContent[hexToInt+i]);
        }
        System.out.println();
    }

    public static String getAtAddress(String address, int size) {
        StringBuilder join = new StringBuilder();
        int hexToInt = Integer.parseInt(address, 16);
        for (int i=0;i<size;i++) {
            join.append(String.format("%02x", fileContent[hexToInt + i]));
        }
        return join.toString();
    }

    public static void saveFile(String extension) throws IOException {
        Files.write(Paths.get(("item/objectcomponents/waist/" +fileName + extension)), fileContent);
    }
}
