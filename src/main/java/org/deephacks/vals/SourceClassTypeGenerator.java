package org.deephacks.vals;



import java.io.IOException;
import java.util.*;

import static java.util.Map.Entry;

class SourceClassTypeGenerator extends SourceGenerator {

  public SourceClassTypeGenerator(SourceClassType type) {
    super(type);
  }

  @Override
  public String writeSource() throws IOException {
    writer.emitPackage(type.getPackageName());
    if (type.hasArrayField()) {
      writer.emitImports(Arrays.class.getName());
    }
    if (type.hasListField()) {
      writer.emitImports(List.class.getCanonicalName());
    }
    if (type.hasMapField()) {
      writer.emitImports(Map.class.getCanonicalName());
    }

    writer.emitImports(Entry.class.getCanonicalName());
    writer.emitImports(Sizes.class.getCanonicalName());

    writer.emitEmptyLine();
    writer.beginType(type.getGeneratedType(), "class", PUBLIC, null, type.getClassName());
    // fields for builder
    for (SourceField field : type.getFields()) {
      if (field.isOptional()) {
        writer.emitField("java.util.Optional<" + field.getFullTypeString() + ">", field.getName(), PRIVATE);
      } else {
        writer.emitField(field.getFullTypeString(), field.getName(), PRIVATE);
      }
    }

    writer.emitField("int[] ", "_pointers", PRIVATE);
    writer.emitField("int", "_totalSize", PRIVATE, "-1");
    writer.emitField("DirectBuffer", "_buffer", PRIVATE);
    writer.emitField("int", "_offset", PRIVATE, "0");

    int numBitFields = type.getMaxId() / 32;

    for (int i = 0; i <= numBitFields; i++) {
      writer.emitField("int", "_bits" + i, PRIVATE, "0");
    }

    writer.emitEmptyLine();

    // constructor for builder
    writer.beginConstructor(PUBLIC, type.getAllFieldsAsStrings(), Collections.emptyList());
    for (SourceField field : type.getFields()) {
      if (!field.isPrimitive()) {
        if (field.hasDefaultValue()) {
          writer.emitStatement("this." + field.getName() +
            " = java.util.Optional.ofNullable(" + field.getName() + ").orElse(" + type.getClassName() +
            ".super." + field.getGetMethod() + "())");
        } else {
          writer.emitStatement("this." + field.getName() + " = " + field.getName());
        }
        writer.beginControlFlow("if (" + field.getName() + " == null)");
        writer.emitStatement("throw new IllegalArgumentException(\"" + field.getName() + " is null\")");
        writer.endControlFlow();
      } else {
        writer.emitStatement("this." + field.getName() + " = " + field.getName());
      }
    }
    writer.endConstructor();
    writer.emitEmptyLine();

    // constructor for deserializer
    writer.beginConstructor(PUBLIC, "DirectBuffer", "buffer", "int", "offset");
    writer.emitStatement("this._buffer = buffer");
    writer.emitStatement("this._offset = offset");
    writer.endConstructor();
    writer.emitEmptyLine();

    writeGetters();
    writer.emitEmptyLine();

    writeWriteToMethods();
    writer.emitEmptyLine();

    writeGetTotalSizeMethod();
    writer.emitEmptyLine();

    writeGetPointersMethod();
    writer.emitEmptyLine();

    writeReadPointersMethod();
    writer.emitEmptyLine();

    writeWriteHeadersMethod();
    writer.emitEmptyLine();

    writeEquals();
    writer.emitEmptyLine();

    writeHashCode();
    writer.emitEmptyLine();

    writeToString();
    writer.emitEmptyLine();

    writer.endType();
    writer.close();
    return out.toString();
  }

