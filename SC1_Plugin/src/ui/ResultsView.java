package ui;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import icons.StringConstants;
import listeners.AnalysisNotifier;
import listeners.PaneMouseListener;
import listeners.TreeMouseListener;
import listeners.TreeSelectListener;
import notifiers.IAnalysisNotifier;
import org.jetbrains.annotations.NotNull;
import state.StateSingleton;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

// Derived from MOIS-assist.

public class ResultsView implements ToolWindowFactory {

  @Override
  public void createToolWindowContent(@NotNull Project project,
                                      @NotNull ToolWindow toolWindow) {
    MessageBus bus = project.getMessageBus();
    JBPanel toolPanel = new JBPanel(new BorderLayout());

    ActionToolbar actionToolbar = null;

    // Toolbar.
    if (StateSingleton.ENABLE_ACTIONS) {
      final DefaultActionGroup actions =
        (DefaultActionGroup) ActionManager.getInstance()
          .getAction("SC1_plugin.results.toolbar");
      actionToolbar = ActionManager.getInstance()
        .createActionToolbar("SC1 results toolbar", actions, true);
      toolPanel.add(actionToolbar.getComponent(), BorderLayout.PAGE_START);
    }

    // List of warnings.
    Tree pathTree = new Tree();
    DefaultTreeModel treeModel = new DefaultTreeModel(null);
    pathTree.setCellRenderer(new TreeRenderer());
    pathTree.setModel(treeModel);
    pathTree.getEmptyText().setText(StringConstants.TODO_RUN);
    JBScrollPane scrollPane = new JBScrollPane(pathTree);
    toolPanel.add(scrollPane, BorderLayout.CENTER);

    JLabel runStatus = null;
    JPanel progressBarWrap = null;

    // Footer.
    if (StateSingleton.ENABLE_ACTIONS) {
      JBPanel notificationPanel = new JBPanel(new BorderLayout());
      notificationPanel.setBorder(JBUI.Borders.empty(4));
      notificationPanel.withPreferredHeight(20);

      runStatus = new JLabel();
      runStatus
        .setFont(new Font(runStatus.getFont().getName(), Font.PLAIN, 11));
      notificationPanel.add(runStatus, BorderLayout.WEST);

      JProgressBar progressBar = new JProgressBar();
      progressBar.setIndeterminate(true);
      progressBarWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      progressBarWrap.setVisible(false);
      progressBarWrap.add(progressBar);
      notificationPanel.add(progressBarWrap, BorderLayout.EAST);

      toolPanel.add(notificationPanel, BorderLayout.PAGE_END);
    }

    // Set up the layout.
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content toolContent = contentFactory.createContent(toolPanel, "", false);
    toolWindow.getContentManager().addContent(toolContent);

    // Communication.
    IAnalysisNotifier analysisNotifier =
      new AnalysisNotifier(pathTree, treeModel, progressBarWrap, runStatus);
    bus.connect()
      .subscribe(IAnalysisNotifier.ANALYSIS_STARTS, analysisNotifier);
    bus.connect().subscribe(IAnalysisNotifier.ANALYSIS_DONE, analysisNotifier);

    // Listeners.
    pathTree
      .addTreeSelectionListener(new TreeSelectListener(pathTree, project));
    pathTree.addMouseListener(new TreeMouseListener(pathTree, project));
    toolPanel.addMouseListener(new PaneMouseListener(project));
    if (StateSingleton.ENABLE_ACTIONS) {
      actionToolbar.getComponent()
        .addMouseListener(new PaneMouseListener(project));
    }
  }
}
