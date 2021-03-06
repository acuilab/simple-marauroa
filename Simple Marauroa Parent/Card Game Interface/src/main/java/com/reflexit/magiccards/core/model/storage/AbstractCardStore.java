package com.reflexit.magiccards.core.model.storage;

import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.EventManager;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractCardStore<T> extends EventManager
        implements ICardStore<T> {

    protected transient boolean initialized = false;
    protected boolean mergeOnAdd = true;

    public final void initialize() {
        if (isInitialized() == false) {
            try {
                doInitialize();
            } catch (Exception ex) {
                Logger.getLogger(AbstractCardStore.class.getName())
                        .log(Level.SEVERE, null, ex);
            } finally {
                setInitialized(true);
            }
        }
    }

    protected abstract void doInitialize() throws Exception;

    @Override
    public boolean addAll(final Collection cards, ICardSet set) {
        initialize();
        boolean modified = doAddAll(cards, set);
        if (modified) {
            fireEvent(new CardEvent(this, CardEvent.ADD, cards));
        }
        return modified;
    }

    public boolean isAutoCommit() {
        IStorage<T> storage = getStorage();
        return storage.isAutoCommit();
    }

    public void setAutoCommit(boolean commit) {
        IStorage<T> storage = getStorage();
        storage.setAutoCommit(commit);
        if (commit) {
            storage.save();
        }
    }

    protected synchronized boolean doAddAll(final Collection<? extends T> col,
            ICardSet set) {
        boolean modified = false;
        boolean commit = isAutoCommit();
        setAutoCommit(false);
        try {
            for (final T element : col) {
                final T card = element;
                if (doAddCard(card, set)) {
                    modified = true;
                }
            }
        } finally {
            setAutoCommit(commit);
        }
        return modified;
    }

    @Override
    public boolean add(T card, ICardSet set) {
        initialize();
        synchronized (this) {
            if (!doAddCard(card, set)) {
                return false;
            }
        }
        fireEvent(new CardEvent(this, CardEvent.ADD, card));
        return true;
    }

    @Override
    public boolean remove(T o, ICardSet set) {
        initialize();
        boolean res;
        synchronized (this) {
            res = doRemoveCard(o, set);
        }
        if (res) {
            fireEvent(new CardEvent(this, CardEvent.REMOVE, o));
        }
        return res;
    }

    @Override
    public boolean removeAll(Collection list, ICardSet set) {
        initialize();
        boolean modified = doRemoveAll(list, set);
        if (modified) {
            fireEvent(new CardEvent(this, CardEvent.REMOVE, list));
        }
        return modified;
    }

    @Override
    public boolean removeAll(ICardSet set) {
        initialize();
        boolean modified = doRemoveAll(set);
        if (modified) {
            fireEvent(new CardEvent(this, CardEvent.REMOVE, null));
        }
        return modified;
    }

    protected boolean doRemoveAll(ICardSet set) {
        boolean modified = false;
        boolean commit = isAutoCommit();
        setAutoCommit(false);
        try {
            for (Object t : set.getCards()) {
                if (doRemoveCard((T) t, set)) {
                    modified = true;
                }
            }
            return modified;
        } finally {
            setAutoCommit(commit);
        }
    }

    protected boolean doRemoveAll(Collection<? extends T> list, ICardSet set) {
        boolean modified = false;
        boolean commit = isAutoCommit();
        setAutoCommit(false);
        try {
            for (T t : list) {
                if (doRemoveCard(t, set)) {
                    modified = true;
                }
            }
            return modified;
        } finally {
            setAutoCommit(commit);
        }
    }

    @Override
    public void setMergeOnAdd(final boolean v) {
        this.mergeOnAdd = v;
    }

    @Override
    public boolean getMergeOnAdd() {
        return this.mergeOnAdd;
    }

    public void setInitialized(boolean b) {
        synchronized (this) {
            initialized = b;
        }
    }

    public boolean isInitialized() {
        synchronized (this) {
            return initialized;
        }
    }

    protected abstract boolean doAddCard(T card, ICardSet set);

    protected abstract boolean doRemoveCard(T card, ICardSet set);

    @Override
    public void addListener(final ICardEventListener lis) {
        addListenerObject(lis);
    }

    @Override
    public void removeListener(final ICardEventListener lis) {
        removeListenerObject(lis);
    }

    protected void fireEvent(final CardEvent event) {
        final Object[] listeners = getListeners();
        for (final Object listener : listeners) {
            final ICardEventListener lis = (ICardEventListener) listener;
            try {
                lis.handleEvent(event);
            } catch (final Throwable ex) {
                Logger.getLogger(AbstractCardStore.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void update(final T card) {
        initialize();
        synchronized (this) {
            if (!doUpdate(card)) {
                return;
            }
        }
        fireEvent(new CardEvent(card, CardEvent.UPDATE, card));
    }

    protected boolean doUpdate(T card) {
        return true;
    }
}
