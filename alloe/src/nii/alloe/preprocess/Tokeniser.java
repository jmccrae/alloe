package nii.alloe.preprocess;

import java.util.*;
import java.io.*;
import java.net.*;

public class Tokeniser {

    DFSMState initialState;

    /**
     * Create a new Tokeniser
     * @param file The file containing the DFSM states (i.e. res/tokeniser.dfsm)
     * @throws java.io.IOException If the file could not be opened
     * @throws java.lang.IllegalArgumentException If the file is misformatted
     */
    public Tokeniser(File file) throws IOException, IllegalArgumentException {
	readStates(file);
    }
    
    /**
     * Create a new Tokeniser
     * @param url The file containing the DFSM states (i.e. res/tokeniser.dfsm)
     * @throws java.io.IOException If the file could not be opened
     * @throws java.lang.IllegalArgumentException If the file is misformatted
     * @throws java.net.URISyntaxException If the URL is incorrect
     */
    public Tokeniser(URL url) throws IOException, IllegalArgumentException, URISyntaxException {        
        readStates(new File(url.toURI()));
    }

    private void readStates(File file) throws IOException, IllegalArgumentException {
	HashMap<Integer,DFSMState> states = new HashMap<Integer,DFSMState>();
	HashMap<Integer,Integer[]> functions = new HashMap<Integer,Integer[]>();
	HashMap<Integer,String> annotations = new HashMap<Integer,String>();
	BufferedReader br = new BufferedReader(new FileReader(file));
	String in = br.readLine();
	while(in != null) {
	    if(in.matches("\\s*")) {
		in = br.readLine();
		continue;
	    }
	    if(!in.matches("\\d+:.*")) {
		throw new IllegalArgumentException("Could not deduce DFSMIndex: " + in);
	    }
	    String[] s = in.split("\\s+");
	    int idx = Integer.parseInt(s[0].substring(0,s[0].length() - 1));
	    states.put(idx, new DFSMState());
	    Integer[] transitions = new Integer[CHAR_TYPE_COUNT];
	    for(int i = 1; i < s.length; i++) {
		if(s[i].equals("null")) {
		    transitions[i-1] = -1;
		} else {
		    transitions[i-1] = Integer.parseInt(s[i]);
		}
	    }
	    functions.put(idx,transitions);
	    in = br.readLine();
	    if(!in.matches("rhs: .*")) {
		throw new IllegalArgumentException("RHS malformed: " + in);
	    }
	    annotations.put(idx,in.substring(5,in.length()));
	    System.out.println("Read state " + idx);
	    in = br.readLine();
	}
	for(Integer idx : states.keySet()) {
	    DFSMState state = states.get(idx);
	    Integer[] transitions = functions.get(idx);
	    for(int i = 0; i < transitions.length; i++) {
		if(transitions[i] >= 0) {
		    state.addTransition(i,states.get(transitions[i]));
		}
	    }
	    String anno = annotations.get(idx);
	    if(anno.equals("null"))
		continue;
	    String[] s1 = anno.split(";");
	    for(int i = 0; i < s1.length; i++) {
		if(s1[i].matches("\\s*"))
		    continue;
		String[] s2 = s1[i].split("=");
		if(s2.length == 1) {
		    state.addAnnotation(s1[i],null);
		} else if(s2.length == 2) {
		    state.addAnnotation(s2[0],s2[1]);
		} else {
		    throw new IllegalArgumentException("Invalid format: " + s1[i]);
		}
	    }
	}
	initialState = states.get(0);		
    }
    
    private static int CHAR_TYPE_COUNT = 44;

    class DFSMState {
	DFSMState[] transitionFunction;
	HashMap<String,String> annotations;
       
	public DFSMState() {
	    transitionFunction = new DFSMState[CHAR_TYPE_COUNT];
	    annotations = new HashMap<String,String>();
	}

	public void addTransition(int i, DFSMState state) {
	    transitionFunction[i] = state;
	}

	public DFSMState getTransition(int charType) {
	    return transitionFunction[charType];
	}

	public void addAnnotation(String type, String val) {
	    annotations.put(type,val);
	}
    }

    /**
     * Tokenise a string. (Note this is a single time function)
     * @param s1 The string to tokenise
     * @return The same string as a list of tokens
     */
    public String[] tokenise(String s1) {
	int currentChar = 0;
	DFSMState state = initialState;
	String currentToken = "";
	LinkedList<String> rval = new LinkedList<String>();
	while(currentChar < s1.length()) {
	    int type = Character.getType(s1.charAt(currentChar));

	    if(state.getTransition(type) == null) {
		rval.add(currentToken);
		currentToken = "";
		state = initialState;
		continue;
	    } else {
		currentToken = currentToken + s1.charAt(currentChar);
		state = state.getTransition(type);
		currentChar++;
	    }
	}
	rval.add(currentToken);
	return rval.toArray(new String[rval.size()]);
    }

    public static void main(String[] args) {
	try {
	    Tokeniser tokeniser = new Tokeniser(new File("/home/john/tokeniser/tokeniser.dfsm"));
	    String[] tokens = tokeniser.tokenise("The quick brown fox jumps over the lazy dog.");
	    for(String s : tokens) {
		System.out.print(s + "|");
	    }
	    System.out.println("");
	} catch(Exception x) {
	    x.printStackTrace();
	}
    }
}
	
	