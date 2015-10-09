

# Introduction #
Many times in my work, i faced  problem of testing JPQL code, SQL code or mix of JPQL/SQL with entity processing logic.
It was extremely difficult to test such things on constantly changing production environment. What I needed was control of the unit test case's environment down to the database. So I googled a bit and found dbunit an unitils.
My first impression was: why the h..l do I need all those dependencies (do i need spring if my project is EJB/JPA...., and many others).
When i finally forced dbunit to load my xml file, it crashed saying that there are some conversion issues. (why is life so difficult.....).
Then i decided to write my own tool that will create testcase's controlled environment for me and delegate conversions to ORM implementation. Instead mapping tables (as dbunit does) and  handling all of the different database vendors i decided to map **entities** and leave conversion issues to the ORM (object relational mapping) provider.

After few evenings of coding, here it is. Probably with some bugs nevertheless very helpfull & easy to use tool: ORMUnit with JPA & Hibernate ports.



# Details #
I assume you already use JPA provider and you are in strong need of unit and integration testing JPQL/SQL/Entity code.

## Download & install ##

besides ORM-Unit for JPA, persistence provider is also needed. ORM-Unit's default configuration supports: OpenJPA, HibernateEM, EclipseLink.

### Maven users ###
maven users: add following code to your pom.xml file:
```
   <repository>
      <id>jpa-unit-repo</id>
      <url>http://jpa-unit.googlecode.com/hg/repository</url>
   </repository>
...
   <dependency>
      <groupId>org.ormunit</groupId>
      <artifactId>ormunit-jpa</artifactId>
      <version>[0.1.0, 1.0.0]</version>
   </dependency>
   <dependency>
      <groupId>org.hsqldb</groupId>
      <artifactId>hsqldb</artifactId>
      <version>2.0.0</version>
   </dependency>
```

### Other users ###
download, unpack and add jars to classpath from: http://jpa-unit.googlecode.com/files/ormunit-jpa-0.9.3-all.zip
In that archive there are DB drivers for: H2, Derby and HSQL. You don't need to use all three of them. Pick just one.

## Define entities ##

```
@Entity
public class Foo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int fooId;

    private String someAttribute;
}
...
@Entity
public class Bar {

    @Id
    private int barId;

    @ManyToOne
    private Foo foo;
}
```
## Persistence unit ##
Create new file: persistence.xml and place it in directory: /META-INF
Content of this might look like this:

```
<persistence-unit name="ormunit-jpa" transaction-type="RESOURCE_LOCAL">

        <class>Foo</class>
        <class>Bar</class>
        <properties>
            <property name="eclipselink.ddl-generation" value="create-tables"/>
            <property name="eclipselink.ddl-generation.output-mode" value="database"/>
        </properties>

</persistence-unit>
```

## Write test case ##
Each test case consists of two parts: testcase class and orm xml file. Testcase class contains tests code and orm xml file contains entities definitions which define test execution environment
### Test class ###
```
@RunWith(JUnit4.class)
public class FooTestCase extends JPAUnitTestCase {
    public JPAUnitSimpleTest() {
        super("ormunit-jpa", "FooTestCase.xml"); // FooTestCase.xml is afile that contains entities definitions that will be persisted before executing any test
    }

    @Before
    public void setUp(){
        super.setUp(); // remember to always call JPAUnitTestCase.setUp() if implementing setUp()
    }

    @Test
    public void testReadAllEntities2() {
        getEm().createQuery("select o from Foo").getResultList();
    }
}
```

first parameter of super constructor is persistence unit name (it is required), second parameter is orm-unit xml file name. Second parameter is optional, if not specified then file which name = ${testcase.name}.xml  will be searched (in the same package as testcase class) and used if found

### ORM-Unit xml file ###
Create file **FooTestCase.xml** and place it under the same package as **FooTestCase** class.
```
<ormunit>
    <Foo ormId="firstFoo" someAttribute="someValue" />
    <Foo ormId="secondFoo" someAttribute="someValue" />

    <Bar barId="1" foo="ormref(firstFoo)" />
    <Bar barId="2" foo="ormref(secondFoo)" />
    <Bar barId="3" foo="ormref(firstFoo)" />
</ormunit>
```