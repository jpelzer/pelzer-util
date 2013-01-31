package com.pelzer.util.merge;

/**
 * <p>
 * Indicates that a class can discern the same logical object from two different domains.
 * <p>
 * For example, an XML product bean and a Product persistence bean represent the same idea if the xml has an attribute
 * that matches the Product's key.
 */
public interface LogicalComparator<T, X> {
  /**
   * @return true if the two objects represent the same logical concept
   */
  boolean equivalent(T type, X xmlType);
}
