/* AUTO-GENERATED FILE. DO NOT MODIFY.
 * This class was automatically generated by the
 * static binding generator from the resources it found.
 * Please do not modify by hand.
 */
package com.tns.gen.co.fitcom.fancycouchbase;

public class TNSDatabaseChangeListener_vendor_66126_71_
    extends co.fitcom.fancycouchbase.TNSDatabaseChangeListener
    implements com.tns.NativeScriptHashCodeProvider {
  public TNSDatabaseChangeListener_vendor_66126_71_() {
    super();
    com.tns.Runtime.initInstance(this);
  }

  public void onChange(com.couchbase.lite.DatabaseChange param_0) {
    java.lang.Object[] args = new java.lang.Object[1];
    args[0] = param_0;
    com.tns.Runtime.callJSMethod(this, "onChange", void.class, args);
  }

  public int hashCode__super() {
    return super.hashCode();
  }

  public boolean equals__super(java.lang.Object other) {
    return super.equals(other);
  }
}
