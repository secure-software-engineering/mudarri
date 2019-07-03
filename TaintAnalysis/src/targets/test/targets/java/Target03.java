package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Object sensitivity.

public class Target03 {

  private class OuterContainer {
    protected class InnerContainer {
      String containedString;
    }

    public InnerContainer ic;

    public OuterContainer() {
      ic = new InnerContainer();
      ic.containedString = "Harmless string.";
    }

    public void setInnerString(String str) {
      ic.containedString = str;
    }
  }

  public void doGet(HttpServletRequest request) {
    String userId = request.getParameter("userId");
    OuterContainer oc = new OuterContainer();
    oc.setInnerString(userId);
    createQuery(oc);
  }

  private void createQuery(OuterContainer container) {
    String parameter = "";
    try {
      parameter = container.ic.containedString;
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
