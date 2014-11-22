package org.deephacks.vals;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.*;

import static org.deephacks.vals.SourceAnnotationProcessor.CompileException;

abstract class SourceClassType {
  static final List<String> KEYWORDS = Arrays.asList(
          "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
          "default", "do", "double", "else", "extends", "final", "finally", "float", "for", "goto", "if",
          "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package",
          "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
          "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
          "true", "false", "null");

  private ProcessingEnvironment processingEnv;
  private String packageName;
  private String className;
  private LinkedHashMap<String, SourceField> fields = new LinkedHashMap<>();
  private boolean hasToString = false;
  private boolean hasHashCode = false;
  private boolean hasEquals = false;
  private boolean hasPostConstruct = false;
  private String builderMethodsPrefix = "";
  private CompileException compileException = new CompileException();

  public SourceClassType(ProcessingEnvironment processingEnv, TypeElement type, String builderMethodsPrefix) throws CompileException {
    Types typeUtils = processingEnv.getTypeUtils();
    this.processingEnv = processingEnv;
    this.className = SourceTypeUtil.classNameOf(type);
    this.packageName = SourceTypeUtil.packageNameOf(type);
    this.builderMethodsPrefix = builderMethodsPrefix;
    /*
    For some reason type.getModifiers() always seem to be Modifier.STATIC even though type is not?!
    if (type.getNestingKind() == NestingKind.MEMBER && !type.getModifiers().contains(Modifier.STATIC)) {
      compileException.add("Nested interfaces must be static", type);
    }
    */

    List<ExecutableElement> methods = new ArrayList<>();
    findMethods(type, methods);
    for (ExecutableElement m : methods) {
      if (isToString(m)) {
        hasToString = true;
      } else if (isHashCode(m)) {
        hasHashCode = true;
      } else if (isEquals(m)) {
        hasEquals = true;
      } else if (isPostConstruct(m)) {
        hasPostConstruct = true;
      }
    }
    Set<Integer> ids = new HashSet<>();

    methods = findGetters(methods);
    for (ExecutableElement m : methods) {
      TypeElement returnType = (TypeElement) typeUtils.asElement(m.getReturnType());
      TypeMirror returnTypeMirror = m.getReturnType();

      SourceTypeInfo typeInfo = new SourceTypeInfo(returnTypeMirror, returnType, typeUtils);
      String fieldName = m.getSimpleName().toString();
      if (KEYWORDS.contains(fieldName.toLowerCase())) {
        compileException.add("Field names with Java keyword are not allowed.", m);
      }
      Id id = m.getAnnotation(Id.class);
      if (id == null) {
        continue;
      }
      if (!ids.add(id.value())) {
        CompileException exception = new CompileException();
        exception.add("Id " + id.value() +  " occurs twice", type);
        throw exception;
      }

      SourceField field;
      if (typeInfo.isEmbedded()) {
        field = new SourceField.EmbeddedField(fieldName, id.value(), typeInfo);
      } else {
        field = new SourceField.ValueField(fieldName, id.value(), m.isDefault(), typeInfo, null);
      }
      SourceField f;
      if (typeInfo.isArray()) {
        f = new SourceField.ArrayField(field);
      } else if (typeInfo.isList()) {
        f = new SourceField.ListField(field);
      } else if (typeInfo.isEnumSet()) {
        f = new SourceField.EnumSetField(field);
      } else if (typeInfo.isMap()) {
        f = new SourceField.MapField(field);
      } else {
        f = new SourceField.SingleField(field);
      }
      if (!getFields().contains(f)) {
        fields.put(f.getName(), f);
      }
    }
    reportCompileException();
  }

  public int getMaxId() {
    int max = 0;
    for (SourceField f : fields.values()) {
      if (f.getId() > max) {
        max = f.getId();
      }
    }
    return max;
  }

  private boolean fieldsContains(SourceField field) {
    if (fields.keySet().contains(field.getName())) {
      return true;
    }
    return false;
  }


  public String getBuilderMethodsPrefix() {
    return builderMethodsPrefix;
  }

  public List<SourceField> getFields() {
    return new ArrayList<>(fields.values());
  }

  public boolean hasArrayField() {
    for (SourceField property : fields.values()) {
      if (property instanceof SourceField.ArrayField) {
        return true;
      }
    }
    return false;
  }

