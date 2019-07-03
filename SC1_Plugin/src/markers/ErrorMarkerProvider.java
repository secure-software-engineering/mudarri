package markers;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiFile;
import com.intellij.util.Function;
import icons.Icons;
import org.jetbrains.annotations.NotNull;
import reporter.Path;
import reporter.WarningNode;
import state.StateSingleton;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ErrorMarkerProvider implements LineMarkerProvider {

  @Nullable
  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
    if (psiElement instanceof PsiExpression) {
      int lineNumber = getLineNumber(psiElement);
      String filePath =
        psiElement.getContainingFile().getVirtualFile().getCanonicalPath();
      if (errorExists(filePath, lineNumber)) {
        psiElement = psiElement.getLastChild();
        Function<PsiElement, String> tooltip =
          element1 -> getTooltip(filePath, lineNumber);
        return new LineMarkerInfo<>(psiElement, psiElement.getTextRange(),
          Icons.ERROR, Pass.UPDATE_ALL, tooltip, null,
          GutterIconRenderer.Alignment.LEFT);
      }
    }
    return null;
  }

  private boolean errorExists(String filePath, int lineNumber) {
    return !(getWarningNode(filePath, lineNumber) == null);
  }

  private String getTooltip(String filePath, int lineNumber) {
    WarningNode node = getWarningNode(filePath, lineNumber);
    return node == null ? "" : node.getJavaUnit();
  }

  private WarningNode getWarningNode(String filePath, int lineNumber) {
    Set<Path> results = StateSingleton.getInstance().getResults();
    if (results == null) return null;
    for (Path path : results) {
      for (WarningNode node : path.getPath().keySet()) {
        if (node.getLineNb() == lineNumber &&
          node.getFileName().equals(filePath)) return node;
      }
    }
    return null;
  }

  // Returns line number for PSI element
  private int getLineNumber(PsiElement psiElement) {
    PsiFile containingFile = psiElement.getContainingFile();
    Project project = containingFile.getProject();
    PsiDocumentManager psiDocumentManager =
      PsiDocumentManager.getInstance(project);
    Document document = psiDocumentManager.getDocument(containingFile);
    int textOffset = psiElement.getTextOffset();
    return document.getLineNumber(textOffset);
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<PsiElement> list, @NotNull
    Collection<LineMarkerInfo> collection) {
  }
}
