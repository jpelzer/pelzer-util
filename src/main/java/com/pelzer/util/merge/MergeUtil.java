package com.pelzer.util.merge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.pelzer.util.Logging;

/**
 * This class handles merging of lists of objects by sorting lists into three
 * collections of elements: matched, create, and delete.
 * 
 * @see MergePair
 * @see MergeResult
 */
public final class MergeUtil {
  private static Logging.Logger log = Logging.getLogger(MergeUtil.class);
  
  private MergeUtil() {
  }
  
  /**
   * Diffs the two collections and returns three collections of objects:
   * matched, delete, create
   * <p>
   * The passed in comparator will be consulted to determine if there is a
   * match. If the comparator is null then the object from the first list will
   * be consulted using its equals() method.
   */
  public static <A, B, C extends LogicalComparator<A, B>> MergeResult<A, B> merge(final Collection<A> aBeans, final Collection<B> bBeans, final C compare) {
    if (aBeans == null || bBeans == null)
      throw new IllegalArgumentException("list parameters cannot be null: " + aBeans + bBeans);
    // log
    for (final A aBean : aBeans) {
      log.debug("Merging A: " + aBean.getClass().getSimpleName());
      break;
    }
    
    // log
    for (final B bBean : bBeans) {
      log.debug("Merging B: " + bBean.getClass().getSimpleName());
      break;
    }
    
    log.debug("Old total: " + aBeans.size() + "   Target new total: " + bBeans.size());
    final List<A> removed = new ArrayList<A>();
    final List<B> create = new ArrayList<B>(bBeans);
    final List<MergePair<A, B>> matched = new ArrayList<MergePair<A, B>>();
    
    for (final A aBean : aBeans) {
      boolean found = false;
      for (final B bBean : bBeans)
        if (compare == null ? aBean.equals(bBean) : compare.equivalent(aBean, bBean)) {
          matched.add(new MergePair<A, B>(aBean, bBean));
          create.remove(bBean);
          found = true;
          break;
        }
      if (!found)
        removed.add(aBean);
    }
    log.debug("New total: " + bBeans.size() + "   matched: " + matched.size() + "   delete: " + removed.size() + "   create: " + create.size());
    return new MergeResult<A, B>(removed, create, matched);
  }
  
}
