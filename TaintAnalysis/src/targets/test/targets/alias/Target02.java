package test.targets.alias;

// No aliases.

public class Target02 {
  class Container {
    public int x;
  }

  public void test() {
    Container c1 = new Container();
    Container c2 = c1;
    c1.x = 3;
    System.out.println(c2.x);
  }
}
