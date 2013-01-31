package com.pelzer.util.merge;

import java.util.List;

/**
 * The result of a merge operation.
 *
 * @see MergeUtil
 */
public class MergeResult<A, B> {
  private final List<A> delete;
  private final List<B> create;
  private final List<MergePair<A, B>> matched;

  /**
   * Use {@link MergeUtil} to get a MergeResult.
   *
   * @param delete
   * @param create
   * @param matched
   */
  MergeResult(final List<A> delete, final List<B> create, final List<MergePair<A, B>> matched) {
    super();
    if (delete == null || create == null || matched == null)
      throw new IllegalArgumentException("lists cannot be null");
    this.delete = delete;
    this.create = create;
    this.matched = matched;
  }

  /**
   * @return List of items that were in the B list but not the A list. These should be created in the A list to make it
   *         match the B list
   */
  public List<B> getCreate() {
    return create;
  }

  /**
   * @return List of items that were in both lists.
   */
  public List<MergePair<A, B>> getMatched() {
    return matched;
  }

  /**
   * @return List of items that were in the A list but not the B list. These should be deleted in the A list to make it
   *         match the B list
   */
  public List<A> getDelete() {
    return delete;
  }
}
