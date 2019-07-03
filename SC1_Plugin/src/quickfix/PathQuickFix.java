package quickfix;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import icons.StringConstants;
import org.jetbrains.annotations.NotNull;
import reporter.WarningNode;

public class PathQuickFix extends QuickFix {

  public PathQuickFix(WarningNode node) { super(node); }

  @NotNull
  public String getName() {
    return StringConstants.QUICK_FIX_PATH;
  }

  @NotNull
  public String getFamilyName() {
    return StringConstants.QUICK_FIX_PATH;
  }

  @Override
  public String description() {
    return this.node.descriptionForPlugin;
  }

  @Override
  public void applyFix(@NotNull Project project,
                       @NotNull ProblemDescriptor descriptor) {
    // See here: https://github.com/JetBrains/intellij-sdk-docs/blob/master
    // /code_samples/comparing_references_inspection/source/com/intellij
    // /codeInspection/ComparingReferencesInspection.java#L95

    // TODO: Gérer les marqueurs.
  }

}
