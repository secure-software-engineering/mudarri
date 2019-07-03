package analysis;

import main.SC1Java;
import org.junit.Test;
import reporter.Reporter.PathResolving;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JavaPathUnitTest extends PathUnitTest {

  @Test
  public void testTarget01() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target01", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(13, 14, 17, 18, 19, 20)));
    setUp("test.targets.java.Target01", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("test.targets.java.Target01", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  @Test
  public void testTarget02() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target02", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(14, 15, -1, 21, 22, 23, 24)));
    setUp("test.targets.java.Target02", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("test.targets.java.Target02", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  @Test
  public void testTarget03() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target03", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(
      Arrays.asList(31, 32, 33, 26, 34, -1, 38, 40, 42, 43, 44, 45)));
    setUp("test.targets.java.Target03", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("test.targets.java.Target03", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  @Test
  public void testTarget04() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target04", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(16, 17, -1, 22, 24, 25, 26, 27)));
    setUp("test.targets.java.Target04", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("test.targets.java.Target04", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  @Test
  public void testTarget05() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target05", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(
      Arrays.asList(26, 27, 28, 21, 29, -1, 17, 31, 33, 34, 35, 36)));
    setUp("test.targets.java.Target05", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("test.targets.java.Target05", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  @Test
  public void testTarget06() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target06", PathResolving.NONE);
    check(0, 0, 0, ref);

    setUp("test.targets.java.Target06", PathResolving.ONE_PATH);
    check(0, 0, 0, ref);

    setUp("test.targets.java.Target06", PathResolving.ALL_PATHS);
    check(0, 0, 0, ref);
  }

  @Test
  public void testTarget07() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target07", PathResolving.NONE);
    check(2, 2, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(14, 16, 19, 22, 23, 24, 25, 26)));
    ref.add(new HashSet<>(Arrays.asList(13, 14, 16, 19, 22, 23, 24, 25)));
    setUp("test.targets.java.Target07", PathResolving.ONE_PATH);
    check(2, 2, 2, ref);

    ref.add(new HashSet<>(Arrays.asList(13, 14, 16, 17, 22, 23, 24, 25)));
    ref.add(new HashSet<>(Arrays.asList(13, 14, 16, 17, 22, 23, 24, 25, 26)));
    setUp("test.targets.java.Target07", PathResolving.ALL_PATHS);
    check(2, 2, 4, ref);
  }

  @Test
  public void testTarget08() {
    // NOTE: Because of the ONE_PATH policy, only one of the sources is found.
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target08", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(18, 19, 20, 23, 24, 25, 26)));
    setUp("test.targets.java.Target08", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    ref.add(new HashSet<>(Arrays.asList(16, 19, 20, 23, 24, 25, 26)));
    setUp("test.targets.java.Target08", PathResolving.ALL_PATHS);
    check(2, 1, 2, ref);
  }

  @Test
  public void testTarget09() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target09", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(13, 15, 18, 21, 22, 23, 24)));
    setUp("test.targets.java.Target09", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    ref.add(new HashSet<>(Arrays.asList(13, 15, 16, 21, 22, 23, 24)));
    setUp("test.targets.java.Target09", PathResolving.ALL_PATHS);
    check(1, 1, 2, ref);
  }

  @Test
  public void testTarget10() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target10", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(15, 16, 20, 21, 22, 23)));
    setUp("test.targets.java.Target10", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("test.targets.java.Target10", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  @Test
  public void testTarget11() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target11", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(13, 14, 17, -1, 26, 32, 33, 34, 35)));
    setUp("test.targets.java.Target11", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    ref.add(
      new HashSet<>(Arrays.asList(13, 14, 15, -1, 21, 22, 32, 33, 34, 35)));
    setUp("test.targets.java.Target11", PathResolving.ALL_PATHS);
    check(1, 1, 2, ref);
  }

  @Test
  public void testTarget12() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target12", PathResolving.NONE);
    check(2, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(18, 19, -1, 25, 26, 27, 28)));
    ref.add(new HashSet<>(Arrays.asList(13, 14, -1, 25, 26, 27, 28)));
    setUp("test.targets.java.Target12", PathResolving.ONE_PATH);
    check(2, 1, 2, ref);

    setUp("test.targets.java.Target12", PathResolving.ALL_PATHS);
    check(2, 1, 2, ref);
  }

  @Test
  public void testTarget13() {
    // NOTE: Because of the ONE_PATH policy, only one of the sources is found.
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target13", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(15, 16, 17, 20, 23, 24, 25, 26)));
    setUp("test.targets.java.Target13", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    ref.add(new HashSet<>(Arrays.asList(14, 15, 16, 17, 18, 23, 24, 25, 26)));
    setUp("test.targets.java.Target13", PathResolving.ALL_PATHS);
    check(2, 1, 2, ref);
  }

  @Test
  public void testTarget14() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target14", PathResolving.NONE);
    check(2, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(19, 20, -1, 26, 27, 28, 29)));
    ref.add(new HashSet<>(Arrays.asList(14, 15, -1, 26, 27, 28, 29)));
    setUp("test.targets.java.Target14", PathResolving.ONE_PATH);
    check(2, 1, 2, ref);

    setUp("test.targets.java.Target14", PathResolving.ALL_PATHS);
    check(2, 1, 2, ref);
  }

  @Test
  public void testTarget15() {
    // Flaky test: the three paths for ONE_PATH can be any among the five for
    // ALL_PATHS.
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target15", PathResolving.NONE);
    check(1, 3, 0, ref);

    // No testing of the paths here because the test is flaky: the three paths
    // for ONE_PATH can be any among the five for ALL_PATHS.
    setUp("test.targets.java.Target15", PathResolving.ONE_PATH);
    check(1, 3, 3, null);

    ref.add(new HashSet<>(
      Arrays.asList(-1, 16, 34, 35, 36, 37, 38, 26, 27, 29, 14, 15)));
    ref.add(
      new HashSet<>(Arrays.asList(-1, 16, 34, 35, 36, 37, 26, 27, 29, 14, 15)));
    ref.add(new HashSet<>(Arrays.asList(-1, 22, 23, 24, 25, 14, 15)));
    ref.add(new HashSet<>(
      Arrays.asList(-1, 34, 35, 36, 37, 14, 15, 16, 22, 23, 24, 25, 28)));
    ref.add(new HashSet<>(
      Arrays.asList(-1, 34, 35, 36, 37, 38, 14, 15, 16, 22, 23, 24, 25, 28)));
    setUp("test.targets.java.Target15", PathResolving.ALL_PATHS);
    check(1, 3, 5, ref);
  }

  @Test
  public void testTarget16() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target16", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(14, 15, 19, 20, 21, 22)));
    setUp("test.targets.java.Target16", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("test.targets.java.Target16", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  @Test
  public void testTarget17() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target17", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(-1, 18, 19, 20, 21, 28, 14, 15)));
    setUp("test.targets.java.Target17", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("test.targets.java.Target17", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  @Test
  public void testTarget18() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target18", PathResolving.NONE);
    check(2, 2, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(-1, 18, 19, 20, 21, 42, 14, 15)));
    ref.add(new HashSet<>(Arrays.asList(-1, 32, 33, 34, 35, 42, 28, 29)));
    setUp("test.targets.java.Target18", PathResolving.ONE_PATH);
    check(2, 2, 2, ref);

    setUp("test.targets.java.Target18", PathResolving.ALL_PATHS);
    check(2, 2, 2, ref);
  }

  @Test
  public void testTarget19() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("test.targets.java.Target19", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(
      new HashSet<>(Arrays.asList(22, 23, 24, 25, 27, 28, 32, 33, 34, 35)));
    setUp("test.targets.java.Target19", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("test.targets.java.Target19", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  private void setUp(String target, PathResolving pathResolvingLevel) {
    SC1Java mta = new SC1Java(target, "./target/classes", pathResolvingLevel);
    try {
      mta.runAnalysis();
      results = mta.getAnalysisResults();
      paths = results.getPaths();
      simplifiedPaths = simplifyPaths(paths);
    } catch (Exception e) {
      System.err.println("Error " + "running the analysis.");
      e.printStackTrace();
    }
  }
}
