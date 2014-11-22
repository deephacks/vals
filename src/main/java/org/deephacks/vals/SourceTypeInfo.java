package org.deephacks.vals;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

class SourceTypeInfo {
  private final TypeMirror typeMirror;
  /**
   * primitive values does not have a declared type
   */
  private DeclaredType declaredType;
  private List<TypeMirror> typeArgs = new ArrayList<>();
  private List<TypeElement> typeArgElements = new ArrayList<>();
  private List<String> typeArgStrings = new ArrayList<>();
  private String fullTypeString;
  private String typeString;
  private String packageName;
  private String simpleClassName;
  private Types typeUtils;
  private boolean isOptional = false;
  private boolean isEnum = false;

  public SourceTypeInfo(TypeMirror typeMirror, TypeElement typeElement, Types typeUtils) {
    this.typeMirror = typeMirror;
    this.typeUtils = typeUtils;
    if (typeElement != null) {
      if ("java.util.Optional".equals(typeElement.toString())) {
        isOptional = true;
      }
      if (typeElement.getKind() == ElementKind.ENUM) {
        this.isEnum = true;
      }
    }
    List<TypeMirror> args = getTypeArgs(typeMirror);
    for (TypeMirror t : args) {
      if (isOptional) {
        typeMirror = args.get(0);
        typeElement = (TypeElement) typeUtils.asElement(typeMirror);
        typeArgElements.add(typeElement);
        List<TypeMirror> types = getTypeArgs(t);
        for (TypeMirror t2 : types) {
          addTypeArgs(t2);
        }
      } else {
        if (isArray()) {
          ArrayType arrayType = typeUtils.getArrayType(t);
          typeElement = typeUtils.boxedClass((PrimitiveType) arrayType.getComponentType());
        } else {
          typeElement = (TypeElement) typeUtils.asElement(t);
        }
        typeArgElements.add(typeElement);
        addTypeArgs(t);
      }
    }
    this.fullTypeString = typeMirror.toString();
    if (this.fullTypeString.equals("java.lang.String")) {
      this.fullTypeString = ByteString.class.getName();
      this.typeString = ByteString.class.getName();
    }
    if (!isPrimitive() && !isArray()) {
      this.typeString = rawTypeToString(declaredType, '.');
      if (isByteStringList()) {
        this.typeString = ByteStringArrayList.class.getName();
        this.fullTypeString = ByteStringArrayList.class.getName();
      }
      if (isMap()) {
        this.typeString = ByteStringHashMap.class.getName();
        this.fullTypeString = ByteStringHashMap.class.getName() + "<" + getTypeArgs().get(0)+ "," + getTypeArgs().get(1)+ ">";
      }
      if (this.fullTypeString.endsWith(ByteString.class.getName())) {
        this.packageName = ByteString.class.getPackage().getName();
        this.simpleClassName = ByteString.class.getSimpleName();
        this.typeString = ByteString.class.getName();
      } else {
        this.packageName = SourceTypeUtil.packageNameOf(typeElement);
        this.simpleClassName = SourceTypeUtil.simpleClassNameOf(typeElement);
      }
    }
  }

  public boolean isByteStringList() {
    return isList() && getTypeArgStrings().get(0).equals(ByteString.class.getName());
  }

  public String getGeneratedType() {
    String packageName = getPackageName();
    String className = getSimpleClassName();
    if (typeArgs.size() == 1) {
      TypeElement type = (TypeElement) typeUtils.asElement(typeArgs.get(0));
      packageName = SourceTypeUtil.packageNameOf(type);
      className = SourceTypeUtil.simpleClassNameOf(type);
    } else if (typeArgs.size() == 2) {
      TypeElement type = (TypeElement) typeUtils.asElement(typeArgs.get(1));
      packageName = SourceTypeUtil.packageNameOf(type);
      className = SourceTypeUtil.simpleClassNameOf(type);
    }

    if (isEmbedded()) {
      return packageName + ".Val_" + className;
    } else {
      throw new IllegalArgumentException("Not a type: " + getFullTypeString());
    }
  }

  public boolean isOptional() {
    return isOptional;
  }

  public boolean isList() {
    return !(isPrimitive() || isArray()) &&
      (typeString.equals("java.util.List") || typeString.equals("java.util.Collection")
        || typeString.equals(ByteStringArrayList.class.getName()));
  }

  public boolean isEnum() {
    return isEnum;
  }

  public boolean isEnumSet() {
    return !(isPrimitive() || isArray()) && (typeString.equals("java.util.EnumSet"));
  }

  public boolean isMap() {
    return !(isPrimitive() || isArray()) && (typeString.equals(Map.class.getName())
      || typeString.equals(ByteStringHashMap.class.getName()));
  }

  public TypeKind getTypeKind() {
    return typeMirror.getKind();
  }

