


# How it works #

all you need to do is subclass `org.ormunit.junit.JPAUnitTestCase`

```

public class TestCaseSubclass extends JPAUnitTestCase {

  public TestCaseSubclass(){
    super("persistence-unit-name", "entities.xml");
  }

}
```

or

```
 new ORMUnitConfigurationReader(getClass())
                        .read(getClass().getResourceAsStream(this.ormUnitFileName), 
                              new JPAORMProvider(getEm()))
                        .execute();
```
and create a ormunit xml file:
```
<ormunit>

    <PropertyAccessEntity id="1"/>
    <PropertyAccessEntity id="2"/>
    <PropertyAccessEntity id="3"/>
    <PropertyAccessEntity id="4"/>

    <FieldAccessEntity integerValue="1" complexType="ref(1)"/>
    <FieldAccessEntity integerValue="2" complexType="ref(1)"/>
    <FieldAccessEntity integerValue="3" complexType="ref(2)"/>
    <FieldAccessEntity integerValue="4" complexType="ref(3)"/>
    <FieldAccessEntity integerValue="5" complexType="ref(4)"/>

</ormunit>
```

Constructor of `org.ormunit.junit.JPAUnitTestCase` as an argument takes persistence unit name. Then when executing the test, ORMUnit will
  1. start transaction,
  1. persist all entities,
  1. run test,
  1. rolls back transaction

Could it be more easy????

# Syntax #
assume, there are classes Foo, Bar, and FooBar
```
public class Foo {
    @Id
    public Integer id;

    public Boolean happy;
    
}
public class Bar {
    @Id
    public Integer id;

    public Foo foo;

    public Date when;

}
public class FooBar {
    @Id
    public Integer id;   

    public BigDecimal value;
    
    public void Map<Integer, Bar> barMap; // maps bar's id to bar

    public void List<Foo> fooListMap;
}
```

## Simple properties ##
```

   <Foo id="1">
      <happy>true</happy> <!-- or false -->
   </Foo>
   <Bar>
      <id>2</id> <!-- simple properties can be listed as subelements or attributes -->
      <happy>true</happy> <!-- or false -->
      <when>22-01-2011</when>
   </Bar>
   <FooBar id="3">
      <value>1.2345</value>
   </FooBar>

```
## References to other entities ##
```

   <Bar>
      <id>1</id>
      <happy>true</happy>
      <foo id="3" happy="true"/>
   </Bar>
   <Bar>
      <id>1</id>
      <foo id="4">
         <happy>true</happy>
      </foo>
   </Bar>

   <Bar>
      <id>2</id>
      <foo>ref(3)</foo>
   </Bar>

```
## Collections ##
```
   <FooBar id="3">
      <fooListMap>
         <foo id="1" happy="true" />
         <foo>
            <id>2</id>
            <happy>false</happy>
         </foo>
      </fooListMap>      
   </FooBar>
```
## Maps ##
```
   <FooBar id="3">
      <barMap>
         <entry key="1">
            <bar id="1" when="22-01-2011">
         </entry>
         <entry key="2">
            <bar>
               <id="2" />
            </bar>
         </entry>
      </barMap>      
   </FooBar>
```