package org.deephacks.vals;

import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.StringWriter;
import java.util.EnumSet;

abstract class SourceGenerator {
  protected StringWriter out;
  protected JavaWriter writer;
  protected SourceClassType type;
  protected String className;

  static final EnumSet<Modifier> PACKAGE_PRIVATE = EnumSet.noneOf(Modifier.class);
  static final EnumSet<Modifier> PUBLIC = EnumSet.of(Modifier.PUBLIC);
  static final EnumSet<Modifier> PUBLIC_STATIC = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC);
  static final EnumSet<Modifier> PRIVATE = EnumSet.of(Modifier.PRIVATE);
  static final EnumSet<Modifier> PRIVATE_STATIC = EnumSet.of(Modifier.PRIVATE, Modifier.STATIC);

  public SourceGenerator(SourceClassType type, String className) {
    this.type = type;
    this.out = new StringWriter();
    this.writer = new JavaWriter(out);
    this.className = className;
  }

  public SourceGenerator(SourceClassType type) {
    this(type, type.getGeneratedType());
  }

  public abstract String writeSource() throws IOException;

  public String getClassName() {
    return className;
  }
}
