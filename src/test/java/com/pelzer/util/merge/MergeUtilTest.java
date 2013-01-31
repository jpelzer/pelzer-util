package com.pelzer.util.merge;

import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

public class MergeUtilTest extends TestCase {

  private class StubComparator implements LogicalComparator<String, Integer> {
    public boolean equivalent(final String type, final Integer xmlType) {
      return type.equals(xmlType.toString());
    }
  }

  private final LogicalComparator<String, Integer> compare = new StubComparator();

  private List<String> beans1;
  private List<Integer> beans2;

  public void testMerge() {
    beans1 = Arrays.asList("1", "2", "3");
    beans2 = Arrays.asList(new Integer(1), new Integer(2), new Integer(3));
    final MergeResult<String, Integer> result = MergeUtil.merge(beans1, beans2, compare);

    assertTrue("Passed array size has changed", beans1.size() == 3);
    assertTrue("Passed array size has changed", beans2.size() == 3);
    assertTrue("Wrong number of matched objects: " + result.getMatched().size(), result.getMatched().size() == 3);
    assertTrue("Wrong number of created objects: " + result.getCreate().size(), result.getCreate().size() == 0);
    assertTrue("Wrong number of deleted objects: " + result.getDelete().size(), result.getDelete().size() == 0);
  }

  public void testMergeWithDefaultComparator() {
    beans1 = Arrays.asList("1", "2", "3");
    final List<String> beans3 = Arrays.asList("2", "3", "1");
    final MergeResult<String, String> result = MergeUtil.merge(beans1, beans3, null);

    assertTrue("Passed array size has changed", beans1.size() == 3);
    assertTrue("Passed array size has changed", beans3.size() == 3);
    assertTrue("Wrong number of matched objects: " + result.getMatched().size(), result.getMatched().size() == 3);
    assertTrue("Wrong number of created objects: " + result.getCreate().size(), result.getCreate().size() == 0);
    assertTrue("Wrong number of deleted objects: " + result.getDelete().size(), result.getDelete().size() == 0);
  }

  public void testMergeOutOfOrder() {
    beans1 = Arrays.asList("3", "2", "1");
    beans2 = Arrays.asList(new Integer(1), new Integer(2), new Integer(3));
    final MergeResult<String, Integer> result = MergeUtil.merge(beans1, beans2, compare);

    assertTrue("Passed array size has changed", beans1.size() == 3);
    assertTrue("Passed array size has changed", beans2.size() == 3);
    assertTrue("Wrong number of matched objects: " + result.getMatched().size(), result.getMatched().size() == 3);
    assertTrue("Incorrect object created", result.getCreate().size() == 0);
    assertTrue("Wrong number of deleted objects: " + result.getDelete().size(), result.getDelete().size() == 0);
  }

  public void testMergeWithDelete() {
    beans1 = Arrays.asList("1", "2", "3", "4");
    beans2 = Arrays.asList(new Integer(1), new Integer(2), new Integer(3));
    final MergeResult<String, Integer> result = MergeUtil.merge(beans1, beans2, compare);

    assertTrue("Passed array size has changed", beans1.size() == 4);
    assertTrue("Passed array size has changed", beans2.size() == 3);
    assertTrue("Wrong number of matched objects: " + result.getMatched().size(), result.getMatched().size() == 3);
    assertTrue("Wrong number of created objects: " + result.getCreate().size(), result.getCreate().size() == 0);
    assertTrue("Wrong number of deleted objects: " + result.getDelete().size(), result.getDelete().size() == 1);
    assertTrue("Incorrect object deleted", result.getDelete().contains("4"));
  }

  public void testMergeWithCreate() {
    beans1 = Arrays.asList("1", "2");
    beans2 = Arrays.asList(new Integer(3), new Integer(2), new Integer(1));
    final MergeResult<String, Integer> result = MergeUtil.merge(beans1, beans2, compare);

    assertTrue("Passed array size has changed", beans1.size() == 2);
    assertTrue("Passed array size has changed", beans2.size() == 3);
    assertTrue("Wrong number of matched objects: " + result.getMatched().size(), result.getMatched().size() == 2);
    assertTrue("Wrong number of created objects: " + result.getCreate().size(), result.getCreate().size() == 1);
    assertTrue("Wrong number of deleted objects: " + result.getDelete().size(), result.getDelete().size() == 0);
    assertTrue("Incorrect object created", result.getCreate().contains(new Integer(3)));
  }

  public void testMergeWithCreateAndDelete() {
    beans1 = Arrays.asList("1", "2", "3");
    beans2 = Arrays.asList(new Integer(1), new Integer(4), new Integer(2));
    final MergeResult<String, Integer> result = MergeUtil.merge(beans1, beans2, compare);

    assertTrue("Passed array size has changed", beans1.size() == 3);
    assertTrue("Passed array size has changed", beans2.size() == 3);
    assertTrue("Wrong number of matched objects: " + result.getMatched().size(), result.getMatched().size() == 2);
    assertTrue("Wrong number of created objects: " + result.getCreate().size(), result.getCreate().size() == 1);
    assertTrue("Wrong number of deleted objects: " + result.getDelete().size(), result.getDelete().size() == 1);
    assertTrue("Incorrect object created", result.getCreate().contains(new Integer(4)));
    assertTrue("Incorrect object deleted", result.getDelete().contains("3"));
  }

  public void testEdgeEmptyMerge() {
    beans1 = Arrays.asList();
    beans2 = Arrays.asList();
    MergeResult<String, Integer> result = MergeUtil.merge(beans1, beans2, compare);

    assertTrue("Passed array size has changed", beans1.size() == 0);
    assertTrue("Passed array size has changed", beans2.size() == 0);
    assertTrue("Wrong number of matched objects: " + result.getMatched().size(), result.getMatched().size() == 0);
    assertTrue("Wrong number of created objects: " + result.getCreate().size(), result.getCreate().size() == 0);
    assertTrue("Wrong number of deleted objects: " + result.getDelete().size(), result.getDelete().size() == 0);

    beans1 = Arrays.asList("1", "2", "3");
    beans2 = Arrays.asList();
    result = MergeUtil.merge(beans1, beans2, compare);
    assertTrue("Passed array size has changed", beans1.size() == 3);
    assertTrue("Passed array size has changed", beans2.size() == 0);
    assertTrue("Wrong number of matched objects: " + result.getMatched().size(), result.getMatched().size() == 0);
    assertTrue("Wrong number of created objects: " + result.getCreate().size(), result.getCreate().size() == 0);
    assertTrue("Wrong number of deleted objects: " + result.getDelete().size(), result.getDelete().size() == 3);

    beans1 = Arrays.asList();
    beans2 = Arrays.asList(new Integer(3), new Integer(2), new Integer(1));
    result = MergeUtil.merge(beans1, beans2, compare);
    assertTrue("Passed array size has changed", beans1.size() == 0);
    assertTrue("Passed array size has changed", beans2.size() == 3);
    assertTrue("Wrong number of matched objects: " + result.getMatched().size(), result.getMatched().size() == 0);
    assertTrue("Wrong number of deleted objects: " + result.getDelete().size(), result.getDelete().size() == 0);
    assertTrue("Wrong number of created objects: " + result.getCreate().size(), result.getCreate().size() == 3);
  }

}