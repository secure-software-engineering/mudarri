package marking;

import java.util.HashSet;
import java.util.Set;

public enum Marker {

  NOTHING("Marker used as a placeholder"),

  // Taint markers

  API_BASE_LOCAL(
    "the parameter is tainted, so the object calling the method is tainted"),
  API_ID("the tainted variable is still tainted"),
  API_ID_DEFAULT("the tainted variable is still tainted"),
  API_ID_DEFAULT2("the tainted variable is still tainted"), API_RECIEVER(
    "a parameter is tainted, so the reciever object is tainted " +
      "(left hand-side of the assignment)"), API_RECIEVER2(
    "the object calling the method is tainted, so the reciever object is " +
      "tainted (left hand-side of the assignment)"),

  API_LEFT("the right side is an unknown method, so the left side is tainted"),
  API_RIGHT("the left side is an unknown method, so the right side is tainted"),

  API_APPEND("call to StringBuilder's append method. If the first parameter " +
    "or the StringBuilder object is tainted, then the taint receiver object " +
    "is tainted (left hand-side of the assignment)"), API_ARRAYCOPY(
    "call to System's arrayCopy method. If the first parameter " +
      "is tainted, then the third parameter is tainted"), API_GETCHARS(
    "call to String's getChars method. If the String object is tainted, then " +
      "the third parameter is tainted"), API_TOSTRING(
    "call to String's toString method. If the String object is " +
      "tainted, then the reciever object is tainted (left hand-side of the " +
      "assignment)"), API_OTHERS("the tainted variable is still tainted"),

  CALL_PARAMETERS("the method argument is tainted, so the corresponding " +
    "parameter in the callee method is tainted"), CALL_STATIC(
    "the static field is tainted, so it is also tainted in the callee" +
      " method"), CALL_THIS(
    "the object calling the method is tainted, so the \"this\" " +
      "variable in the callee method is also tainted"),

  COT_RECIEVER("the tainted variable is still tainted"),

  ID_NFF_ASSIGNSTMT("the tainted variable is still tainted"),
  ID_NFF_NO_ASSIGNSTMT("the tainted variable is still tainted"),
  ID_COT_SUSI("the tainted variable is still tainted"),

  PATH_LOOKUP("registering path reconstruction"),

  RIGHT_TO_LEFT_AR("the right hand-side of the assignment is tainted, so the " +
    "array on the left hand-side is tainted as well"), RIGHT_TO_LEFT_IFR(
    "the right hand-side of the assignment is tainted, so the" +
      " field of the object on the left hand-side is tainted as well"),
  RIGHT_TO_LEFT_LOCAL("the right hand-side of the assignment is tainted, so " +
    "the local variable on the left hand-side is tainted as well"),
  RIGHT_TO_LEFT_SFR("the right hand-side of the assignment is tainted, so the" +
    " field of the static object on the left hand-side is tainted as well"),

  RETURN_PARAMETERS(
    "the argument of the callee method is tainted, so the parameter" + " " +
      "in the caller is tainted"), RETURN_RETVAL(
    "the return value of the callee method is tainted, so the receiving " +
      "variable is tainted"), RETURN_STATIC(
    "the static field is tainted in the callee method, so it is " +
      "also tainted in the caller method"), RETURN_THIS(
    "the \"this\" object is tainted in the callee method, so the " +
      "object calling the method in the caller method is tainted"),

  SINK("the variable is used by a dangerous sink method. It is reported and " +
    "its taint is propagated to the next statement"),
  SOURCE("the method generates dangerous data that is stored in the variable"),

  TAINT_ALIAS("the two variables are aliased. Tainting the first one causes " +
    "the second one to be tainted"),

  // Alias markers

  NEW_VALUE("new() statement. The variable is tracked with this statement as " +
    "its allocation site"),
  ALIAS_ID("the alias information is propagated forward"), LEFT_ALIAS_TO_RIGHT(
    "the left variable now has the right variable's allocation sites (the " +
      "two variables are now aliased)"), PARENT_LEFT_ALIAS_TO_RIGHT(
    "because the left variable and the right " +
      "variable are now aliased, their attributes are aliased as well, so the" +
      " left attribute now has the right's allocation sites"), LEFT_ON_ITS_OWN(
    "the left variable is no longer aliased with other " + "variables"),
  PARENT_LEFT_ON_ITS_OWN("because the left variable is no longer aliased with" +
    " other variables, so are its attributes"),
  ALIAS_FIELD("the variables are transitively aliased");

  private final String description;

  Marker(String description) {
    this.description = description;
  }

  public static Set<Marker> callMarkers() {
    Set<Marker> callMarkers = new HashSet<>();
    callMarkers.add(CALL_PARAMETERS);
    callMarkers.add(CALL_STATIC);
    callMarkers.add(CALL_THIS);
    return callMarkers;
  }

  public static Set<Marker> returnMarkers() {
    Set<Marker> returnMarkers = new HashSet<>();
    returnMarkers.add(RETURN_RETVAL);
    returnMarkers.add(RETURN_PARAMETERS);
    returnMarkers.add(RETURN_STATIC);
    returnMarkers.add(RETURN_THIS);
    return returnMarkers;
  }

  @Override
  public String toString() {
    return this.description;
  }
}
