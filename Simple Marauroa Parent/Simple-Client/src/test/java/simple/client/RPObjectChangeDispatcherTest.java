package simple.client;

import java.util.logging.Level;
import java.util.logging.Logger;
import marauroa.common.game.RPObject;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Javier A. Ortiz Bultron<javier.ortiz.78@gmail.com>
 */
public class RPObjectChangeDispatcherTest {

    private static final Logger LOG
            = Logger.getLogger(RPObjectChangeDispatcherTest.class.getSimpleName());

    @Test
    public void testDispatchModifyRemoved() {
        final RPObjectChangeListener listener = new RPObjectChangeListener() {

            @Override
            public void onAdded(final RPObject object) {
            }

            @Override
            public void onChangedAdded(final RPObject object,
                    final RPObject changes) {
            }

            @Override
            public void onChangedRemoved(final RPObject object,
                    final RPObject changes) {
            }

            @Override
            public void onRemoved(final RPObject object) {
            }

            @Override
            public void onSlotAdded(final RPObject object,
                    final String slotName, final RPObject sobject) {
            }

            @Override
            public void onSlotChangedAdded(final RPObject object,
                    final String slotName, final RPObject sobject,
                    final RPObject schanges) {
            }

            @Override
            public void onSlotChangedRemoved(final RPObject object,
                    final String slotName, final RPObject sobject,
                    final RPObject schanges) {
            }

            @Override
            public void onSlotRemoved(final RPObject object,
                    final String slotName, final RPObject sobject) {
            }

            @Override
            public RPObject onRPEvent(RPObject object) {
                return null;
            }
        };
        try {
            final RPObjectChangeDispatcher dispatcher
                    = new RPObjectChangeDispatcher(listener, listener);
            dispatcher.dispatchModifyRemoved(null, null, false);
            dispatcher.dispatchModifyRemoved(null, null, true);
            assertTrue("make sure we have no NPE", true);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
            fail();
        }
    }
}