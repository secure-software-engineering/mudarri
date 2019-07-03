package main;

import android.EntryPointsManager;
import reporter.AnalysisResults;
import reporter.Reporter;
import soot.G;

public abstract class SC1Runner {
  protected final Reporter reporter;
  private final Reporter.PathResolving pathResolvingLevel;
  EntryPointsManager entryPointsManager;

  private boolean timedout = false;

  SC1Runner(Reporter.PathResolving pathResolvingLevel) {
    this.pathResolvingLevel = pathResolvingLevel;
    this.reporter = new Reporter(pathResolvingLevel);
  }

  public abstract void runAnalysis();

  protected abstract void setTaintAnalysisTransformer();

  public AnalysisResults getAnalysisResults() {
    if (this.timedout) return null;
    return reporter.generateAnalysisResults();
  }

  void printResults() {
    reporter.printWarnings();
  }

  public void stopAnalysis() {
    if (this.reporter != null) reporter.stopPathLookups();
    this.timedout = true;
    G.reset();
  }
}
