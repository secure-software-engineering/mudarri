package automaton;

import java.util.ArrayList;
import java.util.List;

public class Automaton {

  public static String ROOT = "[[ROOT]] ";
  // This is only used for the evaluation. Don't use anywhere else.
  public String data = "";
  private int id;
  private List<Node> roots = new ArrayList<>();
  private List<Node> nodes = new ArrayList<>();

  public void setId(int id) { this.id = id; }

  public int id() { return this.id; }

  public List<Node> getNodes() { return nodes; }

  public Node addNode(String nodeStep) {
    return addOrMerge(this.nodes, new Node(nodeStep));
  }

  private void addTransition(Node orig, Node dest, String label) {
    orig = addOrMerge(this.nodes, orig);
    dest = addOrMerge(this.nodes, dest);
    Transition transition = new Transition(dest, label);
    orig.addTransition(transition);
  }

  public int countTransitions() {
    int count = 0;
    for (Node node : this.nodes) count += node.getTransitions().size();
    return count;
  }

  public void addTransition(String origString, String destString,
                            String label) {
    this.addTransition(new Node(origString), new Node(destString), label);
  }

  public void setAsRoot(Node node) {
    if (nodes.contains(node)) addOrMerge(this.roots, node);
    else throw new RuntimeException("Node does not exist: " + node);
  }

  private Node addOrMerge(List<Node> list, Node node) {
    if (!list.contains(node)) {
      list.add(node);
      return node;
    }
    Node oldNode = list.get(list.indexOf(node));
    oldNode.merge(node);
    return oldNode;
  }

  @Override
  public Automaton clone() {
    Automaton clone = new Automaton();
    clone.setId(0);
    for (Node node : this.nodes) {
      // Copy node.
      Node newNode = clone.addNode(node.getAnalysisStep());
      // If root, set as root.
      if (this.roots.contains(node)) clone.setAsRoot(newNode);
      // Copy transitions.
      for (Transition transition : node.getTransitions())
        clone.addTransition(node.getAnalysisStep(),
          transition.getNextNode().getAnalysisStep(), transition.getLabel());
    }
    return clone;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((roots == null) ? 0 : roots.hashCode());
    if (nodes == null) return result;
    for (Node node : nodes) {
      // += because we don't care about the order of the nodes.
      result += node.hashCode();
      for (Transition transition : node.getTransitions())
        result += transition.hashCode();
    }
    return result * prime;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Automaton other = (Automaton) obj;
    if (roots == null) {
      if (other.roots != null) return false;
    } else if (!roots.equals(other.roots)) return false;
    if (nodes == null) {
      return other.nodes == null;
    } else if (other.nodes == null) {
      return false;
    } else {
      if (nodes.size() != other.nodes.size()) return false;
      for (Node node : nodes) {
        boolean exactContains = false;
        for (Node otherNode : other.nodes) {
          if (node.exactEquals(otherNode)) {
            exactContains = true;
            break;
          }
        }
        if (!exactContains) return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Node node : this.nodes) {
      if (roots.contains(node)) sb.append(ROOT);
      sb.append(node.toString()).append("\n");
      for (Transition transition : node.getTransitions())
        sb.append("\t").append(transition.toString()).append("\n");
    }
    return sb.toString();
  }
}