  public boolean hasListField() {
    for (SourceField property : fields.values()) {
      if (property instanceof SourceField.ListField) {
        return true;
      }
    }
    return false;
  }

  public boolean hasMapField() {
    for (SourceField property : fields.values()) {
      if (property instanceof SourceField.ListField) {
        return true;
      }
    }
    return false;
  }

  public List<String> getAllFieldsAsStrings() {
    List<String> propertyStrings = new ArrayList<>();
    for (SourceField field : getFields()) {
      if (field.isOptional()) {
        propertyStrings.add("java.util.Optional<" + field.getFullTypeString() + ">");
      } else {
        propertyStrings.add(field.getFullTypeString());
      }

      propertyStrings.add(field.getName());
    }
    return propertyStrings;
  }

  public abstract String getGeneratedType();

  public String getClassName() {
    return className;
  }

  public String getSimpleClassName() {
    if (className.contains(".")) {
      return className.substring(className.lastIndexOf('.') + 1);
    } else {
      return className;
    }
  }

  public String getPackageName() {
    return packageName;
  }

  public boolean hasToString() {
    return hasToString;
  }

  public boolean hasHashCode() {
    return hasHashCode;
  }

  public boolean hasEquals() {
    return hasEquals;
  }

  public boolean hasPostConstruct() {
    return hasPostConstruct;
  }


  private void findMethods(TypeElement type, List<ExecutableElement> methods) {
    Types typeUtils = processingEnv.getTypeUtils();
    for (TypeMirror superInterface : type.getInterfaces()) {
      findMethods((TypeElement) typeUtils.asElement(superInterface), methods);
    }
    List<ExecutableElement> theseMethods = ElementFilter.methodsIn(type.getEnclosedElements());
    for (ExecutableElement method : theseMethods) {
        methods.add(method);
    }
  }

  private List<ExecutableElement> findGetters(List<ExecutableElement> methods) {
    List<ExecutableElement> toImplement = new ArrayList<>();
    for (ExecutableElement method : methods) {
      if (method.getSimpleName().toString().startsWith("get") && !isToStringOrEqualsOrHashCode(method)) {
        if (method.getParameters().isEmpty() && method.getReturnType().getKind() != TypeKind.VOID) {
          if (isReferenceArrayType(method.getReturnType())) {
            compileException.add("Class can only have primitive array fields", method);
          }
          toImplement.add(method);
        }
      }
    }
    return toImplement;
  }

  private static boolean isReferenceArrayType(TypeMirror type) {
    return type.getKind() == TypeKind.ARRAY
            && !((ArrayType) type).getComponentType().getKind().isPrimitive();
  }

  private boolean isToStringOrEqualsOrHashCode(ExecutableElement method) {
    return isToString(method) || isEquals(method) || isHashCode(method);
  }

  private boolean isToString(ExecutableElement method) {
    String name = method.getSimpleName().toString();
    boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
    return (isStatic && name.equals("toString") && method.getParameters().size() == 1
            && method.getParameters().get(0).asType().toString().equals(className));
  }

  private boolean isHashCode(ExecutableElement method) {
    String name = method.getSimpleName().toString();
    boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
    return (isStatic && name.equals("hashCode") && method.getParameters().size() == 1
            && method.getParameters().get(0).asType().toString().equals(className));
  }

  private boolean isEquals(ExecutableElement method) {
    String name = method.getSimpleName().toString();
    boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
    return (isStatic && name.equals("equals") && method.getParameters().size() == 2
            && method.getParameters().get(0).asType().toString().equals(className)
            && method.getParameters().get(1).asType().toString().equals(className));
  }

  private boolean isPostConstruct(ExecutableElement method) {
    String name = method.getSimpleName().toString();
    boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
    return (isStatic && name.equals("postConstruct") && method.getParameters().size() == 1
            && method.getParameters().get(0).asType().toString().equals(className));
  }

  public void reportCompileException() throws CompileException {
    if (!compileException.errors.isEmpty()) {
      throw compileException;
    }
  }


  public static class EntityType extends SourceClassType {

    public EntityType(ProcessingEnvironment processingEnv, TypeElement type, String builderMethodsPrefix) throws CompileException {
      super(processingEnv, type, builderMethodsPrefix);
    }

    @Override
    public String getGeneratedType() {
      return getPackageName() + ".Val_" + getSimpleClassName();
    }
  }
}

