

ORM-Unit for JPA by default requires **Zero Configuration**. All you need to do is:
  1. download ORMUnit jars
  1. create ormunit.xml files
  1. run ORMUnit-based tests

# How it works?? #

zeroconf is possible because ORMUnit for JPA assumes that tests will be executed against in-memory database of one of three types:
  * Derby
  * HSQL
  * H2
and using one of these JPA providers
  * Hibernate
  * EclipseLink
  * OpenJPA

ORM-Unit for JPA automatically checks if DB drivers are available and sets properties required by PersistenceProvider used by your persistence unit.

Using driver-persistenceprovider-specific properties set it creates entitymanagers which will be later used by your test code.


# Overriding defaults #

If there is a need to override default bahaviour, you can either
  * put all connection-related properties to "properties" tags in persistence.xml
or
  * create ormunit.properties and set driver-persistenceprovider-specific properties along with `ormunit.datasources` and `ormunit.datasources.default` properties
```
ormunit.datasources=your_ds
ormunit.datasources.default=your_ds

your_ds.hibernate.connection.driver_class=org.postgresql.Driver
your_ds.hibernate.connection.username=username
your_ds.hibernate.connection.password=password
your_ds.hibernate.connection.url=jdbc:postgresql://host/db

```

In case you used second option you must know that ORM-Unit has several levels of properties sets (order of importance: highest at top):
  * persistence context (these are from persistence unit "properties" tag)
  * user defined (these are from ormunit.properties files)
  * defaults (built-in properties sets)

These properties sets override each oder in order: "persistence context" override "user defined" and "user defined" override "defaults"