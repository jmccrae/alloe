package nii.alloe.tools.process;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;
import nii.alloe.tools.getopts.GetOpts;
import nii.alloe.tools.getopts.GetOptsException;
import nii.alloe.tools.strings.Strings;

/**
 * A wrapper to allow for processes to be ran in the terminal. The default operation should allow for nearly any process
 * to be started. Sample override
 * <code><pre>
 * public class RunProcess {
 *      public RunProcess(String[] args) {
 *          super(args);
 *          name = "Process Name";
 *      }
 *      public static void main(String[] args) {
 *          new RunProcess(args);
 *      }
 *      protected void start(String[] args) {
 *          // Some code
 *          autoInit(args, The AlloeProcess class, The definition of its constructor, The names for constructor params);
 *      }
 * }</pre></code>
 *
 * @author john
 */
public class TerminalProcess {

    /** Change to indicate the minimum difference required to report progress change */
    public static double MIN_PROG_STEP = 0.001;
    protected AlloeProcess process;

    protected TerminalProcess(String[] args) {
        boolean init = init(args);
        process.addProgressListener(new AlloeProgressListener() {

            public void finished() {
                System.out.println("");
                finish();
            }
            double lastProgress = 0;

            public void progressChange(double newProgress) {
                if (newProgress - lastProgress >= MIN_PROG_STEP) {
                    System.out.print("\r" + process.getStateMessage() + ": " + (newProgress * 100.0) + "%");
                    lastProgress = newProgress;
                }
            }
        });
        Thread t = new Thread(new Runnable() {

            public void run() {
                if (process instanceof Serializable) {
                    try {
                        process.pause();
                        Random r = new Random();
                        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getName() + r.nextInt()));
                        oos.writeObject(process);
                        oos.close();
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(t);
        if (init) {
            process.start();
        } else {
            process.resume();
        }
    }
    protected String name;

    /** Set to create a name for this particular process. This will be used to dump results if necessary */
    public String getName() {
        return name;
    }

    /** Do everything at the end of execution. Eg saving the result */
    public void finish() {
        for (Field f : process.getClass().getFields()) {
            try {
                autoWriteObject(name + "." + f.getName(), f.get(process));
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }

    /** Prepare the execution. Eg loading all data and instantiating the process 
     * Override this if you want custom resuming and or error messages, otherwise
     * override <code>start(String[])</code>
     * @see #start(String[])
     */
    public boolean init(String[] args) {
        if (args.length > 0 && args[0].equals("-resume")) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[1]));
                process = (AlloeProcess) ois.readObject();
                ois.close();
            } catch (IOException x) {
                x.printStackTrace();
                System.exit(-1);
            } catch (ClassNotFoundException x) {
                x.printStackTrace();
                System.exit(-1);
            }
            return false;
        } else {
            start(args);
            return true;
        }
    }

    /** Called after resuming has been checked. This should be probably overriden
     *  to allow for custom specification of constructor arguments
     * @see #autoInit(String[],Class,Class[],String[])
     * @param args As passed to main function
     */
    protected void start(String[] args) {
        try {
            Class clasz = Class.forName(args[0]);
            name = clasz.getSimpleName() + "-" + args[1];
            if (args.length == 3 || args[2].matches("-.*")) {
                autoInit(Arrays.copyOfRange(args, 2, args.length), clasz);
            } else {
                Vector<Class> params = new Vector<Class>();
                Vector<String> names = new Vector<String>();
                int i;
                for (i = 2; i < args.length; i += 2) {
                    if (args[i].matches("-.*")) {
                        break;
                    }
                    params.add(Class.forName(args[i]));
                    names.add(args[i + 1]);
                }
                String[] newArgs;
                if (i >= args.length) {
                    newArgs = new String[0];
                } else {
                    newArgs = Arrays.copyOfRange(args, i, args.length);
                }
                
                autoInit(newArgs, clasz, (Class[]) params.toArray(), (String[]) names.toArray());
            }
        } catch (ClassNotFoundException x) {
            throw new RuntimeException(x.getMessage());
        } catch(GetOptsException x) {
            System.err.println(x.getMessage());
            System.exit(-1);
        }
    }
    
    private HashMap<Class,Method> registeredLoaders = new HashMap<Class,Method>();
    /** If a class can't be loaded normally register a special loader. For example
     *  if the class is a interface
     * @param clasz The class type that requires a special loader
     * @param method The method which loads it (should be static returning a 
     *  object of type clasz, taking a single string as its parameter)
     */
    protected void registerLoader(Class clasz, Method method) {
        if(method.getReturnType() != clasz)
            throw new IllegalArgumentException("Return type not of type clasz");
        registeredLoaders.put(clasz, method);
    }

    /** Make a reasonable guess at how the command line for this process should be. Basically it assumes
     * that there is only one constructor whose names are given by the class name, then continues as
     * autoInit(String[],Class,Class[],String[]) */
    protected void autoInit(String[] args, Class clasz) throws GetOptsException {
        Constructor constructor = clasz.getConstructors()[0];
        Class[] constructorParams = constructor.getParameterTypes();
        String[] constructorNames = new String[constructorParams.length];
        HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
        for (int i = 0; i < constructorParams.length; i++) {
            String s = constructorParams[i].getSimpleName();
            if (classCounts.containsKey(s)) {
                constructorNames[i] = Strings.toLCFirst(s) + classCounts.get(s);
                classCounts.put(s, classCounts.get(s) + 1);
            } else {
                constructorNames[i] = Strings.toLCFirst(s);
                classCounts.put(s, 2);
            }
        }
            autoInit(args, clasz, constructorParams, constructorNames);
        
    }

    /** A slightly more detailed autoInit. You choose a constructor and a name for all of its parameters, it then
     * assumes that every settable property of the object is valid.
     * @param args As passed to main
     * @param clasz The class to construct an object (must be a sub class of AlloeProcess
     * @param constructorParams The classes of the constructor to use to construct the object
     * @param constructorNames The names of each of the parameters as they appear in args
     * @throws IllegalArgumentException Clasz is not a subclass of AlloeProcess 
     * @throws IllegalArgumentException A valid constructor does not exists matching constructorParams
     * @throws IllegalArgumentException constructorNames and constructorParams not of same length
     * @throws GetOptsException GetOpts could not parse args
     * @throws RuntimeException There was some exception in loading the object
     * @see nii.alloe.tools.getopts.GetOpts
     */
    protected void autoInit(String[] args, Class clasz, Class[] constructorParams, String[] constructorNames) throws GetOptsException {
        if (!AlloeProcess.class.isAssignableFrom(clasz)) {
            throw new IllegalArgumentException("Class must be a sub class of AlloeProcess");
        }
        Constructor constructor;
        try {
            constructor = clasz.getConstructor(constructorParams);
        } catch (NoSuchMethodException x) {
            throw new IllegalArgumentException("No constructor matching passed arguments");
        }
        GetOpts getOpts = new GetOpts();

        if(constructorNames.length != constructorParams.length) {
            throw new IllegalArgumentException("constructorNames and constructorParams not of same length");
        }
        for (int i = 0; i < constructorParams.length; i++) {
            if(registeredLoaders.get(constructorParams[i]) == null) {
                getOpts.addArgumentAuto(constructorNames[i], constructorParams[i], true);
            } else {
                getOpts.addLoadableArgument(constructorNames[i], 
                        registeredLoaders.get(constructorParams[i]), true);
            }
        }
        String[] cn2 = Arrays.copyOf(constructorNames, constructorNames.length);
        Arrays.sort(cn2);
        HashSet<String> optionalParameters = new HashSet<String>();
        for (Method method : clasz.getMethods()) {
            if (method.getParameterTypes().length != 1) {
                continue;
            }
            Matcher matcher = Pattern.compile("set(.*)").matcher(method.getName());
            if(!matcher.matches())
                continue;
            String s = Strings.toLCFirst(matcher.group(1));
            if (matcher.matches() && Arrays.binarySearch(cn2, s) < 0) {
                if(registeredLoaders.get(method.getParameterTypes()[0]) == null) {
                    getOpts.addArgumentAuto(s, method.getParameterTypes()[0], false);
                } else {
                    getOpts.addLoadableArgument(s, registeredLoaders.get(method.getParameterTypes()[0]), false);
                }
                optionalParameters.add(s);
            }
        }
        getOpts.getOpts(args);
        Object[] constObjs = new Object[constructorParams.length];
        for (int i = 0; i < constructorNames.length; i++) {
            constObjs[i] = getOpts.getArgument(constructorNames[i]);
        }
        try {
            process = (AlloeProcess) constructor.newInstance(constObjs);

            for (String s : optionalParameters) {
                Object[] objects = new Object[1];
                objects[0] = getOpts.getArgument(s);
                if (objects[0] == null) {
                    continue;
                }
                Class[] classes = new Class[1];
                classes[0] = getOpts.getArgumentClass(s);
                Method method = clasz.getMethod("set" + Strings.toUCFirst(s), classes);
                method.invoke(process, objects);
            }
        } catch (Exception x) {
            throw new RuntimeException(x.getMessage());
        }
    }

    protected void autoWriteObject(String fileName, Object obj) throws IllegalArgumentException, IOException {
        if (obj instanceof Serializable) {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
            oos.writeObject(obj);
            oos.close();
        } else {
            Method method = null;
            try {
                method = obj.getClass().getMethod("write", new Class[0]);
            } catch (NoSuchMethodException x1) {

                try {
                    method = obj.getClass().getMethod("write" + obj.getClass().getSimpleName(), new Class[0]);
                } catch (NoSuchMethodException x2) {

                    try {
                        method = obj.getClass().getMethod("save", new Class[0]);
                    } catch (NoSuchMethodException x3) {

                        try {
                            method = obj.getClass().getMethod("save" + obj.getClass().getSimpleName(), new Class[0]);
                        } catch (NoSuchMethodException x4) {
                            throw new IllegalArgumentException("No suitable write method");
                        }
                    }
                }
            }
            try {
                method.invoke(obj, new Object[0]);
            } catch (Exception x) {
                throw new RuntimeException(x.getMessage());
            }
        }
    }
}
