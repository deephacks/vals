### vals - immutable interfaces
[![Build Status](https://travis-ci.org/deephacks/vals.png?branch=master)](https://travis-ci.org/deephacks/vals)

The purpose of vals is to provide a productive way of creating extendable immutable value objects with automatically generated builders from standard Java interfaces. No runtime dependencies required.

### How to use @FinalValue

Create an interface and annotate it with @FinalValue. All non-void, parameterless, getter methods on this interface will be treated as properties, each having same type as the return type of the method. 

Two classes will automatically be generated at compile time.

* A class named FinalValue_[name].java that implement the @FinalValue interface.
* A builder class named [name]Builder.java that construct @FinalValue interface objects using the Builder pattern.

Notice the following conventions.

* The implementation is immutable and implements toString, equals and hashCode based on defined properties.
* Default hashCode, equals and toString method can be vetoed by the @FinalValue interface using naming conventions.
* All values are checked for null when constructed/built unless the method is @javax.annotation.Nullable.
* Properties can define default values by returning them from the method on the interface.
* @FinalValue interfaces can extend any interface as long as it provide a default implementation.
* A post construction hook (called inside the constructor) can be defined by @FinalValue interfaces that need to validate/constrain properties further.
* Even tough instances are immutable each builder is equipped with a method that construct a builder copy from an existing instance with same values. This makes it easy to update values without violating immutability.

### pom.xml

Notice that that these dependencies only require the 'provided' scope which means they will not be packaged with the application.

```xml
<dependency>
  <groupId>org.deephacks.vals</groupId>
  <artifactId>vals</artifactId>
  <version>0.5.6</version>
  <scope>provided</scope>
</dependency>
<dependency>
  <groupId>com.sun.codemodel</groupId>
  <artifactId>codemodel</artifactId>
  <version>2.6</version>
  <scope>provided</scope>
</dependency>
```
This plugin may be required for intellij to recognize generated source files.

```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>build-helper-maven-plugin</artifactId>
  <executions>
    <execution>
      <id>add-source</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>add-source</goal>
      </goals>
      <configuration>
        <sources>
          <source>${basedir}/target/generated-sources/annotations</source>
        </sources>
      </configuration>
    </execution>
  </executions>
</plugin>
```



### @FinalValue example

```java
@FinalValue
public interface Example {

  String getString();
  Byte getByteObject();
  Short getShortObject();
  Integer getIntegerObject();
  Long getLongObject();
  Float getFloatObject();
  Double getDoubleObject();
  Character getCharObject();
  Boolean getBooleanObject();

  byte getBytePrim();
  byte[] getBytePrimArray();
  short getShortPrim();
  short[] getShortPrimArray();
  int getIntPrim();
  int[] getIntPrimArray();
  long getLongPrim();
  long[] getLongPrimArray();
  float getFloatPrim();
  float[] getFloatPrimArray();
  double getDoublePrim();
  double[] getDoublePrimArray();
  char getCharPrim();
  char[] getCharPrimArray();
  boolean getBooleanPrim();
  boolean[] getBooleanPrimArray();

  TimeUnit getAnEnum();

  InnerValue getInnerValue();
  List<InnerValue> getInnerValueList();
  Map<String, InnerValue> getInnerValueMap();
  Set<InnerValue> getInnerValueSet();

  @FinalValue
  public static interface InnerValue {
    public String getValue();
    public int getInteger();
  }
}
```

#### A builder class is generated automatically at compile time.

```java
InnerValue inner = new InnerValueBuilder().withInteger(1).withValue("value").build();

Example example = new ExampleBuilder()
        .withAnEnum(TimeUnit.DAYS)
        .withBooleanObject(true)
        .withBooleanPrim(true)
        .withBooleanPrimArray(new boolean[]{true})
        .withByteObject((byte) 1)
        .withBytePrim((byte) 1)
        .withBytePrimArray(new byte[]{1})
        .withCharObject('a')
        .withCharPrim('a')
        .withCharPrimArray(new char[]{'a'})
        .withDoubleObject(Double.MAX_VALUE)
        .withDoublePrim(123456890.1234567890)
        .withDoublePrimArray(new double[]{Double.MIN_VALUE})
        .withFloatObject(1.0f)
        .withFloatPrim(0.123456789f)
        .withFloatPrimArray(new float[]{Float.MIN_VALUE})
        .withInnerValue(inner)
        .withInnerValueList(Arrays.asList(inner))
        .withInnerValueMap(Collections.emptyMap())
        .withInnerValueSet(Collections.singleton(inner))
        .withIntPrim(1)
        .withIntPrimArray(new int[]{1})
        .withIntegerObject(2)
        .withLongObject(Long.MAX_VALUE)
        .withLongPrim(123456789123456789L)
        .withLongPrimArray(new long[]{Long.MIN_VALUE})
        .withShortObject((short) 1)
        .withShortPrim((short) 2)
        .withShortPrimArray(new short[]{1})
        .withString("string")
        .build();
```

#### toString is generated automatically

```java

System.out.println(example);

// print out
Example{anEnum=DAYS,booleanObject=true,booleanPrim=true,booleanPrimArray=[true],byteObject=1,bytePrim=1,bytePrimArray=[1],charObject=a,charPrim=a,charPrimArray=[a],doubleObject=1.7976931348623157E308,doublePrim=1.2345689012345679E8,doublePrimArray=[4.9E-324],floatObject=1.0,floatPrim=0.12345679,floatPrimArray=[1.4E-45],innerValue=InnerValue{integer=1,value=value},innerValueList=[InnerValue{integer=1,value=value}],innerValueMap={},innerValueSet=[InnerValue{integer=1,value=value}],intPrim=1,intPrimArray=[1],integerObject=2,longObject=9223372036854775807,longPrim=123456789123456789,longPrimArray=[-9223372036854775808],shortObject=1,shortPrim=2,shortPrimArray=[1],string=string}
```

#### hashCode and equals are generated automatically

```java
InnerValue i1 = new InnerValueBuilder().withInteger(1).withValue("value").build();
InnerValue i2 = new InnerValueBuilder().withInteger(1).withValue("value").build();

i1.equals(i2);                  // true
i2.equals(i1);                  // true
i1.hashCode() == i2.hashCode(); // true

InnerValue i3 = new InnerValueBuilder().withInteger(3).withValue("value3").build();

i1.equals(i3);                  // false
i3.equals(i1);                  // false
i1.hashCode() == i3.hashCode(); // false
```

#### Properties have null checks.

```java
@FinalValue
public interface Person {
  
  String getName();
  int getAge();
}

// generates NullPointerException("age is null.")
new PersonBuilder().witName("jim").build();

// ok!
new PersonBuilder().witName("jim").withAge(30).build();
```

#### Properties can be nullable.

```java
@FinalValue
public interface Example {
  
  @javax.annotation.Nullable
  String getValue1();
  
  String getValue2();
}

// throws a new NullPointerException("value2 is null.")
new ExampleBuilder().build();

// ok, since value1 is nullable
Example e = new ExampleBuilder().withValue2("value").build();

// use java.util.Optional to access nullable values
Optional<String> nullable = Optional.ofNullable(e.getValue1());
```

#### Properties can have default fallback values.

```java
@FinalValue
public interface Example {

  default String getForename() {
    return "Jim";
  }

  default List<Integer> getNumbers() {
    return Arrays.asList(1, 2, 3);
  }

  default Map<String, Integer> getMap() {
    return Collections.singletonMap("value", 1);
  }

  default Set<Long> getSet() {
    return Collections.emptySet();
  }
}

// ok since all properties have default values!
Example example = new ExampleBuilder().build();

// prints [1, 2, 3]
System.out.println(example.getNumbers());
```

#### Classes can have default methods.


```java
@FinalValue
public interface Person {

  String getForename();

  String getSurname();

  default String fullname() {
    return getForename() + " " + getSurname();
  }
}

Person p = new PersonBuilder().withForename("Linus").withSurename("Torvalds").build();

// prints Linus Torvalds
System.out.println(p.fullname());
```

#### Copy instances


```java
@FinalValue
public interface Person {

  String getForename();

  String getSurname();

  PersonBuilder copy() { return PersonBuilder.builderFrom(this); }
}

Person p1 = new PersonBuilder().withForename("Linus").withSurename("Torvalds").build();
Person p2 = p1.copy().withForename("Wife").build();
```


#### Override hashCode, equals and toString.

A @FinalValue interface can override default implementation of hashCode, equals and toString by defining static
methods following the signature and conventions shown below.

```java
@FinalValue
public interface Example {

  String getValue1();
  String getValue2();
  
  // Convention: static, name 'equals', two arguments with same type, return boolean.
  static boolean equals(Example o1, Example o2) {
    return o1.getValue1().equals(o2.getValue1());
  }
  
  // Convention: static, name 'hashCode', one argument with same type, return int.
  static int hashCode(Example o) {
    return o.getValue1().hashCode();
  }

  // Convention: static, name 'toString', one argument with same type, return String.
  static String toString(Example o) {
    return o.getValue1();
  }
}
```


#### Post construction hook.

A @FinalValue interface that need to validate/constrain properties further can define a method following the signature and conventions shown below.

```java
@FinalValue
public interface Example {

  String getValue1();
  String getValue2();
  
  // Convention: static, name 'postConstruct', one argument with same type, return void.
  static void postConstruct(Example o) {
    if (o.getValue1().equals("illegal")) {
      throw new IllegalArgumentException("illegal value1");
    }
  }
}
```


#### Jackson Json Serialization

Jackson can serialize @FinalValue interfaces directly since all properties are exposed as getter method. Jackson can also deserialize @FinalValue interfaces using @JsonDeserialize with a 'builder' argument.

```java
@FinalValue
@JsonDeserialize(builder=ExampleBuilder.class)
public interface Example {
  String getValue();
  Integer getValue2();
}

Example example = new ExampleBuilder().withValue("v1").withValue2("v2").build();
ObjectMapper mapper = new ObjectMapper();
String exampleString = mapper.writeValueAsString(example);
example = mapper.readValue(exampleString, Example.class);

```

