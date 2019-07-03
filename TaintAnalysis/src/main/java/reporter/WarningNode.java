package reporter;

import marking.Marker;
import marking.Node;

import java.util.HashMap;
import java.util.Map;

public class WarningNode {

  final static WarningNode initialWarningNode = new WarningNode(null);
  private final Map<WarningNode, Marker> parents = new HashMap<>();
  private final Map<WarningNode, Marker> children = new HashMap<>();
  //  NOTE: Only use this for plugin and nothing else.
  public String descriptionForPlugin = "";
  public String prev = "";
  public String displayAbs = "";
  private Node analysisNode;
  //  NOTE: Only use this for plugin and nothing else.
  private int lineNb;
  private String fileName;
  private String javaUnit;
  private boolean source = false;
  private boolean sink = false;
  private Marker marker = null;

  public WarningNode(Node analysisNode) {
    this.analysisNode = analysisNode;
  }

  private boolean isInitialWarningNode() {
    return this.analysisNode == null;
  }

  // Getters and setters.

  public boolean source() {
    return source;
  }

  public void setSource(boolean source) { this.source = source; }

  public Marker marker() {
    return marker;
  }

  public void setMarker(Marker marker) {
    this.marker = marker;
  }

  public boolean sink() { return sink; }

  public void setSink(boolean sink) {
    this.sink = sink;
  }

  public int getLineNb() {
    return lineNb;
  }

  public void setLineNb(int lineNb) { this.lineNb = lineNb; }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getShortFileName() {
    if (!this.fileName.contains("/")) return this.fileName;
    return fileName.substring(fileName.lastIndexOf('/') + 1);
  }

  public String getJavaUnit() { return javaUnit; }

  public void setJavaUnit(String javaUnit) {
    this.javaUnit = javaUnit;
  }

  Map<WarningNode, Marker> getParents() {
    return parents;
  }

  void addParent(WarningNode parent, Marker marker) {
    parents.put(parent, marker);
  }

  Map<WarningNode, Marker> getChildren() {
    return children;
  }

  void addChild(WarningNode child, Marker marker) {
    children.put(child, marker);
  }

  public Node getAnalysisNode() {
    return analysisNode;
  }

  void setAnalysisNode(Node analysisNode) {
    this.analysisNode = analysisNode;
  }

  // Utils.

  @Override
  public int hashCode() {
    if (this.isInitialWarningNode()) return 0;
    final int prime = 31;
    int result = 1;
    int analysisNodeHashCode = 0;
    if (analysisNode != null) {
      analysisNodeHashCode = analysisNode.hashCode();
      if (analysisNode.getUnit() != null)
        analysisNodeHashCode += analysisNode.getUnit().hashCode();
    }
    result = prime * result + analysisNodeHashCode;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    WarningNode other = (WarningNode) obj;
    if (this.isInitialWarningNode() && other.isInitialWarningNode())
      return true;
    if (analysisNode == null) {
      if (other.analysisNode != null) return false;
    } else if (!analysisNode.equals(other.analysisNode)) return false;
    if (analysisNode != null && other.getAnalysisNode() != null) {
      if (analysisNode.getUnit() == null) {
        return other.analysisNode.getUnit() == null;
      } else return analysisNode.getUnit().equals(other.analysisNode.getUnit());
    }
    return true;
  }

  @Override
  public String toString() {
    return this.analysisNode.toString();
  }
}
