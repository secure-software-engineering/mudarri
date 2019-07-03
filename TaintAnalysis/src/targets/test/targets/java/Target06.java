package test.targets.java;

import test.targets.java.library.HttpServletRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// This test case does not pass. This is normal. We don't support inter-procedural alias analysis.

public class Target06 {

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
    OuterContainer oc1 = new OuterContainer();
    OuterContainer oc2 = new OuterContainer();
    oc2 = oc1;
    createQuery(userId, oc1, oc2);
  }

  private void createQuery(String secret, OuterContainer container1,
                           OuterContainer container2)
  {
    try {
      container1.setInnerString(secret);
      String parameter = container2.ic.containedString;
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
