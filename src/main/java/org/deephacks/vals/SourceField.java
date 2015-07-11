package org.deephacks.vals;

import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.deephacks.vals.SourceTypeUtil.toCapitalizedBufType;

abstract class SourceField implements Comparable<SourceField> {
  public static final String VAL_DECODER = "_buffer";
  public static final String VAL_ENCODER = "buffer";
  private final boolean hasDefaultValue;

  protected String name;
  protected int id;
  protected String getMethod;
  protected SourceTypeInfo typeInfo;
  protected String readBuf = VAL_DECODER;
  protected String writeBuf = VAL_ENCODER;

  protected SourceField(String name, int id, SourceTypeInfo typeInfo, boolean hasDefaultValue) {
    this.id = id;
    this.getMethod = name;
    name = name.substring(3, name.length());
    this.name = Character.toLowerCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
    this.typeInfo = typeInfo;
    this.hasDefaultValue = hasDefaultValue;
  }

  protected SourceField(String name, int id, SourceTypeInfo typeInfo, boolean hasDefaultValue, String readBuf, String writeBuf) {
    this.id = id;
    this.getMethod = name;
    name = name.substring(3, name.length());
    this.name = Character.toLowerCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
    this.typeInfo = typeInfo;
    this.readBuf = readBuf;
    this.writeBuf = writeBuf;
    this.hasDefaultValue = hasDefaultValue;
  }

  protected SourceField(SourceField field) {
    this.getMethod = field.getGetMethod();
    this.id = field.getId();
    this.name = field.getName();
    this.typeInfo = field.typeInfo;
    this.readBuf = field.readBuf;
    this.writeBuf = field.writeBuf;
    this.hasDefaultValue = field.hasDefaultValue;
  }

  public void startRead(JavaWriter writer) throws IOException {
    // writer.beginControlFlow("if (" + readBuf + " != null && !" + readBuf + ".hasRead(" + getId() + "))");
    writer.beginControlFlow("if (" + readBuf + " != null && " + checkBitSet(getId()));
    if (isOptional()) {
      writer.beginControlFlow("if (!" + readBuf + ".position(\"" + getName() + "\"))");
      writer.emitStatement(getName() + " = java.util.Optional.empty()");
      writer.emitStatement(setBit(getId()));
      writer.emitStatement("return " + getName());
      writer.endControlFlow();
    } else {
      writer.emitStatement("int offset = _buffer.getInt(4 + 4 + _offset + " + getId() + " * 4) + _offset");
    }
  }

  private static String setBit(int id) {
    String hex = getHex(id);
    String bitsField = "_bits" + id / 32;
    return bitsField + " |= " + hex;
  }

  private static String checkBitSet(int id) {
    String hex = getHex(id);
    String bitsField = "_bits" + id / 32;
    return "(" + bitsField + " & " + hex + ") != " + hex + ")";
  }

  public void endRead(JavaWriter writer) throws IOException {
    writer.emitStatement(setBit(getId()));
    writer.endControlFlow();
  }

  public abstract void read(JavaWriter writer, String... name) throws IOException;

  public abstract void write(JavaWriter writer, String... name) throws IOException;

  public void startWrite(JavaWriter writer) throws IOException {
    // writer.emitStatement(writeBuf + ".start(" + getId() + ")");
  }

  public void endWrite(JavaWriter writer) throws IOException {
    writer.emitStatement(writeBuf + ".end(" + getId() + ")");
  }

  public boolean isOptional() {
    return typeInfo.isOptional();
  }

  public String getValueType() {
    String type;
    if (getTypeArgStrings().size() == 0) {
      type = getFullTypeString();
    } else if (getTypeArgStrings().size() == 1) {
      // List or Set
      type = getTypeArgStrings().get(0);
    } else {
      // Map
      type = getTypeArgStrings().get(1);
    }
    return type;
  }

  protected void setField(JavaWriter writer, String code) throws IOException {
    if (isOptional()) {
      writer.emitStatement(getName() + " = java.util.Optional.ofNullable(" + code + ")");
    } else {
      writer.emitStatement(getName() + " = " + code);
    }
  }

  public String getGetMethod() {
    return getMethod;
  }

