package simple.server.extension.card.storage;

import simple.server.extension.card.ICardSet;

public interface IStorage<T> extends ICardSet<T>{

    public boolean isAutoCommit();

    public void setAutoCommit(boolean value);

    /**
     * Save syncs memory cached data with physical media (from mem to physical).
     * Save would called automatically after each data editing operation unless
     * autoCommit is off.
     */
    public void save();

    public boolean isNeedToBeSaved();

    /**
     * Initiate a save command. It will result is actual save if auto-commit is
     * on. If it is off saving should be postponed.
     */
    public void autoSave();

    /**
     * Load syncs memory cashed data with physical media (from physical to mem).
     * Load would called automatically upon first data access if has not been
     * loaded yet.
     */
    public void load();

    public boolean isLoaded();

    public String getName();

    public String getComment();

    public boolean isVirtual();
}