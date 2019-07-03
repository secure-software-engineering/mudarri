package analysis;

import alias.EquivValue;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.flowfunc.KillAll;
import icfg.AliasICFG;
import marking.Marker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reporter.Reporter;
import soot.Local;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.jimple.infoflow.InfoflowManager;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.data.AccessPathFactory;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.source.SourceInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaintAnalysisFlowFunctions
  implements FlowFunctions<Unit, TaintAbstraction, SootMethod> {

  private final static boolean DEBUG_REPORT = false;
  private final static boolean DEBUG_IFDS_RESULTS = false;
  private final static boolean DEBUG_SUSI = false;
  private final static boolean DEBUG_ALIAS = false;

  private static final Logger logger =
    LoggerFactory.getLogger(TaintAnalysisFlowFunctions.class);
  private final ISourceSinkManager susiManager;
  private AliasICFG icfg;
  private Reporter reporter;
  private InfoflowManager infoFlowManager;
  private Set<String> applicationMethods;

  public TaintAnalysisFlowFunctions(Reporter reporter,
                                    ISourceSinkManager susiManager) {
    this.reporter = reporter;
    this.susiManager = susiManager;
  }

  void setICFG(AliasICFG icfg) {
    this.icfg = icfg;

    // Set up source / sink detector.
    InfoflowAndroidConfiguration ifc = new InfoflowAndroidConfiguration();
    ifc.setAccessPathLength(3);
    this.infoFlowManager =
      new InfoflowManager(null, null, icfg, susiManager, null, null,
        new AccessPathFactory(ifc));
  }

  void setApplicationMethods(Set<String> applicationMethods) {
    this.applicationMethods = applicationMethods;
  }

  private void printDebugIFDSInfo(String flowFctType, Unit src,
                                  TaintAbstraction source,
                                  Set<TaintAbstraction> outSet) {
    boolean zeroPropagation = (source != null // && source.isZeroAbstraction()
      && outSet != null && outSet.size() == 1 &&
      outSet.iterator().next().isZeroAbstraction());

    if (!zeroPropagation) {
      logger.info(flowFctType + " " + src + " --- " +
        TaintAnalysisTransformer.info(source) + " --- " + outSet + " --- " +
        icfg.getMethodOf(src));
    }
  }

  // Automaton.

  private TaintAbstraction deriveWithNewStmt(TaintAbstraction source, Unit src,
                                             Marker marker) {
    return source.deriveWithNewStmt(src, marker);
  }

  private TaintAbstraction deriveWithNewLocal(TaintAbstraction source,
                                              Local local, Unit src,
                                              Marker marker) {
    return source.deriveWithNewLocal(local, src, marker);
  }

  private TaintAbstraction deriveWithNewValue(TaintAbstraction source,
                                              Value value, SootField[] fields,
                                              Unit src, Marker marker) {
    TaintAbstraction abs = source.deriveWithNewValue(value, src, marker);
    abs = abs.append(fields);
    return abs;
  }

  // Flow functions.

  @Override
  public FlowFunction<TaintAbstraction> getNormalFlowFunction(final Unit src,
                                                              Unit dest) {
    final Stmt stmt = (Stmt) src;
    if (stmt instanceof AssignStmt) {

      return source -> {
        final AssignStmt assignStmt = (AssignStmt) stmt;
        final Value left = assignStmt.getLeftOp();
        final Value right = assignStmt.getRightOp();

        Set<TaintAbstraction> outSet = new HashSet<>();

        // If the right side is tainted, we need to taint the left
        // side as well
        for (TaintAbstraction fa : getTaints(right, left, source, src)) {
          outSet.add(fa);
          outSet.addAll(taintAliases(fa));
        }

        // We only propagate the incoming taint forward if the
        // respective variable is not overwritten
        boolean leftSideMatches = false;
        if (left instanceof Local && source.getLocal() == left)
          leftSideMatches = true;
        else if (left instanceof InstanceFieldRef) {
          InstanceFieldRef ifr = (InstanceFieldRef) left;
          if (source.hasPrefix(ifr)) leftSideMatches = true;
        } else if (left instanceof StaticFieldRef) {
          StaticFieldRef sfr = (StaticFieldRef) left;
          if (source.hasPrefix(sfr)) leftSideMatches = true;
        }
        if (!leftSideMatches) {
          outSet.add(deriveWithNewStmt(source, src, Marker.ID_NFF_ASSIGNSTMT));
        }

        removeUnusedTaints(src, outSet);

        if (DEBUG_IFDS_RESULTS) printDebugIFDSInfo("norm", src, source, outSet);

        return outSet;
      };
    }

    return source -> {
      Set<TaintAbstraction> outSet = new HashSet<>();
      outSet.add(deriveWithNewStmt(source, src, Marker.ID_NFF_NO_ASSIGNSTMT));
      removeUnusedTaints(src, outSet);
      if (DEBUG_IFDS_RESULTS)
        printDebugIFDSInfo("norm (id)", src, source, outSet);
      return outSet;
    };
  }

  @Override
  public FlowFunction<TaintAbstraction> getCallFlowFunction(final Unit src,
                                                            final SootMethod dest) {

    // No bodies for excluded.
    if (dest == null || !dest.hasActiveBody() ||
      !Scene.v().getApplicationClasses().contains(dest.getDeclaringClass()))
      return KillAll.v();

    // Get the formal parameter locals in the callee.
    final List<Local> paramLocals = new ArrayList<>();
    if (!dest.getName().equals("<clinit>"))
      for (int i = 0; i < dest.getParameterCount(); i++) {
        paramLocals.add(dest.getActiveBody().getParameterLocal(i));
      }

    return source -> {
      final Stmt stmt = (Stmt) src;
      final InvokeExpr ie = stmt.getInvokeExpr();
      Set<TaintAbstraction> outSet = new HashSet<>();

      if (source.getLocal() == null) {
        // Static fields
        outSet.add(deriveWithNewStmt(source, src, Marker.CALL_STATIC));
      } else if (ie instanceof InstanceInvokeExpr &&
        ((InstanceInvokeExpr) ie).getBase() == source.getLocal()) {
        // Map the "this" value
        outSet.add(
          deriveWithNewLocal(source, dest.getActiveBody().getThisLocal(), src,
            Marker.CALL_THIS));
      } else if (ie.getArgs().contains(source.getLocal())) {
        // Map the parameters
        int argIndex = ie.getArgs().indexOf(source.getLocal());
        TaintAbstraction fa =
          deriveWithNewLocal(source, paramLocals.get(argIndex), src,
            Marker.CALL_PARAMETERS);
        if (DEBUG_IFDS_RESULTS) printDebugIFDSInfo("call (args)", src, source,
          Collections.singleton(fa));
        return Collections.singleton(fa);
      }

      if (DEBUG_IFDS_RESULTS) printDebugIFDSInfo("call", src, source, outSet);
      return outSet;
    };
  }

  @Override
  public FlowFunction<TaintAbstraction> getReturnFlowFunction(
    final Unit callSite, final SootMethod callee, Unit exitStmt, Unit retSite) {

    if (!callee.hasActiveBody()) return KillAll.v();

    // Get the formal parameter locals in the callee
    final List<Local> paramLocals = new ArrayList<>();
    if (!callee.getName().equals("<clinit>"))
      for (int i = 0; i < callee.getParameterCount(); i++)
        paramLocals.add(callee.getActiveBody().getParameterLocal(i));

    return source -> {
      final Value retOp =
        (exitStmt instanceof ReturnStmt) ? ((ReturnStmt) exitStmt).getOp() :
          null;
      final Value tgtOp = (callSite instanceof DefinitionStmt) ?
        ((DefinitionStmt) callSite).getLeftOp() : null;
      final InvokeExpr invExpr = ((Stmt) callSite).getInvokeExpr();
      Set<TaintAbstraction> outSet = new HashSet<>();

      if (retOp != null && source.getLocal() == retOp && tgtOp != null) {
        // Map the return value
        TaintAbstraction fa =
          deriveWithNewLocal(source, (Local) tgtOp, callSite,
            Marker.RETURN_RETVAL);
        fa.setFromReturn();
        outSet.add(fa);
      } else if (invExpr instanceof InstanceInvokeExpr &&
        source.getLocal() == callee.getActiveBody().getThisLocal()) {
        // Map the the "this" local
        Local baseLocal = (Local) ((InstanceInvokeExpr) invExpr).getBase();
        TaintAbstraction fa =
          deriveWithNewLocal(source, baseLocal, callSite, Marker.RETURN_THIS);
        fa.setFromReturn();
        outSet.add(fa);
        outSet.addAll(taintAliases(fa));
      } else if (source.getFields() != null &&
        paramLocals.contains(source.getLocal())) {
        // Map the parameters
        int paramIdx = paramLocals.indexOf(source.getLocal());
        if (!(invExpr.getArg(paramIdx) instanceof Constant)) {
          TaintAbstraction fa =
            deriveWithNewLocal(source, (Local) invExpr.getArg(paramIdx),
              callSite, Marker.RETURN_PARAMETERS);
          fa.setFromReturn();
          outSet.add(fa);
          outSet.addAll(taintAliases(fa));
        }
      }
      // Static variables
      if (source.getLocal() == null) {
        TaintAbstraction newAbs =
          deriveWithNewStmt(source, callSite, Marker.RETURN_STATIC);
        newAbs.setFromReturn();
        outSet.add(newAbs);
      }

      removeUnusedTaints(callSite, outSet);

      if (DEBUG_IFDS_RESULTS)
        printDebugIFDSInfo("ret", callSite, source, outSet);
      return outSet;
    };
  }

  @Override
  public FlowFunction<TaintAbstraction> getCallToReturnFlowFunction(
    final Unit call, final Unit returnSite) {
    return abs -> {
      Set<TaintAbstraction> outSet = new HashSet<>();
      final Stmt stmt = (Stmt) call;
      Set<TaintAbstraction> sources = detectSources((Stmt) call);
      Set<TaintAbstraction> sinks = detectSinks(abs, call);

      if (!sources.isEmpty() || !sinks.isEmpty()) {
        if (abs.isZeroAbstraction()) {
          for (TaintAbstraction source : sources) {
            if (DEBUG_SUSI)
              logger.info("Found a source: " + source + " at " + call);
            outSet.add(source);
            outSet.addAll(taintAliases(source));
          }
        }

        for (TaintAbstraction sink : sinks) {
          if (DEBUG_SUSI)
            logger.info("Found a sink: at " + sink + " --- " + call);

          if (DEBUG_REPORT) logger.info("REPORT callToReturnFF " +
            sink.getSource().getJavaSourceStartLineNumber() + " --- " + abs +
            " -- " + call);
          reporter.report(sink);
          outSet.add(sink);
        }
      }

      // API calls
      // if o is an API object (ex: Point)
      if (!inProject(((Stmt) call).getInvokeExpr().getMethod())) {
        for (TaintAbstraction fa : taintApi(call, abs)) {
          outSet.add(fa);
          outSet.addAll(taintAliases(fa));
        }
        if (DEBUG_IFDS_RESULTS)
          printDebugIFDSInfo("cot API", call, abs, outSet);
        return outSet;
      }

      // ID
      TaintAbstraction newAbs =
        deriveWithNewStmt(abs, call, Marker.COT_RECIEVER);

      // Args or caller
      Value sourceInArgs = null;
      for (Value arg : stmt.getInvokeExpr().getArgs()) {
        if (abs.hasPrefix(arg)) {
          sourceInArgs = arg;
          break;
        }
      }

      // boolean sourceIsCallerObj = false;
      // if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
      // InstanceInvokeExpr iie = (InstanceInvokeExpr) stmt.getInvokeExpr();
      // if (abs.hasPrefix(iie.getBase())) sourceIsCallerObj = true;
      // }

      boolean staticField = (abs.getLocal() == null);

      boolean overwritten = false;
      if (call instanceof AssignStmt) {
        AssignStmt assignStmt = (AssignStmt) call;
        final Local leftLocal = (Local) assignStmt.getLeftOp();
        TaintAbstraction fa =
          new TaintAbstraction(leftLocal, assignStmt, null, null, null);
        if (newAbs.equals(fa)) overwritten = true;
      }

      if (abs.isZeroAbstraction() ||
        (!overwritten && sourceInArgs == null && !staticField))
        // && !sourceIsCallerObj
        outSet.add(newAbs);

      if (DEBUG_IFDS_RESULTS) printDebugIFDSInfo("cot ID", call, abs, outSet);
      return outSet;
    };
  }

  private Set<TaintAbstraction> taintAliases(TaintAbstraction fa) {
    Set<TaintAbstraction> ret = new HashSet<>();
    if (fa.getLocal() != null && fa.getFields().length > 0) {
      Set<EquivValue> mayAliases = icfg.mayAlias(fa.getLocal(), fa.getUnit());
      for (EquivValue aliasEq : mayAliases) {
        // Do not take locals into account.
        if (aliasEq.getField() == null &&
          aliasEq.getLocal().equals(fa.getLocal())) {
          continue;
        }

        // Append the fields if needed.
        TaintAbstraction aliasAbs =
          deriveWithNewLocal(fa, aliasEq.getLocal(), fa.getUnit(),
            Marker.TAINT_ALIAS);
        aliasAbs.resetFields();
        if (aliasEq.getField() != null)
          aliasAbs.append(new SootField[]{aliasEq.getField()});
        aliasAbs.append(fa.getFields());

        // Set the alias node as a predecessor of the resulting node.
        // LISA: No automaton transition. Would be redundant with the one above.
        aliasAbs.addPredecessor(aliasEq, Marker.TAINT_ALIAS);
        ret.add(aliasAbs);
      }

      if (DEBUG_ALIAS) {
        if (!ret.isEmpty()) {
          logger.info("At " + fa.getUnit());
          logger.info("\tAliases of " + fa.getLocal() + " are: " + mayAliases);
          logger.info("\tAlias tainting " + ret);
        }
      }
    }
    return ret;
  }

  private TaintAbstraction getTaint(Value right, Value left,
                                    TaintAbstraction source, Unit src) {
    TaintAbstraction fa = null;
    if (right instanceof CastExpr) right = ((CastExpr) right).getOp();

    if (right instanceof Local && source.getLocal() == right) {
      fa = deriveWithNewValue(source, left, source.getFields(), src,
        Marker.RIGHT_TO_LEFT_LOCAL);
    } else if (right instanceof InstanceFieldRef) {
      InstanceFieldRef ifr = (InstanceFieldRef) right;
      if (source.hasPrefix(ifr)) {
        fa = deriveWithNewValue(source, left, source.getPostfix(ifr), src,
          Marker.RIGHT_TO_LEFT_IFR);
      }
    } else if (right instanceof StaticFieldRef) {
      StaticFieldRef sfr = (StaticFieldRef) right;
      if (source.hasPrefix(sfr)) {
        fa = deriveWithNewValue(source, left, source.getPostfix(sfr), src,
          Marker.RIGHT_TO_LEFT_SFR);
      }
    } else if (right instanceof ArrayRef) {
      ArrayRef ar = (ArrayRef) right;
      if (ar.getBase() == source.getLocal()) fa =
        deriveWithNewValue(source, left, new SootField[]{}, src,
          Marker.RIGHT_TO_LEFT_AR);
    }
    return fa;
  }

  private Set<TaintAbstraction> getTaints(Value right, Value left,
                                          TaintAbstraction source, Unit src) {
    Set<TaintAbstraction> ret = new HashSet<>();
    TaintAbstraction fa = getTaint(right, left, source, src);
    if (fa != null) ret.add(fa);

    // f0 = o.x {o} -> taint f0 and o.x if o is API object
    if (right instanceof InstanceFieldRef && source.getLocal() != null &&
      source.getFields().length == 0) {
      // if o is an API object (ex: Point)
      if (!inProject(((InstanceFieldRef) right).getBase().getType())) {
        fa = deriveWithNewValue(source, right, new SootField[]{}, src,
          Marker.API_RIGHT);
        if (fa.hasPrefix(source.getLocal())) {
          ret.add(fa);
          fa = deriveWithNewValue(fa, left, new SootField[]{}, src,
            Marker.API_LEFT);
          ret.add(fa);
        }
      }
    }
    return ret;
  }

  private boolean inProject(Type type) {
    for (String method : this.applicationMethods) {
      String methodType = method.substring(1, method.indexOf(":"));
      if (methodType.equals(type.toString())) return true;
    }
    return false;
  }

  private boolean inProject(SootMethod sm) {
    for (String method : this.applicationMethods) {
      if (sm.getSignature().equals(method)) return true;
    }
    return false;
  }

  private void removeUnusedTaints(Unit src, Set<TaintAbstraction> outSet) {
    // Set<FlowAbstraction> toRemove = new HashSet<FlowAbstraction>();
    // for (FlowAbstraction fa : outSet) {
    // if (killUnusedTaint(icfg, fa))
    // toRemove.add(fa);
    // }
    // outSet.removeAll(toRemove);
  }

  // Add source calls here.
  private Set<TaintAbstraction> detectSources(Stmt stmt) {
    Set<TaintAbstraction> sources = new HashSet<>();

    if (susiManager == null) {
      // Java. TODO: Better management of sources.
      if (stmt instanceof AssignStmt) {
        AssignStmt as = (AssignStmt) stmt;
        if (as.getInvokeExpr().getMethod().getName().equals("getParameter")) {
          sources.add(new TaintAbstraction(as.getLeftOp(), stmt, stmt,
            TaintAbstraction.zeroAbstraction, Marker.SOURCE));
        }
      }
    } else {
      // Android.
      SourceInfo sourceInfo = susiManager.getSourceInfo(stmt, infoFlowManager);
      if (sourceInfo != null) {
        for (AccessPath ap : sourceInfo.getAccessPaths()) {
          TaintAbstraction ta =
            new TaintAbstraction(ap.getPlainValue(), ap.getFields(), stmt, stmt,
              TaintAbstraction.zeroAbstraction, Marker.SOURCE);
          sources.add(ta);
        }
      }
    }
    return sources;
  }

  // Add sink calls here.
  private Set<TaintAbstraction> detectSinks(TaintAbstraction dff, Unit u) {
    Set<TaintAbstraction> sinks = new HashSet<>();
    Stmt stmt = (Stmt) u;
    boolean sinkMethod = false;

    if (susiManager == null) {
      // Java TODO: Better management of sinks.
      sinkMethod = stmt.toString().contains("executeQuery");
    } else if (!dff.isZeroAbstraction()) {
      // Android.
      AccessPath ap = infoFlowManager.getAccessPathFactory()
        .createAccessPath(dff.getLocal(), dff.getFields(), true);
      sinkMethod = susiManager.isSink(stmt, infoFlowManager, ap);
    }

    if (sinkMethod) {
      boolean sourceInArgs = false;
      for (Value arg : stmt.getInvokeExpr().getArgs()) {
        if (dff.hasPrefix(arg)) {
          sourceInArgs = true;
          break;
        }
      }

      boolean baseIsSource = false;
      // if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
      // InstanceInvokeExpr iie = (InstanceInvokeExpr) stmt.getInvokeExpr();
      // baseIsSource = dff.hasPrefix(iie.getBase());
      // }

      if (sourceInArgs || baseIsSource)
        sinks.add(deriveWithNewStmt(dff, u, Marker.SINK));
    }
    return sinks;
  }

  // Add API calls here.
  private Set<TaintAbstraction> taintApi(Unit call, TaintAbstraction source) {
    Stmt stmt = (Stmt) call;
    final String target = stmt.getInvokeExpr().getMethod().getSignature();
    final List<Value> args = stmt.getInvokeExpr().getArgs();
    final Local baseLocal = stmt.getInvokeExpr() instanceof InstanceInvokeExpr ?
      (Local) ((InstanceInvokeExpr) stmt.getInvokeExpr()).getBase() : null;
    Local receiver = null;
    if (stmt instanceof AssignStmt)
      receiver = (Local) ((AssignStmt) stmt).getLeftOp();

    Set<TaintAbstraction> ret = new HashSet<>();

    // Propagate taint forward.
    ret.add(deriveWithNewStmt(source, call, Marker.API_ID_DEFAULT2));

    // Summaries for API calls
    if (target.contains("java.lang.String toString()")) {
      // If not reciever, propagate taint.
      if (receiver != null && !receiver.equals(source.getLocal()))
        ret.add(deriveWithNewStmt(source, call, Marker.API_TOSTRING));
      // if base is tainted, taint receiver
      if (baseLocal != null && source.getLocal() == baseLocal &&
        receiver != null) ret.add(
        deriveWithNewValue(source, receiver, new SootField[]{}, stmt,
          Marker.API_TOSTRING));
      return ret;
    }

    switch (target) {
      case "<java.lang.String: void getChars(int,int,char[],int)>":
        // If not reciever, propagate taint.
        if (receiver != null && !receiver.equals(source.getLocal()))
          ret.add(deriveWithNewStmt(source, call, Marker.API_GETCHARS));
        // if base is tainted, taint third parameter
        if (baseLocal != null && source.getLocal() == baseLocal) ret.add(
          deriveWithNewValue(source, args.get(2), new SootField[]{}, stmt,
            Marker.API_GETCHARS));
        break;

      case "<java.lang.System: void arraycopy(java.lang.Object,int,java.lang" +
        ".Object,int,int)>":
        // Propagate taint.
        ret.add(deriveWithNewStmt(source, call, Marker.API_ARRAYCOPY));
        // if first parameter is tainted, taint third parameter
        if (source.getLocal() == args.get(0)) ret.add(
          deriveWithNewValue(source, args.get(2), new SootField[]{}, stmt,
            Marker.API_ARRAYCOPY));
        break;

      case "<android.content.ContextWrapper: android.content.Context " +
        "getApplicationContext()>":
      case "<android.content.ContextWrapper: void sendBroadcast(android" +
        ".content.Intent)>":
      case "<android.telephony.SmsManager: void sendTextMessage(java.lang" +
        ".String,java.lang.String,java.lang.String,android.app.PendingIntent," +
        "android.app.PendingIntent)>":
      case "<android.app.Activity: java.lang.Object getSystemService(java" +
        ".lang.String)>":
        // Propagate taint.
        ret.add(deriveWithNewStmt(source, call, Marker.API_OTHERS));
        break;

      case "<java.lang.StringBuilder: java.lang.StringBuilder append(java" +
        ".lang.String)>":
        // Propagate taint.
        ret.add(deriveWithNewStmt(source, call, Marker.API_APPEND));
        // if first parameter or base is tainted, taint receiver
        if (receiver != null && (source.getLocal() == args.get(0) ||
          baseLocal != null && source.getLocal() == baseLocal)) {
          ret.add(deriveWithNewValue(source, receiver, new SootField[]{}, stmt,
            Marker.API_APPEND));
        }
        break;

      default:
        ret.addAll(taintApiDefault(call, source));
        break;
    }
    return ret;
  }

  private Set<TaintAbstraction> taintApiDefault(Unit call,
                                                TaintAbstraction source) {
    Stmt stmt = (Stmt) call;
    final List<Value> args = stmt.getInvokeExpr().getArgs();
    final Local baseLocal = stmt.getInvokeExpr() instanceof InstanceInvokeExpr ?
      (Local) ((InstanceInvokeExpr) stmt.getInvokeExpr()).getBase() : null;
    Local receiver = null;
    if (stmt instanceof AssignStmt)
      receiver = (Local) ((AssignStmt) stmt).getLeftOp();

    Set<TaintAbstraction> ret = new HashSet<>();

    // Propagate taint forward.
    ret.add(deriveWithNewStmt(source, call, Marker.API_ID_DEFAULT));

    // If a parameter is tainted, we taint the base local and the receiver
    if (source.getLocal() != null && args.contains(source.getLocal())) {
      if (baseLocal != null && !baseLocal.toString().equals("this")) ret.add(
        deriveWithNewValue(source, baseLocal, new SootField[]{}, call,
          Marker.API_BASE_LOCAL));
      if (receiver != null) ret.add(
        deriveWithNewValue(source, receiver, new SootField[]{}, call,
          Marker.API_RECIEVER));
    }

    // If the base local is tainted, we taint the receiver
    if (baseLocal != null && source.getLocal() == baseLocal && receiver != null)
      ret.add(deriveWithNewValue(source, receiver, new SootField[]{}, call,
        Marker.API_RECIEVER2));

    return ret;
  }
}
