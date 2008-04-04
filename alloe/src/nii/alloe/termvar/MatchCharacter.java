package nii.alloe.termvar;

/**
 *
 * @author John McCrae, National Institute of Informatics
 */
public class MatchCharacter implements Comparable<MatchCharacter> {
    private final char c;
    private final int type;
    private static final int TYPE_CHAR = 0;
    private static final int TYPE_ANY = 1;
    private static final int TYPE_EMPTY = 2;
    private static final int TYPE_UC = 3;
    private static final int TYPE_LC = 4;
    private static final int TYPE_WORD = 5;
    public static final MatchCharacter ANY = new MatchCharacter(TYPE_ANY);
    public static final MatchCharacter EMPTY = new MatchCharacter(TYPE_EMPTY);
    public static final MatchCharacter UC = new MatchCharacter(TYPE_UC);
    public static final MatchCharacter LC = new MatchCharacter(TYPE_LC);
    public static final MatchCharacter WORD = new MatchCharacter(TYPE_WORD);
    
    
    /** Creates a new instance of MatchCharacter */
    public MatchCharacter(char c) {
        this.c = c;
        this.type = TYPE_CHAR;
    }
    
    /*public MatchCharacter(MatchCharacter c) {
        this.c = c.c;
        this.type = c.type;
    }*/
    
    private MatchCharacter(int type) {
        this.c = 0;
        this.type = type;
    }
    
    public int compareTo(MatchCharacter mc) {
        if(mc.type == TYPE_CHAR && type == TYPE_CHAR) {
            return c < mc.c ? -1 : (c > mc.c ? +1 : 0);
        } else {
            return type < mc.type ? -1 : (c > mc.type ? +1 : 0);
        }
    }
    
    public boolean equals(Object o) {
        if(o instanceof MatchCharacter) {
            return compareTo((MatchCharacter)o) == 0;
        } else {
            return false;
        }
    }
    
    public boolean matches(MatchCharacter mc) {
        if(type == TYPE_CHAR) {
            return mc.c == c && mc.type == TYPE_CHAR;
        } else if(type == TYPE_ANY) {
             return true;
        } else if(type == TYPE_EMPTY) {
            return mc.type == TYPE_EMPTY;
        } else if(type == TYPE_UC) {
            return (mc.type == TYPE_CHAR && Character.isUpperCase(mc.c)) || mc.type == type;
        } else if(type == TYPE_LC) {
            return (mc.type == TYPE_CHAR && Character.isLowerCase(mc.c)) || mc.type == type;
        } else if(type == TYPE_WORD) {
            return (mc.type == TYPE_CHAR && Character.isLetter(mc.c)) || mc.type == TYPE_UC || 
                    mc.type == TYPE_LC || mc.type == type; 
        } else {
            throw new IllegalStateException();
        }
    }
    
    public static MatchCharacter join(MatchCharacter mc1, MatchCharacter mc2) {
        if(mc1.type == TYPE_ANY || mc1.type == TYPE_ANY) {
            return ANY;
        } else if(mc1.type == TYPE_EMPTY || mc2.type == TYPE_EMPTY) {
            if(mc1.type == mc2.type)
                return EMPTY;
            else
                return ANY;
        } else if(mc1.type == TYPE_WORD || mc2.type == TYPE_WORD) {
            if(mc1.type == TYPE_CHAR || mc2.type == TYPE_CHAR) {
                if(mc1.type == TYPE_CHAR) {
                    if(Character.isLetter(mc1.c))
                        return WORD;
                    else
                        return ANY;
                } else {
                    if(Character.isLetter(mc2.c))
                        return WORD;
                    else
                        return ANY;
                }
            } else  {
                return WORD;
            }
        } else if(mc1.type == TYPE_UC || mc2.type == TYPE_UC) {
            if(mc1.type == TYPE_CHAR || mc2.type == TYPE_CHAR) {
                if(mc1.type == TYPE_CHAR) {
                    if(Character.isUpperCase(mc1.c))
                        return UC;
                    else
                        return ANY;
                } else {
                    if(Character.isUpperCase(mc2.c))
                        return UC;
                    else
                        return ANY;
                }
            } else if(mc1.type == TYPE_LC || mc2.type == TYPE_LC) {
                return WORD;
            } else {
                return UC;
            }
        } else if(mc1.type == TYPE_LC || mc2.type == TYPE_LC) {
            if(mc1.type == TYPE_CHAR || mc2.type == TYPE_CHAR) {
                if(mc1.type == TYPE_CHAR) {
                    if(Character.isLowerCase(mc1.c))
                        return UC;
                    else
                        return ANY;
                } else {
                    if(Character.isLowerCase(mc2.c))
                        return UC;
                    else
                        return ANY;
                }
            } else {
                return LC;
            }
        } else {
            assert(mc1.type == TYPE_CHAR && mc2.type == TYPE_CHAR);
            if(mc1.c == mc2.c)
                return mc1;
            if(Character.isUpperCase(mc1.c) && Character.isUpperCase(mc2.c))
                return UC;
            if(Character.isLowerCase(mc1.c) && Character.isLowerCase(mc2.c))
                return LC;
            if(Character.isLetter(mc1.c) && Character.isLowerCase(mc2.c))
                return WORD;
            return ANY;
        }
    }
}
