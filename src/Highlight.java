import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class Highlight {

    private Highlight() {}
    static void writeRangeLine(OutputStream out, byte[] oldLine, byte[] newLine) throws IOException {
        int[] oldCp = new String(oldLine, StandardCharsets.UTF_8).codePoints().toArray();
        int[] newCp = new String(newLine, StandardCharsets.UTF_8).codePoints().toArray();
        List<Edit> ch = Myers.diff(oldCp, newCp);
        String line = "? " + ranges(ch, '-') + " | " + ranges(ch, '+') + "\n";
        out.write(line.getBytes(StandardCharsets.US_ASCII));
    }

    static String ranges(List<Edit> ch, char side) {
        StringBuilder sb = new StringBuilder();
        int start = -1;
        int end = -1;
        for (Edit e : ch) {
            if (e.op() != side) {
                continue;
            }
            int idx = (side == '-') ? e.aIndex() : e.bIndex();
            if (start == -1) {
                start = idx;
                end = idx + 1;
            } else if (idx == end) {
                end++;                          
            } else {
                appendRange(sb, start, end);    
                start = idx;
                end = idx + 1;
            }
        }
        if (start != -1) {
            appendRange(sb, start, end);
        }
        return sb.length() == 0 ? "." : sb.toString();
    }

    private static void appendRange(StringBuilder sb, int start, int end) {
        if (sb.length() > 0) {
            sb.append(',');
        }
        sb.append(start).append('-').append(end);
    }
}
