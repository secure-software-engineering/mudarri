package automaton;

public enum AutomatonType {
  STATES(0), NO_LABELS(1), LABELS(2);

  private int value;

  AutomatonType(int value) { this.value = value; }

  public static AutomatonType getType(int value) {
    for (AutomatonType at : AutomatonType.values())
      if (at.getValue() == value) return at;
    return null;
  }

  public static String description() {
    StringBuilder sb = new StringBuilder();
    for (AutomatonType at : AutomatonType.values())
      sb.append(", " + at.getValue() + "=" + at.name());
    return sb.toString().length() > 2 ? sb.toString().substring(2) :
      sb.toString();
  }

  int getValue() { return this.value; }
}
