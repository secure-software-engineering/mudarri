package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Recursion for loop trimming.

public class Target17 {

  public void doGet(HttpServletRequest request) {
    String uID = request.getParameter("userId");
    String res = getTransformedUID(uID);
    try {
      Connection conn =
        DriverManager.getConnection("url", "userName", "password");
      Statement st = conn.createStatement();
      String query = "SELECT * FROM  User where userId='" + res + "'";
      st.executeQuery(query);
    } catch (Exception e) {
      System.out.println("Something went wrong");
    }
  }

  public String getTransformedUID(String uID) {
    if (uID.length() > 30)
      return uID;
    return getTransformedUID(uID + " " + uID);
  }
}
