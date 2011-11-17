package org.ormunit.jpa.entityinspector;


import org.h2.schema.Sequence;
import org.ormunit.BeanUtils;
import org.ormunit.ORMProviderAdapter;

import javax.persistence.*;
import javax.xml.validation.Schema;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 16.06.11
 * Time: 20:55
 */
public class AnnotationsEntityInspector implements EntityInspector {

    private BeanUtils utils = new BeanUtils();


    public Set<String> getSchemaNames(Class<?> entityClass) {
        Set<String> set = new HashSet<String>();

        while (entityClass != null) {
            String schemaName = getTableSchemaName(entityClass);
            if (schemaName != null)
                set.add(schemaName);

            schemaName = getSequenceGeneratorSchemaName(entityClass);
            if (schemaName != null)
                set.add(schemaName);

            entityClass = entityClass.getSuperclass();
        }
        return set;
    }

    private String getSequenceGeneratorSchemaName(Class<?> entityClass) {
        SequenceGenerator sg = entityClass.getAnnotation(SequenceGenerator.class);
        if (sg != null) {

            String schemaName = extractSchemaName(sg.sequenceName());
            if (schemaName != null) {
                return schemaName;
            }
        }
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private String getTableSchemaName(Class<?> entityClass) {
        Table annotation = entityClass.getAnnotation(Table.class);
        if (annotation != null) {
            if (!"".equals(annotation.schema()))
                return annotation.schema();
            return extractSchemaName(annotation.name());
        }
        return null;
    }

    private String extractSchemaName(String ddlElementName) {
        if (ddlElementName.contains("."))
            return ddlElementName.substring(0, ddlElementName.indexOf("."));
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public ORMProviderAdapter.AccessType getAccessTypeOfClass(Class entityClass) {
        Class clazz = entityClass;
        while (clazz != null) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getAnnotation(Id.class) != null || m.getAnnotation(EmbeddedId.class) != null)
                    return ORMProviderAdapter.AccessType.Property;
            }
            clazz = clazz.getSuperclass();
        }
        clazz = entityClass;
        while (clazz != null) {
            for (Field m : clazz.getDeclaredFields()) {
                if (m.getAnnotation(Id.class) != null || m.getAnnotation(EmbeddedId.class) != null)
                    return ORMProviderAdapter.AccessType.Field;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }


    public Field getIdField(Class<?> entityClass) {
        Set<Field> annotatedFields = utils.getFieldsAnnotatedWith(entityClass, Id.class, EmbeddedId.class);
        if (annotatedFields.size() == 1)
            return annotatedFields.iterator().next();
        return null;
    }

    public PropertyDescriptor getIdProperty(Class<?> entityClass) {
        Set<PropertyDescriptor> annotatedProperties = utils.getPropertiesAnnotatedWith(entityClass, Id.class, EmbeddedId.class);
        if (annotatedProperties.size() == 1)
            return annotatedProperties.iterator().next();
        return null;
    }

    public Class getIdClassValue(Class<?> entityClass) {
        IdClass idClassAnnotation = entityClass.getAnnotation(IdClass.class);
        if (idClassAnnotation != null)
            return idClassAnnotation.value();
        return null;
    }

    public Class<?> getIdType(Class<?> entityClass) {
        Class<?> result = null;
        IdClass idClass = entityClass.getAnnotation(IdClass.class);
        if (idClass != null) {
            result = idClass.value();
        } else {
            if (getAccessTypeOfClass(entityClass) == ORMProviderAdapter.AccessType.Property) {
                return utils.getPropertiesAnnotatedWith(entityClass, Id.class, EmbeddedId.class).iterator().next().getPropertyType();
            } else if (getAccessTypeOfClass(entityClass) == ORMProviderAdapter.AccessType.Field) {
                return utils.getFieldsAnnotatedWith(entityClass, Id.class, EmbeddedId.class).iterator().next().getType();
            }
        }
        return result;
    }

    public boolean isIdGenerated(Class<?> entity) {
        if (getAccessTypeOfClass(entity) == ORMProviderAdapter.AccessType.Field) {
            for (Field field : utils.getFieldsAnnotatedWith(entity.getClass(), Id.class)) {
                if (field.getAnnotation(GeneratedValue.class) != null) {
                    return true;
                }
            }
        } else if (getAccessTypeOfClass(entity) == ORMProviderAdapter.AccessType.Property) {
            for (PropertyDescriptor pd : utils.getPropertiesAnnotatedWith(entity.getClass(), Id.class)) {
                if (pd.getReadMethod().getAnnotation(GeneratedValue.class) != null) {
                    return true;
                }
            }
        }
        return false;
    }


}
