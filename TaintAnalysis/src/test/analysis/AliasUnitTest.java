package analysis;

import alias.EquivValue;
import alias.LocalMayAliasAnalysisWithFields;
import org.junit.Assert;
import org.junit.Test;
import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SourceLocator;
import soot.Transform;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AliasUnitTest extends Tests<String> {

  private LocalMayAliasAnalysisWithFields lmaawf;

  @Test
  public void testTarget01() {
    runAliasAnalysis("test.targets.alias.Target01");
    Set<Set<String>> stringResults = toStringsSet(lmaawf.mayAliasesAtExit());

    Set<Set<String>> ref = new HashSet<>();
    ref.add(new HashSet<>(Collections.singletonList("$i0")));
    ref.add(new HashSet<>(Collections.singletonList("$i1")));
    ref.add(new HashSet<>(Collections.singletonList("this")));
    ref.add(new HashSet<>(Arrays.asList(".out", "$r0")));

    Assert.assertTrue(equals(stringResults, ref));
  }

  @Test
  public void testTarget02() {
    runAliasAnalysis("test.targets.alias.Target02");
    Set<Set<String>> stringResults = toStringsSet(lmaawf.mayAliasesAtExit());

    Set<Set<String>> ref = new HashSet<>();
    ref.add(new HashSet<>(Collections.singletonList("$r0")));
    ref.add(new HashSet<>(Arrays.asList("$r0.x", "$i0")));
    ref.add(new HashSet<>(Collections.singletonList("this")));
    ref.add(new HashSet<>(Arrays.asList(".out", "$r1")));

    Assert.assertTrue(equals(stringResults, ref));
  }

  private void setSootOptions(String sootCp, String className) {
    G.reset();

    Options.v().set_soot_classpath(sootCp);
    Options.v().set_whole_program(false);
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

  private void runAliasAnalysis(String target) {
    setSootOptions("./target/classes", target);
    PackManager.v().getPack("jtp")
      .add(new Transform("jtp.myTransform", new BodyTransformer() {
        @Override
        protected void internalTransform(Body b, String phaseName,
                                         Map<String, String> options) {
          lmaawf =
            new LocalMayAliasAnalysisWithFields(new ExceptionalUnitGraph(b));
        }
      }));
    soot.Main.main(new String[]{target});
  }

  private Set<Set<String>> toStringsSet(Set<Set<EquivValue>> results) {
    Set<Set<String>> res = new HashSet<>();
    for (Set<EquivValue> set : results) {
      Set<String> r = new HashSet<>();
      for (EquivValue eq : set)
        r.add(eq.abstraction());
      res.add(r);
    }
    return res;
  }
}
