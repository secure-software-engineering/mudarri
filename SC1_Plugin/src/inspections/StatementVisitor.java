package inspections;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiStatement;
import org.jetbrains.annotations.NotNull;
import quickfix.PathQuickFix;
import quickfix.QuickFix;
import reporter.WarningNode;
import state.StateSingleton;

public class StatementVisitor extends JavaElementVisitor {

  private final ProblemsHolder holder;

  StatementVisitor(@NotNull final ProblemsHolder holder) {
    super();
    this.holder = holder;
  }

  @Override
  public void visitStatement(PsiStatement statement) {
    super.visitStatement(statement);
    if (statement.getText().isEmpty()) return;
    for (WarningNode node : StateSingleton.getInstance().getPath().keySet())
      mark(statement, new PathQuickFix(node));
  }

  private void mark(PsiStatement statement, QuickFix quickFix) {
    String stmtFilePath =
      statement.getContainingFile().getVirtualFile().getCanonicalPath();
    String filePath = quickFix.getNode().getFileName();
    String stmtText = statement.getText().trim().replaceAll(";", "");
    String text = quickFix.getNode().getJavaUnit().trim().replaceAll(";", "");
    Document document = PsiDocumentManager.getInstance(statement.getProject())
      .getDocument(statement.getContainingFile());
    int stmtLineNb = document.getLineNumber(statement.getTextOffset()) +
      StateSingleton.LINE_OFFSET;
    int lineNb = quickFix.getNode().getLineNb();

    // If same file, same text, and if it does not already exist, report.
    if (stmtFilePath.equals(filePath) && stmtText.equals(text) &&
      !descriptorExists(statement, quickFix) && stmtLineNb == lineNb)
      holder.registerProblem(statement, quickFix.description(), quickFix);
  }

  private boolean descriptorExists(PsiStatement statement, QuickFix quickFix) {
    for (ProblemDescriptor descriptor : holder.getResultsArray()) {
      if (descriptor.getPsiElement().equals(statement)) {
        for (com.intellij.codeInspection.QuickFix fix : descriptor.getFixes()) {
          if (fix.getClass().equals(quickFix.getClass())) return true;
        }
      }
    }
    return false;
  }
}
