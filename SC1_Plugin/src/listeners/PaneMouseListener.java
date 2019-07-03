package listeners;

import com.intellij.openapi.project.Project;
import state.StateSingleton;
import ui.FileOperations;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PaneMouseListener extends MouseAdapter {

  private Project project;

  public PaneMouseListener(Project project) {
    this.project = project;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    // Remove markers from UI only if there were markers before.
    boolean currentPathIsEmpty =
      (StateSingleton.getInstance().getPath().isEmpty());
    StateSingleton.getInstance().setPath(null);
    if (!currentPathIsEmpty) {
      FileOperations.showMarkers(project);
      FileOperations.showAbstractionMarkers(project);
    }
  }
}
