package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Two paths, intra-procedural.

public class Target08 {
  public void doGet(HttpServletRequest request) {
    String userId = "";
    String indeterminate;
    if (request.getParameter("choice").equals("Un"))
      userId = request.getParameter("userId");
    else
      userId = request.getParameter("password");
    indeterminate = request.getParameter("choice");
    indeterminate.replaceAll(" ", "_");
    try {
      Connection conn =
        DriverManager.getConnection("url", "userName", "password");
      Statement st = conn.createStatement();
      String query = "SELECT * FROM  User where userId='" + userId + "'";
      st.executeQuery(query);
    } catch (Exception e) {
      System.out.println("Something went wrong");
    }
  }
}
