package simple.server.extension.d20.alignment;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultron javier.ortiz.78@gmail.com
 */
@ServiceProvider(service = D20Alignment.class)
public class Lawful_Evil extends AbstractAlignment {

    @Override
    public String getShortName() {
        return "LE";
    }

    @Override
    public String getDescription() {
        return "A lawful evil villain methodically takes what he wants within "
                + "the limits of his code of conduct without regard for whom "
                + "it hurts. He cares about tradition, loyalty, and order but"
                + " not about freedom, dignity, or life. He plays by the rules "
                + "but without mercy or compassion. He is comfortable in a "
                + "hierarchy and would like to rule, but is willing to serve. "
                + "He condemns others not according to their actions but "
                + "according to race, religion, homeland, or social rank. "
                + "He is loath to break laws or promises.\n"
                + "\n"
                + "This reluctance comes partly from his nature and partly "
                + "because he depends on order to protect himself from those "
                + "who oppose him on moral grounds. Some lawful evil villains "
                + "have particular taboos, such as not killing in cold blood "
                + "(but having underlings do it) or not letting children come "
                + "to harm (if it can be helped). They imagine that these "
                + "compunctions put them above unprincipled villains.\n"
                + "\n"
                + "Some lawful evil people and creatures commit themselves to "
                + "evil with a zeal like that of a crusader committed to good. "
                + "Beyond being willing to hurt others for their own ends, "
                + "they take pleasure in spreading evil as an end unto itself. "
                + "They may also see doing evil as part of a duty to an evil "
                + "deity or master.\n"
                + "\n"
                + "Lawful evil is sometimes called \"diabolical,\" because "
                + "devils are the epitome of lawful evil.\n"
                + "\n"
                + "Lawful evil is the most dangerous alignment because it "
                + "represents methodical, intentional, and frequently "
                + "successful evil.";
    }
}
