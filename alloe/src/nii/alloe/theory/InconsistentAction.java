package nii.alloe.theory;
import java.util.*;
/**
 * Used by {@link Logic#consistCheck(Model,InconsistentAction)}.
 *
 * @author John McCrae, National Institute of Informatics
 */
public interface InconsistentAction {
    /**
     * This action will be called when a inconsistency in the model is found
     * @param logic The logic object for this inconsistency
     * @param m The model
     * @param rule The specific rule with non-functional assignments
     * @return true if this rule has changed the model to make this rule consistent
     */
    public boolean doAction(Logic logic, 
                            Model m,
                            Rule rule);
}
