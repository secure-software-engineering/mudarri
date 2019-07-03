package ui;

import com.intellij.ui.JBColor;
import reporter.Path;
import reporter.WarningNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

// From MOIS-assist.

public class TreeRenderer extends JLabel implements TreeCellRenderer {

  private JLabel text = new JLabel();

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                boolean selected,
                                                boolean expanded, boolean leaf,
                                                int row, boolean hasFocus) {
    if (selected) text.setForeground(JBColor.WHITE);
    else text.setForeground(JBColor.BLACK);

    if (value instanceof DefaultMutableTreeNode) {
      Object object = ((DefaultMutableTreeNode) value).getUserObject();
      if (object instanceof Path) {
        // Path.
        Path path = (Path) object;
        text.setText(path.getDescription());
        // text.setToolTipText(path.getDescription());
        text.setIcon(null);
      } else if (object instanceof WarningNode) {
        // Warning node.
        WarningNode node = (WarningNode) object;
        text.setText(node.descriptionForPlugin);
        // text.setToolTipText(node.descriptionForPlugin);
        text.setIcon(null); // text.setIcon(Icons.SINK);
      } else {
        // Root (not visible anyways).
        text.setText(value.toString());
        text.setIcon(null);
      }
    }
    return text;
  }
}
