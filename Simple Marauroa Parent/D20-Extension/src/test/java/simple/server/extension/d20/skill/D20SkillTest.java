package simple.server.extension.d20.skill;

import marauroa.common.game.Definition;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import simple.server.core.entity.Entity;
import simple.server.core.entity.RPEntity;
import simple.server.core.entity.RPEntityInterface;
import simple.server.extension.d20.DummyAbility;
import simple.server.extension.d20.DummyAbility2;
import simple.server.extension.d20.DummySkill;
import simple.server.extension.d20.ability.AbstractAbility;
import simple.server.extension.d20.ability.D20Ability;

/**
 *
 * @author Javier A. Ortiz Bultron javier.ortiz.78@gmail.com
 */
public class D20SkillTest {

    public D20SkillTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        new Entity().generateRPClass();
        new RPEntity().generateRPClass();
        Lookup.getDefault().lookupAll(D20Skill.class).stream().forEach((skill) -> {
            ((RPEntityInterface) skill).generateRPClass();
        });
    }

    /**
     * Test of isModifiesAttribute method, of class iD20Ability.
     */
    @Test
    public void testIsModifiesAttribute() {
        System.out.println("isModifiesAttribute");
        DummySkill dq = new DummySkill();
        assertTrue(dq.isModifiesAttribute(DummyAbility.class));
        assertTrue(dq.isModifiesAttribute(DummyAbility2.class));
        assertFalse(dq.isModifiesAttribute(iD20AttributeImpl.class));
    }

    /**
     * Test of getModifier method, of class iD20Ability.
     */
    @Test
    public void testGetModifier() {
        System.out.println("getModifier");
        DummySkill dq = new DummySkill();
        assertEquals(dq.getModifier(iD20AttributeImpl.class), 0);
        assertEquals(dq.getModifier(DummyAbility.class), 1);
        assertTrue(dq.getModifier(DummyAbility2.class) > 1);
    }

    /**
     * Test of getAbility method, of class D20Skill.
     */
    @Test
    public void testGetAbility() {
        System.out.println("getAbility");
        DummySkill ds = new DummySkill();
        assertEquals(DummyAbility.class, ds.getAbility());
    }

    /**
     * Test of Rank methods, of class D20Skill.
     */
    @Test
    public void testRank() {
        System.out.println("Rank");
        DummySkill ds = new DummySkill();
        ds.update();
        assertTrue(0.0 == ds.getRank());
        ds.setRank(1.0);
        assertTrue(1.0 == ds.getRank());
    }

    @ServiceProvider(service = D20Ability.class)
    public static class iD20AttributeImpl extends AbstractAbility {

        @Override
        public String getCharacteristicName() {
            return "Invalid";
        }

        @Override
        public String getShortName() {
            return "IV";
        }

        @Override
        public String getDescription() {
            return "Invalid Description.";
        }

        @Override
        public Definition.Type getDefinitionType() {
            return Definition.Type.INT;
        }
    }
}
