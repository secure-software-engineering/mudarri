package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.project.Project;
import state.StateSingleton;
import ui.FileOperations;

public class AnalyzeAction extends AnAction {

  public void actionPerformed(AnActionEvent event) {
    // Event validation.
    SC1Analysis sc1 = new SC1Analysis(event);

    // Block UI.
    Project project = event.getProject();
    event.getPresentation().setEnabled(false);

    // Remove markers.
    StateSingleton.getInstance().resetLists();
    FileOperations.showMarkers(project);
    FileOperations.showAbstractionMarkers(project);

    // Compile. Run analysis as callback when done.
    CompilerManager.getInstance(project).compile(sc1.getModule(), sc1);
  }
}