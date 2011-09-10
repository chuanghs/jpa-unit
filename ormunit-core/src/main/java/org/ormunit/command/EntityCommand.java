package org.ormunit.command;

import org.ormunit.ORMProvider;
import org.ormunit.TestSet;
import org.ormunit.entity.EntityAccessor;
import org.ormunit.exception.ConfigurationException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 28.12.10
 * Time: 16:07
 */
public class EntityCommand implements ORMUnitCommand {

    private final String ormId;
    private final Object entity;
    private EntityAccessor accessor;
    private final Set<EntityReference> references;
    private static Map<TestSet, Map<String, Object>> entities = new WeakHashMap<TestSet, Map<String, Object>>();

    public EntityCommand(Object entity, EntityAccessor accessor) {
        this(null, entity, accessor);
    }

    public EntityCommand(String ormid, Object entity, EntityAccessor accessor) {
        this(ormid, entity, accessor, new HashSet<EntityReference>());
    }

    public EntityCommand(String ormid, Object entity, EntityAccessor accessor, Set<EntityReference> references) {
        this.ormId = ormid;
        this.entity = entity;
        this.accessor = accessor;
        this.references = references;
    }

    public Class getPropertyClass(String propertyName) {
        return accessor.getType(propertyName);
    }

    public void set(Object entity, Object value, String propertyName) {
        try {
            accessor.set(entity, propertyName, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void visit(TestSet testSet) {
        ORMProvider provider = testSet.getProvider();
        for (EntityReference ref : references) {
            Class propertyClass = getPropertyClass(ref.getPropertyName());
            Object reference = null;
            if (ref.getType() == EntityReference.Type.DB) {
                reference = provider.getEntity(propertyClass, ref.getId());
            } else if (ref.getType() == EntityReference.Type.ORMUNIT) {
                reference = getORMEntity(testSet.getRootTestSet(), (String) ref.getId());
            }

            if (reference == null)
                throw new ConfigurationException(String.format("Entity: %s with id: '%s' cannot be found for entity: %s", propertyClass.getCanonicalName(), ref.getId(), entity.getClass().getCanonicalName()), null);

            set(getEntity(), reference, ref.getPropertyName());
        }
        provider.entity(this.entity);
        if (this.ormId != null) {
            registerORMEntity(testSet.getRootTestSet(), this.entity, this.ormId);
        }
    }

    private static Object getORMEntity(TestSet testSet, String id) {
        if (testSet == null ){
            return null;
        }

        Map<String, Object> stringObjectMap = entities.get(testSet);

        if (stringObjectMap != null && stringObjectMap.get(id)!=null) {
            return stringObjectMap.get(id);
        }
        return null;
    }

    private void registerORMEntity(TestSet testSet, Object entity, String ormId) {
        Map<String, Object> stringObjectMap = entities.get(testSet.getRootTestSet());
        if (stringObjectMap == null) {
            entities.put(testSet, stringObjectMap = new HashMap<String, Object>());
        }
        if (stringObjectMap.get(ormId)!=null)
            throw new ConfigurationException(String.format("Multiple entities with same ormId: %s", ormId), null);
        stringObjectMap.put(ormId, entity);
    }


    @Override
    public String toString() {
        return "EntityCommand{" +
                "ormId=" + ormId +
                ", entity=" + getEntity() +
                ", references=" + references +
                '}';
    }

    public Object getEntity() {
        return entity;
    }

    private boolean sameReferences(EntityCommand that) {
        if (that.references.size() != this.references.size())
            return false;

        for (EntityReference er : that.references) {
            if (!this.references.contains(er))
                return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityCommand that = (EntityCommand) o;

        if (entity != null ? !entity.equals(that.entity) : that.entity != null) return false;
        if (references != null ? !sameReferences(that) : that.references != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = entity != null ? entity.hashCode() : 0;
        if (references != null)
            for (EntityReference er : references) {
                result = 31 * result + er.hashCode();
            }

        return result;
    }
}
