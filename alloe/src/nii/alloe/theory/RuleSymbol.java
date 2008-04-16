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
    private int modelSize;

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
    public string getConstant(int id) {
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
					 + i " arguments");
	    }
	}
    }

    /** Get the number of arguments for a function
     * @throws InvalidArgumentException If the function is not defined */
    public int getFunctionArguments(int id) {
	Integer i = functions.get(id);
	if(i == null) {
	    throw new InvalidArgumentException("Function not defined");
	}
	return i;
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
	for(Map.Entry<Integer,Integer> e : functions) {
	    range.put(e.getKey(),i);
	    i += (int)Math.pow(modelSize,e.getValue());
	}
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