package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ResultsInfoFilter {

  public List<ResultsInfo> filter(Map<String, ResultsInfo> results) {
    List<ResultsInfo> filteredResults = new ArrayList<>();
    for (String apk : results.keySet())
      if (keep(results.get(apk))) filteredResults.add(results.get(apk));
    return filteredResults;
  }

  protected abstract boolean keep(ResultsInfo info);
}
