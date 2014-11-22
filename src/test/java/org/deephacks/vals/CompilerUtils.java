package org.deephacks.vals;

import junit.framework.Test;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic.Kind;
import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class CompilerUtils {
  private static File root = computeMavenProjectRoot(CompilerUtils.class);
  private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
  private static final String sourceBaseDir;
  private static final File generatedSources = new File(root, "target/generated-test-sources/annotations");
  private static final File testClasses = new File(root, "target/test-classes");
  private static final File classes = new File(root, "target/classes");
  static {
    try {
      sourceBaseDir = new File(root, "src/test/java").getCanonicalPath();
      testClasses.mkdirs();
      classes.mkdirs();
      generatedSources.mkdirs();
    }
    catch ( IOException e ) {
      throw new RuntimeException( e );
    }
  }

  public static File computeMavenProjectRoot(Class<?> anyTestClass) {
    final String clsUri = anyTestClass.getName().replace('.', '/') + ".class";
    final URL url = anyTestClass.getClassLoader().getResource(clsUri);
    final String clsPath = url.getPath();
    // located in ./target/test-classes or ./eclipse-out/target
    final File target_test_classes = new File(clsPath.substring(0,
            clsPath.length() - clsUri.length()));
    // lookup parent's parent
    return target_test_classes.getParentFile().getParentFile();
  }

  public static void compile(Class<?>... classes) {
    compile(classes, true);
  }

  public static void compileNoClean(Class<?>[] classes) {
    compile(classes, false);
  }

  private static void compile(Class<?>[] classes, boolean clean) {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    File[] sourceFiles = getSourceFiles(classes);
    boolean success = compile(new SourceAnnotationProcessor(), diagnostics, clean, sourceFiles);
    StringBuilder sb = new StringBuilder();

    diagnostics.getDiagnostics().stream()
            .filter(d -> d.getKind() == Kind.ERROR).forEach(d -> sb.append(d.toString()));
    if (!success) {
      throw new IllegalArgumentException(sb.toString());
    }
  }

  public static File[] getSourceFiles(Class<?>... classes) {
    List<File> files = new ArrayList<>();
    for (Class<?> cls : classes) {
      String sourceFileName = File.separator + cls.getName().replace( ".", File.separator ) + ".java";
      files.add(new File(sourceBaseDir + sourceFileName));
    }
    return files.toArray(new File[files.size()]);
  }

  public static boolean compile(Processor p1, DiagnosticCollector<JavaFileObject> diagnostics, boolean clean, File... sourceFiles) {
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

    Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFiles);

    List<String> options = new ArrayList<>();

    try {
      if (clean) {
        cleanGeneratedClasses();
      }
      fileManager.setLocation(StandardLocation.CLASS_PATH,
              Arrays.asList(classes, testClasses, getJarPath(Test.class)));
      fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, Arrays.asList(generatedSources));
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(testClasses));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, options, compilationUnits);
    task.setProcessors( Arrays.asList( p1 ) );

    return task.call();
  }
  private static File getJarPath(Class<?> cls) {
    return new File(cls.getProtectionDomain().getCodeSource().getLocation().getPath());
  }

  public static void cleanGeneratedClasses() throws IOException {
    generatedSources.mkdirs();
    Files.walk(Paths.get(generatedSources.getAbsolutePath()))
            .filter(f -> f.toFile().getName().endsWith("java"))
            .forEach(f -> f.toFile().delete());
  }

  public static class BuilderProxy<T> {
    private Object object;
    private Class<T> cls;

    public BuilderProxy(T object) {
      this.object = object;
      this.cls = cls;
    }

    public <T> T get(String property) {
      String method = "get" + Character.toLowerCase(property.charAt(0)) + (property.length() > 1 ? property.substring(1) : "");
      try {
        return (T) cls.getMethod(method).invoke(object);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public T get() {
      return (T) object;
    }

    public static class Builder<T> {

      private Object builder;
      private Class<T> cls;
      private Class<T> builderClass;
      private AtomicReference<String> propertyName = new AtomicReference<>();
      private T proxy;
      private Object value;
      private String prefix = "with";
      public Builder(Class<T> cls) {
        try {
          this.cls = (Class<T>) cls;
          Class<?> enclosingClass = cls.getEnclosingClass();
          if (enclosingClass != null) {
            String packageName = cls.getPackage().getName();
            String className = cls.getSimpleName();
            this.builderClass = (Class<T>) Class.forName(packageName + "." + className +  "Builder");
          } else {
            this.builderClass = (Class<T>) Class.forName(cls.getCanonicalName() +  "Builder");
          }

          this.builder = builderClass.newInstance();
          this.proxy = createProxy();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      public Builder(Class<T> cls, Package pkg) {
        try {
          this.cls = (Class<T>) cls;
          this.builderClass = (Class<T>) Class.forName(pkg.getName() + "." + cls.getSimpleName() + "Builder");
          this.builder = builderClass.newInstance();
          this.proxy = createProxy();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      public <U> Builder<T> set(Function<T, U> property, U value) {
        try {
          String propertyName = getPropertyName(property, value);
          for (Method m : builderClass.getMethods()) {
            String methodName = m.getName();
            if (methodName.equalsIgnoreCase(prefix + propertyName)) {
              if (value instanceof Optional) {
                Optional optional = (Optional) value;
                m.invoke(builder, optional.get());
              } else {
                m.invoke(builder, value);
              }
              return this;
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        throw new RuntimeException("Could not find property " + propertyName);
      }

      private <R> String getPropertyName(Function<T, R> method, final R value) throws NoSuchMethodException {
        this.value = value;
        method.apply(proxy);
        return propertyName.get();
      }

      private <R> T createProxy() {
        return (T) java.lang.reflect.Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{ cls },
          new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
              String methodName = method.getName();
              if (methodName.startsWith("get")) {
                propertyName.set(Character.toLowerCase(methodName.charAt(3)) + (methodName.length() > 4 ? methodName.substring(4) : ""));
              }
              return value;
            }
          });
      }

      public BuilderProxy<T> build() {
        try {
          return new BuilderProxy(builderClass.getMethod("build").invoke(builder));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }

    private static final Map<String, Class<?>> ALL_PRIMITIVE_TYPES = new HashMap<>();

    static {
      for (Class<?> primitiveNumber : Arrays.asList(byte.class, short.class,
        int.class, long.class, float.class, double.class)) {
        ALL_PRIMITIVE_TYPES.put(primitiveNumber.getName(), primitiveNumber);
      }
      for (Class<?> primitive : Arrays.asList(char.class, boolean.class)) {
        ALL_PRIMITIVE_TYPES.put(primitive.getName(), primitive);
      }
    }

    public static boolean isPrimitive(Class<?> type) {
      return ALL_PRIMITIVE_TYPES.containsKey(type.getName());
    }
  }

}