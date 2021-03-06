package simple.server.core.entity;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.SyntaxException;
import marauroa.server.game.Statistics;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import simple.server.core.entity.api.RPEventListener;
import simple.server.extension.MarauroaServerExtension;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = RPEntityInterface.class, position = 2)
public class RPEntity extends Entity {

    /**
     * The title attribute name.
     */
    public static final String ATTR_TITLE = "title";
    public static final String DEFAULT_RPCLASS = "rpentity";
    protected static final Statistics STATS = Statistics.getStatistics();
    /**
     * the logger instance.
     */
    private static final Logger LOG
            = Logger.getLogger(RPEntity.class.getSimpleName());

    @Override
    public void generateRPClass() {
        super.generateRPClass();
        try {
            if (!RPClass.hasRPClass(DEFAULT_RPCLASS)) {
                RPClass entity = new RPClass(DEFAULT_RPCLASS);
                entity.isA(Entity.class.newInstance().getRPClassName());
                entity.addAttribute(NAME, Type.STRING);
                entity.addAttribute(ATTR_TITLE, Type.STRING);
                for (MarauroaServerExtension ext
                        : Lookup.getDefault().lookupAll(MarauroaServerExtension.class)) {
                    ext.modifyRootEntityRPClassDefinition(entity);
                }
            } else if (!RPClass.hasRPClass(getRPClassName())) {
                RPClass entity = new RPClass(getRPClassName());
                entity.isA(DEFAULT_RPCLASS);
            }
        } catch (SyntaxException e) {
            LOG.log(Level.SEVERE, "Cannot generateRPClass", e);
        } catch (InstantiationException | IllegalAccessException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public RPEntity(RPObject object) {
        super(object);
    }

    public RPEntity() {
        RPCLASS_NAME = "rpentity";
    }

    public RPEntity(RPObject object, Map<String, RPEventListener> listeners) {
        super(object, listeners);
    }
}
