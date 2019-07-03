package parser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class LineParser {

  private static final String SEPARATOR = " // ";
  private static final DateTimeFormatter dateFormat =
    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
  private final LocalDateTime dateTime;
  private final String apk;
  private final ResultId id;
  private final String text;

  private LineParser(LocalDateTime dateTime, String apk, ResultId id,
                     String text) {
    this.dateTime = dateTime;
    this.apk = apk;
    this.id = id;
    this.text = text;
  }

  static LineParser parseLine(String line) {
    String[] parts = line.split(SEPARATOR);
    if (parts.length < 3) return new LineParser(null, null, null, line);
    LocalDateTime ldt = LocalDateTime.from(dateFormat.parse(parts[0]));
    ResultId id = ResultId.enumFromString(parts[1]);
    String apk = parts[2];
    String text = parts.length == 4 ? parts[3] : "";
    return new LineParser(ldt, apk, id, text);
  }

  public static String makeLine(String apk, ResultId id, String text) {
    LocalDateTime dateTime =
      LocalDateTime.ofInstant((new Date()).toInstant(), ZoneId.systemDefault());
    StringBuilder sb = new StringBuilder(dateFormat.format(dateTime));
    sb.append(SEPARATOR + id);
    sb.append(SEPARATOR + apk);
    sb.append(SEPARATOR + text);
    return sb.toString();
  }

  LocalDateTime dateTime() { return dateTime; }

  String apk() { return apk; }

  ResultId id() { return id; }

  String text() { return text; }

  public enum ResultId {
    ERROR("ERROR_IN_ANALYSIS"), START_ANALYSIS("ANALYSIS_STARTS"),
    END_ANALYSIS("ANALYSIS_ENDS"), NB_SOURCES("NB_SOURCES"),
    NB_SINKS("NB_SINKS"), NB_DECLARED_PATHS("NB_DECLARED_PATHS"),
    PATH("REPORTED_PATH_AUTOMATON"), TIMEOUT("ANALYSIS_TIMEOUT");

    private final String toString;

    ResultId(String toString) { this.toString = toString; }

    public static ResultId enumFromString(String toString) {
      for (ResultId rid : ResultId.values()) {
        if (rid.toString().equals(toString)) return rid;
      }
      throw new RuntimeException("Result id not recognized: " + toString);
    }

    @Override
    public String toString() { return this.toString; }
  }
}
