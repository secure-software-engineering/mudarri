package listeners;

import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import reporter.Path;
import reporter.WarningNode;
import state.StateSingleton;
import ui.FileOperations;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TreeMouseListener extends MouseAdapter {

  private Tree pathTree;
  private Project project;

  public TreeMouseListener(Tree pathTree, Project project) {
    this.pathTree = pathTree;
    this.project = project;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() == 1) {
      // Click on the background.
      TreePath treePath = pathTree.getPathForLocation(e.getX(), e.getY());
      if (treePath == null) {
        // Cancel current selection.
        pathTree.setSelectionRows(new int[]{});
        // Remove markers from UI only if there were markers before.
        boolean currentPathIsEmpty =
          (StateSingleton.getInstance().getPath().isEmpty());
        StateSingleton.getInstance().setPath(null);
        if (!currentPathIsEmpty) {
          FileOperations.showMarkers(project);
          FileOperations.showAbstractionMarkers(project);
        }
      }

    } else if (e.getClickCount() == 2) {
      // Click on an element.
      DefaultMutableTreeNode treeNode =
        (DefaultMutableTreeNode) pathTree.getLastSelectedPathComponent();

      if (treeNode != null) {
        Object object = treeNode.getUserObject();
        WarningNode node = null;
        Path path = null;

        if (object instanceof Path) {
          path = (Path) object;
          if (treeNode.getFirstChild() != null) {
            DefaultMutableTreeNode childNode =
              (DefaultMutableTreeNode) treeNode.getFirstChild();
            node = (WarningNode) childNode.getUserObject();
          }

        } else if (object instanceof WarningNode) {
          node = (WarningNode) object;
          if (treeNode.getParent() != null) {
            DefaultMutableTreeNode parentNode =
              (DefaultMutableTreeNode) treeNode.getParent();
            path = (Path) parentNode.getUserObject();
          }
        }

        if (node != null) {
          // Jump to code location.
          FileOperations.openEditorAndJumpToNode(node, project);
          // Notify of the newly selected path. No need to update the UI, the
          // TreeSelectListener already does it.
          StateSingleton.getInstance().setPath(path);
        }
      }
    }
  }
}
