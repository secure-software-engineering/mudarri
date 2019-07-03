package android;

import org.xmlpull.v1.XmlPullParserException;
import soot.G;
import soot.Main;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.cfg.LibraryClassPatcher;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.source.data.ISourceSinkDefinitionProvider;
import soot.options.Options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class SetupApplicationSimple extends SetupApplication {

  public SetupApplicationSimple(String androidJar, String apkFileLocation) {
    super(androidJar, apkFileLocation);
  }

  @Override
  public InfoflowResults runInfoflow(
    ISourceSinkDefinitionProvider sourcesAndSinks) {
    // Reset our object state
    this.collectedSources =
      config.getLogSourcesAndSinks() ? new HashSet<>() : null;
    this.collectedSinks =
      config.getLogSourcesAndSinks() ? new HashSet<>() : null;
    this.sourceSinkProvider = sourcesAndSinks;
    this.dummyMainMethod = null;

    // Perform some sanity checks on the configuration
    if (config.getEnableLifecycleSources() && config.isIccEnabled()) {
      logger
        .warn("ICC model specified, automatically disabling lifecycle sources");
      config.setEnableLifecycleSources(false);
    }

    // Start a new Soot instance
    initializeSoot(true);

    // Perform basic app parsing
    try {
      parseAppResources();
    } catch (IOException | XmlPullParserException e) {
      logger.error("Callgraph construction failed: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Callgraph construction failed", e);
    }

    // In one-component-at-a-time, we do not have a single entry point
    // creator
    List<SootClass> entrypointWorklist;
    if (config.getOneComponentAtATime())
      entrypointWorklist = new ArrayList<>(entrypoints);
    else {
      entrypointWorklist = new ArrayList<>();
      SootClass dummyEntrypoint;
      if (Scene.v().containsClass("dummy"))
        dummyEntrypoint = Scene.v().getSootClass("dummy");
      else dummyEntrypoint = new SootClass("dummy");
      entrypointWorklist.add(dummyEntrypoint);
    }

    // For every entry point (or the dummy entry point which stands for all
    // entry points at once), run the data flow analysis
    while (!entrypointWorklist.isEmpty()) {
      SootClass entrypoint = entrypointWorklist.remove(0);

      // Perform basic app parsing
      try {
        if (config.getOneComponentAtATime())
          calculateCallbacks(sourcesAndSinks, entrypoint);
        else calculateCallbacks(sourcesAndSinks);
      } catch (IOException | XmlPullParserException e) {
        logger.error("Callgraph construction failed: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Callgraph construction failed", e);
      }

      // Create a new entry point and compute the flows in it. If we
      // analyze all components together, we do not need a new callgraph,
      // but can reuse the one from the callback collection phase.
      if (config.getOneComponentAtATime()) {
        createMainMethod(entrypoint);
        constructCallgraphInternal();
      }

      // We don't need the computed callbacks anymore
      this.callbackMethods.clear();
      this.fragmentClasses.clear();
    }

    // We return the aggregated results
    return null;
  }

  @Override
  protected void initializeSoot(boolean constructCallgraph) {
    // Clean up any old Soot instance we may have
    G.reset();

    Options.v().set_no_bodies_for_excluded(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_output_format(Options.output_format_none);
    Options.v().set_whole_program(constructCallgraph);
    Options.v().set_process_dir(Collections.singletonList(apkFileLocation));
    if (forceAndroidJar) Options.v().set_force_android_jar(androidJar);
    else Options.v().set_android_jars(androidJar);
    Options.v().set_src_prec(Options.src_prec_apk_class_jimple);
    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().set_keep_line_number(true);
    Options.v().set_keep_offset(false);
    Options.v().set_throw_analysis(Options.throw_analysis_dalvik);

    // Set the Soot configuration options. Note that this will needs to be
    // done before we compute the classpath.
    if (sootConfig != null) sootConfig.setSootOptions(Options.v());

    Options.v().set_soot_classpath(getClasspath());
    Main.v().autoSetOptions();

    // Configure the callgraph algorithm
    if (constructCallgraph) {
      Options.v().setPhaseOption("cg.spark", "on");
      Options.v().setPhaseOption("cg.spark", "string-constants:true");
      Options.v().setPhaseOption("cg", "all-reachable:true");
    }
    if (config.getEnableReflection())
      Options.v().setPhaseOption("cg", "types-for-invoke:true");

    // Load whatever we need
    Scene.v().loadNecessaryClasses();

    // Make sure that we have valid Jimple bodies
    PackManager.v().getPack("wjpp").apply();

    // Patch the callgraph to support additional edges. We do this now,
    // because during
    // callback discovery, the context-insensitive callgraph algorithm would
    // flood us
    // with invalid edges.
    LibraryClassPatcher patcher = new LibraryClassPatcher();
    patcher.patchLibraries();
  }
}
