package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Target03 {

  public void doGet(HttpServletRequest request) throws Exception {
    String query = request.getParameter("query");
    String param = query;
    createQuery(param);
  }

  private void createQuery(String parameter) {
    try {
      Connection conn = DriverManager.getConnection("u", "u", "p");
      Statement st = conn.createStatement();
      st.executeQuery(parameter);
    } catch (Exception e) {
      System.out.println("Something went wrong");
    }
  }
}
