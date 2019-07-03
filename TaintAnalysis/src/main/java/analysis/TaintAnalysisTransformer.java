package analysis;

import android.EntryPointsManager;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import heros.EdgeFunction;
import heros.solver.IFDSSolver;
import icfg.AliasICFG;
import marking.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reporter.Reporter;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TaintAnalysisTransformer extends SceneTransformer {

  private final static boolean DEBUG_NEIGHBOURS = false;

  private static final Logger logger =
    LoggerFactory.getLogger(TaintAnalysisTransformer.class);

  private final Reporter reporter;
  private final TaintAnalysisFlowFunctions flowFunctions;
  private final EntryPointsManager entryPointsManager;
  // For each (d1, n), keep d2 (propagate notation: <sp, d1> -> <n, d2>).
  private Table<TaintAbstraction, Unit, List<TaintAbstraction>> forwardSeen =
    HashBasedTable.create();
  // For each (d2, n), keep d1 (propagate notation: <sp, d1> -> <n, d2>).
  private Table<TaintAbstraction, Unit, List<TaintAbstraction>> backwardSeen =
    HashBasedTable.create();

  public TaintAnalysisTransformer(Reporter reporter,
                                  EntryPointsManager entryPointsManager,
                                  TaintAnalysisFlowFunctions flowFunctions) {
    this.reporter = reporter;
    this.entryPointsManager = entryPointsManager;
    this.flowFunctions = flowFunctions;
  }

  static String info(Node abs) {
    if (abs == null) return "null";
    StringBuilder sb = new StringBuilder();
    sb.append(abs);
    if (abs.getUnit() != null) {
      sb.append(":");
      sb.append(abs.getUnit().getJavaSourceStartLineNumber());
    }
    if (abs.getSource() != null) {
      sb.append(":");
      sb.append(abs.getSource().getJavaSourceStartLineNumber());
    }
    sb.append(":");
    sb.append(abs.hashCode());
    return sb.toString();
  }

  // Handling the seen table.

  @Override
  protected void internalTransform(String phaseName,
                                   Map<String, String> options) {
    AliasICFG icfg = new AliasICFG();
    flowFunctions.setICFG(icfg);
    flowFunctions
      .setApplicationMethods(entryPointsManager.applicationMethods());
    IFDSTaintAnalysisProblem problem =
      new IFDSTaintAnalysisProblem(icfg, flowFunctions,
        entryPointsManager.entryPoints());
    IFDSSolver<Unit, TaintAbstraction, SootMethod, AliasICFG> solver =
      new IFDSSolver<Unit, TaintAbstraction, SootMethod, AliasICFG>(problem) {

        // FlowTwist -> PropagateAndMerge
        @Override
        protected void propagate(TaintAbstraction sourceVal, Unit target,
                                 TaintAbstraction targetVal,
                                 EdgeFunction<BinaryDomain> f,
                                 Unit relatedCallSite,
                                 boolean isUnbalancedReturn) {
          // Note: target is a successor of sourceVal.getUnit().

          // Set neighbours.
          List<TaintAbstraction> targetVals =
            seenLookup(forwardSeen, sourceVal, target);
          TaintAbstraction seenTargetVal =
            containsAbstractionByName(targetVals, targetVal);
          // Target15: lineNb tests.
          if (seenTargetVal != null && lineNb(seenTargetVal) != -1 &&
            lineNb(targetVal) != -1) {
            debugNeighbours(seenTargetVal, targetVal);
            seenTargetVal.addNeighbour(targetVal);
          } else {
            addSeen(forwardSeen, sourceVal, target, targetVal);
          }

          // Target11: Special case of interprocedural neighbours.
          List<TaintAbstraction> sourceVals =
            seenLookup(backwardSeen, targetVal, target);
          TaintAbstraction seenSourceVal =
            containsAbstractionByName(sourceVals, sourceVal);
          if (seenSourceVal != null && lineNb(seenSourceVal) != -1 &&
            lineNb(sourceVal) != -1) {
            debugNeighbours(seenSourceVal, sourceVal);
            seenSourceVal.addNeighbour(sourceVal);
          } else {
            addSeen(backwardSeen, targetVal, target, sourceVal);
          }

          if (seenSourceVal == null || seenTargetVal == null) {
            super.propagate(sourceVal, target, targetVal, f, relatedCallSite,
              isUnbalancedReturn);
          }
        }
      };
    reporter.setICFG(icfg);
    solver.solve();
  }

  private List<TaintAbstraction> seenLookup(
    Table<TaintAbstraction, Unit, List<TaintAbstraction>> table,
    TaintAbstraction keyAbs, Unit keyUnit) {
    assert keyAbs != null;
    assert keyUnit != null;
    List<TaintAbstraction> list = table.get(keyAbs, keyUnit);
    if (list == null) return Collections.emptyList();
    return list;
  }

  private void addSeen(
    Table<TaintAbstraction, Unit, List<TaintAbstraction>> table,
    TaintAbstraction keyAbs, Unit keyUnit, TaintAbstraction absToInsert) {
    List<TaintAbstraction> list = table.get(keyAbs, keyUnit);
    if (list == null) list = new ArrayList<>();
    list.add(absToInsert);
    table.put(keyAbs, keyUnit, list);
  }

  // Returns the abstraction contained in targetVals that matches the same name
  // as targetVal, and has a different source.
  private TaintAbstraction containsAbstractionByName(
    List<TaintAbstraction> list, TaintAbstraction abs) {
    for (TaintAbstraction fa : list) {
      if (fa.abstraction().equals(abs.abstraction()) &&
        !fa.isZeroAbstraction() && !fa.exactEquals(abs)) {
        return fa;
      }
    }
    return null;
  }

  private void debugNeighbours(TaintAbstraction neighbour1,
                               TaintAbstraction neighbour2) {
    if (DEBUG_NEIGHBOURS) logger
      .info("Add neighbour: " + info(neighbour1) + " --- " + info(neighbour2));
  }

  private int lineNb(TaintAbstraction ta) {
    return ta.getUnit().getJavaSourceStartLineNumber();
  }
}
