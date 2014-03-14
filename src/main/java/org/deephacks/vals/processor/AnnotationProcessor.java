package org.deephacks.vals.processor;

import org.deephacks.vals.FinalValue;
import org.deephacks.vals.VirtualValue;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@SupportedOptions({ "debug", "verify" })
public class AnnotationProcessor extends AbstractProcessor {

 public AnnotationProcessor() {}

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return new HashSet<>(Arrays.asList(VirtualValue.class.getName(), FinalValue.class.getName()));
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  private void abortWithError(String msg, Element e) throws CompileException {
    reportError(msg, e);
    throw new CompileException();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    boolean claimed = false;
    for (TypeElement type : annotations) {
      Class<? extends Annotation> annotation = asAnnotation(type);
      claimed = annotation != null &&
              (VirtualValue.class.isAssignableFrom(annotation) || FinalValue.class.isAssignableFrom(annotation));
      if (claimed) {
        process(roundEnv, annotation);
      }
    }
    return claimed;
  }

  private void process(RoundEnvironment roundEnv, Class<? extends Annotation> annotation) {
    Collection<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
    Collection<? extends TypeElement> types = ElementFilter.typesIn(annotatedElements);
    for (TypeElement type : types) {
      try {
        processType(type, annotation);
      } catch (CompileException e) {
        reportError(e);
      } catch (RuntimeException e) {
        e.printStackTrace();
        reportError("@" + annotation.getName() + "  processor threw an exception: " + e, type);
      }
    }
  }

  private void processType(TypeElement type, Class<? extends Annotation> annotation) throws CompileException {
    if (type.getKind() != ElementKind.INTERFACE) {
      abortWithError("@" + annotation.getName() + " can only be applied to interfaces", type);
    }
    if (ancestorIs(type)) {
      abortWithError("One @" + annotation.getName() + " class may not extend another", type);
    }

    TypeValue source;
    SourceGenerator subClassGenerator;
    SourceGenerator builderGenerator;
    if (annotation.isAssignableFrom(VirtualValue.class)) {
      source = new TypeValue(processingEnv, type, "VirtualValue_");
      subClassGenerator = new VirtualValueSource(source);
      builderGenerator = new VirtualValueSource(source);
    } else {
      source = new TypeValue(processingEnv, type, "FinalValue_");
      subClassGenerator = new FinalValueSource(source);
      builderGenerator = new FinalValueSource(source);
    }
    String sourceText = subClassGenerator.generateSubClassSource();
    writeSourceFile(source.getSubClassName(), sourceText, type);
    sourceText = builderGenerator.generateBuilderSource();
    writeSourceFile(source.getBuilderClassName(), sourceText, type);
  }

  @SuppressWarnings("unchecked")
  private Class<? extends Annotation> asAnnotation(TypeElement typeElement) {
    try {
      return (Class<? extends Annotation>) Class.forName(typeElement.getQualifiedName().toString());
    } catch (ClassNotFoundException e) {
      reportError("Type element was not found.", typeElement);
      return null;
    }
  }

  private boolean ancestorIs(TypeElement type) {
    while (true) {
      TypeMirror parentMirror = type.getSuperclass();
      if (parentMirror.getKind() == TypeKind.NONE) {
        return false;
      }
      Types typeUtils = processingEnv.getTypeUtils();
      TypeElement parentElement = (TypeElement) typeUtils.asElement(parentMirror);
      if (parentElement.getAnnotation(VirtualValue.class) != null && parentElement.getAnnotation(FinalValue.class) != null) {
        return true;
      }
      type = parentElement;
    }
  }

  private void writeSourceFile(String className, String text, TypeElement originatingType) {
    try {
      JavaFileObject sourceFile =
              processingEnv.getFiler().createSourceFile(className, originatingType);
      try (Writer writer = sourceFile.openWriter()) {
        writer.write(text);
      }
    } catch (IOException e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
              "Could not write generated class " + className + ": " + e);
    }
  }

  @SuppressWarnings("serial")
  static class CompileException extends Exception {
    Map<String, Element> errors = new HashMap<>();
    public CompileException() { }

    public void add(String msg, Element element) { errors.put(msg, element); }

    public Map<String,Element> getErrors() {
      return errors;
    }
  }

  public void reportError(CompileException ex) {
    for (Entry<String, Element> entry : ex.getErrors().entrySet()) {
      reportError(entry.getKey(), entry.getValue());
    }
  }

  public void reportError(String msg, Element element) {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element);
  }

}