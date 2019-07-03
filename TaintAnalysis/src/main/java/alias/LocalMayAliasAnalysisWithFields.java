package alias;

import marking.Marker;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.ConcreteRef;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Conducts a method-local, equality-based may-alias analysis.
 */
public class LocalMayAliasAnalysisWithFields
  extends ForwardFlowAnalysis<Unit, Set<Set<EquivValue>>> {

  private final Body body;
  // Should behave like a set.
  private List<EquivValue> seen;

  public LocalMayAliasAnalysisWithFields(UnitGraph graph) {
    super(graph);
    seen = new ArrayList<>();
    body = graph.getBody();
    doAnalysis();
  }

  @Override
  protected void flowThrough(Set<Set<EquivValue>> source, Unit unit,
                             Set<Set<EquivValue>> target) {
    // ID
    for (Set<EquivValue> set : source) {
      Set<EquivValue> newSet = new HashSet<>();
      for (EquivValue ev : set)
        newSet.add(ev.deriveWithNewStmt(unit, Marker.ALIAS_ID));
      target.add(newSet);
    }

    // Treat alias.
    if (unit instanceof DefinitionStmt) {
      DefinitionStmt def = (DefinitionStmt) unit;

      boolean leftIsLocalOrConcrete = (def.getLeftOp() instanceof Local ||
        def.getLeftOp() instanceof ConcreteRef);
      boolean rightIsLocalOrConcrete = (def.getRightOp() instanceof Local ||
        def.getRightOp() instanceof ConcreteRef ||
        def.getRightOp() instanceof Constant);

      if (leftIsLocalOrConcrete) {
        EquivValue right = getOrCreate(def.getRightOp(), unit);
        if (right == null) return;
        EquivValue left = getOrCreate(def.getLeftOp(), unit);
        if (left == null) return;

        if (rightIsLocalOrConcrete && !(def.getRightOp() instanceof Constant)) {
          // remove left from its sets
          Set<Set<EquivValue>> leftSets = getSets(target, left);
          removeFromSets(target, leftSets, left);
          // add left into right's sets
          left = right.deriveWithNewValue(def.getLeftOp(), unit,
            Marker.LEFT_ALIAS_TO_RIGHT);
          Set<Set<EquivValue>> rightSets = getSets(target, right);
          addAndMerge(target, rightSets, left);

          // Not totally precise here
          if (left.getField() == null) { // left is local
            // find the sets containing left's children
            Map<Set<EquivValue>, Set<EquivValue>> leftChildrenSets =
              getChildrenSets(target, left.getLocal());
            // remove left children from their sets
            removeChildrenFromSets(target, leftChildrenSets);
            // add left children on their own
            for (Set<EquivValue> children : leftChildrenSets.keySet()) {
              for (EquivValue ev : children) {
                ev = left.deriveWithNewValue(ev.getLocal(), ev.getField(), unit,
                  Marker.PARENT_LEFT_ALIAS_TO_RIGHT);
                target.add(Collections.singleton(ev));
              }
            }
          }
        } else {
          // remove left from its sets
          Set<Set<EquivValue>> leftSets = getSets(target, left);
          removeFromSets(target, leftSets, left);
          // add left on its own
          left = right
            .deriveWithNewValue(def.getLeftOp(), unit, Marker.LEFT_ON_ITS_OWN);
          target.add(Collections.singleton(left));

          if (left.getField() == null) { // left is local.
            // find the sets containing left's children
            Map<Set<EquivValue>, Set<EquivValue>> leftChildrenSets =
              getChildrenSets(target, left.getLocal());
            // remove left children from their sets
            removeChildrenFromSets(target, leftChildrenSets);
            // add left children on their own
            for (Set<EquivValue> children : leftChildrenSets.keySet()) {
              for (EquivValue ev : children) {
                ev = left.deriveWithNewValue(ev.getLocal(), ev.getField(), unit,
                  Marker.PARENT_LEFT_ON_ITS_OWN);
                target.add(Collections.singleton(ev));
              }
            }
          }
        }
      }
    }
  }

  private EquivValue getOrCreate(Value val, Unit u) {
    if (!(val instanceof Local) && !(val instanceof StaticFieldRef) &&
      !(val instanceof ArrayRef) && !(val instanceof InstanceFieldRef))
      return null;
    EquivValue eqVal =
      new EquivValue(val, u, u, EquivValue.zeroAbstraction, Marker.NEW_VALUE);
    if (seen.contains(eqVal)) return seen.get(seen.indexOf(eqVal));
    seen.add(eqVal);
    return eqVal;
  }

  /***** Set manipulation *****/

  private Map<Set<EquivValue>, Set<EquivValue>> getChildrenSets(
    Set<Set<EquivValue>> sets, Local local) {
    Map<Set<EquivValue>, Set<EquivValue>> ret = new HashMap<>();
    for (Set<EquivValue> set : sets) {
      Set<EquivValue> ev = getChildren(set, local);
      if (!ev.isEmpty()) ret.put(ev, set);
    }
    return ret;
  }

  private void removeChildrenFromSets(Set<Set<EquivValue>> target,
                                      Map<Set<EquivValue>, Set<EquivValue>> leftChildrenSets) {
    for (Set<EquivValue> children : leftChildrenSets.keySet()) {
      for (EquivValue ev : children)
        removeFromSet(target, leftChildrenSets.get(children), ev);
    }
  }

  private Set<EquivValue> getChildren(Set<EquivValue> set, Local local) {
    Set<EquivValue> children = new HashSet<>();
    for (EquivValue ev : set) {
      if (ev.getLocal() != null && ev.getField() != null) {
        if (local.equivTo(ev.getLocal())) children.add(ev);
      }
    }
    return children;
  }

  private void mergeSets(Set<Set<EquivValue>> target,
                         Set<Set<EquivValue>> right) {
    for (Set<EquivValue> set : right) {
      Set<EquivValue> tmpSet = new HashSet<>();
      for (EquivValue ev : set) {
        Set<Set<EquivValue>> targetSets = getSets(target, ev);
        for (Set<EquivValue> targetSet : targetSets) {
          tmpSet.addAll(targetSet);
          target.remove(targetSet);
        }
      }
      tmpSet.addAll(set);
      target.add(tmpSet);
    }
  }

  private void addAndMerge(Set<Set<EquivValue>> target,
                           Set<Set<EquivValue>> rightSets, EquivValue left) {
    Set<EquivValue> tmp = new HashSet<>();
    for (Set<EquivValue> s : rightSets) {
      tmp.addAll(s);
      target.remove(s);
    }
    tmp.add(left);
    target.add(tmp);
  }

  private void removeFromSets(Set<Set<EquivValue>> target,
                              Set<Set<EquivValue>> sets, EquivValue value) {
    for (Set<EquivValue> set : sets)
      removeFromSet(target, set, value);
  }

  private void removeFromSet(Set<Set<EquivValue>> target, Set<EquivValue> set,
                             EquivValue value) {
    target.remove(set);
    HashSet<EquivValue> setWithoutVal = new HashSet<>(set);
    removeName(setWithoutVal, value);
    if (!setWithoutVal.isEmpty()) target.add(setWithoutVal);
  }

  private void removeName(Set<EquivValue> set, EquivValue value) {
    EquivValue valInSet = null;
    for (EquivValue val : set)
      if (value.equalsName(val)) valInSet = val;
    set.remove(valInSet);
  }

  private Set<Set<EquivValue>> getSets(Set<Set<EquivValue>> sets,
                                       EquivValue value) {
    Set<Set<EquivValue>> ret = new HashSet<>();
    for (Set<EquivValue> set : sets) {
      if (containsName(set, value)) ret.add(set);
    }
    return ret;
  }

  private boolean containsName(Set<EquivValue> set, EquivValue value) {
    for (EquivValue val : set) {
      if (value.equalsName(val)) return true;
    }
    return false;
  }

  /***** Analysis *****/

  @Override
  protected void copy(Set<Set<EquivValue>> source,
                      Set<Set<EquivValue>> target) {
    target.clear();
    target.addAll(source);
  }

  @Override
  protected Set<Set<EquivValue>> entryInitialFlow() {
    // initially all values only alias themselves
    Set<Set<EquivValue>> res = new HashSet<>();
    for (ValueBox vb : body.getUseAndDefBoxes()) {
      if (vb.getValue() instanceof Local ||
        vb.getValue() instanceof ConcreteRef) {
        res.add(Collections.singleton(getOrCreate(vb.getValue(), null)));
      }
    }
    return res;
  }

  @Override
  protected void merge(Set<Set<EquivValue>> source1,
                       Set<Set<EquivValue>> source2,
                       Set<Set<EquivValue>> target) {
    // we could instead also merge all sets that are non-disjoint
    target.clear();
    target.addAll(source1);
    mergeSets(target, source2);
  }

  @Override
  protected Set<Set<EquivValue>> newInitialFlow() {
    return new HashSet<>();
  }

  /***** Queries *****/

  public Set<EquivValue> mayAliases(Value v, Unit u) {
    EquivValue ev = getOrCreate(v, u);
    if (ev == null) return new HashSet<>();
    return getAllAliases(getFlowAfter(u), ev, u);
  }

  private Set<EquivValue> getAllAliases(Set<Set<EquivValue>> aliasSets,
                                        EquivValue ev, Unit unit) {
    Set<EquivValue> ret = new HashSet<>();
    Set<EquivValue> oldSet = new HashSet<>();
    ret.add(ev);
    while (!oldSet.equals(ret)) {
      oldSet = ret;
      ret = new HashSet<>();
      for (EquivValue old : oldSet) {
        ret.add(old);
        // Add all sets with old in it.
        for (Set<EquivValue> s : getSets(aliasSets, old))
          ret.addAll(s);
        // Add all sets with old's parents in it.
        if (old.getField() != null) {
          EquivValue val = getOrCreate(old.getLocal(), null);
          if (val != null) {
            for (Set<EquivValue> s : getSets(aliasSets, val)) {
              for (EquivValue v : s) {
                v = v.deriveWithNewField(old.getField(), unit,
                  Marker.ALIAS_FIELD);
                ret.add(v);
              }
            }
          }
        }
      }
    }
    return ret;
  }

  public Set<Set<EquivValue>> mayAliasesAtExit() {
    Set<Set<EquivValue>> res = new HashSet<>();
    for (Unit u : graph.getTails())
      res.addAll(getFlowAfter(u));
    return res;
  }
}
