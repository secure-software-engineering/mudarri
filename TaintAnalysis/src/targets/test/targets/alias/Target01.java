package test.targets.alias;

// No aliases.

public class Target01 {
  public void test() {
    int i = 0, j = 2;
    i = j;
    int k = 3;
    System.out.println(i + j + k);
  }
}
