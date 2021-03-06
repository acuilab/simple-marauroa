package simple.client.gui;

import java.awt.geom.Rectangle2D;
import java.util.*;
import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import org.openide.util.lookup.ServiceProvider;
import simple.client.entity.ClientEntity;
import simple.client.entity.EntityFactory;

/**
 * Stores the objects that exists on the World right now.
 *
 */
@ServiceProvider(service = IGameObjects.class)
public class GameObjects implements IGameObjects {

    /**
     * the logger instance.
     */
    private static final Logger LOG = Log4J.getLogger(GameObjects.class);
    private final Map<FQID, ClientEntity> objects;

    /**
     * constructor.
     *
     */
    public GameObjects() {
        objects = new HashMap<>();
    }

    @Override
    public Iterator<ClientEntity> iterator() {
        return objects.values().iterator();
    }

    /**
     *
     * @param object
     * @return
     */
    @Override
    public ClientEntity get(RPObject object) {
        return objects.get(FQID.create(object));
    }

    /**
     *
     * @param id
     * @return
     */
    public ClientEntity get(RPObject.ID id) {
        return objects.get(new FQID(id));
    }

    /**
     * Removes all the object entities.
     */
    public synchronized void clear() {
        if (!objects.isEmpty()) {
            LOG.debug("Game objects not empty!");

            // invalidate all entity objects
            Iterator<ClientEntity> it = iterator();

            while (it.hasNext()) {
                ClientEntity entity = it.next();
                LOG.error("Residual entity: " + entity);
                entity.release();
            }

            objects.clear();
        }
    }

