package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Two paths, inter-procedural.

public class Target11 {
  public void doGet(HttpServletRequest request) {
    String userId = request.getParameter("userId");
    if (request.getParameter("choice").equals("Un"))
      function1(userId);
    else
      function2(userId);
  }

  private void function1(String arg1) {
    String pass = arg1;
    execute(pass);
  }

  private void function2(String arg2) {
    execute(arg2);
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
