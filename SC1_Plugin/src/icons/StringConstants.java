package icons;

import com.intellij.codeInsight.daemon.GroupNames;

public class StringConstants {

  public static final String HELP_TITLE = "Help - SC1 analysis";
  public static final String HELP_TEXT_DEFAULT = "SC1 runs a taint analysis.";
  public static final String HELP_TEXT_1 =
    "SC1 runs a taint analysis.\nWarning syntax: l.X ([pvar] -> )[cvar] " +
      "data\n\tX = line number\n\tpvar = previously tainted variable\n\tcvar " +
      "= current tainted variable\n\tdata = Java statement";
  public static final String HELP_TEXT_2 =
    "SC1 runs a taint analysis.\nWarning syntax: l.X ([pvar] -> )[cvar] " +
      "data\n\tX = line number\n\tpvar = previously tainted variable\n\tcvar " +
      "= current tainted variable\n\tdata = reason for the taint transfer";
  public static final String HELP_TEXT_3 =
    "SC1 runs a taint analysis. The analysis warnings are in the right view, " +
      "fix suggestions are in the bottom views.\nWarning syntax: l.X ([pvar] " +
      "-> )[cvar] " +
      "data\n\tX = line number\n\tpvar = previously tainted variable\n\tcvar " +
      "= current tainted variable\n\tdata = reason for the taint transfer";

  public static final String RUNNING = "Running analysis...";
  public static final String NOTHING_FOUND = "No data leaks found";
  public static final String TODO_RUN = "Run the analysis to see the results.";

  public static final String INSPECTION_SHORT_NAME = "SC1Analysis";
  public static final String INSPECTION_DISPLAY_NAME = "SC1 taint analysis";
  public static final String INSPECTION_DESCRIPTION = "SC1 taint analysis";
  public static final String GROUP_DISPLAY_NAME = GroupNames.BUGS_GROUP_NAME;

  public static final String QUICK_FIX_SOURCE = "SC1 source quick fix";
  public static final String QUICK_FIX_SINK = "SC1 sink quick fix";
  public static final String QUICK_FIX_PATH = "SC1 path quick fix";
}
