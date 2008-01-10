package nii.alloe.classify;
import nii.alloe.theory.Logic;
import nii.alloe.theory.Model;
import nii.alloe.theory.ProbabilityGraph;
import nii.alloe.theory.SpecificGraph;
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
    private Map<String,Vector<String>> terms;
    private Map<String,Vector<String>> nonOccTerms;
    private Set<String> termSet;
    static final String glue = " => ";
    
    /**
     * Add a new relation with a given set of attributes
     */
    public void prepRelation(String relation, Iterator<String> attNames) {
        FastVector fv = new FastVector();
        while(attNames.hasNext()) {
            fv.addElement(new Attribute(attNames.next()));
        }
        instances.put(relation, new Instances(relation, fv, 0));
        terms.put(relation, new Vector<String>());
        nonOccTerms.put(relation, new Vector<String>());
    }
    
    /**
     * Add a new instance to the data set
     * @throws IllegalArgumentException if prepRelation has not been called for this relation
     */
    public void addInstance(Instance i, String relation, String term1, String term2) {
        if(instances.get(relation) == null)
            throw new IllegalArgumentException();
        instances.get(relation).add(i);
        terms.get(relation).add(term1 + glue + term2);
        termSet.add(term1);
        termSet.add(term2);
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
     * Build a probability model for this dataset
     * @param classifs Classifiers for each relation see buildClassifierSet
     */
    public Model buildProbModel(Logic logic, Map<String,Classifier> classifs) {
        Iterator<String> relationIter = instances.keySet().iterator();
        Vector<String> termToNum = new Vector<String>(termSet);
        Model rval = new Model(termSet.size());
        rval.addBasicGraphs(logic);
        while(relationIter.hasNext()) {
            String relation = relationIter.next();
            Instances is = instances.get(relation);
            is.setClassIndex(is.numInstances() - 1);
            ProbabilityGraph pg = rval.addProbabilityGraph(relation);
            
            SparseInstance zeroVec = new SparseInstance(1,new double[is.numAttributes()]);
            Classifier classif = classifs.get(relation);
            try {
                double[] dist;
                Vector<String> termList = terms.get(relation);
                
                dist = classif.distributionForInstance(zeroVec);
                pg.setBaseVal(dist[0]);
                
                for(int i = 0; i < is.numInstances(); i++) {
                    String s = termList.get(i);
                    String []ss = s.split(glue);
                    dist = classif.distributionForInstance(is.instance(i));
                    pg.setPosVal(termToNum.indexOf(ss[0]), termToNum.indexOf(ss[1]), dist[0]);
                }
            } catch(Exception x) {
                x.printStackTrace();
                return null;
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
}