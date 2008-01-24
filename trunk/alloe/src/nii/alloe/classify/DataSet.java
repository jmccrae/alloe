package nii.alloe.classify;
import nii.alloe.theory.Logic;
import nii.alloe.theory.Model;
import nii.alloe.theory.ProbabilityGraph;
import nii.alloe.theory.SpecificGraph;
import nii.alloe.corpus.*;
import weka.core.*;
import weka.classifiers.*;
import java.util.*;
import weka.classifiers.*;
import weka.classifiers.functions.*;
import java.io.*;

/**
 * Data Set of relations with all the WEKA instances.
 *
 * After initialization all relations should be added with prepRelation, then all instances for 
 * which there is data they should be added using addInstance (the correctness of this relation,
 * if known, should be set as the last element (0 or 1)). Any positive pairs for which there is
 * no data should be included with addNonOccInstance (all term pairs not found are assumed to be 
 * negative). Any terms (or preferably all) which have no data should then be added with addTerms.
 *
 * If this dataSet is a training set classifiers can be obtained with buildClassifierSet. For
 * test/actual sets the posterior probabilities can be gained with buildProbModel. If true/false
 * data has been input into the model it can be recovered with buildTrueModel
 *
 * @author John McCrae, National Institute of Informatics
 */
public class DataSet implements Serializable {
    public Map<String,Instances> instances;
    private Set<String> trainingSets;
    Map<String,Vector<String>> terms;
    Map<String,Vector<String>> nonOccTerms;
    TermList termSet;
    private FastVector classVec;
    static final String glue = " => ";
    
    /** Create a new instance */
    public DataSet(TermList termList) {
        instances = new HashMap<String,Instances>();
        terms = new HashMap<String,Vector<String>>();
        nonOccTerms = new HashMap<String,Vector<String>>();
        termSet = termList;
        trainingSets = new HashSet<String>();
    }
    
    /**
     * Add a new relation with a given set of attributes
     */
    public void prepRelation(String relation, Iterator<String> attNames) {
        FastVector fv = new FastVector();
        while(attNames.hasNext()) {
            fv.addElement(new Attribute(attNames.next()));
        }
        classVec = new FastVector();
        classVec.addElement("0");
        classVec.addElement("1");
        fv.addElement(new Attribute("class",classVec));
        instances.put(relation, new Instances(relation, fv, 0));
        terms.put(relation, new Vector<String>());
        nonOccTerms.put(relation, new Vector<String>());
    }
    
    public double getClassVal(boolean clasz) {
        if(clasz)
            return classVec.indexOf("1");
        else
            return classVec.indexOf("0");
    }
    
    /**
     * Check if prepRelation has been called
     */
    public boolean isRelationPrepared(String relation) {
        return terms.get(relation) != null;
    }
    
    /**
     * Add a new instance to the data set
     * @throws IllegalArgumentException if prepRelation has not been called for this relation
     */
    public void addInstance(Instance i, String relation, String term1, String term2) {
        if(instances.get(relation) == null || !termSet.contains(term1) || !termSet.contains(term2))
            throw new IllegalArgumentException();
        instances.get(relation).add(i);
        terms.get(relation).add(term1 + glue + term2);
    }
    
    /**
     * Include this value as positive even though it has zero data
     * @throws IllegalArgumentException if prepRelation has not been called for this relation
     */
    public void addNonOccInstance(String relation, String term1, String term2) {
        if(nonOccTerms.get(relation) == null)
            throw new IllegalArgumentException();
        nonOccTerms.get(relation).add(term1 + glue + term2);
        termSet.add(term1);
        termSet.add(term2);
    }
    
    /**
     * Register a term which does not occur, it is advised to do this for the entire term set
     */
    public void addTerm(String term) {
        termSet.add(term);
    }
    
    /**
     * Build a classifier set for this dataset (SMO Regression classifiers are created),
     * the last value is assumed to be a class value
     */
    public Map<String,Classifier> buildClassifierSet() {
        
        return buildClassifierSet(new SMO());
    }
    
    /**
     * Build a classifier set for this dataset
     * @param classif The classifier to use as base classifier
     */
    public Map<String,Classifier> buildClassifierSet(Classifier classif) {
        Iterator<String> relationIter = instances.keySet().iterator();
        Map<String,Classifier> rval = new TreeMap<String,Classifier>();
        while(relationIter.hasNext()) {
            String relation = relationIter.next();
            Instances is = instances.get(relation);
            is.setClassIndex(is.numInstances() - 1);
            try {
                Classifier c = Classifier.makeCopy(classif);
                c.buildClassifier(is);
                rval.put(relation,c);
            } catch(Exception x) {
                x.printStackTrace();
            }
        }
        return rval;
    }
    

    
    /**
     * Build a specific model that represents the true data
     */
    public Model buildTrueModel(Logic logic) {
        Iterator<String> relationIter = instances.keySet().iterator();
        Vector<String> termToNum = new Vector<String>(termSet);
        Model rval = new Model(termSet.size());
        rval.addBasicGraphs(logic);
        while(relationIter.hasNext()) {
            String relation = relationIter.next();
            Instances is = instances.get(relation);
            
            SpecificGraph sg = rval.addSpecificGraph(relation);
            Vector<String> termList = terms.get(relation);
            
            for(int i = 0; i < is.numInstances(); i++) {
                if(is.instance(i).value(is.numAttributes() - 1) == 1) {
                    String s = termList.get(i);
                    String []ss = s.split(glue);
                    sg.add(termToNum.indexOf(ss[0]),
                            termToNum.indexOf(ss[1]));
                }
            }
            
            termList = nonOccTerms.get(relation);
            for(int i = 0; i < termList.size(); i++) {
                String s = termList.get(i);
                String []ss = s.split(glue);
                sg.add(termToNum.indexOf(ss[0]),
                        termToNum.indexOf(ss[1]));
                
            }
        }
        
        return rval;
    }
    
    public boolean isTraining(String relation) {
        return trainingSets.contains(relation);
    }
    
    public void setTraining(String relation, boolean val) {
        if(val)
            trainingSets.add(relation);
        else
            trainingSets.remove(relation);
    }
}