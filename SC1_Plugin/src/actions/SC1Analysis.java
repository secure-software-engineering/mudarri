package actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.impl.ModuleImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import main.Config;
import main.SC1Android;
import main.SC1Java;
import main.SC1Runner;
import marking.Marker;
import notifiers.IAnalysisNotifier;
import reporter.AnalysisResults;
import reporter.Path;
import reporter.Reporter;
import reporter.WarningNode;
import state.StateSingleton;
import ui.FileOperations;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SC1Analysis implements CompileStatusNotification {

  private final String androidJars = Config.ANDROID_PLATFORMS;
  private final String susi = "./config/SourcesAndSinks.txt";
  private final String callbacks = "./config/SourcesAndSinks.txt/AndroidCallbacks.txt";

  private final AnActionEvent event;
  private Module module;
  private Project project;
  private Set<String> sourceRoots;
  private Nature nature;
  private VirtualFile outPath; // For Java.
  private File apkFile; // For Android.

  SC1Analysis(AnActionEvent event) {
    this.event = event;
    initialize();
  }

  Module getModule() { return this.module; }

  private void initialize() {
    project = event.getProject();

    // Get module and current file.
    module = (ModuleImpl) event.getDataContext().getData(LangDataKeys.MODULE);
    // If no module is currently selected, try to get the current editor's.
    if (module == null) {
      FileEditorManager fem = FileEditorManager.getInstance(project);
      VirtualFile vFile = fem.getSelectedEditor().getFile();
      ProjectRootManager p = ProjectRootManager.getInstance(project);
      ProjectFileIndex index = p.getFileIndex();
      module = index.getModuleForFile(vFile);
    }
    // If module is still not resolved, abort.
    if (module == null) {
      Messages.showMessageDialog("No module selected", "Error - SC1 analysis",
        Messages.getErrorIcon());
      return;
    }

    // Not a Java nor Android program.
    if (!(ModuleType.get(module) instanceof JavaModuleType)) {
      Messages.showMessageDialog("The module is not a Java module.",
        "Error -" + " SC1 analysis", Messages.getErrorIcon());
      return;
    }

    // Get path to class files. Abort if they cannot be resolved.
    outPath =
      CompilerModuleExtension.getInstance(module).getCompilerOutputPath();
    if (outPath == null) {
      Messages.showMessageDialog("Project " + module.getName() + " not built.",
        "Error - SC1 analysis", Messages.getErrorIcon());
      return;
    }

    // Get path to source files (used to retrieve the Java units at reporting).
    sourceRoots = new HashSet<>();
    for (VirtualFile file : ModuleRootManager.getInstance(module)
      .getSourceRoots())
      sourceRoots.add(file.getCanonicalPath());
  }

  @Override
  public void finished(boolean aborted, int errors, int warnings,
                       final CompileContext compileContext) {

    // Project must be compilable for Soot to run.
    if (errors > 0) {
      Messages.showMessageDialog("The project does not compile",
        "Error - SC1" + " analysis", Messages.getErrorIcon());
      return;
    }

    // Detect Java or Android nature.
    this.nature = Nature.JAVA;
    File folder = new File(outPath.getCanonicalPath());
    for (File file : folder.listFiles()) {
      if (file.getName().equals(project.getName() + ".apk")) {
        this.nature = Nature.ANDROID;
        this.apkFile = file;
        break;
      }
    }

    // Notification for UI.
    MessageBus messageBus = project.getMessageBus();
    IAnalysisNotifier publisher =
      messageBus.syncPublisher(IAnalysisNotifier.ANALYSIS_STARTS);
    publisher.notifyAnalysisStarts();

    // Run analysis in a background thread and report results.`
    ApplicationManager.getApplication().executeOnPooledThread(
      () -> ApplicationManager.getApplication().runReadAction(() -> {
        try {
          AnalysisResults results = runAnalysis();
          reportResults(results);
        } catch (Exception e) {

          ApplicationManager.getApplication().invokeLater(
            () -> ApplicationManager.getApplication().runWriteAction(
              () -> Messages.showMessageDialog(
                "Error running the analysis on " + module.getName() +
                  ". Please consult the stack trace for more " + "details.",
                "Error - SC1 analysis", Messages.getErrorIcon())));
          e.printStackTrace();
        }
        // Unblock UI.
        event.getPresentation().setEnabled(true);
      }));
  }

  private AnalysisResults runAnalysis() throws Exception {
    Reporter.PathResolving pr = Reporter.PathResolving.ONE_PATH;
    SC1Runner sc1;
    switch (this.nature) {
      case JAVA:
        sc1 = new SC1Java("", outPath.getCanonicalPath(), pr);
        break;
      case ANDROID:
        sc1 = new SC1Android(apkFile.getCanonicalPath(), androidJars, pr, susi,
          callbacks);
        break;
      default:
        throw new RuntimeException("Module nature not known: " + this.nature);
    }
    sc1.runAnalysis();
    return sc1.getAnalysisResults();
  }

  private Set<Path> copyPaths(AnalysisResults orig) {
    Set<Path> results = new HashSet<>();
    for (Path p : orig.getPaths()) {
      Path path = new Path();
      for (Map.Entry<WarningNode, Marker> e : p.getPath().entrySet()) {
        // Make a semi-deep copy.
        WarningNode n = new WarningNode(e.getKey().getAnalysisNode());
        n.setLineNb(e.getKey().getLineNb());
        n.setFileName(e.getKey().getFileName());
        n.setJavaUnit(e.getKey().getJavaUnit());
        n.setSource(e.getKey().source());
        n.setSink(e.getKey().sink());
        n.setMarker(e.getKey().marker());
        path.put(n, "", e.getValue(), Marker.NOTHING);
      }
      results.add(path);
    }
    return results;
  }

  private void reportResults(AnalysisResults res) {
    // Copy all nodes.
    Set<Path> results = copyPaths(res);
    // Notify that the analysis finished.
    MessageBus messageBus = project.getMessageBus();
    IAnalysisNotifier publisher =
      messageBus.syncPublisher(IAnalysisNotifier.ANALYSIS_DONE);
    // IMPORTANT: run notifyAnalysisDone before setResults.
    publisher.notifyAnalysisDone(results, sourceRoots);
    // Update UI.
    StateSingleton.getInstance().setResults(results);
    FileOperations.showMarkers(project);
  }

  private enum Nature {JAVA, ANDROID}
}
