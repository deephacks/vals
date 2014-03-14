package org.deephacks.vals.processor;

public class BaseTest {
  private CompilerUtils compiler = new CompilerUtils();

  public void compile(Class<?>... classes) {
    compiler.compile(classes);
  }

  public void compileNoClean(Class<?>... classes) {
    compiler.compileNoClean(classes);
  }
}
