package analysis;

import automaton.Automaton;
import automaton.Node;
import main.SC1Java;
import marking.Marker;
import org.junit.Test;
import reporter.Reporter.PathResolving;

import java.util.HashSet;
import java.util.Set;

public class JavaAutomataUnitTest extends AutomataUnitTest {

  @Test
  public void testTarget01() {
    Set<Automaton> ref = new HashSet<>();
    Automaton automaton = new Automaton();
    Node root = automaton.addNode(Marker.SOURCE.name());
    automaton.setAsRoot(root);
    automaton
      .addTransition(Marker.SOURCE.name(), Marker.RIGHT_TO_LEFT_LOCAL.name(),
        "alias;14;test/targets/java/Target01.java");
    automaton.addTransition(Marker.RIGHT_TO_LEFT_LOCAL.name(),
      Marker.API_ID_DEFAULT2.name(),
      "alias;17;test/targets/java/Target01.java");
    automaton.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "alias;19;test/targets/java/Target01.java");
    automaton.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.ID_NFF_ASSIGNSTMT.name(),
      "alias;19;test/targets/java/Target01.java");
    automaton.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "alias;18;test/targets/java/Target01.java");
    automaton
      .addTransition(Marker.API_ID_DEFAULT2.name(), Marker.API_APPEND.name(),
        "$r2;19;test/targets/java/Target01.java");
    automaton.addTransition(Marker.ID_NFF_ASSIGNSTMT.name(),
      Marker.API_ID_DEFAULT2.name(),
      "alias;19;test/targets/java/Target01.java");
    automaton.addTransition(Marker.API_APPEND.name(), Marker.API_APPEND.name(),
      "$r3;19;test/targets/java/Target01.java");
    automaton
      .addTransition(Marker.API_APPEND.name(), Marker.API_TOSTRING.name(),
        "query;19;test/targets/java/Target01.java");
    automaton.addTransition(Marker.API_TOSTRING.name(), Marker.SINK.name(),
      "query;20;test/targets/java/Target01.java");
    ref.add(automaton);

    setUp("test.targets.java.Target01", PathResolving.ALL_PATHS);
    check(ref);
  }

  @Test
  public void testTarget07() {
    Set<Automaton> ref = new HashSet<>();

    Automaton automaton1 = new Automaton();
    Node root1 = automaton1.addNode(Marker.SOURCE.name());
    automaton1.setAsRoot(root1);
    automaton1
      .addTransition(Marker.SOURCE.name(), Marker.API_ID_DEFAULT2.name(),
        "userId;14;test/targets/java/Target07.java");
    automaton1.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "userId;16;test/targets/java/Target07.java");
    automaton1
      .addTransition(Marker.API_ID_DEFAULT2.name(), Marker.API_APPEND.name(),
        "$r3;24;test/targets/java/Target07.java");
    automaton1.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.ID_NFF_NO_ASSIGNSTMT.name(),
      "userId;16;test/targets/java/Target07.java");
    automaton1.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "userId;23;test/targets/java/Target07.java");
    automaton1.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.ID_NFF_ASSIGNSTMT.name(),
      "userId;24;test/targets/java/Target07.java");
    automaton1.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "userId;24;test/targets/java/Target07.java");
    automaton1
      .addTransition(Marker.API_APPEND.name(), Marker.API_TOSTRING.name(),
        "query;24;test/targets/java/Target07.java");
    automaton1.addTransition(Marker.API_APPEND.name(), Marker.API_APPEND.name(),
      "$r4;24;test/targets/java/Target07.java");
    automaton1.addTransition(Marker.ID_NFF_NO_ASSIGNSTMT.name(),
      Marker.ID_NFF_ASSIGNSTMT.name(),
      "userId;19;test/targets/java/Target07.java");
    automaton1.addTransition(Marker.ID_NFF_ASSIGNSTMT.name(),
      Marker.API_ID_DEFAULT2.name(),
      "userId;22;test/targets/java/Target07.java");
    automaton1.addTransition(Marker.ID_NFF_ASSIGNSTMT.name(),
      Marker.API_ID_DEFAULT2.name(),
      "userId;24;test/targets/java/Target07.java");
    automaton1.addTransition(Marker.API_TOSTRING.name(), Marker.SINK.name(),
      "query;25;test/targets/java/Target07.java");
    ref.add(automaton1);

    Automaton automaton2 = new Automaton();
    Node root2 = automaton2.addNode(Marker.SOURCE.name());
    automaton2.setAsRoot(root2);
    automaton2
      .addTransition(Marker.SOURCE.name(), Marker.API_ID_DEFAULT2.name(),
        "userId;14;test/targets/java/Target07.java");
    automaton2.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "userId;16;test/targets/java/Target07.java");
    automaton2
      .addTransition(Marker.API_ID_DEFAULT2.name(), Marker.API_APPEND.name(),
        "$r3;24;test/targets/java/Target07.java");
    automaton2.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.ID_NFF_NO_ASSIGNSTMT.name(),
      "userId;16;test/targets/java/Target07.java");
    automaton2.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "userId;23;test/targets/java/Target07.java");
    automaton2.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.ID_NFF_ASSIGNSTMT.name(),
      "userId;24;test/targets/java/Target07.java");
    automaton2.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "userId;24;test/targets/java/Target07.java");
    automaton2
      .addTransition(Marker.API_APPEND.name(), Marker.API_TOSTRING.name(),
        "query;24;test/targets/java/Target07.java");
    automaton2.addTransition(Marker.API_APPEND.name(), Marker.API_APPEND.name(),
      "$r4;24;test/targets/java/Target07.java");
    automaton2.addTransition(Marker.ID_NFF_NO_ASSIGNSTMT.name(),
      Marker.API_ID_DEFAULT2.name(),
      "userId;22;test/targets/java/Target07.java");
    automaton2.addTransition(Marker.ID_NFF_NO_ASSIGNSTMT.name(),
      Marker.ID_NFF_ASSIGNSTMT.name(),
      "userId;17;test/targets/java/Target07.java");
    automaton2.addTransition(Marker.ID_NFF_ASSIGNSTMT.name(),
      Marker.ID_NFF_NO_ASSIGNSTMT.name(),
      "userId;17;test/targets/java/Target07.java");
    automaton2.addTransition(Marker.ID_NFF_ASSIGNSTMT.name(),
      Marker.API_ID_DEFAULT2.name(),
      "userId;24;test/targets/java/Target07.java");
    automaton2.addTransition(Marker.API_TOSTRING.name(), Marker.SINK.name(),
      "query;25;test/targets/java/Target07.java");
    ref.add(automaton2);

    Automaton automaton3 = new Automaton();
    Node root3 = automaton3.addNode(Marker.SOURCE.name());
    automaton3.setAsRoot(root3);
    automaton3
      .addTransition(Marker.SOURCE.name(), Marker.API_ID_DEFAULT2.name(),
        "password;16;test/targets/java/Target07.java");
    automaton3.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "indeterminate;24;test/targets/java/Target07.java");
    automaton3.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "indeterminate;25;test/targets/java/Target07.java");
    automaton3.addTransition(Marker.API_ID_DEFAULT2.name(), Marker.SINK.name(),
      "indeterminate;26;test/targets/java/Target07.java");
    automaton3.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.ID_NFF_NO_ASSIGNSTMT.name(),
      "password;16;test/targets/java/Target07.java");
    automaton3.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "indeterminate;23;test/targets/java/Target07.java");
    automaton3.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "password;16;test/targets/java/Target07.java");
    automaton3.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.ID_NFF_ASSIGNSTMT.name(),
      "indeterminate;24;test/targets/java/Target07.java");
    automaton3.addTransition(Marker.ID_NFF_NO_ASSIGNSTMT.name(),
      Marker.RIGHT_TO_LEFT_LOCAL.name(),
      "indeterminate;19;test/targets/java/Target07.java");
    automaton3.addTransition(Marker.ID_NFF_ASSIGNSTMT.name(),
      Marker.API_ID_DEFAULT2.name(),
      "indeterminate;24;test/targets/java/Target07.java");
    automaton3.addTransition(Marker.RIGHT_TO_LEFT_LOCAL.name(),
      Marker.API_ID_DEFAULT2.name(),
      "indeterminate;22;test/targets/java/Target07.java");
    ref.add(automaton3);

    Automaton automaton4 = new Automaton();
    Node root4 = automaton4.addNode(Marker.SOURCE.name());
    automaton4.setAsRoot(root4);
    automaton4
      .addTransition(Marker.SOURCE.name(), Marker.API_ID_DEFAULT2.name(),
        "userId;14;test/targets/java/Target07.java");
    automaton4.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "indeterminate;24;test/targets/java/Target07.java");
    automaton4.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "indeterminate;25;test/targets/java/Target07.java");
    automaton4.addTransition(Marker.API_ID_DEFAULT2.name(), Marker.SINK.name(),
      "indeterminate;26;test/targets/java/Target07.java");
    automaton4.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "userId;16;test/targets/java/Target07.java");
    automaton4.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.ID_NFF_NO_ASSIGNSTMT.name(),
      "userId;16;test/targets/java/Target07.java");
    automaton4.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.API_ID_DEFAULT2.name(),
      "indeterminate;23;test/targets/java/Target07.java");
    automaton4.addTransition(Marker.API_ID_DEFAULT2.name(),
      Marker.ID_NFF_ASSIGNSTMT.name(),
      "indeterminate;24;test/targets/java/Target07.java");
    automaton4.addTransition(Marker.ID_NFF_NO_ASSIGNSTMT.name(),
      Marker.RIGHT_TO_LEFT_LOCAL.name(),
      "indeterminate;17;test/targets/java/Target07.java");
    automaton4.addTransition(Marker.ID_NFF_NO_ASSIGNSTMT.name(),
      Marker.API_ID_DEFAULT2.name(),
      "indeterminate;22;test/targets/java/Target07.java");
    automaton4.addTransition(Marker.ID_NFF_ASSIGNSTMT.name(),
      Marker.API_ID_DEFAULT2.name(),
      "indeterminate;24;test/targets/java/Target07.java");
    automaton4.addTransition(Marker.RIGHT_TO_LEFT_LOCAL.name(),
      Marker.ID_NFF_NO_ASSIGNSTMT.name(),
      "indeterminate;17;test/targets/java/Target07.java");
    ref.add(automaton4);

    setUp("test.targets.java.Target07", PathResolving.ALL_PATHS);
    check(ref);
  }

  private void setUp(String target, PathResolving pathResolvingLevel) {
    SC1Java mta = new SC1Java(target, "./target/classes", pathResolvingLevel);
    try {
      mta.runAnalysis();
      results = mta.getAnalysisResults();
      automata = simplifyAutomata(results.getPaths());
    } catch (Exception e) {
      System.err.println("Error " + "running the analysis.");
      e.printStackTrace();
    }
  }
}
