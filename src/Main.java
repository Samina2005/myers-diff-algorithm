import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    static List<byte[]> readLines(String path) throws IOException {
        byte[] data = Files.readAllBytes(Path.of(path));
        List<byte[]> lines = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == '\n') {
                int length = i - start;
                byte[] line = new byte[length];
                System.arraycopy(data, start, line, 0, length);
                lines.add(line);
                start = i + 1;
            }
        }
        if (start < data.length) {
            byte[] line = new byte[data.length - start];
            System.arraycopy(data, start, line, 0, line.length);
            lines.add(line);
        }
        return lines;
    }

    static int[][] toIds(List<byte[]> linesA, List<byte[]> linesB) {
        HashMap<ByteBuffer, Integer> ids = new HashMap<>();
        int[] a = new int[linesA.size()];
        int[] b = new int[linesB.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = ids.computeIfAbsent(ByteBuffer.wrap(linesA.get(i)), key -> ids.size());
        }
        for (int i = 0; i < b.length; i++) {
            b[i] = ids.computeIfAbsent(ByteBuffer.wrap(linesB.get(i)), key -> ids.size());
        }
        return new int[][] { a, b };
    }

    public static void main(String[] args) throws IOException {
        boolean known = args.length == 3 && (args[0].equals("lines") || args[0].equals("highlight"));
        if (!known) {
            System.err.println("usage: Main lines|highlight A_PATH B_PATH");
            System.exit(2);
        }
        boolean highlight = args[0].equals("highlight");
        List<byte[]> linesA;
        List<byte[]> linesB;
        try {
            linesA = readLines(args[1]);
            linesB = readLines(args[2]);
        } catch (IOException | InvalidPathException e) {
            System.err.println("error: cannot read file: " + e.getMessage());
            System.exit(2);
            return;
        }

        int[][] ids = toIds(linesA, linesB);
        List<Edit> edits = Myers.diff(ids[0], ids[1]);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(FileDescriptor.out), 1 << 16);
        printDiff(edits, linesA, linesB, highlight, out);
        out.flush();
    }

    static void printDiff(List<Edit> edits, List<byte[]> linesA, List<byte[]> linesB,boolean highlight, OutputStream out) throws IOException {
        List<Edit> dels = new ArrayList<>();
        List<Edit> ins = new ArrayList<>();
        for (Edit e : edits) {
            if (e.op() == '-') {
                dels.add(e);
            } else if (e.op() == '+') {
                ins.add(e);
            } else {
                flushBlock(dels, ins, linesA, linesB, highlight, out);
                writeLine(out, ' ', linesA.get(e.aIndex()));
            }
        }
        flushBlock(dels, ins, linesA, linesB, highlight, out); 
    }

    static void flushBlock(List<Edit> dels, List<Edit> ins, List<byte[]> linesA, List<byte[]> linesB,boolean highlight, OutputStream out) throws IOException {
        for (Edit e : dels) {
            writeLine(out, '-', linesA.get(e.aIndex()));
        }
        for (int j = 0; j < ins.size(); j++) {
            writeLine(out, '+', linesB.get(ins.get(j).bIndex()));
            if (highlight && j < dels.size()) {
                Highlight.writeRangeLine(out,
                        linesA.get(dels.get(j).aIndex()),
                        linesB.get(ins.get(j).bIndex()));
            }
        }
        dels.clear();
        ins.clear();
    }

    static void writeLine(OutputStream out, char prefix, byte[] line) throws IOException {
        out.write(prefix);
        out.write(line);
        out.write('\n');
    }
}
