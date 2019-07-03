package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Looping for loop trimming.

public class Target16 {

  public void doGet(HttpServletRequest request) {
    String uID = request.getParameter("userId");
    while (uID.length() < 20)
      uID += " " + uID;
    try {
      Connection conn =
        DriverManager.getConnection("url", "userName", "password");
      Statement st = conn.createStatement();
      String query = "SELECT * FROM  User where userId='" + uID + "'";
      st.executeQuery(query);
    } catch (Exception e) {
      System.out.println("Something went wrong");
    }
  }
}
