package com.pelzer.util.merge;

/**
 * Represents a pair of objects, used during the merge process.
 */
public class MergePair<A, B> {
  private final A aBean;
  private final B bBean;

  public MergePair(final A bean, final B bean2) {
    super();
    aBean = bean;
    bBean = bean2;
  }

  public A getABean() {
    return aBean;
  }

  public B getBBean() {
    return bBean;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof MergePair))
      return false;
    @SuppressWarnings("rawtypes")
    final MergePair other = (MergePair) obj;
    return ((other.getABean() == getABean()) && (other.getBBean() == getBBean()));
  }

  @Override
  public int hashCode() {
    return aBean.hashCode() + bBean.hashCode();
  }
}