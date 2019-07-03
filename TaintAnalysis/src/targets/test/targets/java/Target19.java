package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Longer alias path

public class Target19 {

  class InnerContainer {
    public String s;
  }

  class Container {
    public InnerContainer ic;
  }

  public void doGet(HttpServletRequest request) {
    String userId = request.getParameter("userId");
    Container c1 = new Container();
    Container c2 = c1;
    Container c3 = c2;

    c1.ic.s = userId;
    String result = c3.ic.s;

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
}
