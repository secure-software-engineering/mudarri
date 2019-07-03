package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Test for trimming inconsistent call stacks.

public class Target18 {

  public void doGet(HttpServletRequest request) {
    String userId = request.getParameter("userId");
    String result = id(userId);
    try {
      Connection conn =
        DriverManager.getConnection("url", "userName", "password");
      Statement st = conn.createStatement();
      String query1 = "SELECT * FROM  User where userId='" + result + "'";
      st.executeQuery(query1);
    } catch (Exception e) {
      System.out.println("Something went wrong");
    }
  }

  public void doPost(HttpServletRequest request) {
    String password = request.getParameter("password");
    String result = id(password);
    try {
      Connection conn =
        DriverManager.getConnection("url", "userName", "password");
      Statement st = conn.createStatement();
      String query2 = "SELECT * FROM  User where userId='" + result + "'";
      st.executeQuery(query2);
    } catch (Exception e) {
      System.out.println("Something went wrong");
    }
  }

  public String id(String parameter) {
    String ret = parameter;
    return ret;
  }
}
