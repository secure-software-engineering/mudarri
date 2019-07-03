package marking;

import soot.Unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Node {

  protected Unit source;
  protected Unit unit;
  private Map<Node, Marker> predecessors = new HashMap<>();
  private Set<Node> neighbours = new HashSet<>();

  protected Node(Unit current, Unit source, Node predecessor, Marker marker) {
    this.unit = current;
    this.source = source;
    this.addPredecessor(predecessor, marker);
  }

  public abstract String abstraction();

  public abstract boolean isZeroAbstraction();

  public Unit getSource() {
    return source;
  }

  public void setSource(Unit source) {
    this.source = source;
  }

  public Unit getUnit() { return unit; }

  public void setUnit(Unit unit) {
    this.unit = unit;
  }

  public String getUnitString() { return unit.toString(); }

  public Map<Node, Marker> getPredecessors() {
    return predecessors;
  }

  public Set<Node> getNeighbours() {
    return neighbours;
  }

  public void addPredecessor(Node predecessor, Marker marker) {
    if (predecessor != null) this.predecessors.put(predecessor, marker);
  }

  public void addNeighbour(Node neighbour) {
    if (neighbour != null) this.neighbours.add(neighbour);
  }

  public boolean exactEquals(Node other) {
    if (this.equals(other)) {
      if (this.source == null && other.source == null) return true;
      return this.source.equals(other.source);
    }
    return false;
  }
}
