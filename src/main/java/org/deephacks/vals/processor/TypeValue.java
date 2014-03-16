package org.deephacks.vals.processor;

import org.deephacks.vals.processor.AnnotationProcessor.CompileException;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

final class TypeValue {
  private static final List<String> KEYWORDS = Arrays.asList(
          "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
          "default", "do", "double", "else", "extends", "final", "finally", "float", "for", "goto", "if",
          "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package",
          "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
          "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
          "true", "false", "null");

  private ProcessingEnvironment processingEnv;
  private String builderClassName;
  private String subClassName;
  private String packageName;
  private String className;
  private SortedSet<PropertyValue> properties = new TreeSet<>();
  private boolean hasToString = false;
  private boolean hasHashCode = false;
  private boolean hasEquals = false;
  private boolean hasPostConstruct = false;
  private String builderMethodsPrefix = "";
  private CompileException compileException = new CompileException();
  private String subClassPrefix;

  public TypeValue(ProcessingEnvironment processingEnv, TypeElement type, String subClassPrefix, String builderMethodsPrefix) throws CompileException {
    Types typeUtils = processingEnv.getTypeUtils();
    this.subClassPrefix = subClassPrefix;
    this.processingEnv = processingEnv;
    this.className = classNameOf(type);
    this.builderClassName = generatedBuilderClassname(type);
    this.subClassName = generatedSubclassName(type);
    this.packageName = packageNameOf(type);
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
    methods = findGetters(methods);
    for (ExecutableElement m : methods) {
      TypeElement returnType = (TypeElement) typeUtils.asElement(m.getReturnType());
      TypeMirror returnTypeMirror = m.getReturnType();
      boolean isNullable = false;
      // primitive types does not have return type?
      if (returnType != null || returnTypeMirror != null) {
        isNullable = m.getAnnotation(Nullable.class) != null;
      }

      if (isNullable && returnTypeMirror.getKind().isPrimitive()) {
        compileException.add("Primitive fields cannot be nullable", type);
      }

      PropertyValue prop = new PropertyValue(m.getSimpleName().toString(), m.isDefault(), isNullable,
              m.getReturnType().getKind(), m.getReturnType().toString(), builderMethodsPrefix);
      if (KEYWORDS.contains(prop.getName())) {
        compileException.add("Properties as Java keyword is not allowed.", m);
      }
      properties.add(prop);
    }
    reportCompileException();
  }


  public List<PropertyValue> getProperties() {
    return new ArrayList<>(properties);
  }

  public String getBuilderClassName() {
    return builderClassName;
  }

  public String getSubClassName() {
    return subClassName;
  }

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

  static String packageNameOf(TypeElement type) {
    while (true) {
      Element enclosing = type.getEnclosingElement();
      if (enclosing instanceof PackageElement) {
        return ((PackageElement) enclosing).getQualifiedName().toString();
      }
      type = (TypeElement) enclosing;
    }
  }

  private String generatedClassName(TypeElement type, String prefix) {
    String name = type.getSimpleName().toString();
    while (type.getEnclosingElement() instanceof TypeElement) {
      type = (TypeElement) type.getEnclosingElement();
      name = type.getSimpleName() + "_" + name;
    }
    String pkg = packageNameOf(type);
    String dot = pkg.isEmpty() ? "" : ".";
    return pkg + dot + prefix + name;
  }

  private String generatedBuilderClassName(TypeElement type, String suffix) {
    String name = type.getSimpleName().toString();
    String pkg = packageNameOf(type);
    String dot = pkg.isEmpty() ? "" : ".";
    return pkg + dot + name + suffix;
  }


  private String generatedSubclassName(TypeElement type) {
    return generatedClassName(type, subClassPrefix);
  }

  private String generatedBuilderClassname(TypeElement type) {
    return generatedBuilderClassName(type, "Builder");
  }

  private static String classNameOf(TypeElement type) {
    return type.getQualifiedName().toString();
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
            compileException.add("Class can only have primitive array properties", method);
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

  public static class PropertyValue implements Comparable<PropertyValue> {
    private boolean isDefault;
    private boolean isNullable = false;
    private String name;
    private String getMethod;
    private TypeKind typeKind;
    private String objectType;
    private String builderMethodsPrefix;

    public PropertyValue(String name, boolean isDefault, boolean isNullable, TypeKind typeKind, String objectType, String builderMethodsPrefix) {
      this.getMethod = name;
      name = name.substring(3, name.length());
      this.name = Character.toLowerCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
      this.isDefault = isDefault;
      this.typeKind = typeKind;
      this.objectType = objectType;
      this.isNullable = isNullable;
      this.builderMethodsPrefix = builderMethodsPrefix;
    }

    public boolean isDefault() {
      return isDefault;
    }

    public String getGetMethod() {
      return getMethod;
    }

    public String getName() {
      return name;
    }

    public String getNameCapital() {
      return Character.toUpperCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
    }

    public boolean isNullable() {
      return isNullable;
    }

    public boolean isArray() {
      return typeKind == TypeKind.ARRAY;
    }

    public boolean isPrimitive() {
      return typeKind.isPrimitive();
    }

    public String getBuilderMethodsPrefix() {
      return builderMethodsPrefix;
    }

    public boolean hasBuilderMethodsPrefix() {
      return builderMethodsPrefix != null && !"".equals(builderMethodsPrefix);
    }

    public String getTypeString() {
      switch (typeKind) {
        case BYTE:
        case SHORT:
        case CHAR:
        case INT:
        case LONG:
        case BOOLEAN:
        case FLOAT:
        case DOUBLE:
          return typeKind.toString().toLowerCase();
        default:
          return objectType;
      }
    }

    public String generateEquals() {
      switch (typeKind) {
        case BYTE:
        case SHORT:
        case CHAR:
        case INT:
        case LONG:
        case BOOLEAN:
          return "this." + getGetMethod() + "() != that." + getGetMethod() + "()";
        case FLOAT:
          return "Float.floatToIntBits(this." + getGetMethod() + "()) != Float.floatToIntBits(that." + getGetMethod() +"())";
        case DOUBLE:
          return "Double.doubleToLongBits(this." + getGetMethod() + "()) != Double.doubleToLongBits(that." + getGetMethod() + "())";
        case ARRAY:
          return "!Arrays.equals(this."+getGetMethod()+ "(), that." + getGetMethod() + "())";
        default:
          if (isNullable()) {
            return "!(this." + getGetMethod() + "() == null ? that." + getGetMethod() + "() == null : this." +getGetMethod() +"().equals(that." + getGetMethod() +"()))";
          } else {
            return "!(this." + getGetMethod() +"().equals(that."+getGetMethod()+"()))";
          }
      }
    }

    public String generateHashCode() {
      switch (typeKind) {
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
          return "Arrays.hashCode(" + getGetMethod() + "())";
        default:
          if (isNullable()) {
            return "(" + getGetMethod() + "() == null) ? 0 : " + getGetMethod() + "().hashCode()";
          } else {
            return getGetMethod() + "().hashCode()";
          }
      }
    }

    @Override
    public int compareTo(PropertyValue o) {
      return this.getGetMethod().compareTo(o.getGetMethod());
    }

  }
}