    /**
     *
     * @param entity
     * @return
     */
    public boolean collides(ClientEntity entity) {
        Rectangle2D area = entity.getArea();

        synchronized (this) {
            if (objects.values().stream().anyMatch((other)
                    -> (other.isObstacle(entity) && area.intersects(other.getArea())))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Update objects based on the lapsus of time elapsed since the last call.
     *
     * @param delta The time since last update (in ms).
     */
    @Override
    public synchronized void update(int delta) {
        objects.values().forEach((entity) -> {
            entity.update(delta);
        });
    }

    /**
     * Create an add an ClientEntity. This does not add to the screen list.
     *
     * @param object The object.
     *
     * @return An entity.
     */
    protected ClientEntity add(final RPObject object) {
        ClientEntity entity = EntityFactory.createEntity(object);

        if (entity != null) {
            synchronized (this) {
                objects.put(FQID.create(object), entity);
            }
        }

        return entity;
    }

    //
    // RPObjectChangeListener
    //
    /**
     * An object was added.
     *
     * @param object The object.
     */
    @Override
    public void onAdded(final RPObject object) {
        if (object.has("server-only")) {
            LOG.debug("Discarding object: " + object);
        } else {
            if (!object.getRPClass().subclassOf("entity")) {
                LOG.debug("Skipping non-entity object: " + object);
                return;
            }

            ClientEntity entity = add(object);

            if (entity != null) {
                LOG.debug("added " + entity);
            } else {
                LOG.error("No entity for: " + object);
            }
        }
    }

    /**
     * The object added/changed attribute(s).
     *
     * @param object The base object.
     * @param changes The changes.
     */
    @Override
    public void onChangedAdded(final RPObject object, final RPObject changes) {
        ClientEntity entity;

        synchronized (this) {
            entity = objects.get(FQID.create(object));
        }

        if (entity != null) {
            entity.onChangedAdded(object, changes);
        }
    }

    /**
     * An object removed attribute(s).
     *
     * @param object The base object.
     * @param changes The changes.
     */
    @Override
    public void onChangedRemoved(final RPObject object, final RPObject changes) {
        ClientEntity entity;

        synchronized (this) {
            entity = objects.get(FQID.create(object));
        }

        if (entity != null) {
            entity.onChangedRemoved(object, changes);
        }
    }

    /**
     * An object was removed.
     *
     * @param object The object.
     */
    @Override
    public void onRemoved(final RPObject object) {
        RPObject.ID id = object.getID();

        LOG.debug("removed " + id);

        ClientEntity entity;

        synchronized (this) {
            entity = objects.remove(FQID.create(object));
        }

        if (entity != null) {
            entity.release();
        }
    }

    /**
     * A slot object was added.
     *
     * @param object The container object.
     * @param slotName The slot name.
     * @param sobject The slot object.
     */
    @Override
    public void onSlotAdded(final RPObject object, final String slotName,
            final RPObject sobject) {
    }

    /**
     * A slot object added/changed attribute(s).
     *
     * @param object The base container object.
     * @param slotName The container's slot name.
     * @param sobject The slot object.
     * @param schanges The slot object changes.
     */
    @Override
    public void onSlotChangedAdded(final RPObject object,
            final String slotName, final RPObject sobject,
            final RPObject schanges) {
        ClientEntity entity;

        synchronized (this) {
            entity = objects.get(FQID.create(object));
        }

        if (entity != null) {
            entity.onSlotChangedAdded(object, slotName, sobject, schanges);
        }
    }

    /**
     * A slot object removed attribute(s).
     *
     * @param object The base container object.
     * @param slotName The container's slot name.
     * @param sobject The slot object.
     * @param schanges The slot object changes.
     */
    @Override
    public void onSlotChangedRemoved(final RPObject object,
            final String slotName, final RPObject sobject,
            final RPObject schanges) {
        ClientEntity entity;

        synchronized (this) {
            entity = objects.get(FQID.create(object));
        }

        if (entity != null) {
            entity.onSlotChangedRemoved(object, slotName, sobject, schanges);
        }
    }

    /**
     * A slot object was removed.
     *
     * @param object The container object.
     * @param slotName The slot name.
     * @param sobject The slot object.
     */
    @Override
    public void onSlotRemoved(final RPObject object, final String slotName,
            final RPObject sobject) {
    }

    @Override
    public RPObject onRPEvent(RPObject object) {
        return object;
    }

    //
    //
    /**
     * A fully qualified ID. This will make an nested ID unique, even when in a
     * slot tree.
     */
    protected static class FQID {

        /**
         * The object identification path.
         */
        protected Object[] path;

        /**
         * Create a fully qualified ID.
         *
         * @param id And object ID.
         */
        public FQID(RPObject.ID id) {
            this(new Object[]{id.getObjectID()});
        }

        /**
         * Create a fully qualified ID.
         *
         * @param path An identification path.
         */
        public FQID(Object[] path) {
            this.path = path;
        }

        //
        // FQID
        //
        /**
         * Create a FQID from an object tree.
         *
         * @param object An object.
         *
         * @return A FQID.
         */
        public static FQID create(final RPObject object) {
            LinkedList<Object> path = new LinkedList<>();
            RPObject node = object;

            while (true) {
                path.addFirst(node.getID().getObjectID());

                RPSlot slot = node.getContainerSlot();

                if (slot == null) {
                    break;
                }

                path.addFirst(slot.getName());
                node = node.getContainer();
            }

            return new FQID(path.toArray());
        }

        /**
         * Get the tree path of object identifiers.
         *
         * @return The identifier path.
         */
        public Object[] getPath() {
            return path;
        }

        //
        // Object
        //
        /**
         * Check if this equals another object.
         *
         * @param obj The object to compare to.
         * @return true if equal.
         */
        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof FQID)) {
                return false;
            }

            return Arrays.equals(getPath(), ((FQID) obj).getPath());
        }

        /**
         * Get the hash code.
         *
         * @return The hash code.
         */
        @Override
        public int hashCode() {
            int value = 0;

            for (Object obj : getPath()) {
                value ^= obj.hashCode();
            }

            return value;
        }

        /**
         * Get the string representation.
         *
         * @return The string representation.
         */
        @Override
        public String toString() {
            StringBuilder sbuf = new StringBuilder();

            sbuf.append('[');
            sbuf.append(path[0]);

            for (int i = 1; i < path.length; i++) {
                sbuf.append(':');
                sbuf.append(path[i]);
            }

            sbuf.append(']');

            return sbuf.toString();
        }
    }
}
