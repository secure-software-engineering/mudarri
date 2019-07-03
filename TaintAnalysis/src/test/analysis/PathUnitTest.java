package analysis;

import org.junit.Assert;
import reporter.AnalysisResults;
import reporter.Path;
import reporter.WarningNode;

import java.util.HashSet;
import java.util.Set;

class PathUnitTest extends Tests<Integer> {

  AnalysisResults results;
  Set<Path> paths;
  Set<Set<Integer>> simplifiedPaths;

  Set<Set<Integer>> simplifyPaths(Set<Path> paths) {
    assert paths != null;
    Set<Set<Integer>> simplifiedPaths = new HashSet<>();
    for (Path path : paths) {
      Set<Integer> simplifiedPath = new HashSet<>();
      for (WarningNode node : path.getPath().keySet()) {
        if (node.getAnalysisNode().getUnit() != null) simplifiedPath
          .add(node.getAnalysisNode().getUnit().getJavaSourceStartLineNumber());
      }
      if (!simplifiedPath.isEmpty()) simplifiedPaths.add(simplifiedPath);
    }
    return simplifiedPaths;
  }

  void check(int nbSources, int nbSinks, int nbPaths, Set<Set<Integer>> sp) {
    Assert.assertNotNull(simplifiedPaths);
    Assert.assertEquals(nbSources, results.getNbSources());
    Assert.assertEquals(nbSinks, results.getNbSinks());
    Assert.assertEquals(nbPaths, paths.size());
    if (sp != null) Assert.assertTrue(equals(sp, simplifiedPaths));
  }
}
