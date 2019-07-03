package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Object sensitivity and intra-procedural aliasing

public class Target05 {

  private class Container {
    String containedString;

    public void setString(String str) {
      containedString = str;
    }

    public String getString() {
      return containedString;
    }
  }

  public void doGet(HttpServletRequest request) {
    String userId = request.getParameter("userId");
    Container oc1 = new Container();
    Container oc2 = oc1;
    oc1.setString(userId);
    try {
      String parameter = oc2.getString();
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
