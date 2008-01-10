package nii.alloe.corpus;

/**
 * Action to be performed on each term pair. 
 *
 * @see TermPairSet#forEachPair(EachTermPairAction)
 *
 * @author John McCrae, National Institute of Informatics
 */
public interface EachTermPairAction {
    public void doAction(String term1, String term2);
}
