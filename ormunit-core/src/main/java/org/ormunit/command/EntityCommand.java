package org.ormunit.command;

import org.ormunit.ORMProvider;
import org.ormunit.ORMUnitTestSet;
import org.ormunit.entity.EntityAccessor;
import org.ormunit.exception.ORMUnitConfigurationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 28.12.10
 * Time: 16:07
 */
public class EntityCommand extends ORMUnitCommand {

    private final String ormId;
    private final Object entity;
    private EntityAccessor accessor;
    private final Set<EntityReference> references;

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

    public String getOrmId() {
        return ormId;
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

    @Override
    public void visit(ORMUnitTestSet testSet) {
        ORMProvider provider = testSet.getProvider();
        for (EntityReference ref : references) {
            Class propertyClass = getPropertyClass(ref.getPropertyName());
            Object reference = null;
            if (ref.getType() == EntityReference.Type.DB) {
                reference = provider.getDBEntity(propertyClass, ref.getId());
            } else if (ref.getType() == EntityReference.Type.ORMUNIT) {
                reference = getORMEntity(testSet, (String) ref.getId());
            }

            if (reference == null)
                throw new ORMUnitConfigurationException("Entity: " + propertyClass.getCanonicalName() + " with id: '" + ref.getId() + "' cannot be found for entity: " + entity.getClass().getCanonicalName());

            set(getEntity(), reference, ref.getPropertyName());
        }
        provider.entity(this.entity);
        if (this.ormId != null) {
            registerORMEntity(testSet, this.entity, this.ormId);
        }
    }

    private static Map<ORMUnitTestSet, Map<String, Object>> entities = new HashMap<ORMUnitTestSet, Map<String, Object>>();

    private Object getORMEntity(ORMUnitTestSet testSet, String id) {
        Map<String, Object> stringObjectMap = entities.get(testSet);
        if (stringObjectMap != null) {
            return stringObjectMap.get(id);
        }
        return null;
    }

    private void registerORMEntity(ORMUnitTestSet testSet, Object entity, String ormId) {
        Map<String, Object> stringObjectMap = entities.get(testSet);
        if (stringObjectMap == null) {
            entities.put(testSet, stringObjectMap = new HashMap<String, Object>());
        }
        if (stringObjectMap.get(ormId)!=null)
            throw new ORMUnitConfigurationException("Multiple entities with same ormId: "+ormId);
        stringObjectMap.put(ormId, entity);
    }


    @Override
    public String toString() {
        return "EntityWithRefsCommand{" +
                "ormId=" + getOrmId() +
                "entity=" + getEntity() +
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
