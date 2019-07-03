package notifiers;

import com.intellij.util.messages.Topic;
import reporter.Path;

import java.util.Set;

public interface IAnalysisNotifier {

  Topic<IAnalysisNotifier> ANALYSIS_STARTS =
    Topic.create("Analysis starts.", IAnalysisNotifier.class);
  Topic<IAnalysisNotifier> ANALYSIS_DONE =
    Topic.create("Analysis done.", IAnalysisNotifier.class);

  void notifyAnalysisDone(Set<Path> results, Set<String> srcRoots);

  void notifyAnalysisStarts();

}

