package org.deephacks.vals.processor;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import org.deephacks.vals.processor.TypeValue.PropertyValue;

import javax.annotation.Generated;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

abstract class SourceGenerator {
  protected TypeValue type;
  protected JCodeModel codeModel = new JCodeModel();
  protected JDefinedClass subclass;

  public SourceGenerator(TypeValue type) {
    this.type = type;
    codeModel._package(type.getPackageName());
  }

  public abstract void generateSubClassConstructor(JMethod constructor) throws ClassNotFoundException;

  protected abstract void generateSubClassMethods() throws ClassNotFoundException;

  protected abstract void generateSubClassFields() throws ClassNotFoundException;

  protected abstract JExpression getSubClassNullCheck(PropertyValue p);

  public abstract JFieldRef getSubClassField(PropertyValue p);

  protected abstract void builderSetProperty(JBlock block, JVar param, PropertyValue p);

  protected abstract List<JFieldVar> addBuilderFields(JDefinedClass builderclass) throws ClassNotFoundException;

  public String generateSubClassSource() {
    try {
      // define subclass implementing interface
      this.subclass = codeModel._class(type.getSubClassName());
      JClass realClass = codeModel.ref(type.getClassName());
      subclass.annotate(Generated.class)
              .param("value", AnnotationProcessor.class.getName())
              .param("date", LocalDateTime.now().toString());
      subclass._implements(realClass);

      JMethod constructor = subclass.constructor(JMod.NONE);

      generateSubClassFields();
      generateSubClassConstructor(constructor);
      if (type.hasPostConstruct()) {
        constructor.body().directStatement(type.getClassName() + ".postConstruct(this);");
      }

      generateSubClassMethods();

      generateEquals(subclass, realClass);
      generateHashcode(subclass);
      generateToString(subclass);

      return new SourceWriter(codeModel).getSource();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void generateToString(JDefinedClass subclass) {
    JMethod toString = subclass.method(JMod.PUBLIC, String.class, "toString");
    toString.annotate(Override.class);
    if (type.hasToString()) {
      toString.body()._return(JExpr.direct(type.getClassName() + ".toString(this)"));
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("return \"").append(type.getSimpleClassName()).append("{").append("\"\n");
      ListIterator<PropertyValue> it = type.getProperties().listIterator();
      while (it.hasNext()) {
        PropertyValue p = it.next();
        if (p.isArray()) {
          sb.append("          + \"").append(p.getName()).append("=\" + Arrays.toString(").append(p.getGetMethod()).append("())");
        } else {
          sb.append("          + \"").append(p.getName()).append("=\" + ").append(p.getGetMethod()).append("()");
        }
        if (it.hasNext()) {
          sb.append(" + \",\"");
        }
        sb.append("\n");
      }
      sb.append("          + \"}\";");
      toString.body().directStatement(sb.toString());
    }
  }

  private void generateHashcode(JDefinedClass subclass) {
    JMethod hashCode = subclass.method(JMod.PUBLIC, int.class, "hashCode");
    hashCode.annotate(Override.class);
    if (type.hasHashCode()) {
      hashCode.body()._return(JExpr.direct(type.getClassName() + ".hashCode(this)"));
    } else {
      JVar h = hashCode.body().decl(codeModel.INT, "h").init(JExpr.lit(1));
      hashCode.body().directStatement("h *= 1000003;");
      ListIterator<PropertyValue> it = type.getProperties().listIterator();
      while (it.hasNext()) {
        PropertyValue p = it.next();
        hashCode.body().directStatement("h ^= " + p.generateHashCode() + ";");
        if (it.hasNext()) {
          hashCode.body().directStatement("h *= 1000003;");
        }
      }
      hashCode.body()._return(h);
    }
  }

  private void generateEquals(JDefinedClass subclass, JClass realClass) {
    JMethod equals = subclass.method(JMod.PUBLIC, boolean.class, "equals");
    equals.annotate(Override.class);
    equals.annotate(SuppressWarnings.class).param("value", "all");
    JVar o = equals.param(Object.class, "o");
    if (type.hasEquals()) {
      equals.body()._if(JOp.eq(o, JExpr._this()))._then()._return(JExpr.TRUE);
      equals.body()._if(JOp.not(JOp._instanceof(o, subclass)))._then()._return(JExpr.FALSE);
      equals.body()._return(JExpr.direct(type.getClassName() + ".equals(this, (" + type.getClassName() + ") o)"));
    } else {
      equals.body()._if(JOp.eq(o, JExpr._this()))._then()._return(JExpr.TRUE);
      equals.body()._if(JOp.not(JOp._instanceof(o, subclass)))._then()._return(JExpr.FALSE);
      boolean hasArrays = false;
      if (type.getProperties().size() > 0) {
        ListIterator<PropertyValue> it = type.getProperties().listIterator();
        JVar that = equals.body().decl(subclass, "that");
        that.init(JExpr.cast(subclass, o));

        while (it.hasNext()) {
          StringBuilder sb = new StringBuilder();
          PropertyValue p = it.next();
          sb.append("if (");
          if (p.isArray()) {
            hasArrays = true;
          }
          sb.append(p.generateEquals());
          sb.append(") {").append(" return false;").append(" }").append("\n");
          equals.body().directStatement(sb.toString());
        }
      }
      if (hasArrays) {
        // force code model to import java.util.Arrays by defining an unused variable declaration.
        JClass arrays = codeModel.ref(Arrays.class);
        equals.body().decl(arrays, "unused");
      }
      equals.body()._return(JExpr.TRUE);
    }
  }

  public String generateBuilderSource() {
    try {
      JDefinedClass builderclass = codeModel._class(type.getBuilderClassName());
      builderclass.annotate(Generated.class)
              .param("value", AnnotationProcessor.class.getName())
              .param("date", LocalDateTime.now().toString());
      JClass subclass = codeModel.ref(type.getSubClassName());
      JClass realClass = codeModel.ref(type.getClassName());

      // storage
      List<JFieldVar> fields = addBuilderFields(builderclass);

      // add setter methods
      for (PropertyValue p : type.getProperties()) {
        generateBuilderMethod(p, builderclass);
      }

      // build method
      JMethod build = builderclass.method(JMod.PUBLIC, realClass, "build");

      JInvocation returnStmt = JExpr._new(subclass);
      for (JFieldVar field : fields) {
        returnStmt.arg(field);
      }

      build.body()._return(returnStmt);

      /*
      // _create method
      JMethod create = builderclass.method(JMod.PRIVATE | JMod.STATIC, subclass, "_create");
      JVar storage = create.param(VirtualState.class, "storage");
      create.body()._return(JExpr._new(subclass).arg(storage));
      */

      return new SourceWriter(codeModel).getSource();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void generateBuilderMethod(PropertyValue p, JDefinedClass builderclass) throws ClassNotFoundException {
    JType returnType = codeModel.parseType(p.getTypeString());
    JMethod method;
    if (p.hasBuilderMethodsPrefix()) {
      method = builderclass.method(JMod.PUBLIC, builderclass, p.getBuilderMethodsPrefix() + p.getNameCapital());
    } else {
      method = builderclass.method(JMod.PUBLIC, builderclass, p.getBuilderMethodsPrefix() + p.getName());
    }

    JVar param = method.param(returnType, p.getName());
    builderSetProperty(method.body(), param, p);
    method.body()._return(JExpr._this());
  }

  private static class SourceWriter extends CodeWriter {
    private ByteArrayOutputStream out = new ByteArrayOutputStream();
    private JCodeModel codeModel;

    public SourceWriter(JCodeModel codeModel) {
      this.codeModel = codeModel;
    }

    @Override
    public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
      return new FilterOutputStream(out) {
        public void close() {
        }
      };
    }

    @Override
    public void close() throws IOException {

    }

    public String getSource() {
      try {
        codeModel.build(this);
        return new String(out.toByteArray());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
