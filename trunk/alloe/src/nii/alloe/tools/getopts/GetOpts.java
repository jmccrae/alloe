package nii.alloe.tools.getopts;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import nii.alloe.tools.strings.Strings;

/**
 * Deals with the loading of a set of command line options. Includes many feature including automatic
 * deserialization and loading, checking for presence of compulsory arguments etc.
 *
 * @author John McCrae, National Institute of Informatics
 */
public class GetOpts {
    private HashMap<String,Argument> arguments;
    private HashSet<String> necessaryArguments;
    private boolean isReady = false;
    
    /** Creates a new instance of GetOpts */
    public GetOpts() {
        arguments = new HashMap<String,Argument>();
        necessaryArguments = new HashSet<String>();
    }
    
    /** Add an argument. This function will attempt to autoguess the way this argument should be used
     * firstly it will check if it a base type, then if it is serializable and finally if it has a suitable
     * loading method called either loadClass, openClass, readClass, load, open or read
     * @throws IllegalArgumentException If it was impossible to guess the argument type.
     */
    public void addArgumentAuto(String name, Class clasz, boolean necessary) {
        if(clasz.equals(String.class)) {
            addStringArgument(name,necessary);
        } else if(clasz.equals(Integer.class) || clasz.equals(int.class)) {
            addIntegerArgument(name,necessary);
        } else if(clasz.equals(Boolean.class) || clasz.equals(boolean.class)) {
            addBooleanArgument(name);
        } else if(clasz.equals(Double.class) || clasz.equals(double.class)) {
            addDoubleArgument(name,necessary);
        } else if(clasz.equals(Long.class) || clasz.equals(long.class)) {
            addLongArgument(name,necessary);
        } else if(clasz.equals(Character.class) || clasz.equals(char.class)) {
            addCharArgument(name,necessary);
        } else if(clasz.equals(Float.class) || clasz.equals(float.class)) {
            addFloatArgument(name,necessary);
        } else if(Serializable.class.isAssignableFrom(clasz)) {
            addSerializableArgument(name,clasz,necessary);
        } else {
            for(Method method : clasz.getMethods()) {
                if(method.getName().matches("[open|read|load](" + clasz.getSimpleName() + ")?")) {
                    Class[] args = method.getParameterTypes();
                    if(!method.getReturnType().getName().equals("void") && args.length == 1 &&
                            (String.class.equals(args[0]) || File.class.equals(args[0])) &&
                            Modifier.isStatic(method.getModifiers())) {
                        addLoadableArgument(name,method,necessary);
                        return;
                    }
                }
            }
            throw new IllegalArgumentException("Couldn't automatically deduce the loading of this function");
        }
    }
    
    /** Register a new boolean argument, specifiable by presence or absence of "-name"
     * @throws IllegalArgumentException If the name already exists or contains non-word characters
     */
    public void addBooleanArgument(String name) {
        if(arguments.containsKey(name) || !name.matches("\\w+"))
            throw new IllegalArgumentException("Argument " + name + " already exists or is not valid");
        arguments.put(name, new BooleanArgument());
    }
    
    /** Register a string argument, specifiable by "-name string"
     * @param necessary Set to true if this argument must be loaded
     * @throws IllegalArgumentException If the name already exists or contains non-word characters
     */
    public void addStringArgument(String name, boolean necessary) {
        if(arguments.containsKey(name) || !name.matches("\\w+"))
            throw new IllegalArgumentException("Argument " + name + " already exists or is not valid");
        arguments.put(name, new DirectArgument(DirectArgument.ARG_STRING));
        if(necessary)
            necessaryArguments.add(name);
    }
    
