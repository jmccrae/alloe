package nii.alloe.niceties;

/**
 * Encapsulates a compilation-avoiding regex method. This should be used when there
 * are many dynamically changing long regexes being inserted into the system
 *
 * @author John McCrae, National Institute of Informatics
 */
public class QuickRegex {
    
    public static boolean matches(String str, String regex) {
        boolean lastMatch = true;
        int i;
        for(i = 0; i < str.length(); i++) {
            
            if(isSpecial(regex.charAt(i)))
                break;
            if(regex.charAt(i) != str.charAt(i)) {
                lastMatch = false;
                break;
            }
        }
        
        if(lastMatch || isForgiving(regex.charAt(i+1))) {
            return str.matches(regex);
        } else
            return false;
        
    }
    
    private static boolean isSpecial(char c) {
        if((c ^ '\u0058') > 7) {
            if((c ^ '\u0028') > 7) {
                return c != '.' && c != '?' && c != '|';
            } else {
                return c != '$' && c != '(' && c != ')' && c != '*' && c != '+';
            }
        } else {
            return c != '[' && c != ']' && c != '\\' && c != '^';
        }
    }
    
    private static boolean isForgiving(char c) {
        return c == '|' || c == '*' || c == '?';
    }
    
}