  private void addTypeArgs(TypeMirror typeArg) {
    this.typeArgs.add(typeArg);
    if (typeArg.toString().equals("java.lang.String")) {
      this.typeArgStrings.add(ByteString.class.getName());
    } else {
      this.typeArgStrings.add(typeArg.toString());
    }
  }

  private List<TypeMirror> getTypeArgs() {
    return typeArgs;
  }

  public List<String> getTypeArgStrings() {
    return typeArgStrings;
  }

  public boolean isEmbedded() {
    if (declaredType != null && declaredType.asElement().getAnnotation(Val.class) != null) {
      return true;
    }
    final AtomicReference<Boolean> ref = new AtomicReference<>(false);
    for (TypeMirror type : getTypeArgs()) {
      type.accept(new SimpleTypeVisitor8<Void, Void>() {
        @Override
        public Void visitDeclared(DeclaredType declaredType, Void v) {
          ref.set(declaredType.asElement().getAnnotation(Val.class) != null);
          return null;
        }
      }, null);
    }
    return ref.get();
  }

  public boolean isEmbeddedTypeArg(int i) {
    final AtomicReference<Boolean> ref = new AtomicReference<>(false);
    TypeMirror type = getTypeArgs().get(i);
    type.accept(new SimpleTypeVisitor8<Void, Void>() {
      @Override
      public Void visitDeclared(DeclaredType declaredType, Void v) {
        ref.set(declaredType.asElement().getAnnotation(Val.class) != null);
        return null;
      }
    }, null);
    return ref.get();
  }

  public String getFullTypeString() {
    return fullTypeString;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getSimpleClassName() {
    return simpleClassName;
  }


  public boolean isPrimitive() {
    return typeMirror.getKind().isPrimitive();
  }

  public boolean isArray() {
    return typeMirror.getKind() == TypeKind.ARRAY;
  }

  private List<TypeMirror> getTypeArgs(TypeMirror typeMirror) {
    if (typeMirror.getKind().isPrimitive()) {
      return new ArrayList<>();
    }
    List<TypeMirror> typeArgs = new ArrayList<>();
    typeMirror.accept(new SimpleTypeVisitor8<Void, Void>() {
      @Override
      public Void visitDeclared(DeclaredType declaredType, Void v) {
        SourceTypeInfo.this.declaredType = declaredType;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        for (int i = 0; i < typeArguments.size(); i++) {
          typeArgs.add(typeArguments.get(i));
        }
        return null;
      }

      @Override
      public Void visitArray(ArrayType arrayType, Void v) {
        TypeMirror type = arrayType.getComponentType();
        if (type instanceof PrimitiveType) {
          typeArgs.add(type); // Don't box, since this is an array.
        }
        return null;
      }

      @Override
      public Void visitError(ErrorType errorType, Void v) {
        // Error type found, a type may not yet have been generated, but we need the type
        // so we can generate the correct code in anticipation of the type being available
        // to the compiler.

        // Paramterized types which don't exist are returned as an error type whose name is "<any>"
        if ("<any>".equals(errorType.toString())) {
          throw new IllegalStateException("Type reported as <any> is likely a not-yet generated parameterized type.");
        }
        // TODO(cgruber): Figure out a strategy for non-FQCN cases.
        addTypeArgs(errorType);
        return null;
      }

      @Override
      protected Void defaultAction(TypeMirror typeMirror, Void v) {
        throw new UnsupportedOperationException(
                "Unexpected TypeKind " + typeMirror.getKind() + " for " + typeMirror);
      }
    }, null);
    return typeArgs;
  }

  private PackageElement getPackage(Element type) {
    while (type.getKind() != ElementKind.PACKAGE) {
      type = type.getEnclosingElement();
    }
    return (PackageElement) type;
  }

  private String rawTypeToString(TypeMirror type, char innerClassSeparator) {
    if (!(type instanceof DeclaredType)) {
      throw new IllegalArgumentException("Unexpected type: " + type + " " + typeMirror);
    }
    StringBuilder result = new StringBuilder();
    DeclaredType declaredType = (DeclaredType) type;
    rawTypeToString(result, (TypeElement) declaredType.asElement(), innerClassSeparator);
    return result.toString();
  }


  private void rawTypeToString(StringBuilder result, TypeElement type,
                               char innerClassSeparator) {
    String packageName = getPackage(type).getQualifiedName().toString();
    String qualifiedName = type.getQualifiedName().toString();
    if (packageName.isEmpty()) {
      result.append(qualifiedName.replace('.', innerClassSeparator));
    } else {
      result.append(packageName);
      result.append('.');
      result.append(
              qualifiedName.substring(packageName.length() + 1).replace('.', innerClassSeparator));
    }
  }

  public boolean typeArgIsEnum(int pos) {
    if (typeArgElements == null || typeArgElements.isEmpty()) {
      return false;
    }
    return typeArgElements.get(pos).getKind() == ElementKind.ENUM;
  }
}
