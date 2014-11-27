### vals - immutable interfaces
[![Build Status](https://travis-ci.org/deephacks/vals.png?branch=master)](https://travis-ci.org/deephacks/vals)

The purpose of vals is to provide a productive way of creating extendable immutable value objects with automatically generated builders from standard Java interfaces. No runtime dependencies required.

### How to use @Val

Create an interface and annotate it with @Val. All non-void, parameterless, getter methods on this interface will be treated as properties, each having same type as the return type of the method. 

Two classes will automatically be generated at compile time.

* A class named Val_[name].java that implement the @Val interface.
* A builder class named [name]Builder.java that construct @Val interface objects using the Builder pattern.

Notice the following conventions.

* The implementation is immutable and implements toString, equals and hashCode based on defined properties.
* Default hashCode, equals and toString method can be vetoed by the @Val interface using naming conventions.
* All values are checked for null when constructed/built unless the method is @javax.annotation.Nullable.
* Properties can define default values by returning them from the method on the interface.
* @Val interfaces can extend any interface as long as it provide a default implementation.
* A post construction hook (called inside the constructor) can be defined by @Val interfaces that need to validate/constrain properties further.
* Even tough instances are immutable each builder is equipped with a method that construct a builder copy from an existing instance with same values. This makes it easy to update values without violating immutability.

### pom.xml

Notice that that these dependencies only require the 'provided' scope which means they will not be packaged with the application.

```xml
<dependency>
  <groupId>org.deephacks.vals</groupId>
  <artifactId>vals</artifactId>
  <version>${version}</version>
  <scope>provided</scope>
</dependency>
<dependency>
  <groupId>com.squareup</groupId>
  <artifactId>javawriter</artifactId>
  <version>2.4.0</version>
  <scope>provided</scope>
</dependency>
```

### @Val example

```java
@Val
public interface Example {

  @Id(0) String getString();
  @Id(1) Byte getByteObject();
  @Id(3) Short getShortObject();
  @Id(4) Integer getIntegerObject();
  @Id(5) Long getLongObject();
  @Id(6) Float getFloatObject();
  @Id(7) Double getDoubleObject();
  @Id(8) Character getCharObject();
  @Id(9) Boolean getBooleanObject();

  @Id(10) byte getBytePrim();
  @Id(11) byte[] getBytePrimArray();
  @Id(12) short getShortPrim();
  @Id(13) short[] getShortPrimArray();
  @Id(14) int getIntPrim();
  @Id(15) int[] getIntPrimArray();
  @Id(16) long getLongPrim();
  @Id(17) long[] getLongPrimArray();
  @Id(18) float getFloatPrim();
  @Id(19) float[] getFloatPrimArray();
  @Id(20) double getDoublePrim();
  @Id(21) double[] getDoublePrimArray();
  @Id(22) char getCharPrim();
  @Id(23) char[] getCharPrimArray();
  @Id(24) boolean getBooleanPrim();
  @Id(25) boolean[] getBooleanPrimArray();

  @Id(26) TimeUnit getAnEnum();

  @Id(27) InnerVal getInnerValue();
  @Id(28) List<InnerVal> getInnerValueList();
  @Id(29) Map<String, InnerVal> getInnerValueMap();

  @Val
  public static interface InnerVal {
    @Id(0) String getValue();
    @Id(1) int getInteger();
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
InnerVal i1 = new InnerValBuilder().withInteger(1).withValue("value").build();
InnerVal i2 = new InnerValBuilder().withInteger(1).withValue("value").build();

i1.equals(i2);                  // true
i2.equals(i1);                  // true
i1.hashCode() == i2.hashCode(); // true

InnerVal i3 = new InnerValBuilder().withInteger(3).withValue("value3").build();

i1.equals(i3);                  // false
i3.equals(i1);                  // false
i1.hashCode() == i3.hashCode(); // false
```

#### Properties have null checks.

```java
@Val
public interface Person {
  
  @Id(0) String getName();
  @Id(1) int getAge();
}

// generates NullPointerException("age is null.")
new PersonBuilder().witName("jim").build();

// ok!
new PersonBuilder().witName("jim").withAge(30).build();
```

#### Properties can be nullable.

```java
@Val
public interface Example {
  
  @javax.annotation.Nullable
  @Id(0) String getValue1();
  @Id(1) String getValue2();
}

// throws a new NullPointerException("value2 is null.")
new ExampleBuilder().build();

// ok, since value1 is nullable
Example e = new ExampleBuilder().withValue2("value").build();

// use java.util.Optional to access nullable values
Optional<String> nullable = Optional.ofNullable(e.getValue1());
```

#### Properties can have default values.

```java
@Val
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
@Val
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
@Val
public interface Person {

  @Id(0) String getForename();

  @Id(1) String getSurname();

  PersonBuilder copy() { return PersonBuilder.builderFrom(this); }
}

Person p1 = new PersonBuilder().withForename("Linus").withSurename("Torvalds").build();
Person p2 = p1.copy().withForename("Wife").build();
```


#### Override hashCode, equals and toString.

A @Val interface can override default implementation of hashCode, equals and toString by defining static
methods following the signature and conventions shown below.

```java
@Val
public interface Example {

  @Id(0) String getValue1();
  @Id(1) String getValue2();
  
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

A @Val interface that need to validate/constrain properties further can define a method following the signature and conventions shown below.

```java
@Val
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
@Val @JsonDeserialize(builder=ExampleBuilder.class)
public interface Example {
  String getValue();
  Integer getValue2();
}

Example example = new ExampleBuilder().withValue("v1").withValue2("v2").build();
ObjectMapper mapper = new ObjectMapper();
String exampleString = mapper.writeValueAsString(example);
example = mapper.readValue(exampleString, Example.class);

```