    /** Register an integer argument, specifiable by "-name n"
     * @param necessary Set to true if this argument must be loaded
     * @throws IllegalArgumentException If the name already exists or contains non-word characters
     */
    public void addIntegerArgument(String name, boolean necessary) {
        if(arguments.containsKey(name) || !name.matches("\\w+"))
            throw new IllegalArgumentException("Argument " + name + " already exists or is not valid");
        arguments.put(name, new DirectArgument(DirectArgument.ARG_INT));
        if(necessary)
            necessaryArguments.add(name);
    }
    /** Register a double-precision floating point argument, specifiable by "-name n.n"
     * @param necessary Set to true if this argument must be loaded
     * @throws IllegalArgumentException If the name already exists or contains non-word characters
     */
    public void addDoubleArgument(String name, boolean necessary) {
        if(arguments.containsKey(name) || !name.matches("\\w+"))
            throw new IllegalArgumentException("Argument " + name + " already exists or is not valid");
        arguments.put(name, new DirectArgument(DirectArgument.ARG_DOUBLE));
        if(necessary)
            necessaryArguments.add(name);
    }
    
    /** Register a long integer argument, specifiable by "-name n"
     * @param necessary Set to true if this argument must be loaded
     * @throws IllegalArgumentException If the name already exists or contains non-word characters
     */
    public void addLongArgument(String name, boolean necessary) {
        if(arguments.containsKey(name) || !name.matches("\\w+"))
            throw new IllegalArgumentException("Argument " + name + " already exists or is not valid");
        arguments.put(name, new DirectArgument(DirectArgument.ARG_LONG));
        if(necessary)
            necessaryArguments.add(name);
    }
    
    /** Register a single character argument, specifiable by "-name ch"
     * @param necessary Set to true if this argument must be loaded
     * @throws IllegalArgumentException If the name already exists or contains non-word characters
     */
    public void addCharArgument(String name, boolean necessary) {
        if(arguments.containsKey(name) || !name.matches("\\w+"))
            throw new IllegalArgumentException("Argument " + name + " already exists or is not valid");
        arguments.put(name, new DirectArgument(DirectArgument.ARG_CHAR));
        if(necessary)
            necessaryArguments.add(name);
    }
    
    /** Register a single precision floating point argument, specifiable by "-name n.n"
     * @param necessary Set to true if this argument must be loaded
     * @throws IllegalArgumentException If the name already exists or contains non-word characters
     */
    public void addFloatArgument(String name, boolean necessary) {
        if(arguments.containsKey(name) || !name.matches("\\w+"))
            throw new IllegalArgumentException("Argument " + name + " already exists or is not valid");
        arguments.put(name, new DirectArgument(DirectArgument.ARG_FLOAT));
        if(necessary)
            necessaryArguments.add(name);
    }
    
    /** Register a serializable object argument, specifiable by "-name file". The file should
     * be a file gained by serializing this object.
     * @param clasz The class of the serialized object
     * @throws IllegalArgumentException If the name already exists, contains non-word characters or
     * the class passed does not implement Serializable */
    public void addSerializableArgument(String name, Class clasz, boolean necessary) {
        if(arguments.containsKey(name) || !name.matches("\\w+"))
            throw new IllegalArgumentException("Argument " + name + " already exists or is not valid");
        if(!Serializable.class.isAssignableFrom(clasz))
            throw new IllegalArgumentException("Class passed is not serializable");
        arguments.put(name, new SerializableArgument(clasz));
        if(necessary)
            necessaryArguments.add(name);
    }
    
    /** Register a loadable object argument, specifiable by "-name file". The file should be
     * loadable by invoking method with this file as the only parameter
     * @param method A method to load the object. This method must be static, return an object and have only
     * one argument which is either a String or a File
     * @throws IllegalArgumentException If the name already exists, contains non-word characters or the
     *method is not valid*/
    public void addLoadableArgument(String name, Method method, boolean necessary) {
        if(arguments.containsKey(name) || !name.matches("\\w+"))
            throw new IllegalArgumentException("Argument " + name + " already exists or is not valid");
        Class[] args = method.getParameterTypes();
        if(method.getReturnType().getName().equals("void") || args.length != 1 ||
                !(String.class.equals(args[0]) || File.class.equals(args[0])) ||
                !Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method passed either has a void return type, more than one argument, is not static or the argument is not a String or File");
        }
        arguments.put(name, new LoadableArgument(method));
        if(necessary)
            necessaryArguments.add(name);
    }
    
