package org.ormunit.command;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 28.12.10
 * Time: 16:07
 */
public class EntityCommand extends ORMUnitCommand {

    private final Object entity;
    private final Set<EntityReference> references;

    public EntityCommand(Object entity) {
        this.entity = entity;
        this.references = new HashSet<EntityReference>();
    }

    public EntityCommand(Object entity, Set<EntityReference> references) {
        this.entity = entity;
        this.references = references;
    }

    @Override
    public void visit(ORMCommandVisitor visitor) {

        for (EntityReference ref : references) {
            Object reference = visitor.getReference(ref.getPropertyClass(), ref.getId());
            ref.setReference(getEntity(), reference);
        }
        visitor.entity(this.entity);

    }

    @Override
    public String toString() {
        return "EntityWithRefsCommand{" +
                "entity=" + getEntity() +
                ", references=" + references +
                '}';
    }

    public Object getEntity() {
        return entity;
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
    public int hashCode() {
        int result = entity != null ? entity.hashCode() : 0;
        if (references != null)
            for (EntityReference er : references) {
                result = 31 * result + er.hashCode();
            }

        return result;
    }
}
