import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static List<byte[]>readLines(String path) throws IOException{
        byte[] data = Files.readAllBytes(Path.of(path));
        List<byte[]> lines = new ArrayList<>();
        int start =0;
        for(int i=0;i<data.length;i++){
            if(data[i]=='\n'){
                int length = i-start;
                byte[] line = new byte[length];
                System.arraycopy(data,start,line,0,length);
                lines.add(line);
                start = i+1;
            } 
        }

        if(start<data.length){
            byte[] line = new byte[data.length - start];
            System.arraycopy(data,start,line,0,line.length);
            lines.add(line);
        }
        return lines;
    }
    public static void main(String[] args) {
        boolean known = args.length == 3 && (args[0].equals("lines") || args[0].equals("highlight"));
        if (!known) {
            System.err.println("usage: Main lines|highlight A_PATH B_PATH");
            System.exit(2);
        }
        String command = args[0];
        String aPath = args[1];
        String bPath = args[2];
        // TODO: read both files as raw bytes (brief, Section 2), then print the listing.

        try {
            List<byte[]> lines1 = readLines(aPath);
            List<byte[]> lines2 = readLines(bPath);
            System.out.println("First file lines: " + lines1.size());
            System.out.println("Second file lines: " + lines2.size());
        } catch (IOException e) {
            System.err.println("error: " + e.getMessage());
            System.exit(2);
        }
    }
}
