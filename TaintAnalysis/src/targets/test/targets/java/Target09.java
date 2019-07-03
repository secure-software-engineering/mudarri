package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Two paths, intra-procedural.

public class Target09 {
  public void doGet(HttpServletRequest request) {
    String userId = request.getParameter("userId");
    String indeterminate;
    if (request.getParameter("choice").equals("Un"))
      indeterminate = userId;
    else
      indeterminate = userId;
    try {
      Connection conn =
        DriverManager.getConnection("url", "userName", "password");
      Statement st = conn.createStatement();
      String query = "SELECT * FROM  User where userId='" + indeterminate + "'";
      st.executeQuery(query);
    } catch (Exception e) {
      System.out.println("Something went wrong");
    }
  }
}
