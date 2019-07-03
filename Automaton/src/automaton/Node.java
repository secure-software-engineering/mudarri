package automaton;

import java.util.HashSet;
import java.util.Set;

public class Node {
  private String analysisStep;
  private Set<Transition> transitions;

  public Node(String analysisStep) {
    this.analysisStep = analysisStep;
    this.transitions = new HashSet<>();
  }

  public String getAnalysisStep() {
    return this.analysisStep;
  }

  public Set<Transition> getTransitions() {
    return this.transitions;
  }

  void addTransition(Transition transition) {
    this.transitions.add(transition);
  }

  void merge(Node node) {
    if (!this.analysisStep.equals(node.getAnalysisStep()))
      throw new RuntimeException("Nodes not equal.");
    this.transitions.addAll(node.getTransitions());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
      prime * result + ((analysisStep == null) ? 0 : analysisStep.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Node other = (Node) obj;
    if (analysisStep == null) {
      return other.analysisStep == null;
    } else return analysisStep.equals(other.analysisStep);
  }

  @Override
  public String toString() {
    // NOTE: Never recurse here. Do not call the transitions.
    return this.analysisStep;
  }

  boolean exactEquals(Node otherNode) {
    if (!this.analysisStep.equals(otherNode.getAnalysisStep())) return false;
    return this.transitions.equals(otherNode.getTransitions());
  }
}
