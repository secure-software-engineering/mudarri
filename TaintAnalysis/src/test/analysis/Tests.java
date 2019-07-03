package analysis;

import java.util.Set;

class Tests<T> {

  boolean equals(Set<Set<T>> paths1, Set<Set<T>> paths2) {
    if (paths1.size() != paths2.size()) return false;
    for (Set<T> path1 : paths1) {
      if (!contains(paths2, path1)) return false;
    }
    return true;
  }

  private boolean contains(Set<Set<T>> paths2, Set<T> path1) {
    for (Set<T> path2 : paths2) {
      if (eq(path1, path2)) return true;
    }
    return false;
  }

  boolean eq(Set<T> path1, Set<T> path2) {
    if (path1.size() != path2.size()) return false;
    if (!path1.containsAll(path2)) return false;
    return path2.containsAll(path1);
  }
}
