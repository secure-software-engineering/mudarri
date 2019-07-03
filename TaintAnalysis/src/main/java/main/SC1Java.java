package main;

import analysis.TaintAnalysisFlowFunctions;
import analysis.TaintAnalysisTransformer;
import android.EntryPointsManager;
import reporter.Reporter.PathResolving;
import soot.G;
import soot.Main;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Transform;
import soot.options.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SC1Java extends SC1Runner {

  // Leave targetClass empty to scan all files in targetDir.
  private final String targetClass;
  private final String targetDir;

  public SC1Java(String targetClass, String targetDir,
                 PathResolving pathResolvingLevel) {
    super(pathResolvingLevel);
    this.targetClass = targetClass;
    this.targetDir = targetDir;
    this.entryPointsManager = new EntryPointsManager() {
      @Override
      public Set<String> entryPoints() {
        // TODO: Better management of entry points.
        String[] entryPointPatterns =
          new String[]{": void main(", "doGet", "doPost"};
        Set<String> entryPoints = new HashSet<>();
        for (SootClass sc : Scene.v().getApplicationClasses()) {
          for (SootMethod sm : sc.getMethods()) {
            for (String pattern : entryPointPatterns) {
              if (sm.getSignature().contains(pattern))
                entryPoints.add(sm.getSignature());
            }
          }
        }
        return entryPoints;
      }
    };
  }

  public static void main(String[] args) {
    String targetClass = "test.targets.java.Target15";
    SC1Java main = new SC1Java(targetClass, "./TaintAnalysis/target/classes",
      PathResolving.ALL_PATHS);
    main.runAnalysis();
    main.getAnalysisResults();
    main.printResults();
  }

  public void runAnalysis() {
    G.reset();
    setTaintAnalysisTransformer();
    setSootOptions(targetDir, targetClass);
    String target = targetClass;
    if (target.isEmpty()) target = ".";
    Main.main(new String[]{target});
  }

  protected void setTaintAnalysisTransformer() {
    assert (entryPointsManager != null);
    assert (reporter != null);

    Transform transform = new Transform("wjtp.taintanalysis",
      new TaintAnalysisTransformer(reporter, entryPointsManager,
        new TaintAnalysisFlowFunctions(reporter, null)));
    PackManager.v().getPack("wjtp").add(transform);
  }

  private void setSootOptions(String sootCp, String className) {
    Options.v().set_soot_classpath(sootCp);

    Options.v().set_whole_program(true);
    Options.v().setPhaseOption("cg.spark", "on");
    Options.v().setPhaseOption("cg.spark", "string-constants:true");
    Options.v().setPhaseOption("cg", "all-reachable:true");

    Options.v().set_no_bodies_for_excluded(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().set_keep_line_number(true);
    Options.v().set_prepend_classpath(true);

    Options.v().set_exclude(new ArrayList<>(Arrays.asList("java.", "javax.")));
    Options.v().set_src_prec(Options.src_prec_only_class);
    Options.v().set_output_format(Options.output_format_none);

    if (className.isEmpty()) Options.v()
      .set_process_dir(new ArrayList<>(Collections.singletonList(sootCp)));
    else loadClassAndSupport(sootCp, className);

    Scene.v().addBasicClass("java.lang.StringBuilder");
    Scene.v().loadNecessaryClasses();
  }

  private void loadClassAndSupport(String directory, String className) {
    for (String cl : SourceLocator.v().getClassesUnder(directory)) {
      if (cl.equals(className) || cl.startsWith(className + "$")) {
        SootClass sc = Scene.v().forceResolve(cl, SootClass.BODIES);
        if (!sc.isPhantom()) sc.setApplicationClass();
      }
    }
  }
}
