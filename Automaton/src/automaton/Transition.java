package automaton;

public class Transition {
  private Node nextNode;
  private String label;

  Transition(Node nextNode, String label) {
    this.nextNode = nextNode;
    this.label = label;
  }

  public String getLabel() {
    return this.label;
  }

  public Node getNextNode() {
    return this.nextNode;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((label == null) ? 0 : label.hashCode());
    result = prime * result + ((nextNode == null) ? 0 : nextNode.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Transition other = (Transition) obj;
    if (label == null) {
      if (other.label != null) return false;
    } else if (!label.equals(other.label)) return false;
    if (nextNode == null) {
      return other.nextNode == null;
    } else return nextNode.equals(other.nextNode);
  }

  @Override
  public String toString() {
    return "[" + this.label + "] -> " + this.nextNode.toString();
  }
}
