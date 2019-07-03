package android;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.util.HashSet;
import java.util.Set;

public abstract class EntryPointsManager {
  public abstract Set<String> entryPoints();

  public Set<String> applicationMethods() {
    Set<String> applicationMethods = new HashSet<>();
    for (SootClass sc : Scene.v().getApplicationClasses()) {
      for (SootMethod sm : sc.getMethods()) {
        if (sm.hasActiveBody()) applicationMethods.add(sm.getSignature());
      }
    }
    return applicationMethods;
  }
}
