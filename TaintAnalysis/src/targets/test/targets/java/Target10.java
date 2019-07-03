package test.targets.java;

import test.targets.java.library.HttpServletRequest;
import test.targets.java.library.Point;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// API call.

public class Target10 {

  public void doGet(HttpServletRequest request) {
    String userId = request.getParameter("userId");
    userId = Point.getCoordinates(userId);

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
