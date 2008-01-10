package nii.alloe.theory;

/**
 * Used by {@link Rule#forAllAssignments(Model, AssignmentAction)}.
 *
 * @author John McCrae, National Institute of Informatics
 */
public interface AssignmentAction {
    /**
     * @param r The rule
     * @param i The term
     * @param j Assignment to term lhs
     * @param k Assignment to term rhs
     */
    public boolean action(Rule r, int i, int j, int k);
}
