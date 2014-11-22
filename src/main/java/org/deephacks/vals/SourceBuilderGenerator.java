package org.deephacks.vals;


import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

class SourceBuilderGenerator extends SourceGenerator {
  private String prefix;

  public SourceBuilderGenerator(SourceClassType type) {
    super(type, type.getPackageName() + "." + type.getSimpleClassName() + "Builder");
    this.prefix = type.getBuilderMethodsPrefix();
  }

  @Override
  public String writeSource() throws IOException {
    writer.emitPackage(type.getPackageName());
    if (type.hasArrayField()) {
      writer.emitImports(Arrays.class.getName());
    }
    writer.emitEmptyLine();

    writer.beginType(className, "class", PUBLIC);

    // fields for builder
    for (SourceField field : type.getFields()) {
      if (field.isOptional()) {
        writer.emitField("java.util.Optional<" + field.getFullTypeString() + ">", field.getName(), PRIVATE, "java.util.Optional.empty()");
      } else {
        writer.emitField(field.getFullTypeString(), field.getName(), PRIVATE);
      }
    }

    writer.emitEmptyLine();

    writeWithMethod();

    writer.emitEmptyLine();

    writeBuildMethod();

    writeCopyMethod();

    writeDecodeMethods();
    writer.emitEmptyLine();

    writer.endType();
    writer.close();
    return out.toString();
  }

  private void writeDecodeMethods() throws IOException {
    /*
    writer.beginMethod(type.getClassName(), "parseFrom", SourceGenerator.PUBLIC_STATIC, "byte[]", "bytes");
    writer.emitStatement("return null");
    writer.endMethod();
    writer.beginMethod(type.getClassName(), "parseFrom", SourceGenerator.PUBLIC_STATIC, "Bytes", "bytes");
    writer.emitStatement("return null");
    writer.endMethod();
    */
  }

  private void writeCopyMethod() throws IOException {
    writer.beginMethod(className, "copy", PUBLIC_STATIC, type.getClassName(), "entity");
    writer.emitStatement(className + " builder = new " + className + "()");
    Iterator<SourceField> it = type.getFields().iterator();
    while (it.hasNext()) {
      SourceField field = it.next();
      String methodName = "".equals(prefix.trim()) ? field.getName() : prefix + field.getNameFirstCapitalized();
      if (field.isOptional()) {
        writer.emitStatement("builder." + methodName + "(entity." + field.getGetMethod() + "().get())");
      } else {
        writer.emitStatement("builder." + methodName + "(entity." + field.getGetMethod() + "())");
      }
    }
    writer.emitStatement("return builder");
    writer.endMethod();
    writer.emitEmptyLine();
  }

  private void writeBuildMethod() throws IOException {
    writer.beginMethod(type.getClassName(), "build", PUBLIC);
    StringBuilder sb = new StringBuilder();
    Iterator<SourceField> it = type.getFields().iterator();
    while (it.hasNext()) {
      SourceField field = it.next();
      sb.append(field.getName());
      if (it.hasNext()) {
        sb.append(",");
      }
    }
    writer.emitStatement("return new " + type.getGeneratedType() + "("+sb.toString()+")");
    writer.endMethod();
    writer.emitEmptyLine();
  }

  private void writeWithMethod() throws IOException {
    for (SourceField field : type.getFields()) {
      String methodName = "".equals(prefix.trim()) ? field.getName() : prefix + field.getNameFirstCapitalized();
      writer.beginMethod(className, methodName, PUBLIC, field.getExternalTypeString(), field.getName());
      if (field.isOptional()) {
        writer.emitStatement("this." + field.getName() + " = java.util.Optional.ofNullable(" + field.getName() + ")");
      } else {
        if (field.isByteString()) {
          writer.emitStatement("this." + field.getName() + " = new " + ByteString.class.getName() + "(" + field.getName() + ")");
        } else if (field.isByteStringArrayList()) {
          writer.emitStatement("this." + field.getName() + " = new " + ByteStringArrayList.class.getName() + " (" + field.getName() +")");
        } else if (field.isMap()) {
          writer.emitStatement("this." + field.getName() + " = new " + ByteStringHashMap.class.getName() + " (" + field.getName() +")");
        } else {
          writer.emitStatement("this." + field.getName() + " = " + field.getName());
        }
      }
      writer.emitStatement("return this");
      writer.endMethod();
      writer.emitEmptyLine();
    }
  }
}
