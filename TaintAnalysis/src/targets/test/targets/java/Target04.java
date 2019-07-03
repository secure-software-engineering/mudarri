package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Static variables.

public class Target04 {

  private static String uID;

  public void doGet(HttpServletRequest request) {
    uID = request.getParameter("userId");
    createQuery();
  }

  private void createQuery() {
    try {
      String parameter = uID;
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
