package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import icons.StringConstants;
import state.StateSingleton;

public class HelpAction extends AnAction {

  public void actionPerformed(AnActionEvent event) {
    String helpText = StringConstants.HELP_TEXT_DEFAULT;
    switch (StateSingleton.PLUGIN_ID) {
      case 1:
        helpText = StringConstants.HELP_TEXT_1;
        break;
      case 2:
        helpText = StringConstants.HELP_TEXT_2;
        break;
      case 3:
        helpText = StringConstants.HELP_TEXT_3;
        break;
    }
    Messages.showMessageDialog(helpText, StringConstants.HELP_TITLE,
      Messages.getQuestionIcon());
  }
}