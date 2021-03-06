package simple.server.core.action;

import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

public interface ActionInterface {

    /**
     * Callback for the registered action
     *
     * @param player
     * @param action
     */
    void onAction(RPObject player, RPAction action);
}
