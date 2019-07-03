package alias;

import marking.Marker;
import marking.Node;
import soot.Local;
import soot.SootField;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;

public class EquivValue extends Node {
  final static EquivValue zeroAbstraction =
    new EquivValue(null, null, null, null, null);
  private Local local;
  private SootField field;

  EquivValue(Value value, Unit unit, Unit source, EquivValue predecessor,
             Marker marker) {
    super(unit, source, predecessor, marker);
    setLocalAndField(value);
  }

  private EquivValue(Local local, SootField field, Unit unit, Unit source,
                     EquivValue predecessor, Marker marker) {
    super(unit, source, predecessor, marker);
    this.local = local;
    this.field = field;
  }

  public Local getLocal() {
    return local;
  }

  private void setLocal(Local local) {
    this.local = local;
  }

  public SootField getField() {
    return field;
  }

  private void setField(SootField field) {
    this.field = field;
  }

  private void setLocalAndField(Value v) {
    this.local = null;
    this.field = null;
    if (v == null) return;
    if (v instanceof Local) {
      this.local = (Local) v;
    } else if (v instanceof InstanceFieldRef) {
      InstanceFieldRef ifr = (InstanceFieldRef) v;
      this.local = (Local) ifr.getBase();
      this.field = ifr.getField();
    } else if (v instanceof StaticFieldRef) {
      StaticFieldRef sfr = (StaticFieldRef) v;
      this.local = null;
      this.field = sfr.getField();
    } else if (v instanceof ArrayRef) {
      ArrayRef ar = (ArrayRef) v;
      this.local = (Local) ar.getBase();
    }
  }

  // Used privately. Returns a clone of this abstraction, with this node as the
  // precedessor node for the new abstraction.
  private EquivValue derive(Marker marker, Unit unit) {
    return new EquivValue(this.local, this.field, unit, this.source, this,
      marker);
  }

  EquivValue deriveWithNewStmt(Unit unit, Marker marker) {
    EquivValue copy = derive(marker, unit);
    copy.setUnit(unit);
    return copy;
  }

  EquivValue deriveWithNewValue(Value value, Unit unit, Marker marker) {
    EquivValue copy = derive(marker, unit);
    copy.setLocalAndField(value);
    return copy;
  }

  EquivValue deriveWithNewValue(Local local, SootField field, Unit unit,
                                Marker marker) {
    EquivValue copy = derive(marker, unit);
    copy.setLocal(local);
    copy.setField(field);
    return copy;
  }

  EquivValue deriveWithNewField(SootField field, Unit unit, Marker marker) {
    EquivValue copy = derive(marker, unit);
    copy.setField(field);
    return copy;
  }

  @Override
  public int hashCode() {
    if (isZeroAbstraction()) return 0;
    final int prime = 31;
    int result = 1;
    result = prime * result + ((local == null) ? 0 : local.hashCode());
    result = prime * result + ((field == null) ? 0 : field.hashCode());
    result = prime * result + ((unit == null) ? 0 : unit.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    if (((Node) o).isZeroAbstraction() && isZeroAbstraction()) return true;
    if (!(o instanceof EquivValue)) return false;
    EquivValue other = (EquivValue) o;
    if (local == null) {
      if (other.local != null) return false;
    } else if (!local.equals(other.local)) return false;
    if (field == null) {
      if (other.field != null) return false;
    } else if (!field.equals(other.field)) return false;
    if (unit == null) {
      return other.unit == null;
    } else return unit.equals(other.unit);
  }

  @Override
  public String toString() {
    return abstraction();
  }

  @Override
  public String abstraction() {
    if (isZeroAbstraction()) return "<ZERO>";
    String res = "";
    if (local != null) res += local.getName();
    if (field != null) res += "." + field.getName();
    return res;
  }

  @Override
  public boolean isZeroAbstraction() {
    return local == null && field == null;
  }

  boolean equalsName(EquivValue val) {
    if (local == null) {
      if (val.getLocal() != null) return false;
    } else if (!local.equals(val.getLocal())) return false;
    if (field == null) {
      return val.getField() == null;
    } else return field.equals(val.getField());
  }
}