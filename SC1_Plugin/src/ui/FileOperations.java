package ui;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.impl.HighlightInfoProcessor;
import com.intellij.codeInsight.daemon.impl.LocalInspectionsPass;
import com.intellij.codeInsight.daemon.impl.UpdateHighlightersUtil;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl;
import com.intellij.codeInspection.ex.InspectionManagerEx;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.codeStyle.CodeStyleFacade;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.JBColor;
import inspections.Inspection;
import org.jetbrains.annotations.NotNull;
import reporter.WarningNode;
import state.StateSingleton;

import java.awt.*;
import java.util.Collections;
import java.util.Set;

public class FileOperations {

  private static final TextAttributes TEXT_ATTRIBUTES_ABSTRACTION =
    new TextAttributes(JBColor.RED, null, JBColor.RED,
      EffectType.WAVE_UNDERSCORE, Font.BOLD);

  static void highlightAbstractions(Set<WarningNode> path) {
    for (WarningNode node : path) {
      highlightAbstraction(node, node.prev);
      highlightAbstraction(node, node.displayAbs);
    }
  }

  // https://github.com/JetBrains/intellij-community/blob/master/platform
  // /platform-impl/src/com/intellij/openapi/editor/impl
  // /LazyRangeMarkerFactoryImpl.java#L57
  private static int calculateOffset(@NotNull Document document, final int line,
                                     final int column, int tabSize) {
    int offset;
    if (0 <= line && line < document.getLineCount()) {
      final int lineStart = document.getLineStartOffset(line);
      final int lineEnd = document.getLineEndOffset(line);
      final CharSequence docText = document.getCharsSequence();

      offset = lineStart;
      int col = 0;
      while (offset < lineEnd && col < column) {
        col += docText.charAt(offset) == '\t' ? tabSize : 1;
        offset++;
      }
    } else {
      offset = document.getTextLength();
    }
    return offset;
  }

  private static void highlightAbstraction(WarningNode node, String abs) {
    if (abs.length() < 2) return;
    Editor editor = StateSingleton.getInstance().getEditor();
    if (editor != null && node.getJavaUnit().contains(abs)) {
      VirtualFile file =
        LocalFileSystem.getInstance().findFileByPath(node.getFileName());
      if (file == null) return;
      int tabSize = CodeStyleFacade.getInstance(editor.getProject())
        .getTabSize(file.getFileType());
      int lineOffset = node.getJavaUnit().replaceAll("\t", "").indexOf(abs);
      int startOffset = calculateOffset(editor.getDocument(),
        node.getLineNb() - StateSingleton.LINE_OFFSET, lineOffset, tabSize);
      int endOffset = startOffset + abs.length();
      editor.getMarkupModel()
        .addRangeHighlighter(startOffset, endOffset, HighlighterLayer.FIRST,
          TEXT_ATTRIBUTES_ABSTRACTION, HighlighterTargetArea.EXACT_RANGE);
    }
  }

  public static void openEditorAndJumpToNode(WarningNode node,
                                             Project project) {
    VirtualFile file =
      LocalFileSystem.getInstance().findFileByPath(node.getFileName());
    if (file == null) return;
    new OpenFileDescriptor(project, file,
      node.getLineNb() - StateSingleton.LINE_OFFSET, 0, false).navigate(false);
  }

  public static void showMarkers(Project project) {
    // Run the inspection to display the warnings in all open editors.
    for (FileEditor fileEditor : FileEditorManager.getInstance(project)
      .getAllEditors()) {
      PsiFile psiFile =
        PsiManager.getInstance(project).findFile(fileEditor.getFile());
      showMarkers(project, psiFile);
    }
  }

  public static void showAbstractionMarkers(Project project) {
    // Remove existing abstraction highlights.
    if (StateSingleton.getInstance().getEditor() != null)
      StateSingleton.getInstance().getEditor().getMarkupModel()
        .removeAllHighlighters();
    // Save the editor for the next abstraction.
    StateSingleton.getInstance().setEditor(
      FileEditorManager.getInstance(project).getSelectedTextEditor());
    // Highlight abstractions.
    highlightAbstractions(StateSingleton.getInstance().getPath().keySet());
  }

  //  From https://intellij-support.jetbrains
  // .com/hc/en-us/community/posts/206111999-How-to-keep-inspections-created
  // -by-LocalInspectionTool-in-place
  private static void showMarkers(Project project, PsiFile pf) {
    Document document = PsiDocumentManager.getInstance(project).getDocument(pf);

    LocalInspectionsPass lip =
      new LocalInspectionsPass(pf, document, 0, document.getTextLength(),
        LocalInspectionsPass.EMPTY_PRIORITY_RANGE, true,
        HighlightInfoProcessor.getEmpty());

    InspectionManagerEx ime =
      (InspectionManagerEx) InspectionManager.getInstance(project);
    GlobalInspectionContextImpl ic = ime.createNewGlobalContext(false);

    ProgressManager.getInstance().runProcess(() -> {
      // Run the inspection.
      lip.doInspectInBatch(ic, ime, Collections
        .singletonList(new LocalInspectionToolWrapper(new Inspection())));

      // Update the UI markers.
      ApplicationManager.getApplication().invokeLater(
        () -> ApplicationManager.getApplication().runWriteAction(
          () -> UpdateHighlightersUtil
            .setHighlightersToEditor(project, document, 0,
              document.getTextLength(), lip.getInfos(), null,
              Pass.UPDATE_ALL)));
    }, new EmptyProgressIndicator());
  }
}
