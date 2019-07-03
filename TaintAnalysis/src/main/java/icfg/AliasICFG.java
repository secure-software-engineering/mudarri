package icfg;

import alias.EquivValue;
import alias.LocalMayAliasAnalysisWithFields;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import heros.DontSynchronize;
import heros.SynchronizedBy;
import heros.solver.IDESolver;
import soot.*;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.ide.icfg.AbstractJimpleBasedICFG;
import soot.jimple.toolkits.pointer.LocalMustNotAliasAnalysis;
import soot.toolkits.graph.UnitGraph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AliasICFG extends AbstractJimpleBasedICFG {

  @SynchronizedBy("by use of synchronized LoadingCache class")
  private final LoadingCache<Body, LocalMustNotAliasAnalysis> bodyToLMNAA =
    IDESolver.DEFAULT_CACHE_BUILDER
      .build(new CacheLoader<Body, LocalMustNotAliasAnalysis>() {
        @Override
        public LocalMustNotAliasAnalysis load(Body body) throws Exception {
          return new LocalMustNotAliasAnalysis(getOrCreateUnitGraph(body),
            body);
        }
      });
  @SynchronizedBy("by use of synchronized LoadingCache class")
  private final LoadingCache<Unit, Set<SootMethod>> unitToCallees =
    IDESolver.DEFAULT_CACHE_BUILDER
      .build(new CacheLoader<Unit, Set<SootMethod>>() {
        @Override
        public Set<SootMethod> load(Unit u) throws Exception {
          Stmt stmt = (Stmt) u;
          InvokeExpr ie = stmt.getInvokeExpr();
          FastHierarchy fastHierarchy = Scene.v().getFastHierarchy();
          // FIXME Handle Thread.start etc.
          if (ie instanceof InstanceInvokeExpr) {
            if (ie instanceof SpecialInvokeExpr) {
              // special
              return Collections.singleton(ie.getMethod());
            } else {
              // virtual and interface
              InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
              Local base = (Local) iie.getBase();
              RefType concreteType =
                bodyToLMNAA.getUnchecked(unitToOwner.get(u))
                  .concreteType(base, stmt);
              if (concreteType != null) {
                // the base variable definitely points to a
                // single concrete type
                SootMethod singleTargetMethod = fastHierarchy
                  .resolveConcreteDispatch(concreteType.getSootClass(),
                    iie.getMethod());
                return Collections.singleton(singleTargetMethod);
              } else {
                SootClass baseTypeClass;
                if (base.getType() instanceof RefType) {
                  RefType refType = (RefType) base.getType();
                  baseTypeClass = refType.getSootClass();
                } else if (base.getType() instanceof ArrayType) {
                  baseTypeClass = Scene.v().getSootClass("java.lang.Object");
                } else if (base.getType() instanceof NullType) {
                  // if the base is definitely null then there
                  // is no call target
                  return Collections.emptySet();
                } else {
                  throw new InternalError(
                    "Unexpected base type:" + base.getType());
                }
                if (iie.getMethod().isAbstract() || iie.getMethod().isPhantom())
                  return Collections.emptySet();
                return fastHierarchy
                  .resolveAbstractDispatch(baseTypeClass, iie.getMethod());
              }
            }
          } else {
            // static
            return Collections.singleton(ie.getMethod());
          }
        }
      });
  @SynchronizedBy("by use of synchronized LoadingCache class")
  private final LoadingCache<Body, LocalMayAliasAnalysisWithFields>
    bodyToLMAAWF = IDESolver.DEFAULT_CACHE_BUILDER
    .build(new CacheLoader<Body, LocalMayAliasAnalysisWithFields>() {
      @Override
      public LocalMayAliasAnalysisWithFields load(Body body) throws Exception {
        return new LocalMayAliasAnalysisWithFields(
          (UnitGraph) getOrCreateUnitGraph(body));
      }
    });
  @DontSynchronize("readonly")
  private final CallGraph cg;
  @SynchronizedBy("explicit lock on data structure")
  private Map<SootMethod, Set<Unit>> methodToCallers = new HashMap<>();

  public AliasICFG() {
    cg = Scene.v().getCallGraph();
    initializeUnitToOwner();
  }

  private void initializeUnitToOwner() {
    for (SootClass sc : Scene.v().getApplicationClasses()) {
      for (SootMethod m : sc.getMethods()) {
        initializeUnitToOwner(m);
      }
    }
  }

  private void initializeUnitToOwner(SootMethod m) {
    Body b;
    if (!m.hasActiveBody()) {
      try {
        b = m.retrieveActiveBody();
      } catch (RuntimeException e) {
        // Could not resolve method.
        return;
      }
    } else b = m.getActiveBody();

    PatchingChain<Unit> units = b.getUnits();
    for (Unit unit : units) {
      unitToOwner.put(unit, b);
    }
  }

  public Set<EquivValue> mayAlias(Value v, Unit u) {
    return bodyToLMAAWF.getUnchecked(unitToOwner.get(u)).mayAliases(v, u);
  }

  public Set<SootMethod> getCalleesOfCallAt(Unit u) {
    return unitToCallees.getUnchecked(u);
  }

  public Set<Unit> getCallersOf(SootMethod m) {
    Set<Unit> callers = methodToCallers.get(m);
    return callers == null ? Collections.emptySet() : callers;

    // throw new
    // UnsupportedOperationException("This class is not suited for
    // unbalanced problems");
  }

  @Override
  public Collection<Unit> getStartPointsOf(SootMethod m) {
    if (m == null) return Collections.emptySet();
    return super.getStartPointsOf(m);
  }
}
