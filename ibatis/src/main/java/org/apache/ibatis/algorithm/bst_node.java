package org.apache.ibatis.algorithm;

public class bst_node implements rbtree_node {
  private rbtree_node parent;
  private rbtree_node left;
  private rbtree_node right;
  private long node_id;
  private long key;
  private long value;

  public bst_node() {
    parent = null;
    left = null;
    right = null;
    key = 0;
    value = 0;
  }

  @Override
  public boolean compare_nodes(Object other) {
    return !(this.equals(other));
  }

  @Override
  public rbtree_node get_parent() {
    return this.parent;
  }

  @Override
  public boolean set_parent(rbtree_node parent) {
    if (parent == null) {
      return false;
    }
    this.parent = parent;
    return true;
  }

  @Override
  public rbtree_node get_leftchild() {
    return left;
  }

  @Override
  public rbtree_node get_rightchild() {
    return right;
  }

  @Override
  public boolean set_leftchild(rbtree_node left) {
    if (left == null) {
      return false;
    }
    this.left = left;
    return true;
  }

  @Override
  public boolean set_rightchild(rbtree_node right) {
    if (right == null) {
      return false;
    }
    this.right = right;
    return true;
  }


}
