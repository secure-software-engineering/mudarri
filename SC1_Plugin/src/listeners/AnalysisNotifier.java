package listeners;

import com.intellij.ui.treeStructure.Tree;
import icons.StringConstants;
import notifiers.IAnalysisNotifier;
import reporter.Path;
import reporter.WarningNode;
import state.StateSingleton;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AnalysisNotifier implements IAnalysisNotifier {

  private Tree pathTree;
  private DefaultTreeModel treeModel;

  private JLabel runStatus;
  private JPanel progressBarWrap;

  private Set<String> sourceRoots;

  public AnalysisNotifier(Tree pathTree, DefaultTreeModel treeModel,
                          JPanel progressBarWrap, JLabel runStatus) {
    this.pathTree = pathTree;
    this.treeModel = treeModel;
    this.progressBarWrap = progressBarWrap;
    this.runStatus = runStatus;
  }

  @Override
  public void notifyAnalysisStarts() {
    if (progressBarWrap != null) progressBarWrap.setVisible(true);
    if (runStatus != null) runStatus.setText(StringConstants.RUNNING);
    pathTree.getEmptyText().setText(StringConstants.RUNNING);
  }

  @Override
  public void notifyAnalysisDone(Set<Path> results, Set<String> srcRoots) {
    sourceRoots = srcRoots;
    int nbPaths = results.size();
    String message = StringConstants.NOTHING_FOUND;
    if (nbPaths > 0)
      message = nbPaths + " data " + (nbPaths == 1 ? "leak" : "leaks");
    if (progressBarWrap != null) progressBarWrap.setVisible(false);
    if (runStatus != null) runStatus.setText(message);
    displayPaths(results);
  }

  private void displayPaths(Set<Path> paths) {
    // TODO: Gérer paths qui restent les mêmes de run en run.

    if (paths.size() < 1) {
      treeModel.setRoot(null);
      pathTree.getEmptyText().setText(StringConstants.NOTHING_FOUND);
      return;
    }

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("");

    int count = 1;
    List<Path> list = new ArrayList<>(paths);
    Collections.sort(list); // Sort by warning size.
    for (Path path : list) {
      DefaultMutableTreeNode pathNode = new DefaultMutableTreeNode(path);
      // TODO: better identification of the paths.
      path.setDescription("Warning #" + count);

      String abs = "";
      WarningNode last = null;
      String prevAbs = StateSingleton.ZERO;
      int countSteps = 0;

      for (WarningNode node : path.getPath().keySet()) {
        countSteps++;
        setJavaUnit(node);
        node.setMarker(path.getPath().get(node));
        boolean inProject = node.getLineNb() >= 0;

        if (last == null) last = node;
        if (!inProject) continue;

        if (StateSingleton.FULL_PATHS) {
          node.displayAbs = StateSingleton.parseAbs(node);
        } else {
          boolean lastStep = (countSteps == path.getPath().size());
          if (!node.getJavaUnit().equals(last.getJavaUnit()) || lastStep) {
            last.displayAbs = abs;
            String newAbs = StateSingleton.parseAbs(node);
            if (!newAbs.isEmpty()) abs = newAbs;
          } else {
            String newAbs = StateSingleton.parseAbs(node);
            if (!newAbs.isEmpty()) abs = newAbs;
            continue;
          }
        }

        // Set up the node.
        if (pathNode.getChildCount() == 0) last.setSource(true);
        last.prev = prevAbs;
        if (!last.displayAbs.equals("")) prevAbs = last.displayAbs;
        last.descriptionForPlugin =
          StateSingleton.fullDescription(last, path.getPath().get(last));
        pathNode.add(new DefaultMutableTreeNode(last));
        last = node;
      }

      if (last != null) {
        // Remove last path step if it has the same line as the sink.
        if (pathNode.getLastLeaf().getUserObject() instanceof WarningNode) {
          WarningNode prev =
            (WarningNode) pathNode.getLastLeaf().getUserObject();
          if (prev.getJavaUnit().equals(last.getJavaUnit()) &&
            prev.getLineNb() == last.getLineNb() && !prev.source()) {
            pathNode.remove(pathNode.getLastLeaf());
          }
        }
        // Enter sink in path.
        last.setSink(true);
        last.displayAbs = prevAbs;
        last.prev = "";
        last.descriptionForPlugin =
          StateSingleton.fullDescription(last, path.getPath().get(last));
        pathNode.add(new DefaultMutableTreeNode(last));
      }
      root.add(pathNode);
      count++;
    }

    treeModel.setRoot(root);
    pathTree.setRootVisible(false);
  }

  private void setJavaUnit(WarningNode node) {
    // If already resolved don't resolve again.
    if (node.getJavaUnit() != null) return;
    // Look up the file.
    if (node.getLineNb() > 0 && sourceRoots != null) {
      for (String sourceRoot : sourceRoots) {
        String javaUnit = getJavaUnit(sourceRoot, node);
        if (javaUnit != null) {
          node.setJavaUnit(javaUnit);
          node.setFileName(sourceRoot + "/" + node.getFileName());
          return;
        }
      }
    }
    // Could not be resolved.
    node.setJavaUnit("");
  }

  private String getJavaUnit(String sourceRoot, WarningNode node) {
    File file = new File(sourceRoot + "/" + node.getFileName());
    if (!file.exists()) return null;

    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(file));
      String line;
      int count = 1;
      while ((line = br.readLine()) != null) {
        if (count == node.getLineNb()) {
          br.close();
          return line;
        }
        count++;
      }
      br.close();
    } catch (IOException e) {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e1) { }
      }
    }

    return null;
  }
}