    /** Get the class of the argument.
     * @return The class of the argument or null if no class is expected
     */
    public Class getArgumentClass(String name) {
        Argument arg = arguments.get(name);
        if(arg != null)
            return arg.getArgumentClass();
        else
            return null;
    }
    
    /** Call this function to parse all command line arguments.
     * Command line arguments can be of the following forms
     * <ul><li><code>-arg val
     * <li>--arg val
     * <li>-arg=val
     * <li>--arg=val
     * </code></ul>
     * @param args The list of arguments, in the same form as passed to <code>void main(String[])</code>
     * @return Any unparsed arguments
     * @throws GetOptsException If the format is not correct, or some error occurred loading an argument */
    public String[] getOpts(String[] args) throws GetOptsException {
        Vector<String> unparsed = new Vector<String>();
        isReady = false;
        HashSet<String> loadedArguments = new HashSet<String>(necessaryArguments);
        for(Argument arg : arguments.values()) {
            arg.reset();
        }
        for(int i = 0; i < args.length; i++) {
            Matcher matcher = Pattern.compile("--?([^=]*)").matcher(args[i]);
            if(matcher.matches()) {
                Argument arg = arguments.get(matcher.group(1));
                if(arg == null) {
                    throw new GetOptsException("Unknown parameter: " + matcher.group(1) + "\n" + getUsage());
                }
                try {
                    if(arg instanceof BooleanArgument) {
                        arg.readArgument(null);
                    } else {
                        arg.readArgument(args[++i]);
                    }
                } catch(GetOptsException x) {
                    throw new GetOptsException("Argument " + matcher.group(1) + " : " + x.getMessage() + "\n" + getUsage());
                }
                loadedArguments.remove(matcher.group(1));
                continue;
            }
            matcher = Pattern.compile("--?([^=]*)=([^=]*)").matcher(args[i]);
            if(matcher.matches()) {
                Argument arg = arguments.get(matcher.group(1));
                if(arg == null) {
                    throw new GetOptsException("Unknown parameter: " + matcher.group(1) + "\n" + getUsage());
                }
                try {
                    if(arg instanceof BooleanArgument) {
                        arg.readArgument(null);
                    } else {
                        arg.readArgument(matcher.group(2));
                    }
                } catch(GetOptsException x) {
                    throw new GetOptsException("Argument " + matcher.group(1) + " : " + x.getMessage() + "\n" + getUsage());
                }
                loadedArguments.remove(matcher.group(1));
                continue;
            }
            unparsed.add(args[i]);
        }
        if(!loadedArguments.isEmpty()) {
            throw new GetOptsException("The following parameters were not specified:" + Strings.join(",",loadedArguments) + "\n");
        }
        isReady = true;
        return unparsed.toArray(new String[0]);
    }
    
    private String getUsage() {
        String rval = "Usage:\n\t command";
        for(Map.Entry<String,Argument> entry : arguments.entrySet()) {
            if(entry.getValue() instanceof BooleanArgument) {
                rval = rval + " [ -" + entry.getKey() + " ]";
            } else {
                rval = rval + (necessaryArguments.contains(entry.getKey()) ? " -" : " [ -")
                + entry.getKey() + (necessaryArguments.contains(entry.getKey()) ? " val " : " val ] ");
            }
        }
        return rval;
    }
    
    /** Get the value of the particular Argument
     * @throws IllegalArgumentException If the name was not registered
     * @throws IllegalStateExcpetion If getOpts(String[]) has not yet been called successfully
     */
    public Object getArgument(String name) {
        Argument arg = arguments.get(name);
        if(!isReady)
            throw new IllegalStateException("Arguments not initialized");
        if(arg == null)
            throw new IllegalArgumentException("Argument " + name + " had not been registered");
        return arg.getObject();
    }
}

