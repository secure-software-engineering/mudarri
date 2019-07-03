package analysis;

import main.Config;
import main.SC1Android;
import org.junit.Test;
import reporter.Reporter.PathResolving;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AndroidPathUnitTest extends PathUnitTest {

  public AndroidPathUnitTest() {
  }

  @Test
  public void testTarget01() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("./targets_android/Target01.apk", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(20, 22, 23, 24)));
    setUp("./targets_android/Target01.apk", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("./targets_android/Target01.apk", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  @Test
  public void testTarget02() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("./targets_android/Target02.apk", PathResolving.NONE);
    check(0, 0, 0, ref);

    setUp("./targets_android/Target02.apk", PathResolving.ONE_PATH);
    check(0, 0, 0, ref);

    setUp("./targets_android/Target02.apk", PathResolving.ALL_PATHS);
    check(0, 0, 0, ref);
  }

  @Test
  public void testTarget03() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("./targets_android/Target03.apk", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(23, 25, 26, 27)));
    setUp("./targets_android/Target03.apk", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("./targets_android/Target03.apk", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  @Test
  public void testTarget04() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("./targets_android/Target04.apk", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(19, 20, -1, 24, 25, 26)));
    setUp("./targets_android/Target04.apk", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("./targets_android/Target04.apk", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  @Test
  public void testTarget05() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("./targets_android/Target05.apk", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(20, 21, -1, 25, 26)));
    setUp("./targets_android/Target05.apk", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("./targets_android/Target05.apk", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  @Test
  public void testTarget06() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("./targets_android/Target06.apk", PathResolving.NONE);
    check(0, 0, 0, ref);

    setUp("./targets_android/Target06.apk", PathResolving.ONE_PATH);
    check(0, 0, 0, ref);

    setUp("./targets_android/Target06.apk", PathResolving.ALL_PATHS);
    check(0, 0, 0, ref);
  }

  @Test
  public void testTarget07() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("./targets_android/Target07.apk", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(20, -1, 25, 26)));
    setUp("./targets_android/Target07.apk", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("./targets_android/Target07.apk", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  @Test
  public void testTarget08() {
    Set<Set<Integer>> ref = new HashSet<>();
    setUp("./targets_android/Target08.apk", PathResolving.NONE);
    check(1, 1, 0, ref);

    ref.add(new HashSet<>(Arrays.asList(-1, 16, 17, 19, 20, 8, 13, 14)));
    setUp("./targets_android/Target08.apk", PathResolving.ONE_PATH);
    check(1, 1, 1, ref);

    setUp("./targets_android/Target08.apk", PathResolving.ALL_PATHS);
    check(1, 1, 1, ref);
  }

  private void setUp(String target, PathResolving pathResolvingLevel) {
    SC1Android mta =
      new SC1Android(target, Config.ANDROID_PLATFORMS,
        pathResolvingLevel, "./config/SourcesAndSinks.txt",
        "./config/AndroidCallbacks.txt");
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
