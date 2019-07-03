package parser;

import automaton.Automaton;
import automaton.Node;
import utils.ResultsInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class LogParser {

  public static Map<String, ResultsInfo> extract(String log, String apkDir)
    throws Exception {
    Map<String, ResultsInfo> results = new LinkedHashMap<>();
    String line;
    BufferedReader br = new BufferedReader(new FileReader(log));

    int id = 0;
    ResultsInfo resultsInfo = null;
    Automaton automaton = null;
    String node = null;
    while ((line = br.readLine()) != null) {
      LineParser parsedLine = LineParser.parseLine(line);

      if (parsedLine.id() != null) {
        resultsInfo = results.get(parsedLine.apk());
        if (resultsInfo == null)
          resultsInfo = new ResultsInfo(parsedLine.apk(), apkDir);

        switch (parsedLine.id()) {
          case START_ANALYSIS:
            resultsInfo.setStartTime(parsedLine.dateTime());
            break;
          case END_ANALYSIS:
            resultsInfo.setEndTime(parsedLine.dateTime());
            if (resultsInfo.scanStatus() == ResultsInfo.Status.UNFINISHED)
              resultsInfo.setScanStatus(ResultsInfo.Status.COMPLETE);
            break;
          case NB_SOURCES:
            resultsInfo.setNbSources(Integer.parseInt(parsedLine.text()));
            break;
          case NB_SINKS:
            resultsInfo.setNbSinks(Integer.parseInt(parsedLine.text()));
            break;
          case NB_DECLARED_PATHS:
            resultsInfo.setNbPathsDeclared(Integer.parseInt(parsedLine.text()));
            break;
          case TIMEOUT:
            resultsInfo.setScanStatus(ResultsInfo.Status.TIMEOUT);
            break;
          case ERROR:
            resultsInfo.setScanStatus(ResultsInfo.Status.ERROR);
            break;
          case PATH:
            automaton = new Automaton();
            automaton.setId(++id);
            // TODO: the automaton is entered in the map before it is fully
            // completed. This makes the get() function fail later. Fix this.
            resultsInfo.addAutomaton(automaton,
              ResultsInfo.Classification.getClassification(parsedLine.text()));
            break;
        }
        results.put(parsedLine.apk(), resultsInfo);
        continue;

      } else {
        // Read automaton.
        assert automaton != null;

        if (line.startsWith("\t")) {
          // Read transition.
          assert node != null;
          String text = parsedLine.text();
          String label =
            text.substring(text.indexOf("[") + 1, text.lastIndexOf("]"));
          String nodeId = text.substring(text.lastIndexOf(" -> ") + 4);
          automaton.addNode(nodeId);
          automaton.addTransition(node, nodeId, label);

          // Extra information.
          String lineNb =
            label.substring(label.indexOf(";") + 1, label.lastIndexOf(";"));
          if (!lineNb.equals("-1")) resultsInfo.setNoLineNumbers(false);
          if (!label.endsWith(".apk")) resultsInfo.setLabelsContainApk(false);

        } else {
          // Read node.
          String text = parsedLine.text();
          if (text.isEmpty()) continue;
          node = text;
          if (text.startsWith(Automaton.ROOT)) node = node.substring(9);
          Node n = automaton.addNode(node);
          if (text.startsWith(Automaton.ROOT)) automaton.setAsRoot(n);
        }
      }
    }
    br.close();
    return results;
  }
}
