package simple.client;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * A dispatcher for RPObjectChangeListeners. This normalizes the tree deltas
 * into individual object deltas.
 *
 * NOTE: The order of dispatch between contained objects and when their
 * container is very specific. Children objects are given a chance to perform
 * creation/updates before their parent is notified it happened to that specific
 * child. For cases of object removal, the parent is notified first, in case the
 * child does destruction/cleanup.
 */
public class RPObjectChangeDispatcher {

    /**
     * The logger.
     */
    private static final Logger LOG
            = Logger.getLogger(RPObjectChangeDispatcher.class.getSimpleName());
    /**
     * The normal listener.
     */
    protected RPObjectChangeListener listener;
    /**
     * The user object listener.
     */
    protected ArrayList<RPObjectChangeListener> userListeners
            = new ArrayList<>();

    /**
     * Create an RPObjectChange event dispatcher.
     *
     * @param listener The normal listener.
     * @param userListener The user object listener.
     */
    public RPObjectChangeDispatcher(final RPObjectChangeListener listener,
            final RPObjectChangeListener userListener) {
        this.listener = listener;
        userListeners.add(userListener);
    }

    //
    // RPObjectChangeDispatcher
    //
    /**
     * Dispatch object added event.
     *
     * @param object The object.
     * @param user If this is the private user object.
     */
    public void dispatchAdded(RPObject object, boolean user) {
        try {
            LOG.log(Level.FINE, "Object({0}) added to client", object.getID());
            fixContainers(object);
            fireAdded(object, user);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "dispatchAdded failed, object is "
                    + object, e);
        }
    }

    /**
     * Dispatch object removed event.
     *
     * @param object The object.
     * @param user If this is the private user object.
     */
    public void dispatchRemoved(RPObject object, boolean user) {
        try {
            LOG.log(Level.FINE, "Object({0}) removed from client",
                    object.getID());
            fixContainers(object);
            fireRemoved(object, user);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "dispatchRemovedonDeleted failed, object is " + object, e);
        }
    }

    /**
     * Dispatch object added/changed attribute(s) event.
     *
     * @param object The base object.
     * @param changes The changes.
     * @param user If this is the private user object.
     */
    public void dispatchModifyAdded(final RPObject object,
            final RPObject changes, final boolean user) {
        try {
            LOG.log(Level.FINE, "Object({0}) modified in client",
                    object.getID());
            fixContainers(object);
            fixContainers(changes);
            fireChangedAdded(object, changes, user);
            object.applyDifferences(changes, null);
        } catch (Exception e) {
            LOG.log(Level.FINE, "dispatchModifyAdded failed, object is " + object
                    + ", changes is " + changes, e);
        }
    }

    /**
     * Dispatch object removed attribute(s) event.
     *
     * @param object The base object.
     * @param changes The changes.
     * @param user If this is the private user object.
     */
    public void dispatchModifyRemoved(RPObject object, RPObject changes,
            boolean user) {
        try {
            if (object != null && changes != null) {
                LOG.log(Level.FINE, "Object({0}) modified in client", object.getID());
                LOG.log(Level.FINE, "Original({0}) modified in client", object);

                fixContainers(object);
                fixContainers(changes);
                fireChangedRemoved(object, changes, user);
                object.applyDifferences(null, changes);

                LOG.log(Level.FINE, "Modified({0}) modified in client", object);
                LOG.log(Level.FINE, "Changes({0}) modified in client", changes);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "dispatchModifyRemoved failed, object is " + object
                    + ", changes is " + changes, e);
        }
    }

    /**
     * Dump an object out in an easily readable format. TEMP!! TEST METHOD -
     * USED FOR DEBUGING.
     *
     * Probably should be in a common util class if useful long term.
     *
     * @param object to be dumped
     */
    public static void dumpObject(RPObject object) {
        StringBuilder sbuf = new StringBuilder();

        sbuf.append(object.getRPClass().getName());
        sbuf.append('[');
        buildIDPath(sbuf, object);
        sbuf.append(']');

        LOG.info(sbuf.toString());

        for (String name : object) {
            LOG.log(Level.INFO, "  {0}: {1}",
                    new Object[]{name, object.get(name)});
        }
    }

    /**
     *
     * @param sbuf
     * @param object
     */
    protected static void buildIDPath(final StringBuilder sbuf,
            final RPObject object) {
        RPSlot slot = object.getContainerSlot();

        if (slot != null) {
            buildIDPath(sbuf, object.getContainer());
            sbuf.append(':');
            sbuf.append(slot.getName());
            sbuf.append(':');
        }

        sbuf.append(object.getID().getObjectID());
    }

    /**
     * Fix parent <-> child linkage. on creation.
     *
     * @param object whose slots shall be fixed.
     */
    protected void fixContainers(final RPObject object) {
        object.slots().forEach((slot) -> {
            for (RPObject sobject : slot) {
                if (!sobject.isContained()) {
                    LOG.log(Level.FINE, "Fixing container: {0}", slot);
                    sobject.setContainer(object, slot);
                }

                fixContainers(sobject);
            }
        });
    }

    /**
     * Notify listeners that an object was added.
     *
     * @param object The object.
     * @param user If this is the private user object.
     */
    protected void fireAdded(RPObject object, boolean user) {

        /*
         * Call before children have been notified
         */
        listener.onAdded(object);

        if (user) {
            userListeners.forEach((userListener) -> {
                userListener.onAdded(object);
            });
        }

        /*
         * Walk each slot
         */
        object.slots().forEach((slot) -> {
            String slotName = slot.getName();

            for (RPObject sobject : slot) {
                fireAdded(object, slotName, sobject, user);
            }
        });
    }

    /**
     * Notify listeners that a slot object was added.
     *
     * @param object The parent object.
     * @param slotName The slot name.
     * @param sobject The slot object.
     * @param user If this is the private user object.
     */
    protected void fireAdded(final RPObject object, final String slotName,
            final RPObject sobject, final boolean user) {
        /*
         * Notify child
         */
        fireAdded(sobject, user);

        /*
         * Call after the child has been notified
         */
        listener.onSlotAdded(object, slotName, sobject);

        if (user) {
            userListeners.forEach((userListener) -> {
                userListener.onSlotAdded(object, slotName, sobject);
            });
        }
    }

    /**
     * Notify listeners that an object added/changed attribute(s). This will
     * cascade down slot trees.
     *
     * @param object The base object.
     * @param changes The changes.
     * @param user If this is the private user object.
     */
    protected void fireChangedAdded(RPObject object, RPObject changes,
            boolean user) {

        /*
         * Walk each slot
         */
        changes.slots().stream().filter((cslot)
                -> (cslot.size() != 0)).forEachOrdered((cslot) -> {
            fireChangedAdded(object, cslot, user);
        });

        /*
         * Call after children have been notified
         */
        listener.onChangedAdded(object, changes);

        if (user) {
            userListeners.forEach((userListener) -> {
                userListener.onChangedAdded(object, changes);
            });
        }
    }

    /**
     * Notify listeners that an object slot added/changed attribute(s). This
     * will cascade down object trees.
     *
     * @param object The base object.
     * @param cslot The changes slot.
     * @param user If this is the private user object.
     */
    protected void fireChangedAdded(RPObject object, RPSlot cslot, boolean user) {
        String slotName = cslot.getName();
        RPSlot slot;

        /*
         * Find the original slot entry (if any)
         */
        if (object.hasSlot(slotName)) {
            slot = object.getSlot(slotName);
        } else {
            slot = null;
        }

        /*
         * Walk the changes
         */
        for (RPObject schanges : cslot) {
            RPObject.ID id = schanges.getID();

            if ((slot != null) && slot.has(id)) {
                RPObject sobject = slot.get(id);

                listener.onSlotChangedAdded(object, slotName, sobject, schanges);

                if (user) {
                    userListeners.forEach((userListener) -> {
                        userListener.onSlotChangedAdded(object, slotName, sobject,
                                schanges);
                    });
                }

                fireChangedAdded(sobject, schanges, user);
            } else {
                if (!schanges.isContained()) {
                    LOG.log(Level.WARNING, "!!! Not contained! - {0}", schanges);
                }

                fireAdded(object, slotName, schanges, user);
            }
        }
    }

    /**
     * Notify listeners that an object removed attribute(s). This will cascade
     * down slot trees.
     *
     * @param object The base object.
     * @param changes The changes.
     * @param user If this is the private user object.
     */
    protected void fireChangedRemoved(RPObject object, RPObject changes,
            boolean user) {

        /*
         * Call before children have been notified
         */
        listener.onChangedRemoved(object, changes);

        if (user) {
            userListeners.forEach((userListener) -> {
                userListener.onChangedRemoved(object, changes);
            });
        }

        /*
         * Walk each slot
         */
        changes.slots().stream().filter((cslot)
                -> (cslot.size() != 0)).forEachOrdered((cslot) -> {
            fireChangedRemoved(object, cslot, user);
        });
    }

    /**
     * Notify listeners that an object slot removed attribute(s). This will
     * cascade down object trees.
     *
     * @param object The base object.
     * @param cslot The changes slot.
     * @param user If this is the private user object.
     */
    protected void fireChangedRemoved(RPObject object, RPSlot cslot,
            boolean user) {
        String slotName = cslot.getName();

        /*
         * Find the original slot entry
         */
        RPSlot slot = object.getSlot(slotName);

        /*
         * Walk the changes
         */
        for (RPObject schanges : cslot) {
            RPObject sobject = slot.get(schanges.getID());

            if (sobject == null) {
                LOG.log(Level.FINE, "Unable to find existing: {0}", schanges);
                continue;
            }

            /*
             * Remove attrs vs. object [see applyDifferences()]
             */
            if (schanges.size() > 1) {
                listener.onSlotChangedRemoved(object, slotName, sobject,
                        schanges);

                if (user) {
                    userListeners.forEach((userListener) -> {
                        userListener.onSlotChangedRemoved(object, slotName,
                                sobject, schanges);
                    });
                }

                fireChangedRemoved(sobject, schanges, user);
            } else {
                fireRemoved(object, slotName, sobject, user);
            }
        }
    }

    /**
     * Notify listeners that an object was removed.
     *
     * @param object The object.
     * @param user If this is the private user object.
     */
    protected void fireRemoved(RPObject object, boolean user) {

        /*
         * Walk each slot
         */
        object.slots().forEach((slot) -> {
            String slotName = slot.getName();

            for (RPObject sobject : slot) {
                fireRemoved(object, slotName, sobject, user);
            }
        });

        /*
         * Call after children have been notified
         */
        listener.onRemoved(object);

        if (user) {
            userListeners.forEach((userListener) -> {
                userListener.onRemoved(object);
            });
        }
    }

    /**
     * Notify listeners that a slot object was removed.
     *
     * @param object The container object.
     * @param slotName The slot name.
     * @param sobject The slot object.
     * @param user If this is the private user object.
     */
    protected void fireRemoved(final RPObject object, final String slotName,
            final RPObject sobject, final boolean user) {
        /*
         * Call before the child is notified
         */
        listener.onSlotRemoved(object, slotName, sobject);

        if (user) {
            userListeners.forEach((userListener) -> {
                userListener.onSlotRemoved(object, slotName, sobject);
            });
        }

        /*
         * Notify child
         */
        fireRemoved(sobject, user);
    }

    public boolean addRPObjectChangeListener(RPObjectChangeListener listener) {
        return userListeners.add(listener);
    }

    public boolean removeRPObjectChangeListener(RPObjectChangeListener listener) {
        return userListeners.remove(listener);
    }
}