interface Argument {
    void readArgument(String arg) throws GetOptsException;
    Object getObject();
    void reset();
    Class getArgumentClass();
}

class DirectArgument implements Argument {
    final static int ARG_STRING = 0;
    final static int ARG_INT = 1;
    final static int ARG_LONG = 2;
    final static int ARG_CHAR = 3;
    final static int ARG_DOUBLE = 4;
    final static int ARG_FLOAT = 5;
    final int argType;
    Object object;
    
    DirectArgument(int argType) {
        if(argType < 0 || argType > 5)
            throw new IllegalArgumentException("Invalid argType!");
        this.argType = argType;
    }
    
    public void readArgument(String arg) throws GetOptsException {
        if(argType == ARG_STRING) {
            object = arg;
        } else if(argType == ARG_INT) {
            object = Integer.parseInt(arg);
        } else if(argType == ARG_LONG) {
            object = Long.parseLong(arg);
        } else if(argType == ARG_CHAR) {
            if(arg.length() != 1)
                throw new GetOptsException("Character argument not a singlecharacter");
            object = arg.charAt(0);
        } else if(argType == ARG_DOUBLE) {
            object = Double.parseDouble(arg);
        } else if(argType == ARG_FLOAT) {
            object = Float.parseFloat(arg);
        } else {
            throw new RuntimeException("Unreachable statement");
        }
    }
    
    public Object getObject() { return object; }
    
    public void reset() { object = null; }
    
    public Class getArgumentClass() {
        if(argType == ARG_STRING) {
            return String.class;
        } else if(argType == ARG_INT) {
            return Integer.class;
        } else if(argType == ARG_LONG) {
            return Long.class;
        } else if(argType == ARG_CHAR) {
            return Character.class;
        } else if(argType == ARG_DOUBLE) {
            return Double.class;
        } else if(argType == ARG_FLOAT) {
            return Float.class;
        } else {
            throw new RuntimeException("Unreachable statement");
        }
    }
}

class SerializableArgument implements Argument {
    final Class clasz;
    Object object;
    
    SerializableArgument(Class clasz) {
        this.clasz = clasz;
    }
    
    public void readArgument(String arg) throws GetOptsException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arg));
            object = ois.readObject();
            if(!clasz.isInstance(object))
                throw new GetOptsException("Deserialized class not of expected type");
        } catch(IOException x) {
            x.printStackTrace();
            throw new GetOptsException("Couldn't deserialize object: " + x.getMessage());
        } catch(ClassNotFoundException x) {
            x.printStackTrace();
            throw new GetOptsException("Couldn't deserialize object: " + x.getMessage());
        }
    }
    
    public Object getObject() {
        return object;
    }
    
    public void reset() { object = null; }
    
    public Class getArgumentClass() { return clasz; }
}

class LoadableArgument implements Argument {
    final Method method;
    Object object;
    
    LoadableArgument(Method method) {
        this.method = method;
    }
    
    public void readArgument(String arg) throws GetOptsException {
        Object [] args = new Object[1];
        if(method.getParameterTypes()[0].equals(String.class)) {
            args[0] = arg;
        } else {
            args[0] = new File(arg);
        }
        try {
            object = method.invoke(null,args);
        } catch(IllegalAccessException x) {
            throw new RuntimeException("Unreachable");
        } catch(IllegalArgumentException x) {
            throw new RuntimeException("Unreachable");
        } catch(InvocationTargetException x) {
            x.printStackTrace();
            throw new GetOptsException("Couldn't read object: " + x.getMessage());
        }
    }
    
    public Object getObject() {
        return object;
    }
    
    public void reset() { object = null; }
    
    public Class getArgumentClass() { return method.getReturnType(); }
}

class BooleanArgument implements Argument {
    boolean read;
    
    public void readArgument(String arg) throws GetOptsException{
        read = true;
    }
    
    public Object getObject() {
        return read ? Boolean.TRUE : Boolean.FALSE;
    }
    
    public void reset() { read = false; }
    
    public Class getArgumentClass() { return Boolean.class; }
}
