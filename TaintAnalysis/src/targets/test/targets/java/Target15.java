package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Three sinks

public class Target15 {

  public void doGet(HttpServletRequest request) {
    String uID = request.getParameter("userId");
    query1(uID);
    query2(uID);
  }

  public void query1(String parameter) {
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

  public void query2(String parameter) {
    try {
      Connection conn =
        DriverManager.getConnection("url", "userName", "password");
      Statement st = conn.createStatement();
      String query = "SELECT * FROM  User where userId='" + parameter + "'";
      st.executeQuery(query);
      st.executeQuery(parameter);
    } catch (Exception e) {
      System.out.println("Something went wrong");
    }
  }
}
