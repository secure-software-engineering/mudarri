package analysis;

import heros.FlowFunctions;
import icfg.AliasICFG;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IFDSTaintAnalysisProblem
  extends DefaultJimpleIFDSTabulationProblem<TaintAbstraction, AliasICFG> {

  private final TaintAnalysisFlowFunctions flowFunctions;
  private final Set<String> entryPoints;

  IFDSTaintAnalysisProblem(AliasICFG icfg,
                           TaintAnalysisFlowFunctions flowFunctions,
                           Set<String> entryPoints) {
    super(icfg);
    this.flowFunctions = flowFunctions;
    this.entryPoints = entryPoints;
  }

  @Override
  public Map<Unit, Set<TaintAbstraction>> initialSeeds() {
    Map<Unit, Set<TaintAbstraction>> res = new HashMap<>();
    for (SootClass c : Scene.v().getApplicationClasses()) {
      for (SootMethod sm : c.getMethods()) {
        if (sm != null && sm.hasActiveBody() &&
          entryPoints.contains(sm.getSignature())) {
          res.put(sm.getActiveBody().getUnits().getFirst(),
            Collections.singleton(zeroValue()));
        }
      }
    }
    return res;
  }

  @Override
  protected FlowFunctions<Unit, TaintAbstraction, SootMethod> createFlowFunctionsFactory() {
    return flowFunctions;
  }

  @Override
  protected TaintAbstraction createZeroValue() {
    return TaintAbstraction.zeroAbstraction;
  }

  @Override
  public boolean autoAddZero() {
    return true;
  }
}
