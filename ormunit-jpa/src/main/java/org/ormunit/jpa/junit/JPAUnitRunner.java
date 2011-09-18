package org.ormunit.jpa.junit;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.ormunit.jpa.annotations.Em;
import org.ormunit.jpa.annotations.JPAUnitTestCase;
import org.ormunit.junit.BaseORMUnitRunner;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: jan
 * Date: 18.09.11
 * Time: 20:44
 * To change this template use File | Settings | File Templates.
 */
public class JPAUnitRunner extends BaseORMUnitRunner {

    private JPADBController dbController;

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @throws org.junit.runners.model.InitializationError
     *          if the test class is malformed.
     */
    public JPAUnitRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }


    protected Statement withBefores(final FrameworkMethod method, final Object target,
                                    final Statement statement) {
        return new JPABeforeStatement(target, method, super.withBefores(method, target, statement));
    }

    protected Statement withAfters(FrameworkMethod method, Object target,
                                   Statement statement) {

        return new JPAAfterStatement(super.withAfters(method, target, statement));

    }


    private class JPABeforeStatement extends Statement {
        private final Object target;
        private final FrameworkMethod method;
        private Statement statement;

        public JPABeforeStatement(Object target, FrameworkMethod method, Statement statement) {
            this.target = target;
            this.method = method;
            this.statement = statement;
        }

        @Override
        public void evaluate() throws Throwable {
            JPAUnitRunner.this.dbController = createDBController(target, method);
            if (JPAUnitRunner.this.dbController != null) {
                JPAUnitRunner.this.dbController.setUp();
                injectEm(target, JPAUnitRunner.this.dbController);
            }
            this.statement.evaluate();
        }

        protected JPADBController createDBController(Object target, final FrameworkMethod method) {
            JPAUnitTestCase annotation = method.getAnnotation(JPAUnitTestCase.class);
            if (annotation == null) {
                annotation = method.getMethod().getDeclaringClass().getAnnotation(JPAUnitTestCase.class);
            }
            if (annotation != null) {
                return new JPADBController(target.getClass(), annotation.unitName(), annotation.ormUnitFileName());
            }
            return null;
        }

        private void injectEm(Object target, JPADBController jpadbController) {
            EntityManager em = jpadbController.getProvider().getEntityManager();

            Class<? extends Object> aClass = target.getClass();
            while (aClass != null) {
                for (Field f : aClass.getDeclaredFields()) {
                    if (f.getAnnotation(Em.class) != null) {
                        f.setAccessible(true);
                        try {
                            f.set(target, em);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Cannot set entitymanager.");
                        }
                    }
                }
                aClass = aClass.getSuperclass();
            }
        }
    }

    private class JPAAfterStatement extends Statement {
        private Statement statement;

        public JPAAfterStatement(Statement statement) {
            this.statement = statement;
        }

        @Override
        public void evaluate() throws Throwable {
            this.statement.evaluate();
            if (JPAUnitRunner.this.dbController != null) {
                JPAUnitRunner.this.dbController.tearDown();
                JPAUnitRunner.this.dbController = null;
            }
        }
    }
}
