/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nii.alloe.tools.getopts;

import java.io.*;
import java.lang.reflect.Method;
import junit.framework.TestCase;

/**
 *
 * @author john
 */
public class GetOptsTest extends TestCase {

    public GetOptsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of addArgumentAuto method, of class GetOpts.
     */
    public void testAddArgumentAuto() {
        System.out.println("addArgumentAuto");
        String name = "name";
        Class clasz = Boolean.class;
        boolean necessary = false;
        GetOpts instance = new GetOpts();
        instance.addArgumentAuto(name, clasz, necessary);
        assertEquals(instance.getArgumentClass(name), Boolean.class);
    }

    /**
     * Test of addBooleanArgument method, of class GetOpts.
     */
    public void testAddBooleanArgument() {
        System.out.println("addBooleanArgument");
        String name = "name";
        GetOpts instance = new GetOpts();
        instance.addBooleanArgument(name);
        assertEquals(instance.getArgumentClass(name), Boolean.class);
        String[] args = {"-name"};
        try {
            instance.getOpts(args);
        } catch (GetOptsException x) {
            fail(x.getMessage());
        }
        assertEquals(instance.getArgument(name), Boolean.TRUE);
    }

    /**
     * Test of addStringArgument method, of class GetOpts.
     */
    public void testAddStringArgument() {
        System.out.println("addStringArgument");
        String name = "name";
        boolean necessary = false;
        GetOpts instance = new GetOpts();
        instance.addStringArgument(name, necessary);
        assertEquals(instance.getArgumentClass(name), String.class);
        String[] args = {"-name", "name"};
        try {
            instance.getOpts(args);
        } catch (GetOptsException x) {
            fail(x.getMessage());
        }
        assertEquals(instance.getArgument(name), "name");
    }

    /**
     * Test of addIntegerArgument method, of class GetOpts.
     */
    public void testAddIntegerArgument() {
        System.out.println("addIntegerArgument");
        String name = "name";
        boolean necessary = false;
        GetOpts instance = new GetOpts();
        instance.addIntegerArgument(name, necessary);
        assertEquals(instance.getArgumentClass(name), Integer.class);
        String[] args = {"-name", "3"};
        try {
            instance.getOpts(args);
        } catch (GetOptsException x) {
            fail(x.getMessage());
        }
        assertEquals(instance.getArgument(name), 3);
    }

    /**
     * Test of addDoubleArgument method, of class GetOpts.
     */
    public void testAddDoubleArgument() {
        System.out.println("addDoubleArgument");
        String name = "name";
        boolean necessary = false;
        GetOpts instance = new GetOpts();
        instance.addDoubleArgument(name, necessary);
        assertEquals(instance.getArgumentClass(name), Double.class);
        String[] args = {"-name", "3.14"};
        try {
            instance.getOpts(args);
        } catch (GetOptsException x) {
            fail(x.getMessage());
        }
        assertEquals(instance.getArgument(name), 3.14);
    }

    /**
     * Test of addLongArgument method, of class GetOpts.
     */
    public void testAddLongArgument() {
        System.out.println("addLongArgument");
        String name = "name";
        boolean necessary = false;
        GetOpts instance = new GetOpts();
        instance.addLongArgument(name, necessary);
        assertEquals(instance.getArgumentClass(name), Long.class);
        String[] args = {"-name", "543816724863"};
        try {
            instance.getOpts(args);
        } catch (GetOptsException x) {
            fail(x.getMessage());
        }
        assertEquals(instance.getArgument(name), 543816724863l);
    }

    /**
     * Test of addCharArgument method, of class GetOpts.
     */
    public void testAddCharArgument() {
        System.out.println("addCharArgument");
        String name = "name";
        boolean necessary = false;
        GetOpts instance = new GetOpts();
        instance.addCharArgument(name, necessary);
        assertEquals(instance.getArgumentClass(name), Character.class);
        String[] args = {"-name", "c"};
        try {
            instance.getOpts(args);
        } catch (GetOptsException x) {
            fail(x.getMessage());
        }
        assertEquals(instance.getArgument(name), 'c');
    }

    /**
     * Test of addFloatArgument method, of class GetOpts.
     */
    public void testAddFloatArgument() {
        System.out.println("addFloatArgument");
        String name = "name";
        boolean necessary = false;
        GetOpts instance = new GetOpts();
        instance.addFloatArgument(name, necessary);
        assertEquals(instance.getArgumentClass(name), Float.class);
        String[] args = {"-name", "3.14"};
        try {
            instance.getOpts(args);
        } catch (GetOptsException x) {
            fail(x.getMessage());
        }
        assertEquals(instance.getArgument(name), 3.14f);
    }

    /**
     * Test of addSerializableArgument method, of class GetOpts.
     */
    public void testAddSerializableArgument() {
        System.out.println("addSerializableArgument");
        String name = "name";
        Class clasz = String.class;
        boolean necessary = false;
        GetOpts instance = new GetOpts();
        instance.addSerializableArgument(name, clasz, necessary);
        assertEquals(instance.getArgumentClass(name), String.class);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("test.string"));
            oos.writeObject("test");
            oos.close();
        } catch (IOException x) {
            fail("Could not test:" + x.getMessage());
        }
        String[] args = {"-name", "test.string"};
        try {
            instance.getOpts(args);
        } catch (GetOptsException x) {
            fail(x.getMessage());
        } finally {
            File f = new File("test.string");
            f.delete();
        }
        assertEquals(instance.getArgument(name), "test");
    }

    /**
     * Test of addLoadableArgument method, of class GetOpts.
     */
    public void testAddLoadableArgument() {
        System.out.println("addLoadableArgument");
        String name = "name";
        Method method;
        try {
            method = LoadableObject.class.getMethod("read", String.class);
        } catch(NoSuchMethodException x) {
            fail("This shouldn't happen!");
            return;
        }
        boolean necessary = false;
        GetOpts instance = new GetOpts();
        instance.addLoadableArgument(name, method, necessary);
        assertEquals(instance.getArgumentClass(name), LoadableObject.class);
        try {
            (new LoadableObject(6)).write("test.string");
        } catch (IOException x) {
            fail("Could not test:" + x.getMessage());
        }
        String[] args = {"-name", "test.string"};
        try {
            instance.getOpts(args);
        } catch (GetOptsException x) {
            fail(x.getMessage());
        } finally {
            File f = new File("test.string");
            f.delete();
        }
        assertEquals(instance.getArgument(name), new LoadableObject(6));   
    }

    /**
     * Test of getArgumentClass method, of class GetOpts.
     */
    public void testGetArgumentClass() {
        System.out.println("getArgumentClass (no test)");
    }

    /**
     * Test of getOpts method, of class GetOpts.
     */
    public void testGetOpts() throws Exception {
        System.out.println("getOpts (no test)");
    }

    /**
     * Test of getArgument method, of class GetOpts.
     */
    public void testGetArgument() {
        System.out.println("getArgument (no test)");
    }
}

class LoadableObject {

    private final int data;

    public LoadableObject(int data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof LoadableObject) {
            return data == ((LoadableObject) arg0).data;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.data;
        return hash;
    }

    public void write(String fileName) throws IOException {
        PrintStream ps = new PrintStream(fileName);
        ps.println(data);
        ps.close();
    }

    public static LoadableObject read(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        return new LoadableObject(Integer.parseInt(br.readLine()));
    }
}
