import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class Myers {
    private Myers(){}

    static List<Edit> diff(int[]a,int[]b){
        int n = a.length;
        int m =b.length;

        //Common prefix
        int pre=0;
        while(pre<n && pre<m && a[pre]==b[pre]){
            pre++;
        }

        //common suffix
        int suf =0;
        while(suf<n-pre && suf<m-pre && a[n-1-suf]==b[m-1-suf]){
            suf++;
        }

        List<Edit> edits =new ArrayList<>(n+m);
        for(int i=0;i<pre;i++){
            edits.add(new Edit(' ',i,i));
        }
        
        middle(a,pre,n-suf,b,pre,m-suf,edits);

        for(int i=0;i<suf;i++){
            edits.add(new Edit(' ',n-suf+i,m-suf+i));
        }
        return edits;
    }

    public static void middle(int[]a,int alo, int ahi,int[]b, int blo,int bhi,List<Edit>edits){
        int n = ahi -alo;
        int m = bhi - blo;
        int max = n+m;
        int off = max+1;
        int[]v = new int[2*max+3];
        List<int[]>trace = new ArrayList<>();

        int found =-1;
        outer:
        for(int d=0;d<=max;d++){
            int[]snap = new int[2*d+3];
            System.arraycopy(v, off - d, snap, 1, 2 * d + 1);
            trace.add(snap);

            for (int k = -d; k <= d; k += 2) {
                int x;
                if (k == -d || (k != d && v[off + k - 1] < v[off + k + 1])) {
                    x = v[off + k + 1];       
                } else {
                    x = v[off + k - 1] + 1;    
                }
                int y = x - k;
                while (x < n && y < m && a[alo + x] == b[blo + y]) {
                    x++;
                    y++;
                }
                v[off + k] = x;
                if (x >= n && y >= m) {        
                    found = d;
                    break outer;
                }
            }
        }

        List<Edit> rev = new ArrayList<>();
        int x = n;
        int y = m;
        for (int d = found; d >= 0; d--) {
            int[] snap = trace.get(d);
            int so = d + 1;                    
            int k = x - y;

            int prevK;
            if (k == -d || (k != d && snap[so + k - 1] < snap[so + k + 1])) {
                prevK = k + 1;                 
            } else {
                prevK = k - 1;                 
            }
            int prevX = snap[so + prevK];
            int prevY = prevX - prevK;
            while (x > prevX && y > prevY) {
                rev.add(new Edit(' ', alo + x - 1, blo + y - 1));
                x--;
                y--;
            }
            if (d > 0) {
                if (x == prevX) {
                    rev.add(new Edit('+', -1, blo + y - 1));
                } else {
                    rev.add(new Edit('-', alo + x - 1, -1));
                }
            }
            x = prevX;
            y = prevY;
        }

        Collections.reverse(rev);              
        edits.addAll(rev);
    }


}
