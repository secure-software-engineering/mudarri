package analysis;

import automaton.Automaton;
import org.junit.Assert;
import reporter.AnalysisResults;
import reporter.Path;

import java.util.HashSet;
import java.util.Set;

class AutomataUnitTest extends Tests<Automaton> {

  AnalysisResults results;
  Set<Automaton> automata;

  Set<Automaton> simplifyAutomata(Set<Path> paths) {
    assert results != null;
    Set<Automaton> simplifiedAutomata = new HashSet<>();
    for (Path path : paths)
      simplifiedAutomata.add(path.getAutomaton());
    return simplifiedAutomata;
  }

  void check(Set<Automaton> ref) {
    Assert.assertNotNull(ref);
    Assert.assertNotNull(automata);
    Assert.assertTrue(eq(ref, automata));
  }
}
