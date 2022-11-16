package org.apache.ibatis.algorithm;

public interface rbtree_node {
  boolean compare_nodes(Object other);

  rbtree_node get_parent();

  boolean set_parent(rbtree_node parent);

  rbtree_node get_leftchild();

  rbtree_node get_rightchild();

  boolean set_leftchild(rbtree_node left);

  boolean set_rightchild(rbtree_node right);

}
