package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

//Test for neighbours, intra-procedural

public class Target13 {

  public void doGet(HttpServletRequest request) {
    String uID = request.getParameter("userId");
    String pwd = request.getParameter("password");
    String parameter = "";
    if (uID.contains("a"))
      parameter = uID;
    else
      parameter = pwd;
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
