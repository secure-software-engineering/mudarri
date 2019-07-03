package main;

import parser.LogParser;
import utils.ResultsInfo;
import utils.ResultsInfoFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExtractUsefulAndroidProjectsFromLog {

  private final String log;
  private final String apkDir;
  private Map<String, ResultsInfo> results = new LinkedHashMap<>();

  private ExtractUsefulAndroidProjectsFromLog(String log, String apkDir) {
    this.log = log;
    this.apkDir = apkDir;
  }

  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage: ./me path/to/log.txt path/to/apk/directory");
      return;
    }
    ExtractUsefulAndroidProjectsFromLog euapfl =
      new ExtractUsefulAndroidProjectsFromLog(args[0], args[1]);
    euapfl.doExtraction();
  }

  // From https://stackoverflow
  // .com/questions/3758606/how-to-convert-byte-size-into-human-readable
  // -format-in-java
  private String humanReadableByteCount(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

  private void doExtraction() {
    try {
      this.results = LogParser.extract(log, apkDir);
      printResults();
      // moveApps();
    } catch (Exception e) {
      System.err.println("Error in extraction " + log);
      e.printStackTrace();
    }
  }

  private void printResults() {
    List<ResultsInfo> resultsTimeout = (new ResultsInfoFilter() {
      protected boolean keep(ResultsInfo info) {
        return info.scanStatus() == ResultsInfo.Status.TIMEOUT;
      }
    }).filter(results);
    printResults(resultsTimeout, "***** Timeouts");

    List<ResultsInfo> resultsError = (new ResultsInfoFilter() {
      protected boolean keep(ResultsInfo info) {
        return info.scanStatus() == ResultsInfo.Status.ERROR;
      }
    }).filter(results);
    printResults(resultsError, "***** Errors");

    List<ResultsInfo> resultsUnfinished = (new ResultsInfoFilter() {
      protected boolean keep(ResultsInfo info) {
        return info.scanStatus() == ResultsInfo.Status.UNFINISHED;
      }
    }).filter(results);
    printResults(resultsUnfinished, "***** Unfinished");

    List<ResultsInfo> resultsApkInLabel = (new ResultsInfoFilter() {
      protected boolean keep(ResultsInfo info) {
        return info.labelsContainApk() && info.automata().size() > 0 &&
          info.scanStatus() == ResultsInfo.Status.COMPLETE;
      }
    }).filter(results);
    printResults(resultsApkInLabel, "***** Apk in label");

    List<ResultsInfo> resultsNoLineNumber = (new ResultsInfoFilter() {
      protected boolean keep(ResultsInfo info) {
        return info.noLineNumbers() && info.automata().size() > 0 &&
          info.scanStatus() == ResultsInfo.Status.COMPLETE;
      }
    }).filter(results);
    printResults(resultsNoLineNumber, "***** Missing line numbers");

    List<ResultsInfo> noResults = (new ResultsInfoFilter() {
      protected boolean keep(ResultsInfo info) {
        return info.automata().size() < 2 &&
          info.scanStatus() == ResultsInfo.Status.COMPLETE;
      }
    }).filter(results);
    printResults(noResults, "***** < 2 results");

    List<ResultsInfo> goodApks = (new ResultsInfoFilter() {
      protected boolean keep(ResultsInfo info) {
        return (info.scanStatus() == ResultsInfo.Status.COMPLETE);
      }
    }).filter(results);
    printResults(goodApks, "***** Good results");
    System.out.println("***** Total #apps: " + results.size());
  }

  private void printResults(List<ResultsInfo> list, String label) {
    Collections.sort(list);
    for (ResultsInfo ri : list) {
      String sb = ri.scanStatus().name() + " " +
        humanReadableByteCount(ri.apkSize(), false) + " " +
        ri.duration().getSeconds() + "s" + " [" + ri.nbSources() + "-" +
        ri.nbSinks() + "-" + ri.nbPaths() + "]" + " " + ri.apk();
      System.out.println(sb);
    }

    Duration total = Duration.of(0, ChronoUnit.MINUTES);
    for (ResultsInfo ri : list)
      total = total.plus(ri.duration());
    long s = total.getSeconds();
    String time =
      String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    System.out.println(label + ": " + list.size() + ", " + time + ".");
  }

  private void moveApps() throws IOException {
    String pathsGood = "/path/to/apps";
    String pathsNope = "/path/to/apps";

    // Check if directories exist.
    if (!(new File(pathsGood)).exists())
      throw new RuntimeException("Directory does not exist " + pathsGood);
    if (!(new File(pathsNope)).exists())
      throw new RuntimeException("Directory does not exist " + pathsNope);

    // Get good apks from log file.
    List<ResultsInfo> goodApks = (new ResultsInfoFilter() {
      protected boolean keep(ResultsInfo info) {
        boolean statusComplete =
          (info.scanStatus() == ResultsInfo.Status.COMPLETE);
        boolean atLeast2Results = (info.automata().size() > 1);
        boolean noApkInLabel = !info.labelsContainApk();
        boolean lineNumbers = !info.noLineNumbers();
        return statusComplete && atLeast2Results && noApkInLabel && lineNumbers;
      }
    }).filter(results);

    for (ResultsInfo ri : results.values()) {
      // If the apk still exists ...
      if (!(new File(apkDir + "/" + ri.apk())).exists()) {
        System.out.println("Apk does not exist: " + apkDir + "/" + ri.apk());
        continue;
      }
      // ... move it to the appropriate directory.
      String moveTo = goodApks.contains(ri) ? pathsGood : pathsNope;
      Files.move(Paths.get(apkDir + "/" + ri.apk()),
        Paths.get(moveTo + "/" + ri.apk()));
    }
  }
}
