package state;

import com.intellij.openapi.editor.Editor;
import marking.Marker;
import org.jetbrains.annotations.NotNull;
import reporter.Path;
import reporter.WarningNode;
import soot.jimple.Stmt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StateSingleton {

  public static final int PLUGIN_ID = 1;
  public static final boolean ENABLE_ACTIONS = false;
  public static final boolean FULL_PATHS = false;
  public static final String ZERO = "<ZERO>";
  public static final int LINE_OFFSET = 1;

  private static StateSingleton instance = new StateSingleton();
  private Set<Path> results;
  private Path path;
  private Editor lastHighightedEditor;

  private StateSingleton() { }

  public static StateSingleton getInstance() {
    return instance;
  }

  public static String parseAbs(WarningNode node) {
    String abs = node.getAnalysisNode().abstraction();
    if (FULL_PATHS) return abs;
    if (abs.equals(ZERO)) return "";
    String[] parts = abs.split("\\.");
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
      if (!part.startsWith("$")) {
        if (sb.length() > 0) sb.append(".");
        if (part.contains("$")) part = part.substring(0, part.lastIndexOf("$"));
        sb.append(part);
      }
    }
    return sb.toString();
  }

  public static String fullDescription(WarningNode node, Marker marker) {
    String abs = node.displayAbs;
    String pred = node.prev;
    String absPrint = ((abs.isEmpty() || abs.equals(ZERO)) ?
      ((pred.isEmpty() || pred.equals(ZERO)) ? "_?_" : pred) : abs);
    String predPrint =
      (pred.equals(ZERO) || pred.equals(absPrint) || pred.isEmpty()) ? "" :
        " [" + pred + "] ->";
    String start = "l." + node.getLineNb() + predPrint + " [" + absPrint + "] ";
    String markerInfo = node.getJavaUnit().trim();
    if (PLUGIN_ID == 1) return start + markerInfo;

    String method = "";
    Stmt stmt = (Stmt) node.getAnalysisNode().getUnit();
    if (stmt.containsInvokeExpr()) method =
      stmt.getInvokeExpr().getMethod().getDeclaringClass().getName() + "." +
        stmt.getInvokeExpr().getMethod().getName() + "()";

    if (node.source())
      markerInfo = "gets dangerous information from method <" + method + "> ";
    else if (node.sink())
      markerInfo = "is passed to the dangerous method <" + method + "> ";
    else if (marker != null) {
      boolean addMName = marker.name().contains("API_") && !method.isEmpty();
      markerInfo = (addMName ?
        (marker.name().contains("API_") ? "through" : "in") + " <" + method +
          ">" + " " : "") + marker.toString();
    }
    return start + markerInfo;
  }

  public void resetLists() { this.path = null; }

  public Set<Path> getResults() { return this.results; }

  public void setResults(@NotNull Set<Path> results) {
    this.results = results;
  }

  public Map<WarningNode, Marker> getPath() {
    if (this.path == null) return new HashMap<>();
    return this.path.getPath();
  }

  public void setPath(Path path) { this.path = path; }

  public Editor getEditor() { return this.lastHighightedEditor; }

  public void setEditor(Editor e) { this.lastHighightedEditor = e; }
}