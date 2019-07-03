package reporter;

import automaton.Automaton;
import automaton.Node;
import marking.Marker;

import java.util.LinkedHashMap;

public class Path implements Comparable<Path> {
  private LinkedHashMap<WarningNode, Marker> path;
  private Automaton automaton;

  private String description;

  public Path(Marker initialMarker) {
    // Initialize path.
    this.path = new LinkedHashMap<>();

    // Initialize automaton.
    this.automaton = new Automaton();
    setAsRoot(initialMarker);
  }

  public Path() {
    this.path = new LinkedHashMap<>();
    this.automaton = new Automaton();
  }

  Path(Path path) {
    this.path = new LinkedHashMap<>(path.getPath());
    this.automaton = path.getAutomaton().clone();
  }

  private void setAsRoot(Marker marker) {
    Node node = automaton.addNode(marker.name());
    automaton.setAsRoot(node);
  }

  public void put(WarningNode node, String label, Marker marker,
                  Marker prevMarker) {
    // Build path
    this.path.put(node, marker);

    // Build automaton.
    if (prevMarker != null)
      this.automaton.addTransition(prevMarker.name(), marker.name(), label);
  }

  public Automaton getAutomaton() {
    return this.automaton;
  }

  public LinkedHashMap<WarningNode, Marker> getPath() {
    return this.path;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public int compareTo(Path other) {
    int comp = Integer.compare(this.path.size(), other.getPath().size());
    if (comp != 0 || path.size() < 1) return comp;
    String absthis =
      path.keySet().iterator().next().getAnalysisNode().abstraction();
    String absOther =
      other.getPath().keySet().iterator().next().getAnalysisNode()
        .abstraction();
    return absthis.compareTo(absOther);
  }
}
