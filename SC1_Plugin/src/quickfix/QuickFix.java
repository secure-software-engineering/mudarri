package quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import reporter.WarningNode;

public abstract class QuickFix implements LocalQuickFix {

  protected WarningNode node;

  QuickFix(WarningNode node) { this.node = node; }

  public WarningNode getNode() { return node; }

  public abstract String description();

}
