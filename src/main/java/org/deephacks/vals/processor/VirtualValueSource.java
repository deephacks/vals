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
import org.deephacks.vals.processor.TypeValue.PropertyValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualValueSource extends SourceGenerator {
  private String stateField = "state";

  public VirtualValueSource(TypeValue type) {
    super(type);
  }

  @Override
  public JFieldRef getSubClassField(PropertyValue p) {
    return JExpr._this().ref(stateField);
  }

  @Override
  public void generateSubClassConstructor(JMethod constructor) throws ClassNotFoundException {
    JType type = codeModel.ref(Map.class.getName()).narrow(
            codeModel.ref("String"),
            codeModel.ref("Object"));
    JVar param = constructor.param(type, stateField);
    JFieldRef ref = JExpr._this().ref(stateField);
    constructor.body().assign(ref, param);
    for (PropertyValue p : this.type.getProperties()) {
      nullCheck(constructor, p);
    }
  }

  private void nullCheck(JMethod constructor, PropertyValue p) {
    if (p.isNullable()) {
      return;
    }
    // check value
    JExpression nullCheck = getSubClassNullCheck(p);
    if (nullCheck == null) {
      return;
    }
    JInvocation nullPointer = JExpr._new(codeModel._ref(NullPointerException.class));
    nullPointer.arg(JExpr.lit(p.getName() + " is null."));
    if (p.isDefault()) {
      // if value is null, check default.
      JExpression isNull = nullCheck.cand(JExpr.direct(type.getClassName() + ".super." + p.getGetMethod() + "()").eq(JExpr._null()));
      constructor.body()._if(isNull)._then()._throw(nullPointer);
    } else {
      constructor.body()._if(nullCheck)._then()._throw(nullPointer);
    }
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
    JInvocation get = JExpr._this().ref(stateField).invoke("get").arg(p.getName());
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
    JType type = codeModel.ref(Map.class.getName()).narrow(
            codeModel.ref("String"),
            codeModel.ref("Object"));
    subclass.field(JMod.PRIVATE | JMod.FINAL, type, stateField);
  }

  @Override
  protected JExpression getSubClassNullCheck(PropertyValue p) {
    return JOp.not(getSubClassField(null).invoke("containsKey").arg(JExpr.lit(p.getName())));
  }

  @Override
  protected void builderSetProperty(JBlock block, JVar param, PropertyValue p) {
    JInvocation put = JExpr._this().ref(stateField).invoke("put");
    put.arg(JExpr.lit(p.getName()));
    put.arg(param);
    block.add(put);
  }

  @Override
  protected List<JFieldVar> addBuilderFields(JDefinedClass builderclass) throws ClassNotFoundException {
    JType type = codeModel.ref(HashMap.class.getName()).narrow(
            codeModel.ref("String"),
            codeModel.ref("Object"));
    return Arrays.asList(builderclass.field(JMod.PRIVATE | JMod.FINAL, type, stateField, JExpr._new(type)));
  }

}
