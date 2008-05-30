package nii.alloe.theory;
import java.util.*;

/** Tracks all the constants and functions used in a Logic.
 * We track constants (that is named entities in the logic)
 * and functions, which are gained from Skolemization
 * note that we refer to skolemization "constants" as 0-ary functions
 * to avoid confusion with named constants
 * Note that constants are consecutively numbered from zero and functions
 * are numbered by the number as in the logic
 */
public class RuleSymbol {
    Vector<String> constants;
    HashMap<Integer,Integer> functions;
    int modelSize;
    int fullModelSize;
    
    /** Create a new instance */
    public RuleSymbol() {
        constants = new Vector<String>();
        functions = new HashMap<Integer,Integer>();
    }
    
    /** Add a new constant
     * @return True if the constant is new */
    public boolean addConstant(String name) {
        return constants.add(name);
    }
    
    /** Get the index of a constant
     * @return The index of this constant */
    public int getConstantIndex(String name) {
        return constants.indexOf(name);
    }
    
    /** Get the name for a constant
     * @return The name of the constant */
    public String getConstant(int id) {
        return constants.get(id);
    }
    
    /** Get all the constants (for iteration etc)
     */
    public Collection<String> getConstants() {
        return constants;
    }
    
    /** Add a new function
     * @returns True if the function is new
     * @param id The (unique) id of this function
     * @param args The number of arguments this function takes
     * @throws LogicException If this function has already been added with
     *  a different argument number */
    public boolean addFunction(int id, int args) throws LogicException {
        Integer i = functions.get(id);
        if(i == null) {
            functions.put(id,args);
            return true;
        } else {
            if(i == args) {
                return false;
            } else {
                throw new LogicException("Attempt to redefine function " +
                        id + " from " + args + " arguments to "
                        + i + " arguments");
            }
        }
    }
    
    /** Get the number of arguments for a function
     * @throws InvalidArgumentException If the function is not defined */
    public int getFunctionArguments(int id) {
        Integer i = functions.get(id);
        if(i == null) {
            throw new IllegalArgumentException("Function not defined");
        }
        return i;
    }

    public final class FunctionID {
	final int id;
	final int[] args;
	public ModelID(int id, int[] args) {
	    this.id = id;
	    this.args = args;
	}
    }

    /** Convert a model id to a function id. Also extracts all the arg values
     * @throws IllegalArgumentException If modelSize &lt; id  || fullModelSize &gt; id 
     */
    public FunctionID modelIdToFunctionId(int modelID) {
	int i = modelSize;
	for(Map.Entry<Integer,Integer> entry : functions) {
	    if(modelID >= i && modelID < i + Math.pow(modelSize,entry.getValue())) {
		int[] args = new int[entry.getValue()];
		modelID -= i;
		for(int j = args.length-1; j >= 0; j--) {
		    args[j] = modelID % modelSize;
		    modelID = modelID / modelSize;
		    
		}
		return new FunctionID(entry.getKey(),args);		
	    }
	    else
		i += Math.pow(modelSize,entry.getValue());
	}
	throw new IllegalArgumentException("Model ID passed not valid");
    }

    /** Convert a function id to a model id
     * @throws IllegalArgumentException If function ID has not been added
     * @throws IllegalArgumentException If the functions arg count is not the length of <code>args</args>
     */
    public int functionIdToModelId(int functionID, int [] args) {
	functionIdToModelId(new FunctionId(functionID, args));
    }

    /** Convert a function id to a model id
     * @throws IllegalArgumentException If function ID has not been added
     * @throws IllegalArgumentException If the functions arg count is not the length of <code>id.args</args>
     */
    public int functionIdToModelId(FunctionID id) {
	int i = modelSize;
	for(Map.Entry<Integer,Integer> entry : functions) {
	    if(id.id == entry.getKey()) {
		if(entry.getValue() != args.length) {
		    throw new IllegalArgumentException();
		for(int j = 0; j < args.length; j++) {
		    i += Math.pow(modelSize, args.length - j - 1) * id.args[j];
		}
		return i;
		}
	    }
	}
	throw new IllegalArgumentException();
    }

    
    /** Get the functions
     */
    public Collection<Integer> getFunctions() {
        return functions.keySet();
    }
    
    private Map<Integer,Integer> range;
    
    
    /** Sets the model size. As a side effect the ranges are also calculated.
     * @param modelSize The number of elements in the Model
     * @throws IllegalArgumentException If modelSize &lt; constants.size()
     */
    public void setModelSize(int modelSize) {
        if(modelSize < constants.size()) {
            throw new IllegalArgumentException("More constants than elements in the model!");
        }
        int i = modelSize;
        range = new HashMap<Integer,Integer>();
        for(Map.Entry<Integer,Integer> e : functions.entrySet()) {
            range.put(e.getKey(),i);
            i += (int)Math.pow(modelSize,e.getValue());
        }
        fullModelSize = i;
        this.modelSize = modelSize;
    }
    
    /** Get the model size */
    public int getModelSize() { return modelSize; }
    
    /** Return the ranges,  that is the ids to be used in the model. The
     * ranges are as such, first the constants, then any other elements
     * from the Model, then the functions in order, each one taking up
     * consecutively modelSize ^ arguments ids.
     * @return The range or <code>null</code> if the model size has not been set
     */
    public Map<Integer,Integer> getRange() {
        return range;
    }
}
