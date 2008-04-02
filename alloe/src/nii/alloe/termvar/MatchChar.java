package nii.alloe.termvar;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class MatchChar {
    char c;
    int type;
    public static final int TYPE_CHAR = 0;
    public static final int TYPE_EMPTY = 1;
    public static final int TYPE_ANY = 2;
    public static final MatchChar EMPTY = new MatchChar('_',1);
    public static final MatchChar ANY = new MatchChar('*',2);    
    public MatchChar(char c) {
        this.c = c;
        this.type = TYPE_CHAR;
    }
    
    private MatchChar(char c, int i) {
        this.c = c;
        this.type = i;
    }
    
    public boolean equals(Object o) {
        if(o instanceof MatchChar) {
            return c == ((MatchChar)o).c && type == ((MatchChar)o).type;
        }
        return false;
    }
    
    public String toString() { return "" + c; }
}
