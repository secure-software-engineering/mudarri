package utils;

import automaton.Automaton;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class ResultsInfo implements Comparable<ResultsInfo> {

  private final String apk;
  private final String apkDir;
  private Status scanStatus = Status.UNFINISHED;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Map<Automaton, Classification> automata = new HashMap<>();
  private int nbSources;
  private int nbSinks;
  // To get the actual nb of paths, use automata.size() rather than this.
  private int nbPathsDeclared;

  private boolean labelsContainApk = true; // Can tolerate a .apk sometimes.
  private boolean noLineNumbers = true; // Can tolerate a -1 from time to time.

  public ResultsInfo(String apk, String apkDir) {
    this.apk = apk;
    this.apkDir = apkDir;
  }

  public void setScanStatus(Status scanStatus) { this.scanStatus = scanStatus; }

  public Status scanStatus() { return this.scanStatus; }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  public Duration duration() {
    if (this.startTime == null || this.endTime == null)
      return Duration.of(-1, ChronoUnit.MINUTES);
    return Duration.between(this.startTime, this.endTime);
  }

  public void setNbSources(int nbSources) { this.nbSources = nbSources; }

  public int nbSources() { return this.nbSources; }

  public void setNbSinks(int nbSinks) { this.nbSinks = nbSinks; }

  public int nbSinks() { return this.nbSinks; }

  public void setNbPathsDeclared(int nbPathsDeclared) {
    this.nbPathsDeclared = nbPathsDeclared;
  }

  public void addAutomaton(Automaton automaton, Classification classification) {
    automata.put(automaton, classification);
  }

  public Map<Automaton, Classification> automata() { return this.automata; }

  public int nbPaths() { return this.automata.size(); }

  public void setLabelsContainApk(boolean labelsContainApk) {
    this.labelsContainApk = labelsContainApk;
  }

  public boolean labelsContainApk() { return this.labelsContainApk; }

  public boolean noLineNumbers() { return this.noLineNumbers; }

  public void setNoLineNumbers(boolean noLineNumbers) {
    this.noLineNumbers = noLineNumbers;
  }

  public String apk() { return this.apk; }

  // In bytes.
  public Long apkSize() {
    String pathToApk = this.apkDir + "/" + this.apk;
    File file = new File(pathToApk);
    if (!file.exists() || !file.isFile()) return new Long(-1);
    return file.length();
  }

  @Override
  public int compareTo(ResultsInfo other) {
    return this.apkSize().compareTo(other.apkSize());
    // return this.duration().compareTo(other.duration());
  }

  public enum Status {COMPLETE, ERROR, TIMEOUT, UNFINISHED}

  public enum Classification {
    FALSE_POSITIVE("FP"), TRUE_POSITIVE("TP"), UNCLASSIFIED("UC");

    private String value;

    Classification(String value) { this.value = value; }

    public static Classification getClassification(String value) {
      for (Classification c : Classification.values())
        if (c.getValue().equals(value)) return c;
      return null;
    }

    public static String description() {
      StringBuilder sb = new StringBuilder();
      for (Classification c : Classification.values())
        sb.append(", " + c.getValue() + "=" + c.name());
      return sb.toString().length() > 2 ? sb.toString().substring(2) :
        sb.toString();
    }

    String getValue() { return this.value; }

  }
}
