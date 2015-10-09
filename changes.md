

### 2011-02-12 ###
  * if entities are identified by generated id they couldn't be injected into someother entity (in ormunit xml file). To resolve this issue ormId entity attribute and ormref() keyword was presented;
  * moved to hsqldb 2.0

### 2011-01-23 ###
  * default configuration for Derby, HSQL, H2 allows to using ORM-Unit for JPA with zeroconf,
  * automatically importing all classes listed in "class" elements in persistenc.xml (no need for importing manually)
  * Collections and Map (type parameters are recognized) handling in ormunit.xml files
```
  <import class="com.example.SimplePOJO" alias="sp"> <!-- this is not needed  as long as it's listed in persistence.xml  -->

   <sp id="1">
      <someWeirdMapProperty>
            <entry key="">
                 <mapEntryValueProperty>value</mapEntryValueProperty>
                 <mapEntryValueProperty2>1.22</mapEntryValuePropert2>
            </entry>
      </someWeirdMapProperty>
   </sp>
   <sp id="2">
      <stringProperty>some another string value</stringProperty>
   </sp>
</ormunit>
```

### 2011-01-07 ###
  * when using JPA port there is no need to import all used Entity classes. ORMUnit-jpa will  find persistence-unit that u use and automatically discover managed classes.
Before:
```
<ormunit>

   <import class="com.example.SimplePOJO" alias="sp"> <!-- alias is optional -->

   <sp id="1">
      <stringProperty>some string value</stringProperty>
   </sp>
   <sp id="2">
      <stringProperty>some another string value</stringProperty>
   </sp>
</ormunit>
```
Now:
```
<ormunit>
   <SimplePOJO id="1">
      <stringProperty>some string value</stringProperty>
   </SimplePOJO>
   <SimplePOJO id="2">
      <stringProperty>some another string value</stringProperty>
   </SimplePOJO>
</ormunit>
```
  * when ormunit xml file has same name as test case class name, this file will be discovered automatically, even if not passed explicitly as `org.ormunit.junit.JPAUnitTestCase` constructor parameter
Before:
```
public class YourTestCase extends JPAUnitTestCase {
   public YourTestCase(){
      super("persistenc-unit-name", "./YourTestCase.xml");
   }
}
```
Now:
```
public class YourTestCase extends JPAUnitTestCase {
   public YourTestCase(){
      super("persistenc-unit-name");
   }
}
```