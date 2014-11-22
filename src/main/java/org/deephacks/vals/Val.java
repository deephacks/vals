package org.deephacks.vals;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface Val {
  String name() default "";
  String builderPrefix() default "with";
}
