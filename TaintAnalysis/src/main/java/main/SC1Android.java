package main;

import analysis.TaintAnalysisFlowFunctions;
import analysis.TaintAnalysisTransformer;
import android.EntryPointsManager;
import android.SetupApplicationSimple;
import reporter.Reporter.PathResolving;
import soot.Main;
import soot.PackManager;
import soot.Transform;
import soot.jimple.infoflow.source.ISourceSinkManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SC1Android extends SC1Runner {

  private final String apk;
  private final String androidJars;
  private final String susiPath;
  private final String callbacksPaths;
  private SetupApplicationSimple app;

  public SC1Android(String apk, String androidJars,
                    PathResolving pathResolvingLevel, String susiPath,
                    String callbacksPaths) {
    super(pathResolvingLevel);
    this.apk = apk;
    this.androidJars = androidJars;
    this.susiPath = susiPath;
    this.callbacksPaths = callbacksPaths;
    this.entryPointsManager = new EntryPointsManager() {
      @Override
      public Set<String> entryPoints() {
        assert (app != null);
        return new HashSet<>(
          Collections.singletonList(app.getDummyMainMethod().getSignature()));
      }
    };
  }

  SC1Android(String apk, String androidJars, PathResolving pathResolvingLevel) {
    this(apk, androidJars, pathResolvingLevel,
      "./TaintAnalysis/config/SourcesAndSinks.txt",
      "./TaintAnalysis/config/AndroidCallbacks.txt");
  }

  public static void main(String[] args) {
    String apk = "./TaintAnalysis/targets_android/Target08.apk";
    String androidJars = Config.ANDROID_PLATFORMS;
    String susiPath = "./TaintAnalysis/config/SourcesAndSinks.txt";
    String callbacksPaths = "./TaintAnalysis/config/AndroidCallbacks.txt";
    SC1Android main =
      new SC1Android(apk, androidJars, PathResolving.ALL_PATHS, susiPath,
        callbacksPaths);
    main.runAnalysis();
    main.getAnalysisResults();
    main.printResults();
  }

  public void runAnalysis() {
    app = new SetupApplicationSimple(androidJars, apk);
    app.setCallbackFile(callbacksPaths);

    try {
      app.runInfoflow(susiPath);
      setTaintAnalysisTransformer();
      Main.main(
        new String[]{app.getDummyMainMethod().getDeclaringClass().getName()});
    } catch (Exception e) {
      System.err.println("Error running the analysis.");
      e.printStackTrace();
    }
  }

  protected void setTaintAnalysisTransformer() {
    assert (app != null);
    assert (entryPointsManager != null);
    assert (reporter != null);

    ISourceSinkManager susiManager = app.getSourceSinkManager();
    susiManager.initialize();

    Transform transform = new Transform("wjtp.taintanalysis",
      new TaintAnalysisTransformer(reporter, entryPointsManager,
        new TaintAnalysisFlowFunctions(reporter, susiManager)));
    PackManager.v().getPack("wjtp").add(transform);
  }
}
