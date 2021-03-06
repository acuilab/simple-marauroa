package com.reflexit.magiccards.core.storage.database;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Lookup;

import com.reflexit.magiccards.core.model.DefaultCardGame;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardCollection;
import com.reflexit.magiccards.core.model.IGame;
import com.reflexit.magiccards.core.model.IGameCellRendererImageFactory;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class DefaultCardGameTest
{
  private static final Logger LOG
          = Logger.getLogger(DefaultCardGameTest.class.getName());
  private static Game game;
  private static final List<String> ATTRIBS = new ArrayList<String>();
  private static final List<String> COLLECTION_TYPES =
          new ArrayList<String>();
  private static final Map<String, String> COLLECTIONS = 
          new HashMap<String, String>();

  @BeforeClass
  public static void setUpClass() throws Exception
  {
    Lookup.getDefault().lookup(IDataBaseCardStorage.class)
            .setPU("Card_Game_Interface_TestPU");
    Lookup.getDefault().lookup(IDataBaseCardStorage.class).initialize();
  }

  public DefaultCardGameTest()
  {
  }

  /**
   * Test the database.
   */
  @Test
  public void testDatabase()
  {
    try
    {
      Lookup.getDefault().lookup(IDataBaseCardStorage.class).initialize();
    }
    catch (DBException ex)
    {
      LOG.log(Level.SEVERE, null, ex);
      fail();
    }
  }

  public static class DefaultCardGameImpl extends DefaultCardGame
          implements DataBaseStateListener
  {
    static
    {
      ATTRIBS.add("rarity");
      ATTRIBS.add("creature");

      ATTRIBS.add("power");
      ATTRIBS.add("toughness");

      COLLECTION_TYPES.add("Deck");
      COLLECTION_TYPES.add("Collection");

      COLLECTIONS.put("Collection", "My Pages");
    }

    @Override
    public String getName()
    {
      return "Test Game";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialized()
    {
      try
      {
        HashMap parameters = new HashMap();
        DefaultCardGame instance = new DefaultCardGameImpl();
        parameters.put("name", instance.getName());
        List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .namedQuery("Game.findByName", parameters);
        assertTrue(result.isEmpty());
        super.initialized();
        parameters.put("name", getName());
        result = Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .namedQuery("Game.findByName", parameters);
        game = (Game) result.get(0);
        System.out.println("Check types");
        parameters.put("name", "rarity");
        assertFalse(Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .namedQuery("CardAttribute.findByName", parameters).isEmpty());
        parameters.put("name", "creature");
        assertFalse(Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .namedQuery("CardAttribute.findByName", parameters).isEmpty());
        System.out.println("Check the attributes");
        for (String rarity : ATTRIBS)
        {
          parameters.put("name", rarity);
          assertFalse(Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                  .namedQuery("CardAttribute.findByName", parameters).isEmpty());
        }
        System.out.println("Create set");
        CardSet cs1 = (CardSet) Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .createCardSet((IGame) game, "Test Set", "TS", new Date());
        assertTrue(cs1 != null);
        CardSet cs2 = (CardSet) Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .createCardSet((IGame) game, "Test Set2", "TS2", new Date());
        assertTrue(cs2 != null);
        System.out.println("Create card type");
        CardType sampleType = (CardType) Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .createCardType("sample");
        System.out.println("Create a card");
        Card card = (Card) Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .createCard(sampleType, "Sample", "Sample body text".getBytes(), cs1);
        Card card2 = (Card) Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .createCard(sampleType, "Sample2", "Sample body text".getBytes(), cs2);
        parameters.put("name", card.getName());
        result = Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .namedQuery("Card.findByName", parameters);
        assertFalse(result.isEmpty());
        Card temp = (Card) result.get(0);
        assertTrue(temp.getName().equals(card.getName()));
        assertTrue(Arrays.equals(temp.getText(), card.getText()));
        System.out.println("Add an attribute to a card");
        for (String rarity : ATTRIBS)
        {
          parameters.put("name", rarity);
          result = Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                  .namedQuery("CardAttribute.findByName", parameters);
          assertFalse(result.isEmpty());
          Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                  .addAttributeToCard((ICard) card,
                  rarity, "test");
        }
        System.out.println("Check attributes");
        for (CardHasCardAttribute chca : card.getCardHasCardAttributeList())
        {
          assertTrue(chca.getValue().equals("test"));
        }
        System.out.println("Add card to set");
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.add(card);
        Lookup.getDefault().lookup(IDataBaseCardStorage.class).addCardsToSet(cards, cs1);
        assertTrue(cs1.getCardList().size() == cards.size());
        cards.add(card2);
        Lookup.getDefault().lookup(IDataBaseCardStorage.class).addCardsToSet(cards, cs2);
        assertTrue(cs2.getCardList().size() == cards.size());
        System.out.println("Print Sets");
        String set1 = Lookup.getDefault().lookup(IDataBaseCardStorage.class).printCardsInSet(cs1);
        String set2 = Lookup.getDefault().lookup(IDataBaseCardStorage.class).printCardsInSet(cs2);
        assertFalse(set1.isEmpty());
        assertFalse(set2.isEmpty());
        assertTrue(set1.length() < set2.length());
        System.out.println(set1);
        System.out.println(set2);
        System.out.println("Check Collection Type");
        for (String type : COLLECTION_TYPES)
        {
          parameters.put("name", type);
          assertFalse(Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                  .namedQuery("CardCollectionType.findByName", parameters).isEmpty());
        }
        System.out.println("Check Collections");
        for (Map.Entry<String, String> entry : COLLECTIONS.entrySet())
        {
          parameters.put("name", entry.getValue());
          assertFalse(Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                  .namedQuery("CardCollection.findByName", parameters).isEmpty());
        }
        System.out.println("Adding pages to collection");
        parameters.put("name", "My Pages");
        CardCollection collection = (CardCollection) Lookup.getDefault()
                .lookup(IDataBaseCardStorage.class)
                .namedQuery("CardCollection.findByName", parameters).get(0);
        HashMap<Card, Integer> addToCollection = new HashMap<Card, Integer>();
        int totalPages = 3, toRemove = 1;
        addToCollection.put(temp, totalPages);
        assertTrue(collection.getCardCollectionHasCardList().isEmpty());
        collection = (CardCollection) Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .addCardsToCollection(addToCollection, (ICardCollection) collection);
        assertTrue(collection.getCardCollectionHasCardList().size() == 1);
        assertTrue(collection.getCardCollectionHasCardList().get(0).getAmount() == totalPages);
        System.out.println("Removing pages from collection");
        addToCollection.put(temp, toRemove);
        collection = (CardCollection) Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .removeCardsFromCollection(addToCollection, (ICardCollection) collection);
        assertTrue(collection.getCardCollectionHasCardList().get(0).getAmount() == (totalPages - toRemove));
        System.out.println("Print collection");
        String collectionList = Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .printCardsCollection((ICardCollection) collection);
        assertFalse(collectionList.isEmpty());
        System.out.println(collectionList);
      }
      catch (DBException ex)
      {
        LOG.log(Level.SEVERE, null, ex);
        fail();
      }
    }

    @Override
    public Runnable getUpdateRunnable()
    {
      //Do nothing
      return null;
    }

    @Override
    public Image getBackCardIcon()
    {
      return null;
    }

    @Override
    public IGameCellRendererImageFactory getCellRendererImageFactory()
    {
      return null;
    }
  }
}
