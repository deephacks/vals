### vals - immutable interfaces

The purpose of vals is to provide Java programmers with a productive way of creating extendable immutable value objects with automatically generated builders from standard Java interfaces.

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
* A post construction hook can be defined by @FinalValue interfaces that need to validate/constrain properties further.

### pom.xml

Notice that that these dependencies only require the 'provided' scope which means they will not be packaged with the application.

```xml
<dependency>
  <groupId>org.deephacks.vals</groupId>
  <artifactId>vals</artifactId>
  <version>0.5.1</version>
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
InnerValue inner = new InnerValueBuilder().integer(1).value("value").build();

Example example = new ExampleBuilder()
        .anEnum(TimeUnit.DAYS)
        .booleanObject(true)
        .booleanPrim(true)
        .booleanPrimArray(new boolean[]{true})
        .byteObject((byte) 1)
        .bytePrim((byte) 1)
        .bytePrimArray(new byte[]{1})
        .charObject('a')
        .charPrim('a')
        .charPrimArray(new char[]{'a'})
        .doubleObject(Double.MAX_VALUE)
        .doublePrim(123456890.1234567890)
        .doublePrimArray(new double[]{Double.MIN_VALUE})
        .floatObject(1.0f)
        .floatPrim(0.123456789f)
        .floatPrimArray(new float[]{Float.MIN_VALUE})
        .innerValue(inner)
        .innerValueList(Arrays.asList(inner))
        .innerValueMap(Collections.emptyMap())
        .innerValueSet(Collections.singleton(inner))
        .intPrim(1)
        .intPrimArray(new int[]{1})
        .integerObject(2)
        .longObject(Long.MAX_VALUE)
        .longPrim(123456789123456789L)
        .longPrimArray(new long[]{Long.MIN_VALUE})
        .shortObject((short) 1)
        .shortPrim((short) 2)
        .shortPrimArray(new short[]{1})
        .string("string")
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
InnerValue i1 = new InnerValueBuilder().integer(1).value("value").build();
InnerValue i2 = new InnerValueBuilder().integer(1).value("value").build();

i1.equals(i2);                  // true
i2.equals(i1);                  // true
i1.hashCode() == i2.hashCode(); // true

InnerValue i3 = new InnerValueBuilder().integer(3).value("value3").build();

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
new PersonBuilder().name("jim").build();

// ok!
new PersonBuilder().name("jim").age(30).build();
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
Example e = new ExampleBuilder().value2("value").build();

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

#### Properties can have default fallback values.

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

Person p = new PersonBuilder().forename("Linus").surename("Torvalds").build();

// prints Linus Torvalds
System.out.println(p.fullname());
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
  static String toString(OverrideAndPostConstructFinal o) {
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
  static void postConstruct(OverrideAndPostConstructFinal o) {
    if (o.getValue1().equals("illegal")) {
      throw new IllegalArgumentException("illegal value1");
    }
  }
}
```


#### Jackson Json Serialization

Jackson can serialize @FinalValue interfaces directly since all properties are exposed as getter method. Jackson can also deserialize @FinalValue interfaces using @JsonDeserialize with a 'builder' argument.

```java
@FinalValue(builderPrefix = "with")
@JsonDeserialize(builder=ExampleBuilder.class)
public interface Example {
  String getValue();
  Integer getValue2();
}

Example example = new ExampleBuilder().withValue("v1").withValue2("v2").build();
ObjectMapper mapper = new ObjectMapper();
String exampleString = mapper.writeValueAsString(james);
example = mapper.readValue(exampleString, Example.class);

```

