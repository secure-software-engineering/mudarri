package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Target02 {

  public void doGet(HttpServletRequest request) {
    String query = request.getParameter("query");
    createQuery(query);
  }

  private void createQuery(String parameter) {
    try {
      Connection conn =
        DriverManager.getConnection("url", "userName", "password");
      Statement st = conn.createStatement();
      st.executeQuery(parameter);
    } catch (Exception e) {
      System.out.println("Something went wrong");
    }
  }
}
