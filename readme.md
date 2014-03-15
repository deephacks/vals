### vals - immutable interfaces

The purpose of vals is to provide Java programmers with a non-intrusive and productive way of creating extendable immutable value objects with automatically generated builders from standard Java interfaces.

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

// generates NullPointerException("value2 is null.")
new ExampleBuilder().build();

// ok, since value1 is nullable
Example e = new ExampleBuilder().value2("value").build();

// use java.util.Optional to access
Optional<String> nullable = Optional.ofNullable(e.getValue1());
```

#### Properties can have default fallback values.

```java
@FinalValue
public interface Example {

  String getForename() {
    return "Jim";
  }

  List<Integer> getNumbers() {
    return Arrays.asList(1, 2, 3);
  }

  Map<String, Integer> getMap() {
    return Collections.singletonMap("value", 1);
  }

  Set<Long> getSet() {
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

Person p = new PersonBuilder().forename("Linus").surename("Torvalds").build();

// prints Linus Torvalds
System.out.println(p.fullname());
```


#### Classes can extend interfaces with a default implementation.

```java
@FinalValue
public interface Person extends Serializable, Comparable<Person> {

  String getForename();

  String getSurname();

  default int compareTo(Person that) {
    int result = this.getForename().compareTo(that.getForename());
    if (result == 0){
      result = this.getSurname().compareTo(that.getSurname());
    }
    return result;
  }
}
```
