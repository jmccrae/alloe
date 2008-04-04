package nii.alloe.classify;
import nii.alloe.theory.*;
import java.io.*;
import java.util.*;
import nii.alloe.tools.process.AlloeProcess;
import nii.alloe.tools.process.AlloeProgressListener;
import nii.alloe.tools.process.CannotPauseException;
import weka.classifiers.*;
import weka.core.*;

/**
 * Process for building probabilistic models
 *
 * @author John McCrae, National Institute of Informatics
 */
public class ProbModelBuilder implements AlloeProcess, Serializable, Runnable {
    private transient LinkedList<AlloeProgressListener> aplListeners;
    private transient Thread theThread;
    private transient int state;
    private static final int STATE_OK = 0;
    private static final int STATE_STOPPING = 1;
    private static final int STATE_UNPAUSEABLE = 2;
    
    /** The result of calling buildProbModel */
    public Model model;
    private Logic logic;
    private DataSet dataSet;
    private Map<String,Classifier> classifs;
    private Map<String,String> dataSetToLogicName;
    
    /** Create a new instance
     * @param logic The logic this model should represent
     * @param dataSet The set of feature vectors to be classified
     * @param classifs The set of classifiers, indexed by dataSet names
     * @param dataSetToLogicName A map from the names of feature vector sets to realtions in the logic
     */
    public ProbModelBuilder(Logic logic, DataSet dataSet, Map<String,Classifier> classifs, Map<String,String> dataSetToLogicName) {
        this.logic = logic;
        this.dataSet = dataSet;
        this.classifs = classifs;
        this.dataSetToLogicName = dataSetToLogicName;
    }
    
    public void setClassifiers(Map<String,Classifier> classifs) {
        this.classifs = classifs;
    }
    
    public void setDataSetToLogicName(Map<String,String> dataSetToLogicName) {
        this.dataSetToLogicName = dataSetToLogicName;
    }
    
    /** Register a progress listener */
    public void addProgressListener(AlloeProgressListener apl) {
        if(aplListeners == null)
            aplListeners = new LinkedList<AlloeProgressListener>();
        if(!aplListeners.contains(apl))
            aplListeners.add(apl);
    }
    private void fireNewProgressChange(double newProgress) {
        Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
        while(apliter.hasNext()) {
            apliter.next().progressChange(newProgress);
        }
    }
    
    private void fireFinished() {
        Iterator<AlloeProgressListener> apliter = aplListeners.iterator();
        while(apliter.hasNext()) {
            apliter.next().finished();
        }
    }
    
    
    /** Start process. It is expected that this function should start the progress
     * in a new thread */
    public void start() {
        theThread = new Thread(this);
        state = STATE_OK;
        theThread.start();
    }
    
    /** Pause the process. The assumption is that this will work by changing a variable
     * in the running thread and then wait for this thread to finish by use of join().
     * It is assumed that the this object is Serializable, otherwise it's your problem
     * to assure the object is ok when resume() is called.
     *
     * @throws CannotPauseException If the process is not in a state where it can be resumed
     */
    public void pause() throws CannotPauseException {
        try {
            if(state == STATE_UNPAUSEABLE)
                throw new CannotPauseException("Some reason");
            state = STATE_STOPPING;
            theThread.join();
        } catch(InterruptedException x) {
            throw new CannotPauseException("The thread was interrupted");
        }
    }
    
    /** Resume the process.
     * @see #pause()
     */
    public void resume() {
        
        theThread = new Thread(this);
        state = STATE_OK;
        theThread.start();
    }
    
    
    /** Get a string representation of the current action being performed */
    public String getStateMessage() { return "Creating Prob Model: "; }

    public void run() {
        buildProbModel();
    }
    
    private String relation;
    private int i;
    double relationCount,instCount;
    
    /**
     * Build a probability model
     */
    public Model buildProbModel() {
        Iterator<String> relationIter = classifs.keySet().iterator();
        final double relationTotal = classifs.keySet().size();
        relationCount = 0;
        Vector<String> termToNum = new Vector<String>(dataSet.termSet);
        model = new Model(dataSet.termSet.size());
        model.addBasicGraphs(logic);
        while(relationIter.hasNext() && state == STATE_OK) {
            relationCount++;
            if(relation != null) {
                while(!relation.equals(relationIter.next()));
            } else {
                relation = relationIter.next();
                i = 0;
            }
            Instances is = dataSet.instances.get(relation);
            double instTotal = is.numInstances();
            is.setClassIndex(is.numInstances() - 1);
            ProbabilityGraph pg = model.addProbabilityGraph(dataSetToLogicName.get(relation));
            
            //Instances zeroVecIs = new Instances(is,1);
            SparseInstance zeroVec = new SparseInstance(1,new double[is.numAttributes()]);
            zeroVec.setDataset(is);
            Classifier classif = classifs.get(relation);
            try {
                double[] dist;
                Vector<String> termList = dataSet.terms.get(relation);
                
                dist = classif.distributionForInstance(zeroVec);
                pg.setBaseVal(dist[0]);
                
                for(; i < is.numInstances() && state == STATE_OK; i++) {
                    String s = termList.get(i);
                    String []ss = s.split(dataSet.glue);
                    dist = classif.distributionForInstance(is.instance(i));
                    pg.setPosVal(termToNum.indexOf(ss[0]), termToNum.indexOf(ss[1]), dist[1]);
                }
                fireNewProgressChange((relationCount / relationTotal) * ((double)i / instTotal)); 
            } catch(Exception x) {
                x.printStackTrace();
                return null;
            }
            relation = null;
        }
        model.addCompulsorys(logic);
        fireFinished();
        return model;
    }
    
}
