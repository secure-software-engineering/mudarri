package reporter;

import icfg.AliasICFG;
import marking.Marker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reporter.Reporter.PathResolving;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import timeout.TimeoutHandler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AnalysisResults {

  private static final Logger logger =
    LoggerFactory.getLogger(AnalysisResults.class);

  private final Set<WarningNode> sources;
  private final Set<WarningNode> sinks;
  private final PathResolving pathResolvingLevel;
  private final AliasICFG icfg;
  private Set<Path> paths;

  private TimeoutHandler timeoutHandler;

  public AnalysisResults(AliasICFG icfg, PathResolving pathResolvingLevel) {
    this.icfg = icfg;
    this.paths = null;
    this.sources = new HashSet<>();
    this.sinks = new HashSet<>();
    this.pathResolvingLevel = pathResolvingLevel;
    this.timeoutHandler = new TimeoutHandler();
  }

  void stopPathLookups() {
    System.out.println("Killing futures of Analysis results");
    this.timeoutHandler.killAllFutures();
  }

  private void generatePaths() {
    paths = new HashSet<>();
    if (pathResolvingLevel.equals(PathResolving.NONE)) return;

    for (WarningNode source : sources) {
      source.setSource(true);

      Set<Path> pathsFromSource = new HashSet<>();
      Marker initialMarker =
        source.getParents().get(WarningNode.initialWarningNode);

      try {
        timeoutHandler.runWithTimeout(
          () -> generatePaths(source, pathsFromSource, new Path(initialMarker),
            new HashSet<>(), initialMarker, null), 1, TimeUnit.MINUTES);
      } catch (Exception | Error e) {
        logger.info("Generate path failed for: " +
          source.getAnalysisNode().abstraction() + "\n\t at " +
          source.getAnalysisNode().getUnit() + "\n\t with " + "source: " + "" +
          source.getAnalysisNode().getSource());
        e.printStackTrace();
      }

      paths.addAll(pathsFromSource);
    }
  }

  private void generatePaths(WarningNode node, Set<Path> pathsFromSource,
                             Path currentPath, Set<WarningNode> seen,
                             Marker marker, Marker prevMarker)
    throws StackOverflowError {
    // TimeoutHandler.
    if (Thread.currentThread().isInterrupted())
      throw new RuntimeException("Timeout in generate path.");

    // Avoid loops.
    if (seen.contains(node)) return;
    seen.add(node);
    setNodeMetadata(node);

    currentPath.put(node, makeLabel(node), marker, prevMarker);

    // Check call-stack consistency.
    if (!consistantCallStack(currentPath)) return;

    // Stopping condition -> Reached a leaf (a sink).
    if (node.getChildren().size() == 0) {
      if (pathResolvingLevel.equals(PathResolving.ALL_PATHS) ||
        !alreadyFound(pathsFromSource, currentPath))
        pathsFromSource.add(currentPath);
      node.setSink(true);
    }

    // Go down all children.
    for (WarningNode child : node.getChildren().keySet()) {
      Path currentPathClone = new Path(currentPath);
      Set<WarningNode> seenClone = new HashSet<>(seen);
      // Recursive call.
      generatePaths(child, pathsFromSource, currentPathClone, seenClone,
        node.getChildren().get(child), marker);
    }
  }

  private boolean alreadyFound(Set<Path> pathsFromSource, Path currentPath) {
    // Get head and tail.
    Unit currentSource = null;
    Unit currentSink = null;
    for (WarningNode currentStep : currentPath.getPath().keySet()) {
      if (currentSource == null)
        currentSource = currentStep.getAnalysisNode().getUnit();
      currentSink = currentStep.getAnalysisNode().getUnit();
    }

    for (Path path : pathsFromSource) {
      // Get head and tail.
      Unit pathSource = null;
      Unit pathSink = null;
      for (WarningNode pathStep : path.getPath().keySet()) {
        if (pathSource == null)
          pathSource = pathStep.getAnalysisNode().getUnit();
        pathSink = pathStep.getAnalysisNode().getUnit();
      }

      if (currentSource.equals(pathSource) && currentSink.equals(pathSink))
        return true;
    }
    return false;
  }

  private boolean consistantCallStack(Path currentPath) {
    Deque<SootMethod> callStack = new ArrayDeque<>();
    for (WarningNode node : currentPath.getPath().keySet()) {
      SootMethod nodeMethod =
        icfg.getMethodOf(node.getAnalysisNode().getUnit());
      // If call or return.
      if (!nodeMethod.equals(callStack.peek())) {
        Marker marker = currentPath.getPath().get(node);
        if (Marker.callMarkers().contains(marker)) {
          // If call, add to the call stack.
          callStack.push(nodeMethod);
        } else if (Marker.returnMarkers().contains(marker)) {
          // If return, check for consistency.
          if (callStack.peek() != null && !nodeMethod.equals(callStack.pop()))
            return false;
        }
      }
    }
    return true;
  }

  private String makeLabel(WarningNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append(node.getAnalysisNode().abstraction());
    Unit unit = node.getAnalysisNode().getUnit();
    if (unit != null) {
      sb.append(";");
      sb.append(unit.getJavaSourceStartLineNumber());
      sb.append(";");
      sb.append(getSourceFileName(unit));
    }
    return sb.toString();
  }

  private void setNodeMetadata(WarningNode node) {
    Unit unit = node.getAnalysisNode().getUnit();
    if (unit == null) return;
    node.setLineNb(unit.getJavaSourceStartLineNumber());
    String fileName = getSourceFileName(unit);
    node.setFileName(fileName);
  }

  private String getSourceFileName(Unit u) {
    SootClass sc = icfg.getMethodOf(u).getDeclaringClass();
    String packageName = sc.getPackageName().replaceAll("\\.", "/");
    String fileName;
    if (sc.getTag("SourceFileTag") != null)
      fileName = packageName + "/" + sc.getTag("SourceFileTag").toString();
    else fileName = packageName + "/" + sc.getJavaStyleName() + ".java";
    return fileName;
  }

  public int getNbSources() {
    return sources.size();
  }

  public int getNbSinks() {
    return sinks.size();
  }

  void addSink(WarningNode sink) {
    this.sinks.add(sink);
  }

  void addSource(WarningNode source) {
    this.sources.add(source);
  }

  public Set<Path> getPaths() {
    if (paths == null) generatePaths();
    return paths;
  }
}
