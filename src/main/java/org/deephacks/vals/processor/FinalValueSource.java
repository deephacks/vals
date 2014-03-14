package org.deephacks.vals.processor;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import org.deephacks.vals.processor.TypeValue.PropertyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FinalValueSource extends SourceGenerator {

  public FinalValueSource(TypeValue type) {
    super(type);
  }

  @Override
  public void generateSubClassConstructor(JMethod constructor) throws ClassNotFoundException {
    for (PropertyValue p : type.getProperties()) {
      JType type = codeModel.parseType(p.getTypeString());
      JVar param = constructor.param(type, p.getName());
      JFieldRef ref = JExpr._this().ref(param);
      constructor.body().assign(ref, param);
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
    JFieldRef ref = JExpr._this().ref(p.getName());
    if (p.isArray()) {
      codeModel.ref(Arrays.class);
    }
    if (p.isDefault()) {
      // invoke default method if value is null
      JVar value = method.body().decl(returnType, "value").init(ref);
      JExpression valueNotNull = JOp.ne(value, JExpr._null());
      JExpression invokeDefault = JExpr.direct(type.getClassName() + ".super." + p.getGetMethod() + "()");
      method.body()._return(JOp.cond(valueNotNull, value, invokeDefault));
    } else {
      method.body().block()._return(ref);
    }
  }


  @Override
  protected void generateSubClassFields() throws ClassNotFoundException {
    for (PropertyValue p : type.getProperties()) {
      JType type = codeModel.parseType(p.getTypeString());
      subclass.field(JMod.PRIVATE | JMod.FINAL, type, p.getName());
    }
  }

  @Override
  protected JExpression getSubClassNullCheck(PropertyValue p) {
    if (p.isPrimitive()) {
      return null;
    }
    return getSubClassField(p).eq(JExpr._null());
  }

  @Override
  public JFieldRef getSubClassField(PropertyValue p) {
    return JExpr._this().ref(p.getName());
  }

  @Override
  protected void builderSetProperty(JBlock block, JVar param, PropertyValue p) {
    block.assign(JExpr._this().ref(p.getName()), param);
  }

  @Override
  protected List<JFieldVar> addBuilderFields(JDefinedClass builderclass) throws ClassNotFoundException {
    List<JFieldVar> fields = new ArrayList<>();
    for (PropertyValue p : type.getProperties()) {
      JType type = codeModel.parseType(p.getTypeString());
      fields.add(builderclass.field(JMod.PRIVATE, type, p.getName()));
    }
    return fields;
  }
}
