/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.deephacks.vals.processor;

import javax.annotation.Nullable;
import javax.annotation.processing.Processor;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompilerUtils {

	private final JavaCompiler compiler;

	private final String sourceBaseDir;
  private final File generatedSources = new File("target/generated-test-sources/annotations");
  private final File testClasses = new File("target/test-classes");
  private final File classes = new File("target/classes");



  public CompilerUtils() {
    this(ToolProvider.getSystemJavaCompiler());
  }

	public CompilerUtils(JavaCompiler compiler) {
		this.compiler = compiler;
		String basePath;
		try {
			basePath = new File( "." ).getCanonicalPath();
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}

		this.sourceBaseDir = basePath + "/src/test/java";
	}

  public void compile(Class<?>... classes) {
    compile(classes, true);
  }

  public void compileNoClean(Class<?>[] classes) {
    compile(classes, false);
  }

  private void compile(Class<?>[] classes, boolean clean) {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    File[] sourceFiles = getSourceFiles(classes);
    boolean success = compile(new AnnotationProcessor(), diagnostics, clean, sourceFiles);
    StringBuilder sb = new StringBuilder();

    diagnostics.getDiagnostics().stream()
            .filter(d -> d.getKind() == Kind.ERROR).forEach(d -> sb.append(d.toString()));
    if (!success) {
      throw new IllegalArgumentException(sb.toString());
    }
  }

  public File[] getSourceFiles(Class<?>... classes) {
    List<File> files = new ArrayList<>();
    for (Class<?> cls : classes) {
      String sourceFileName = File.separator + cls.getName().replace( ".", File.separator ) + ".java";
      files.add(new File(sourceBaseDir + sourceFileName));
    }
    return files.toArray(new File[files.size()]);
  }

	public boolean compile(Processor annotationProcessor, DiagnosticCollector<JavaFileObject> diagnostics, boolean clean, File... sourceFiles) {
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFiles);

		List<String> options = new ArrayList<>();

		try {
      if (clean) {
        cleanGeneratedClasses();
      }
      fileManager.setLocation(StandardLocation.CLASS_PATH, Arrays.asList(classes, testClasses, getJarPath(Nullable.class)));
      fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, Arrays.asList(generatedSources));
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(testClasses));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, options, compilationUnits);
		task.setProcessors( Arrays.asList( annotationProcessor ) );

		return task.call();
	}
  private static File getJarPath(Class<?> cls) {
    return new File(cls.getProtectionDomain().getCodeSource().getLocation().getPath());
  }

  public void cleanGeneratedClasses() throws IOException {
    generatedSources.mkdirs();
    Files.walk(Paths.get(generatedSources.getAbsolutePath()))
            .filter(f -> f.toFile().getName().endsWith("java"))
            .forEach(f -> f.toFile().delete());
  }

}