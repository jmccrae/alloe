package nii.alloe.termvar;


class Match implements Comparable<Match> {
    int i1, i2;
    
    Match(int i, int j) {
        i1 = i;
        i2 = j;
    }
    
    public int compareTo(Match m) {
        if(i1 < m.i1) {
            return -1;
        } else if(i1 > m.i1) {
            return +1;
        } else if(i2 < m.i2) {
            return -1;
        } else if(i2 > m.i2) {
            return +1;
        } else {
            return 0;
        }
    }
    
    public String toString() { return "[" + i1 + "->" + i2 + "]"; }
}