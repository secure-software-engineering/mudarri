package inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import icons.StringConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Inspection extends LocalInspectionTool {

  @NotNull
  public String getDisplayName() {
    return StringConstants.INSPECTION_DISPLAY_NAME;
  }

  @Nullable
  public String getStaticDescription() {
    return StringConstants.INSPECTION_DESCRIPTION;
  }

  @NotNull
  public String getGroupDisplayName() {
    return StringConstants.GROUP_DISPLAY_NAME;
  }

  @NotNull
  public String getShortName() {
    return StringConstants.INSPECTION_SHORT_NAME;
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder,
                                        boolean isOnTheFly) {
    return new StatementVisitor(holder);
  }

  public boolean isEnabledByDefault() {
    return true;
  }
}