package sun.processor.profile;

import java.util.List;

import sun.processor.core.IProfile;
import sun.processor.profile.predicate.BranchPredicate;
import sun.processor.profile.predicate.FloatKindPredicate;
import sun.processor.profile.predicate.ReturnPredicate;
import sun.processor.profile.predicate.ScalarPairPredicate;

/**
 * a predicate should contain the following four sections:
 * 
 * <li>scalar pairs</li>
 * 
 * <li>returns</li>
 * 
 * <li>branches</li>
 * 
 * <li>function entries</li>
 * 
 * @author Chengnian Sun
 * 
 */
public interface IPredicateProfile extends IProfile {

	List<ScalarPairPredicate> getScalarPredicates();

	List<ReturnPredicate> getReturnPredicates();

	List<BranchPredicate> getBranchPredicates();
	
	List<FloatKindPredicate> getFloatKindPredicates();
	
}
