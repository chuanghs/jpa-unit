package org.ormunit;

import org.ormunit.exception.ConfigurationException;
import org.ormunit.exception.FileReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: jan
 * Date: 18.09.11
 * Time: 20:05
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseDBController {

    private final static Logger log = LoggerFactory.getLogger(BaseDBController.class);

    private TestSet testSet;
    private Class<?> callerClass;
    private String ormUnitFileName;
    private ORMUnitPropertiesReader ormUnit;


    public BaseDBController(Class<?> callerClass, String ormUnitFileName) {
        this.callerClass = callerClass;

        this.ormUnitFileName = ormUnitFileName;
        this.ormUnit = new ORMUnitPropertiesReader(callerClass);
    }

    public abstract void tearDown();

    public abstract void setUp();

    protected abstract boolean isWithDB();

    protected abstract ORMProvider getProvider();

    private TestSet readTestSet() {
        if (isWithDB()) {
            testSet = new TestSet(getProvider());

            InputStream inputStream = null;
            if (ormUnitFileName != null && !"".equals(ormUnitFileName)) {
                inputStream = getCallerClass().getResourceAsStream(ormUnitFileName);
            } else {
                inputStream = getCallerClass().getResourceAsStream("./" + getCallerClass().getSimpleName() + ".xml");
            }
            if (inputStream != null) {
                try {
                    ormUnit.read(inputStream, testSet);
                } catch (FileReadException e) {
                    throw new ConfigurationException(e);
                }
            }
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            return testSet;
        }
        return null;
    }

    public ORMUnitPropertiesReader getORMUnit() {
        return ormUnit;
    }

    protected void executeTestSet() {
        if (testSet == null)
            this.testSet = readTestSet();
        testSet.execute();
    }

    protected Class<?> getCallerClass() {
        return callerClass;
    }
}
