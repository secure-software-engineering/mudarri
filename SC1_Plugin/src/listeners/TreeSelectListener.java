package listeners;

import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import reporter.Path;
import reporter.WarningNode;
import state.StateSingleton;
import ui.FileOperations;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class TreeSelectListener implements TreeSelectionListener {

  private Tree pathTree;
  private Project project;

  public TreeSelectListener(Tree pathTree, Project project) {
    this.pathTree = pathTree;
    this.project = project;
  }

  @Override
  public void valueChanged(TreeSelectionEvent e) {

    DefaultMutableTreeNode treeNode =
      (DefaultMutableTreeNode) pathTree.getLastSelectedPathComponent();

    if (treeNode != null) {
      Object object = treeNode.getUserObject();
      Path path = null;
      if (object instanceof Path) {
        path = (Path) object;
      } else if (object instanceof WarningNode) {
        if (treeNode.getParent() != null) {
          DefaultMutableTreeNode parentNode =
            (DefaultMutableTreeNode) treeNode.getParent();
          path = (Path) parentNode.getUserObject();
        }
      }
      StateSingleton.getInstance().setPath(path);
      if (path != null) {
        FileOperations.showMarkers(project);
        FileOperations.showAbstractionMarkers(project);
      }
    }
  }
}
