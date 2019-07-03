package reporter;

import analysis.TaintAbstraction;
import icfg.AliasICFG;
import marking.Marker;
import marking.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import timeout.TimeoutHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Reporter {

  private static final Logger logger = LoggerFactory.getLogger(Reporter.class);

  private final List<WarningNode> nodes;
  private final PathResolving pathResolvingLevel;
  private AnalysisResults results;
  // Should behave like a set based on exactEquals.
  private List<Node> seenTaint;
  private List<Node> seenAlias;
  private List<Node> sinkNodes;

  private TimeoutHandler timeoutHandler;

  public Reporter(PathResolving pathResolvingLevel) {
    this.results = null;
    this.nodes = new ArrayList<>();
    this.seenTaint = new ArrayList<>();
    this.seenAlias = new ArrayList<>();
    this.sinkNodes = new ArrayList<>();
    this.pathResolvingLevel = pathResolvingLevel;
    this.timeoutHandler = new TimeoutHandler();
  }

  public static String nodeDescription(WarningNode node, Marker marker) {
    int lineNb = (node.getAnalysisNode().getUnit() == null) ? -2 :
      node.getAnalysisNode().getUnit().getJavaSourceStartLineNumber();
    String fileName = node.getFileName();
    return "\t" + node.hashCode() + "\t[" +
      node.getAnalysisNode().abstraction() + "]\t(" + node.getParents().size() +
      "-" + node.getChildren().size() + ")\t" + lineNb + ":" + fileName + " " +
      (marker == null ? "" : marker.name()) + " --- " +
      node.getAnalysisNode().getUnit();
  }

  public void setICFG(AliasICFG icfg) {
    results = new AnalysisResults(icfg, pathResolvingLevel);
  }

  // Printing.
  public void printWarnings() {
    if (results == null) {
      logger.info("No results found.");
      return;
    }

    // Summary.
    logger.info(results.getNbSources() +
      (results.getNbSources() == 1 ? " source, " : " sources, ") +
      results.getNbSinks() +
      (results.getNbSinks() == 1 ? " sink.\n\n" : " sinks.\n\n"));

    if (pathResolvingLevel.equals(PathResolving.NONE)) return;

    // Path details.
    logger.info("***** PATHS *****");
    Set<Path> paths = results.getPaths();
    for (Path path : paths) {
      WarningNode source = null;
      for (WarningNode node : path.getPath().keySet()) {
        if (source == null) source = node;
        logger.info(nodeDescription(node, path.getPath().get(node)));
      }
      logger.info("AUTOMATON:\n" + path.getAutomaton().toString() + "\n\n");
    }
  }

  public void report(TaintAbstraction fa) {
    if (!seen(sinkNodes, fa)) sinkNodes.add(fa);
  }

  // Reporting.

  public AnalysisResults generateAnalysisResults() {
    assert results != null;

    this.seenTaint = new ArrayList<>();
    this.seenAlias = new ArrayList<>();

    for (Node sinkNode : sinkNodes) {
      WarningNode sink = retrieveWarningNode(sinkNode);
      results.addSink(sink);
      nodes.add(sink);

      try {
        timeoutHandler
          .runWithTimeout(() -> findPath(sink), 5, TimeUnit.MINUTES);
      } catch (Exception | Error e) {
        logger.info(
          "Lookup path failed for: " + sink.getAnalysisNode().abstraction() +
            "\n\t at " + sink.getAnalysisNode().getUnit() + "\n\t with " +
            "source: " + "" + sink.getAnalysisNode().getSource());
        e.printStackTrace();
      }
    }
    return results;
  }

  // Go up from current to source.
  private boolean findPath(WarningNode current) throws StackOverflowError {
    // TimeoutHandler.
    if (Thread.currentThread().isInterrupted())
      throw new RuntimeException("Timeout in find path.");

    for (Node predecessor : current.getAnalysisNode().getPredecessors()
      .keySet()) {
      List<Node> seen;
      if (current.getAnalysisNode() instanceof TaintAbstraction)
        seen = seenTaint;
      else seen = seenAlias;
      seen.add(current.getAnalysisNode());

      Marker marker =
        current.getAnalysisNode().getPredecessors().get(predecessor);

      if (marker.equals(Marker.SOURCE)) {
        // Source found. Stop here and transmit source marker.
        current.addParent(WarningNode.initialWarningNode, marker);
        results.addSource(current);
        if (!pathResolvingLevel.equals(PathResolving.ALL_PATHS)) return true;
      }

      // Predecessor node.
      WarningNode parentNode = retrieveWarningNode(predecessor);
      if (!isZero(current.getAnalysisNode())) {
        nodes.add(parentNode);

        current.addParent(parentNode, marker);
        parentNode.addChild(current, marker);
        // Recursive call if no loop.
        if (!seen(seen, predecessor)) {
          if (findPath(parentNode) &&
            !pathResolvingLevel.equals(PathResolving.ALL_PATHS)) return true;
        }
      }

      // Predecessor's neighbour node.
      for (Node neighbour : parentNode.getAnalysisNode().getNeighbours()) {
        WarningNode neighbourNode = retrieveWarningNode(neighbour);
        if (!isZero(current.getAnalysisNode())) {
          nodes.add(neighbourNode);
          current.addParent(neighbourNode, marker);
          neighbourNode.addChild(current, marker);
          // Recursive call if no loop.
          if (!seen(seen, neighbour)) {
            if (findPath(neighbourNode) &&
              !pathResolvingLevel.equals(PathResolving.ALL_PATHS)) return true;
          }
        }
      }
    }
    return false;
  }

  // Return the node in the list that has the same inner Node as the
  // node. If it does not exist, return a new WarningNode.
  private WarningNode retrieveWarningNode(Node node) {
    WarningNode retNode = new WarningNode(node);
    if (this.nodes.contains(retNode)) {
      retNode = this.nodes.get(this.nodes.indexOf(retNode));
      retNode.setAnalysisNode(node);
    }
    return retNode;
  }

  private boolean isZero(Node n) {
    return n.isZeroAbstraction();
  }

  private boolean seen(List<Node> list, Node node) {
    if (node.isZeroAbstraction()) return false;
    for (Node n : list) {
      if (node.exactEquals(n)) return true;
    }
    return false;
  }

  public void stopPathLookups() {
    this.timeoutHandler.killAllFutures();
    if (this.results != null) results.stopPathLookups();
  }

  // Loop detection

  public enum PathResolving {
    NONE, ONE_PATH, ALL_PATHS
  }
}