  public String getGeneratedType() {
    return typeInfo.getGeneratedType();
  }

  public String getCapitalizedBufType() {
    if (getFullTypeString().equals(ByteString.class.getName())) {
      return "String";
    }
    return toCapitalizedBufType(getFullTypeString());
  }

  public String getCapitalizedBufType(String value) {
    if (value.equals(ByteString.class.getName())) {
      return "String";
    }
    return toCapitalizedBufType(value);
  }

  public String getFullTypeString() {
    return typeInfo.getFullTypeString();
  }

  public String getExternalTypeString() {
    if (isByteString()) {
      return "String";
    } else if (isByteStringArrayList()) {
      return "java.util.List<String>";
    } else if (isMap()) {
      return "java.util.Map<" + getExternalType(0) + ", " + getExternalType(1)+ ">";
    }
    return typeInfo.getFullTypeString();
  }

  public String getSimpleType() {
    String type = typeInfo.getFullTypeString();
    return type.substring(0, type.length() - 2);
  }

  public String getName() {
    return name;
  }

  public int getId() {
    return id;
  }

  public String getGetName() {
    if (isOptional()) {
      return name + ".get()";
    } else {
      return name;
    }
  }

  public String getNameFirstCapitalized() {
    return Character.toUpperCase(name.charAt(0)) + name.substring(1, name.length());
  }

  public List<String> getTypeArgStrings() {
    return typeInfo.getTypeArgStrings();
  }

  public String getTypeArgString(int pos) {
    return typeInfo.getTypeArgStrings().get(pos);
  }

  public boolean isKeyByteString() {
    return getTypeArgString(0).equals(ByteString.class.getName());
  }


  public boolean typeArgIsEnum(int pos) {
    return typeInfo.typeArgIsEnum(pos);
  }

  public String generateEquals() {
    switch (typeInfo.getTypeKind()) {
      case BYTE:
      case SHORT:
      case CHAR:
      case INT:
      case LONG:
      case BOOLEAN:
        return "this." + getGetMethod() + "() != that." + getGetMethod() + "()";
      case FLOAT:
        return "Float.floatToIntBits(this." + getGetMethod() + "()) != Float.floatToIntBits(that." + getGetMethod() + "())";
      case DOUBLE:
        return "Double.doubleToLongBits(this." + getGetMethod() + "()) != Double.doubleToLongBits(that." + getGetMethod() + "())";
      case ARRAY:
        return "!java.util.Arrays.equals(this." + getGetMethod() + "(), that." + getGetMethod() + "())";
      default:
        if (isOptional()) {
          return "!(this." + getGetMethod() + "() == null ? that." + getGetMethod() + "() == null : this." + getGetMethod() + "().equals(that." + getGetMethod() + "()))";
        } else {
          return "!(this." + getGetMethod() + "().equals(that." + getGetMethod() + "()))";
        }
    }
  }