  private void writeWriteHeadersMethod() throws IOException {
    writer.beginMethod("void", "writeHeaders", SourceGenerator.PRIVATE, "DirectBuffer", "buffer", "int", "offset");
    writer.emitStatement("getPointers()");
    writer.emitStatement("buffer.putInt(offset, _totalSize)");
    writer.emitStatement("offset += 4");
    writer.emitStatement("buffer.putInt(offset, _pointers.length)");
    writer.emitStatement("offset += 4");
    writer.beginControlFlow("for (int i = 0; i < _pointers.length; i++)");
    writer.emitStatement("buffer.putInt(offset, _pointers[i])");
    writer.emitStatement("offset += 4");
    writer.endControlFlow();
    writer.endMethod();
  }

  private void writeWriteToMethods() throws IOException {
    List<String> params = Arrays.asList(DirectBuffer.class.getSimpleName(), "buffer", "int", "offset");

    writer.beginMethod("void", "writeTo", SourceGenerator.PUBLIC, params, null);
    writer.emitStatement("writeHeaders(buffer, offset)");
    writer.emitStatement("int typeSize = 0");
    writer.emitStatement("int fieldOffset = 0");
    for (SourceField field : type.getFields()) {
      field.startWrite(writer);
      field.write(writer);
      // field.endWrite(writer);
      writer.emitEmptyLine();
    }
    writer.endMethod();

    writer.beginMethod("byte[]", "toByteArray", SourceGenerator.PUBLIC);
    writer.emitStatement("DirectBuffer buffer = new DirectBuffer(new byte[getTotalSize()])");
    writer.emitStatement("this.writeTo(buffer, 0)");
    writer.emitStatement("return buffer.byteArray()");
    writer.endMethod();
  }

  private void writeGetPointersMethod() throws IOException {
    writer.beginMethod("int[]", "getPointers", SourceGenerator.PRIVATE, null, null);
    writer.beginControlFlow("if (_pointers != null)");
    writer.emitStatement("return _pointers");
    writer.endControlFlow();
    writer.beginControlFlow("if (_buffer != null)");
    writer.emitStatement("readPointers()");
    writer.emitStatement("return _pointers");
    writer.endControlFlow();
    writer.emitStatement("_pointers = new int[" + (type.getMaxId() + 1) + "]");
    writer.emitStatement("_totalSize = 4 + 4 + _pointers.length * 4");
    for (SourceField field : type.getFields()) {
      writer.emitStatement("_pointers[" + field.getId() + "] = _totalSize");
      addTotalSize(field);
    }
    writer.emitStatement("return _pointers");
    writer.endMethod();
  }

  private void writeReadPointersMethod() throws IOException {
    writer.beginMethod("int[]", "readPointers", SourceGenerator.PRIVATE, null, null);
    writer.beginControlFlow("if (_pointers != null)");
    writer.emitStatement("return _pointers");
    writer.endControlFlow();
    writer.emitStatement("int offset = _offset");
    writer.emitStatement("_pointers = new int[" + (type.getMaxId() + 1) + "]");
    writer.emitStatement("_totalSize = _buffer.getInt(offset)");
    writer.emitStatement("offset += 4");
    writer.emitStatement("int numPointers = _buffer.getInt(offset)");
    writer.emitStatement("offset += 4");
    writer.beginControlFlow("for (int i = 0; i < numPointers; i++)");
    writer.emitStatement("_pointers[i] = _buffer.getInt(offset)");
    writer.emitStatement("offset += 4");
    writer.endControlFlow();
    writer.emitStatement("return _pointers");
    writer.endMethod();
  }

