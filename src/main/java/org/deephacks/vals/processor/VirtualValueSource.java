package org.deephacks.vals.processor;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import org.deephacks.vals.VirtualState;
import org.deephacks.vals.VirtualState.DefaultVirtualStorage;
import org.deephacks.vals.processor.TypeValue.PropertyValue;

import java.util.Arrays;
import java.util.List;

public class VirtualValueSource extends SourceGenerator {
  private String virtualStateField = "virtualState";

  public VirtualValueSource(TypeValue type) {
    super(type);
  }

  @Override
  public JFieldRef getSubClassField(PropertyValue p) {
    return JExpr._this().ref(virtualStateField);
  }

  @Override
  public void generateSubClassConstructor(JMethod constructor) throws ClassNotFoundException {
    JType type = codeModel.parseType(VirtualState.class.getName());
    JVar param = constructor.param(type, virtualStateField);
    JFieldRef ref = JExpr._this().ref(virtualStateField);
    constructor.body().assign(ref, param);
  }

  @Override
  protected void generateSubClassMethods() throws ClassNotFoundException {
    for (PropertyValue p : type.getProperties()) {
      generatePropertyMethod(p, subclass);
    }
  }

  private void generatePropertyMethod(PropertyValue p, JDefinedClass subclass) throws ClassNotFoundException {
    JType returnType = codeModel.parseType(p.getTypeString());
    JMethod method = subclass.method(JMod.PUBLIC, returnType, p.getGetMethod());
    method.annotate(Override.class);
    method.annotate(SuppressWarnings.class).param("value", "all");
    JInvocation get = JExpr._this().ref(virtualStateField).invoke("get").arg(p.getName());
    JExpression cast = JExpr.cast(returnType, get);
    if (p.isArray()) {
      codeModel.ref(Arrays.class);
    }
    if (p.isDefault()) {
      // invoke default method if value is null
      JVar value = method.body().decl(returnType, "value").init(cast);
      JExpression valueNotNull = JOp.ne(value, JExpr._null());
      JExpression invokeDefault = JExpr.direct(type.getClassName() + ".super." + p.getGetMethod() + "()");
      method.body()._return(JOp.cond(valueNotNull, value, invokeDefault));
    } else {
      method.body().block()._return(cast);
    }
  }

  @Override
  protected void generateSubClassFields() {
    subclass.field(JMod.PRIVATE | JMod.FINAL, VirtualState.class, virtualStateField);
  }

  @Override
  protected JExpression getSubClassNullCheck(PropertyValue p) {
    return getSubClassField(null).invoke("isNull").arg(JExpr.lit(p.getName()));
  }

  @Override
  protected void builderSetProperty(JBlock block, JVar param, PropertyValue p) {
    JInvocation set = JExpr._this().ref(virtualStateField).invoke("set");
    set.arg(JExpr.lit(p.getName()));
    set.arg(param);
    block.add(set);
  }

  @Override
  protected List<JFieldVar> addBuilderFields(JDefinedClass builderclass) throws ClassNotFoundException {
    JType proxyType = codeModel.parseType(DefaultVirtualStorage.class.getName());
    return Arrays.asList(builderclass.field(JMod.PRIVATE | JMod.FINAL, DefaultVirtualStorage.class, virtualStateField, JExpr._new(proxyType)));
  }

}
