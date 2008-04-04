package nii.alloe.tools.process;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;
import nii.alloe.tools.getopts.GetOpts;
import nii.alloe.tools.getopts.GetOptsException;
import nii.alloe.tools.strings.Strings;

/**
 * A wrapper to allow for processes to be ran in the terminal. The default operation should allow for nearly any process
 * to be started
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
                if(newProgress - lastProgress >= MIN_PROG_STEP) {
                    System.out.print("\r" + process.getStateMessage() + ": " + (newProgress * 100.0) + "%");
                    lastProgress = newProgress;
                }
            }
        });
        Thread t = new Thread(new Runnable() {
            public void run() {
                if(process instanceof Serializable) {
                    try {
                        process.pause();
                        Random r = new Random();
                        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getName() + r.nextInt()));
                        oos.writeObject(process);
                        oos.close();
                    } catch(Exception x) {
                        x.printStackTrace();
                    }
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(t);
        if(init)
            process.start();
        else
            process.resume();
    }
    
    
    protected String name;
    /** Set to create a name for this particular process. This will be used to dump results if necessary */
    public String getName() {
        return name;
    }
    
    /** Do everything at the end of execution. Eg saving the result */
    public void finish() {
        for(Field f : process.getClass().getFields()) {
            try {
                autoWriteObject(name + "." + f.getName(),f.get(process));
            } catch(Exception x) {
                x.printStackTrace();
            }
        }
    }
    
    /** Prepare the execution. Eg loading all data and instantiating the process */
    public boolean init(String[] args) {
        try {
            if(args.length < 3) {
                System.err.println("Usage: java " + this.getClass().getName() + " className runName [Constructor details] classParams");
            }
            if(args[0].equals("-resume")) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[1]));
                    process = (AlloeProcess)ois.readObject();
                    ois.close();
                } catch(IOException x) {
                    x.printStackTrace();
                    System.exit(-1);
                } catch(ClassNotFoundException x) {
                    x.printStackTrace();
                    System.exit(-1);
                }
                return false;
            } else {
                Class clasz = Class.forName(args[0]);
                name = clasz.getSimpleName() + "-" + args[1];
                if(args.length == 3 || args[2].matches("-.*")) {
                    autoInit(Arrays.copyOfRange(args,2,args.length),clasz);
                } else {
                    Vector<Class> params = new Vector<Class>();
                    Vector<String> names = new Vector<String>();
                    int i;
                    for(i = 2; i < args.length; i+= 2) {
                        if(args[i].matches("-.*"))
                            break;
                        params.add(Class.forName(args[i]));
                        names.add(args[i+1]);
                    }
                    String[] newArgs;
                    if(i >= args.length)
                        newArgs = new String[0];
                    else
                        newArgs = Arrays.copyOfRange(args,i,args.length);
                    autoInit(newArgs,clasz,(Class[])params.toArray(),(String[])names.toArray());
                }
                return true;
            }
        } catch(ClassNotFoundException x) {
            throw new RuntimeException(x.getMessage());
        }
    }
    
    /** Make a reasonable guess at how the command line for this process should be. Basically it assumes
     * that there is only one constructor whose names are given by the class name, then continues as
     * autoInit(String[],Class,Class[],String[]) */
    protected void autoInit(String[] args, Class clasz) {
        Constructor constructor = clasz.getConstructors()[0];
        Class [] constructorParams = constructor.getParameterTypes();
        String []constructorNames = new String[constructorParams.length];
        HashMap<String,Integer> classCounts = new HashMap<String,Integer>();
        for(int i = 0; i < constructorParams.length; i++) {
            String s  = constructorParams[i].getSimpleName();
            if(classCounts.containsKey(s)) {
                constructorNames[i] = Strings.toLCFirst(s) + classCounts.get(s);
                classCounts.put(s,classCounts.get(s) + 1);
            } else {
                constructorNames[i] = Strings.toLCFirst(s);
                classCounts.put(s,2);
            }
        }
        autoInit(args,clasz,constructorParams,constructorNames);
    }
    
    /** A slightly more detailed autoInit. You choose a constructor and a name for all of its parameters, it then
     * assumes that every settable property of the object is valid.
     * @throws IllegalArgumentException For so many reasons, you may as well just cross your fingers and pray */
    protected void autoInit(String[] args, Class clasz, Class[] constructorParams, String[] constructorNames) {
        if(!AlloeProcess.class.isAssignableFrom(clasz)) {
            throw new IllegalArgumentException("Class must be a sub class of AlloeProcess");
        }
        Constructor constructor;
        try {
            constructor = clasz.getConstructor(constructorParams);
        } catch(NoSuchMethodException x) {
            throw new IllegalArgumentException("No constructor matching passed arguments");
        }
        GetOpts getOpts = new GetOpts();
        
        for(int i = 0; i < constructorParams.length; i++) {
            getOpts.addArgumentAuto(constructorNames[i],constructorParams[i],true);
        }
        String []cn2 = Arrays.copyOf(constructorNames,constructorNames.length);
        Arrays.sort(cn2);
        HashSet<String> optionalParameters = new HashSet<String>();
        for(Method method : clasz.getMethods()) {
            if(method.getParameterTypes().length != 1)
                continue;
            Matcher matcher = Pattern.compile("set(.*)").matcher(method.getName());
            String s = Strings.toLCFirst(matcher.group(1));
            if(matcher.matches() && Arrays.binarySearch(cn2,s) < 0) {
                getOpts.addArgumentAuto(s,method.getParameterTypes()[0],false);
                optionalParameters.add(s);
            }
        }
        try {
            getOpts.getOpts(args);
        } catch(GetOptsException x) {
            x.printStackTrace();
            throw new IllegalArgumentException();
        }
        Object []constObjs = new Object[constructorParams.length];
        for(int i = 0; i < constructorNames.length; i++) {
            constObjs[i] = getOpts.getArgument(constructorNames[i]);
        }
        try {
            process = (AlloeProcess)constructor.newInstance(constObjs);
            
            for(String s : optionalParameters) {
                Object[] objects = new Object[1];
                objects[0] = getOpts.getArgument(s);
                if(objects[0] == null)
                    continue;
                Class[] classes = new Class[1];
                classes[0] = getOpts.getArgumentClass(s);
                Method method = clasz.getMethod("set" + Strings.toUCFirst(s),classes);
                method.invoke(process,objects);
            }
        } catch(Exception x) {
            throw new RuntimeException(x.getMessage());
        }
    }
    
    protected void autoWriteObject(String fileName, Object obj) throws IllegalArgumentException, IOException {
        if(obj instanceof Serializable) {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
            oos.writeObject(obj);
            oos.close();
        } else {
            Method method = null;
            try {
                method = obj.getClass().getMethod("write",new Class[0]);
            } catch(NoSuchMethodException x1) {
                
                try {
                    method = obj.getClass().getMethod("write" + obj.getClass().getSimpleName(),new Class[0]);
                } catch(NoSuchMethodException x2) {
                    
                    try {
                        method = obj.getClass().getMethod("save",new Class[0]);
                    } catch(NoSuchMethodException x3) {
                        
                        try {
                            method = obj.getClass().getMethod("save" + obj.getClass().getSimpleName(),new Class[0]);
                        } catch(NoSuchMethodException x4) {
                            throw new IllegalArgumentException("No suitable write method");
                        }
                    }
                }
            }
            try {
                method.invoke(obj,new Object[0]);
            } catch(Exception x) {
                throw new RuntimeException(x.getMessage());
            }
        }
    }
}
