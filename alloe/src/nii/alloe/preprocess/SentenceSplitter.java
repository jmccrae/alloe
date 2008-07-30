package nii.alloe.preprocess;

import java.util.*;
import java.io.*;
import nii.alloe.tools.strings.Strings;

/**
 * Split a sentence. The rules are as follow, split at any occurrence of 1-3 full stops
 * unless the preceeding token is numeric (i.e., 127.0.0.1) or a known abbreviation (i.e., i.e.) or
 * a single capital (i.e., B.B.C.) or the following token starts with a lowercase (i.e., www.google.com), 
 * or is all caps (i.e., AUTOEXEC.BAT). Also split at any sequence of "!" or "?" or double newline
 * 
 * @see Tokeniser
 * @author John McCrae, National Institute of Informatics
 */
public class SentenceSplitter {
    static int FULL_STOP = 0;
    static int PUNCT = 1;
    static int PRE_EXC = 2;
    static int POST_EXC = 3;
    static int BOTH_EXC = 4;
    static int NEW_LINE = 5;
    static int OTHER = 6;
    static int TOKEN_TYPES = 7;
    
    HashSet<String> knownAbbreviations; 
    
    DFSMState initial, terminating;
    
    public SentenceSplitter(File knownAbbreviations) throws IOException{
       this.knownAbbreviations = new HashSet<String>();
       BufferedReader br = new BufferedReader(new FileReader(knownAbbreviations));
       String in = br.readLine();
       while(in != null) {
           this.knownAbbreviations.add(Strings.chomp(in));
           in = br.readLine();
       }
       buildDFSM();
    }
    
    private void buildDFSM() {
        DFSMState state0 = new DFSMState();
        DFSMState state1 = new DFSMState();
        state0.trans[PRE_EXC] = state1;
        state0.trans[BOTH_EXC] = state1;
        state1.trans[PRE_EXC] = state1;
        state1.trans[BOTH_EXC] = state1;
        state0.trans[POST_EXC] = state0;
        state0.trans[OTHER] = state0;
        for(int i = 0; i < TOKEN_TYPES; i++) {
            state1.trans[i] = state0;
        }
        DFSMState state2 = new DFSMState();
        DFSMState state3 = new DFSMState();
        DFSMState state4 = new DFSMState();
        DFSMState state5 = new DFSMState();
        DFSMState state6 = new DFSMState();
        DFSMState stateF = new DFSMState();
        state0.trans[FULL_STOP] = state2;
        state0.trans[PUNCT] = state5;
        state0.trans[NEW_LINE] = state6;
        for(int i = 0; i < TOKEN_TYPES; i++) {
            state2.trans[i] = stateF;
            state3.trans[i] = stateF;
            state4.trans[i] = stateF;
            state5.trans[i] = stateF;
            state6.trans[i] = state0;
        }
        state2.trans[FULL_STOP] = state3;
        state2.trans[POST_EXC] = state0;
        state2.trans[BOTH_EXC] = state0;
        state3.trans[FULL_STOP] = state4;
        state3.trans[POST_EXC] = state0;
        state3.trans[BOTH_EXC] = state0;
        state4.trans[FULL_STOP] = state0;
        state4.trans[POST_EXC] = state0;
        state4.trans[BOTH_EXC] = state0;
        state5.trans[PUNCT] = state5;
        state6.trans[NEW_LINE] = stateF;
        initial = state0;
        terminating = stateF;
    }
    
     class DFSMState {
	DFSMState[] trans;
        public DFSMState() {
            trans = new DFSMState[TOKEN_TYPES];
        }
     }
    
    
     /**
      * Split a list of tokens into sentences.
      * @param tokens A list of tokens, see Tokeniser
      * @return The tokens split into sentences
      * @see Tokeniser.tokenise(String)
      */
     public List<List<String>> split(String[] tokens) {
         int currentToken = 0;
         DFSMState currentState = initial;
         LinkedList<String> currentSentence = new LinkedList<String>();
         LinkedList<List<String>> rval = new LinkedList<List<String>>();
         while(currentToken < tokens.length) {
             currentState = currentState.trans[getType(tokens[currentToken])];
             if(currentState == terminating) {
                 rval.add(currentSentence);
                 currentState = initial;
                 currentSentence = new LinkedList<String>();
             } else {
                 currentSentence.add(tokens[currentToken]);
                 currentToken++;
             }
         }
         if(!currentSentence.isEmpty())
            rval.add(currentSentence);
         return rval;
     }
     
    private int getType(String s) {
        if(s.equals(".")) {
            return FULL_STOP;
        } else if(s.equals("!") || s.equals("?")) {
            return PUNCT;
        } else if(s.matches("\\p{Ll}.*") || s.matches("\\p{Lu}\\p{Lu}+")) {
            return POST_EXC;
        } else if(s.matches("\\p{Lu}")) {
            return BOTH_EXC;
        } else if(s.matches("\\p{Nd}+") || knownAbbreviations.contains(s)) {
            return PRE_EXC;
        } else if(s.matches("\n") || s.matches("\r")) {
            return NEW_LINE;
        } else
            return OTHER;
    }
}
