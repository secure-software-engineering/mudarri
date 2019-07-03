package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Test for neighbours, inter-procedural

public class Target14 {

  public void doGet(HttpServletRequest request) {
    String uID = request.getParameter("userId");
    query(uID);
  }

  public void doPost(HttpServletRequest request) {
    String pwd = request.getParameter("password");
    query(pwd);
  }

  public void query(String parameter) {
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
