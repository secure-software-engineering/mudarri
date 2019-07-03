package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Two paths, inter-procedural.

public class Target12 {
  public void doGet(HttpServletRequest request) {
    String userId = request.getParameter("userId");
    execute(userId);
  }

  public void doPost(HttpServletRequest request) {
    String password = request.getParameter("password");
    execute(password);
  }

  private void execute(String parameter) {
    try {
      Connection conn =
        DriverManager.getConnection("url", "userName", "password");
      Statement st = conn.createStatement();
      String query = "SELECT * FROM  User where userId='" + parameter + "'";
      st.executeQuery(query);
    } catch (Exception e) {
      System.out.println("Something went wrong");
    }
  }
}