  public String generateHashCode() {
    switch (typeInfo.getTypeKind()) {
      case BYTE:
      case SHORT:
      case CHAR:
      case INT:
        return getGetMethod() + "()";
      case LONG:
        return "(" + getGetMethod() + "() >>> 32) ^ " + getGetMethod() + "()";
      case FLOAT:
        return "Float.floatToIntBits(" + getGetMethod() + "())";
      case DOUBLE:
        return "(Double.doubleToLongBits(" + getGetMethod() + "()) >>> 32) ^ "
                + "Double.doubleToLongBits(" + getGetMethod() + "())";
      case BOOLEAN:
        return getGetMethod() + "() ? 1231 : 1237";
      case ARRAY:
        return "java.util.Arrays.hashCode(" + getGetMethod() + "())";
      default:
        if (isOptional()) {
          return "(" + getGetMethod() + "() == null) ? 0 : " + getGetMethod() + "().hashCode()";
        } else {
          return getGetMethod() + "().hashCode()";
        }
    }
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SourceField that = (SourceField) o;

    if (name != null ? !name.equals(that.name) : that.name != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  @Override
  public int compareTo(SourceField o) {
    return Integer.compare(this.getId(), o.getId());
  }

  public boolean isPrimitive() {
    return typeInfo.isPrimitive();
  }

  public boolean isBasicType() {
    if (typeInfo.isList() || typeInfo.isArray()) {
      return SourceTypeUtil.isBasicType(typeInfo.getTypeArgStrings().get(0));
    }
    return SourceTypeUtil.isBasicType(typeInfo.getFullTypeString());
  }

  public boolean hasDefaultValue() {
    return hasDefaultValue;
  }

  public SourceTypeInfo getTypeInfo() {
    return typeInfo;
  }

  public boolean isByteString() {
    return typeInfo.getFullTypeString().equals(ByteString.class.getName());
  }

  public boolean isByteStringArrayList() {
    return typeInfo.isByteStringList();
  }

  public boolean isMap() {
    return typeInfo.isMap();
  }

  public boolean isList() {
    return typeInfo.isList();
  }

  public static class ValueField extends SourceField {
    private Optional<Integer> size;

    public ValueField(String name, int id, boolean hasDefaultValue, SourceTypeInfo typeInfo, Integer size) {
      super(name, id, typeInfo, hasDefaultValue, VAL_DECODER, VAL_ENCODER);
      this.size = Optional.ofNullable(size);
    }

    @Override
    public void read(JavaWriter writer, String... name) throws IOException {
      String stmt = name[0] + " = " + readBuf + ".get" + getCapitalizedBufType() + "(offset)";
      if (isByteStringArrayList() || isByteString()) {
        stmt = name[0] + " = " + readBuf + ".getString(offset)";
      } else if (getTypeInfo().isEnum()) {
        stmt = name[0] + " = " + getFullTypeString() + ".values()[" + readBuf + ".getInt(offset)]";
      }
      writer.emitStatement(stmt);
    }

    @Override
    public void write(JavaWriter writer, String... name) throws IOException {
      writer.emitStatement("fieldOffset = _pointers[" + getId() + "] + offset");
      if (isByteStringArrayList() || isByteString()) {
        String stmt = writeBuf + ".putString(fieldOffset, " + name[0] + "())";
        writer.emitStatement(stmt);
      } else  if (getTypeInfo().isEnum()) {
        String stmt = writeBuf + ".putInt(fieldOffset," + name[0] + "().ordinal())";
        writer.emitStatement(stmt);
      } else {
        String stmt = writeBuf + ".put" + getCapitalizedBufType() + "(fieldOffset, " + name[0] + "())";
        writer.emitStatement(stmt);
      }
    }
  }

  public static class EmbeddedField extends SourceField {

    public EmbeddedField(String name, int id, SourceTypeInfo typeInfo) {
      super(name, id, typeInfo, false);
    }

    @Override
    public void read(JavaWriter writer, String... name) throws IOException {
      String type = getValueType();
      String var = name.length > 1 ? name[1] : name[0];
      writer.emitStatement(var + " = " + get("offset", type));
      //writer.emitStatement(var + " = " + readBuf + ".getObject(offset, " + type + ".class)");
    }

    @Override
    public void write(JavaWriter writer, String... name) throws IOException {
      writer.emitStatement("fieldOffset = _pointers[" + getId() + "] + offset");
      String var = name.length > 0 ? name[0] : getName();
      writer.emitStatement(writeBuf + ".putObject(fieldOffset, " + var + "())");
    }
  }


  public static class SingleField extends SourceField {
    private SourceField field;

    public SingleField(SourceField field) {
      super(field);
      this.field = field;
    }

    @Override
    public void read(JavaWriter writer, String... name) throws IOException {
      if (isPrimitive()) {
        writer.emitStatement(getFullTypeString() + " _value");
      } else {
        writer.emitStatement(getFullTypeString() + " _value = null");
      }

      field.read(writer, "_value");
      if (isOptional()) {
        writer.emitStatement(getName() + " = java.util.Optional.ofNullable(_value)");
      } else {
        writer.emitStatement(getName() + " = _value");
      }
    }

    @Override
    public void write(JavaWriter writer, String... name) throws IOException {
      if (isOptional()) {
        writer.beginControlFlow("if (" + getName() + ".isPresent())");
        field.write(writer, getName() + ".get()");
      } else {
        field.write(writer, getGetMethod());
      }
      if (isOptional()) {
        writer.endControlFlow();
      }
    }
  }


  public static class ArrayField extends SourceField {
    private SourceField field;

    public ArrayField(SourceField field) {
      super(field);
      this.field = field;
    }

    @Override
    public void read(JavaWriter writer, String... name) throws IOException {
      writer.emitStatement("int size = " + readBuf + ".getInt(offset)");
      writer.emitStatement("offset += 4");
      writer.emitStatement(getName() + " = new " + getSimpleType() + "[size]");
      writer.emitStatement("int typeSize = " + getSizeStmt());
      writer.beginControlFlow("for (int i = 0; i < size; i++)");
      field.read(writer, getName() + "[i]");
      writer.emitStatement("offset += typeSize");
      writer.endControlFlow();
    }

    @Override
    public void write(JavaWriter writer, String... name) throws IOException {
      writer.emitStatement("fieldOffset = _pointers[" + getId() + "] + offset");
      writer.emitStatement(writeBuf + ".putInt(fieldOffset, " + getGetMethod() + "().length)");
      writer.emitStatement("fieldOffset += 4");
      writer.emitStatement("typeSize = " + getSizeStmt());
      writer.beginControlFlow("for (int i = 0; i < " + getGetMethod() + "().length; i++)");
      writer.emitStatement(put("fieldOffset", 0, getGetMethod() + "()[i]"));
      if (isBasicType()) {
        writer.emitStatement("fieldOffset += typeSize");
      } else {
        writer.emitStatement("fieldOffset += Sizes.getSize(val)");
      }
      writer.endControlFlow();

    }
  }

  public static class ListField extends SourceField {

    private SourceField field;

    public ListField(SourceField field) {
      super(field);
      this.field = field;
    }

    @Override
    public void read(JavaWriter writer, String... name) throws IOException {
      if (isOptional()) {
        writer.emitStatement(getName() + " = java.util.Optional.of(new java.util.ArrayList<>())");
      } else {
        if (isByteStringArrayList()) {
          writer.emitStatement(getName() + " = new " + ByteStringArrayList.class.getName() + "()");
        } else {
          writer.emitStatement(getName() + " = new java.util.ArrayList<>()");
        }
      }
      writer.emitStatement("int listSize  = " + readBuf + ".getInt(offset)");
      writer.emitStatement("offset += 4");
      if (isBasicType()) {
        writer.emitStatement("int typeSize = " + getSizeStmtTypeArg(0, getName()));
      }
      writer.beginControlFlow("for (int i = 0; i < listSize; i++)");
      if (typeArgIsEnum(0)) {
        writer.emitStatement(getType(0) + " val = " + getEnum("offset", getType(0)));
      } else {
        writer.emitStatement(getType(0) + " val = " + get("offset", getType(0)));
      }

      if (isBasicType()) {
        writer.emitStatement("offset += typeSize");
      } else {
        writer.emitStatement("offset += " + getSizeStmtTypeArg(0, "val"));
      }
      writer.emitStatement(getGetName() + ".add(val)");
      writer.endControlFlow();
    }

    @Override
    public void write(JavaWriter writer, String... name) throws IOException {
      if (isOptional()) {
        writer.beginControlFlow("if (" + getName() + ".isPresent())");
      }
      writer.emitStatement("fieldOffset = _pointers[" + getId() +  "] + offset");
      writer.emitStatement(writeBuf + ".putInt(fieldOffset, " + getGetMethod() + "().size())");
      writer.emitStatement("fieldOffset += 4");
      writer.beginControlFlow("for (" + getType(0) + " e : " + getListIterator() + ")");
      writer.emitStatement(put("fieldOffset", 0, "e"));
      writer.emitStatement("fieldOffset += " + getSizeStmtTypeArg(0, "e"));
      writer.endControlFlow();
      if (isOptional()) {
        writer.endControlFlow();
      }
    }
  }

  public static class EnumSetField extends SourceField {
    private SourceField field;

    public EnumSetField(SourceField field) {
      super(field);
      this.field = field;
    }

    @Override
    public void read(JavaWriter writer, String... name) throws IOException {
      writer.emitStatement(field.getName() + " = java.util.EnumSet.noneOf(" + field.getTypeArgStrings().get(0)+ ".class)");
      writer.emitStatement("int size = " + readBuf + ".getInt(offset)");
      writer.emitStatement("offset += 4");
      writer.beginControlFlow("for (int i = 0; i < size; i++)");
      writer.emitStatement(getTypeArgStrings().get(0) + " val = " + getTypeArgStrings().get(0) +
        ".values()[" + readBuf + ".getInt(offset)]");
      writer.emitStatement(field.getGetName() + ".add(val)");
      writer.emitStatement("offset += 4");
      writer.endControlFlow();
    }

    @Override
    public void write(JavaWriter writer, String... name) throws IOException {
      writer.emitStatement("fieldOffset = _pointers[" + getId() +  "] + offset");
      writer.emitStatement(writeBuf + ".putInt(fieldOffset, " + field.getGetName() + ".size())");
      writer.emitStatement("fieldOffset += 4");
      writer.beginControlFlow("for (" + field.getTypeArgStrings().get(0) + " e : " + field.getGetName() + ")");
      writer.emitStatement(writeBuf + ".putInt(fieldOffset, e.ordinal())");
      writer.emitStatement("fieldOffset += 4");
      writer.endControlFlow();
    }
  }


  public static class MapField extends SourceField {

    private SourceField field;

    public MapField(SourceField field) {
      super(field);
      this.field = field;
    }

    @Override
    public void read(JavaWriter writer, String... name) throws IOException {
      String keyType = getTypeArgStrings().get(0);
      String valType = getTypeArgStrings().get(1);
      if (isOptional()) {
        writer.emitStatement(getName() + " = java.util.Optional.of(new " + ByteStringHashMap.class.getName() + "())");
      } else {
        writer.emitStatement(getName() + " = new " + ByteStringHashMap.class.getName() + "<>()");
      }
      writer.emitStatement("int mapSize = " + readBuf + ".getInt(offset)");
      writer.emitStatement("offset += 4");
      writer.beginControlFlow("for (int i = 0; i < mapSize; i++)");
      if (typeArgIsEnum(0)) {
        writer.emitStatement(keyType + " key = " + keyType + ".values()[" + readBuf + ".getInt(offset)]");
        writer.emitStatement("offset += " + getSizeStmt(keyType, "key"));
      } else {
        writer.emitStatement(keyType + " key = " + readBuf + ".get" + getCapitalizedBufType(keyType) + "(offset)");
        writer.emitStatement("offset += Sizes.getSize(key)");
      }

      if (typeArgIsEnum(1)) {
        writer.emitStatement(valType + " val = " + valType + ".values()[" + readBuf + ".getInt(offset)]");
        writer.emitStatement("offset += 4");
      } else {
        writer.emitStatement(valType + " val = " + get("offset", getTypeArgStrings().get(1)));
        //field.read(writer, getTypeArgStrings().get(1) + " val");
        writer.emitStatement("offset += " + getSizeStmt(valType, "val"));
      }
      writer.emitStatement(getGetName() + ".put(key, val)");
      writer.endControlFlow();
    }

    @Override
    public void write(JavaWriter writer, String... name) throws IOException {
      String keyType = getTypeArgStrings().get(0);
      if (isOptional()) {
        writer.beginControlFlow("if (" + getName() + ".isPresent())");
      }
      writer.emitStatement("fieldOffset = _pointers[" + getId() +  "] + offset");
      writer.emitStatement(writeBuf + ".putInt(fieldOffset, " + getName() + ".size())");
      writer.emitStatement("fieldOffset += 4");
      writer.beginControlFlow("for (" + keyType + " k : " + getMapKeySet() + ")");
      if (typeInfo.isEmbeddedTypeArg(0)) {
        throw new IllegalArgumentException("Keys are not allowed to be embedded type: " + field.getName());
      }
      writer.emitStatement(put("fieldOffset", 0, "k"));
      writer.emitStatement("fieldOffset += " + getSizeStmtTypeArg(0, "k"));
      writer.emitStatement(getType(1) + " kv = " + getMapValue("k"));
      writer.emitStatement(put("fieldOffset", 1, "kv"));
      writer.emitStatement("fieldOffset += " + getSizeStmtTypeArg(1, "kv"));
      writer.endControlFlow();
      if (isOptional()) {
        writer.endControlFlow();
      }
    }
  }

  public String put(String offset, int pos, String field) {
    if (typeArgIsEnum(pos)) {
      return putEnum(offset, field);
    } else {
      return writeBuf + ".put" + getCapitalizedBufType(getType(pos)) + "("+ offset + ", " + field + ")";
    }
  }

  public String get(String offset, String type) {
    if (type.equals(ByteString.class.getName())) {
      return readBuf + ".getString("+ offset + ")";
    } else if (SourceTypeUtil.isBasicType(type)) {
      return readBuf + ".get" + getCapitalizedBufType(type) + "("+ offset + ")";
    } else {
      return "new " + getGeneratedType() + "(_buffer, " + offset +")";
    }
  }

  public String getEnum(String offset, String type) {
    return type + ".values()[" + readBuf + ".getInt" + "(" + offset + ")]";
  }

  public String put(String offset, String type, String field) {
    return writeBuf + ".put" + getCapitalizedBufType(type) + "("+ offset + ", " + field + ")";
  }

  public String putEnum(String offset, String fieldName) {
    return writeBuf + ".putInt(" + offset + ", " + fieldName + ".ordinal())";
  }

  public String getSizeStmtTypeArg(int pos, String fieldName) {
    String type = getType(pos);
    return getSizeStmt(type, fieldName);
  }

  public String getSizeStmt() {
    return getSizeStmt(getSimpleType(), getName());
  }

  // only applicable for maps
  public String getType(int pos) {
    String type = getTypeArgString(pos);
    if (type.equals(ByteString.class.getName())) {
      type = ByteString.class.getName();
    }
    return type;
  }

  public String getExternalType() {
    if (isMap()) {
      return "java.util.Map<" + getExternalType(0) + ", " + getExternalType(1) + ">";
    } else if (isList()) {
      return "java.util.List<" + getExternalType(0) + ">";
    } else {
      return getExternalType(getFullTypeString());
    }
  }

  private String getExternalType(int pos) {
    String type = getTypeArgString(pos);
    return getExternalType(type);
  }

  private String getExternalType(String type) {
    if (type.equals(ByteString.class.getName())) {
      type = String.class.getName();
    }
    return type;
  }
  public String getMapValue(String keyField) {
    String getValue = getName() + ".get(" + keyField + ")";
    if (getType(1).equals(ByteString.class.getName())) {
      getValue = getName() + ".getByteString( " + keyField + " )";
    }
    return getValue;
  }

  // only applicable for maps
  public String getMapKeySet() {
    String type = getTypeArgStrings().get(0);
    String keySet = ".keySet()";
    if (type.equals(ByteString.class.getName())) {
      keySet = ".byteStringKeySet()";
    }
    return getName() + keySet;
  }

  // only applicable for maps
  public String getListIterator() {
    String type = getTypeArgStrings().get(0);
    String keySet = "";
    if (type.equals(ByteString.class.getName())) {
      keySet = ".byteStrings()";
    }
    return getName() + keySet;
  }

  public static String getSizeStmt(String type, String field) {
    if (type.equals(byte.class.getSimpleName()) || type.equals(Byte.class.getName())) {
      return "1";
    } else if (type.equals(short.class.getSimpleName()) || type.equals(Short.class.getName())) {
      return "2";
    } else if (type.equals(int.class.getSimpleName()) || type.equals(Integer.class.getName())) {
      return "4";
    } else if (type.equals(long.class.getSimpleName()) || type.equals(Long.class.getName())) {
      return "8";
    } else if (type.equals(float.class.getSimpleName()) || type.equals(Float.class.getName())) {
      return "4";
    } else if (type.equals(double.class.getSimpleName()) || type.equals(Double.class.getName())) {
      return "8";
    } else if (type.equals(char.class.getSimpleName()) || type.equals(Character.class.getName())) {
      return "2";
    } else if (type.equals(boolean.class.getSimpleName()) || type.equals(Boolean.class.getName())) {
      return "1";
    }
    return "Sizes.getSize(" + field + ")";
  }

  private static String getHex(int id) {
    return String.format("0x%08x", 1 << id);
  }

}
