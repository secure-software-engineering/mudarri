package analysis;

import marking.Marker;
import marking.Node;
import soot.Local;
import soot.SootField;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.Constant;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;

import java.util.Arrays;

public class TaintAbstraction extends Node {

  final static TaintAbstraction zeroAbstraction =
    new TaintAbstraction(null, null, null, null, null);
  private static final int MAX_AP_LENGTH = 3;
  private Local local;
  private SootField[] fields = new SootField[]{};
  private boolean fromReturn = false; // Should not be copied over in derive.

  // Constructor from a Soot Value.
  TaintAbstraction(Value v, Unit unit, Unit source,
                   TaintAbstraction predecessor, Marker marker) {
    super(unit, source, predecessor, marker);
    fillAndTruncateAccessPath(v);
  }

  // Constructor from a Local and array of SootField.
  TaintAbstraction(Local local, SootField[] fields, Unit unit, Unit source,
                   Node predecessor, Marker marker) {
    super(unit, source, predecessor, marker);
    this.local = local;
    this.fields = fields == null ? new SootField[]{} : fields;
  }

  private void fillAndTruncateAccessPath(Value v) {
    this.local = null;
    this.fields = new SootField[]{};
    if (v == null) return;
    if (v instanceof Local) {
      this.local = (Local) v;
    } else if (v instanceof InstanceFieldRef) {
      InstanceFieldRef ifr = (InstanceFieldRef) v;
      this.local = (Local) ifr.getBase();
      this.fields = new SootField[]{ifr.getField()};
    } else if (v instanceof StaticFieldRef) {
      StaticFieldRef sfr = (StaticFieldRef) v;
      this.local = null;
      this.fields = new SootField[]{sfr.getField()};
    } else if (v instanceof ArrayRef) {
      ArrayRef ar = (ArrayRef) v;
      this.local = (Local) ar.getBase();
    } else throw new RuntimeException(
      "Unexpected left side " + v + " (" + v.getClass() + ")");
    truncateFields();
  }

  @Override
  public boolean isZeroAbstraction() {
    return (local == null && (fields == null || fields.length == 0));
  }

  /***** Getters and setters *****/

  Local getLocal() {
    return this.local;
  }

  private void setLocal(Local local) {
    this.local = local;
  }

  SootField[] getFields() {
    return this.fields;
  }

  void resetFields() {
    this.fields = new SootField[]{};
  }

  void setFromReturn() {
    this.fromReturn = true;
  }

  /***** Utils *****/

  @Override
  public int hashCode() {

    if (isZeroAbstraction()) return 0;

    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(fields);
    result = prime * result + ((local == null) ? 0 : local.hashCode());
    result = prime * result + ((unit == null) ? 0 : unit.hashCode());
    result = prime * result + (fromReturn ? 1 : 0);
    if (local == null) {
      SootField firstField = fields[0];
      result = prime * result + firstField.getDeclaringClass().hashCode();
    }
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    TaintAbstraction other = (TaintAbstraction) obj;
    if (other.isZeroAbstraction() && isZeroAbstraction()) return true;
    if (!Arrays.equals(fields, other.fields)) return false;
    if (fromReturn != other.fromReturn) return false;
    if (local == null) {
      if (other.local != null) return false;
      else if (!fields[0].getDeclaringClass()
        .equals(other.getFields()[0].getDeclaringClass())) return false;
    } else if (!local.equals(other.local)) return false;
    if (unit == null) {
      return other.unit == null;
    } else return unit.equals(other.unit);
  }

  @Override
  public String abstraction() {
    if (isZeroAbstraction()) return "<ZERO>";

    String res = "";
    if (local != null) res += local.getName();
    else res += fields[0].getDeclaringClass().getName();
    if (fields != null && fields.length > 0) {
      for (SootField sf : fields)
        res += "." + sf.getName();
    }
    return res;
  }

  @Override
  public String toString() {
    return abstraction();
  }

  // Used privately. Returns a clone of this abstraction, with this node as the
  // precedessor node for the new abstraction.
  private TaintAbstraction derive(Marker marker, Unit unit) {
    return new TaintAbstraction(this.local, this.fields, unit, this.source,
      this, marker);
  }

  TaintAbstraction deriveWithNewStmt(Unit unit, Marker marker) {
    TaintAbstraction copy = derive(marker, unit);
    copy.setUnit(unit);
    return copy;
  }

  TaintAbstraction deriveWithNewLocal(Local local, Unit unit, Marker marker) {
    TaintAbstraction copy = derive(marker, unit);
    copy.setUnit(unit);
    copy.setLocal(local);
    return copy;
  }

  TaintAbstraction deriveWithNewValue(Value value, Unit unit, Marker marker) {
    TaintAbstraction copy = derive(marker, unit);
    copy.fillAndTruncateAccessPath(value);
    copy.setUnit(unit);
    return copy;
  }

  /**** Field operations ****/

  TaintAbstraction append(SootField[] newFields) {
    SootField[] a = new SootField[fields.length + newFields.length];
    System.arraycopy(fields, 0, a, 0, fields.length);
    System.arraycopy(newFields, 0, a, fields.length, newFields.length);
    this.fields = a;
    this.truncateFields();
    return this;
  }

  boolean hasPrefix(Value v) { // if this has prefix v
    if (v instanceof Local) {
      if (local == null) return false;
      else return (local.equals(v));
    } else if (v instanceof InstanceFieldRef) {
      InstanceFieldRef ifr = (InstanceFieldRef) v;
      if (local == null) {
        if (ifr.getBase() != null) return false;
      } else if (!local.equals(ifr.getBase())) return false;
      return fields.length > 0 && ifr.getField() == fields[0];
    } else if (v instanceof StaticFieldRef) {
      StaticFieldRef sfr = (StaticFieldRef) v;
      if (local != null) return false;
      return fields.length > 0 && sfr.getField() == fields[0];
    } else if (v instanceof ArrayRef) {
      ArrayRef ar = (ArrayRef) v;
      if (local == null) return false;
      else return (local.equals(ar.getBase()));
    } else if (v instanceof Constant) {
      return false;
    } else throw new RuntimeException("Unexpected left side " + v.getClass());
  }

  SootField[] getPostfix(Value v) { // this is longer than v
    if (v instanceof InstanceFieldRef || v instanceof StaticFieldRef) {
      if (fields.length > 0)
        return Arrays.copyOfRange(fields, 1, fields.length);
      return new SootField[]{};
    } else if (v instanceof ArrayRef) {
      return new SootField[]{};
    } else throw new RuntimeException("Unexpected left side " + v.getClass());
  }

  private void truncateFields() {
    if (this.fields.length > MAX_AP_LENGTH) {
      this.fields = Arrays.copyOf(this.fields, MAX_AP_LENGTH);
    }
  }
}