  private void addTotalSize(SourceField field) throws IOException {
    SourceTypeInfo info = field.getTypeInfo();
    if (info.isList() || info.isEnumSet()) {
      writer.emitStatement("_totalSize += 4");
      writer.beginControlFlow("for (" + field.getType(0) + " e : " + field.getListIterator() + ")");
      writer.emitStatement("_totalSize += " + field.getSizeStmtTypeArg(0, "e"));
      writer.endControlFlow();
    } else if (info.isArray()) {
      writer.emitStatement("_totalSize += 4");
      writer.emitStatement("_totalSize += " + field.getName() + ".length * " + field.getSizeStmt());
    } else if (info.isMap()) {
      if (info.isEmbeddedTypeArg(0)) {
        throw new IllegalArgumentException("Keys are not allowed to be embedded type: " + field.getName());
      }
      writer.emitStatement("_totalSize += 4");
      writer.beginControlFlow("for (" + field.getType(0) + " k : " + field.getMapKeySet() + ")");
      writer.emitStatement("_totalSize += " + field.getSizeStmtTypeArg(0, "k"));
      writer.emitStatement("_totalSize += " + field.getSizeStmtTypeArg(1, field.getMapValue("k")));
      writer.endControlFlow();
    } else {
      writer.emitStatement("_totalSize += " + field.getSizeStmt());
    }
  }

  private void writeGetTotalSizeMethod() throws IOException {
    writer.beginMethod("int", "getTotalSize", SourceGenerator.PUBLIC, null, null);
    writer.beginControlFlow("if (_totalSize == -1)");
    writer.emitStatement("getPointers()");
    writer.endControlFlow();
    writer.emitStatement("return _totalSize");
    writer.endMethod();
  }

  private void writeGetters() throws IOException {
    for (SourceField field : type.getFields()) {
      if (field.isOptional()) {
        writer.beginMethod("java.util.Optional<" + field.getFullTypeString() + ">",
          field.getGetMethod(), PUBLIC);
      } else {
        writer.beginMethod(field.getExternalType(), field.getGetMethod(), PUBLIC);
      }
      field.startRead(writer);
      field.read(writer);
      field.endRead(writer);
      if (field.isByteString()) {
        writer.emitStatement("return " + field.getName() + ".getString()");
      } else {
        writer.emitStatement("return " + field.getName());
      }
      writer.endMethod();
      writer.emitEmptyLine();
    }
  }

  private void writeToString() throws IOException {
    ListIterator<SourceField> it;
    writer.emitAnnotation("Override");
    writer.beginMethod("String", "toString", PUBLIC);

    String s = "return \"" + type.getSimpleClassName() + "{\" \n";
    it = type.getFields().listIterator();
    while (it.hasNext()) {
      SourceField field = it.next();
      if (field instanceof SourceField.ArrayField) {
        s += "+ \"" + field.getName() + "=\" + java.util.Arrays.toString(" + field.getGetMethod() + "())";
      } else {
        s += "+ \"" + field.getName() + "=\" + " + field.getGetMethod() + "()";
      }
      if (it.hasNext()) {
        s += " + \",\"\n";
      }
    }
    s += " + \"}\"";
    writer.emitStatement(s);
    writer.endMethod();
  }

  private void writeHashCode() throws IOException {
    writer.emitAnnotation("Override");
    writer.beginMethod("int", "hashCode", PUBLIC);
    writer.emitStatement("int h = 1");
    writer.emitStatement("h *= 1000003");
    ListIterator<SourceField> it = type.getFields().listIterator();
    while (it.hasNext()) {
      SourceField field = it.next();
      writer.emitStatement("h ^= " + field.generateHashCode());
      if (it.hasNext()) {
        writer.emitStatement("h *= 1000003");
      }
    }
    writer.emitStatement("return h");
    writer.endMethod();
  }

  private void writeEquals() throws IOException {
    writer.emitAnnotation("Override");
    writer.beginMethod("boolean", "equals", PUBLIC, "Object", "o");
    writer.emitStatement("if (o == this) return true");
    writer.emitStatement("if (!(o instanceof " + type.getClassName() + ")) return true");
    writer.emitStatement(type.getClassName() + " that = (" + type.getClassName() + ") o");
    for (SourceField value : type.getFields()) {
      writer.emitStatement("if (" + value.generateEquals() + ") return false");
      // writer.emitStatement("if (!this." + value.getGetMethod() + "().equals(that." + value.getGetMethod() + "())) return false");
    }
    writer.emitStatement("return true");
    writer.endMethod();
  }
}
