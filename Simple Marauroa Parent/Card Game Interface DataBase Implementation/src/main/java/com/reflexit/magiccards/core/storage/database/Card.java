/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.reflexit.magiccards.core.storage.database;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.reflexit.magiccards.core.model.CardImpl;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@Entity
@Table(name = "card")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Card.findAll", query = "SELECT c FROM Card c"),
    @NamedQuery(name = "Card.findById", query = "SELECT c FROM Card c WHERE c.cardPK.id = :id"),
    @NamedQuery(name = "Card.findByCardTypeId", query = "SELECT c FROM Card c WHERE c.cardPK.cardTypeId = :cardTypeId"),
    @NamedQuery(name = "Card.findByName", query = "SELECT c FROM Card c WHERE c.name = :name")})
public class Card extends CardImpl {

    @Basic(optional = false)
    @Lob
    @Column(name = "text", nullable = false)
    private byte[] text;
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CardPK cardPK;
    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    @ManyToMany(mappedBy = "cardList")
    private List<CardSet> cardSetList;
    @JoinColumn(name = "card_type_id", referencedColumnName = "id", 
            nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private CardType cardType;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "card")
    private List<CardCollectionHasCard> cardCollectionHasCardList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "card")
    private List<CardHasCardAttribute> cardHasCardAttributeList;
    @Basic(optional = false)
    @Column(name = "cardId", nullable = false, length = 255)
    private String id;

    public Card() {
    }

    public Card(CardPK cardPK) {
        this.cardPK = cardPK;
    }

    public Card(CardPK cardPK, String name, byte[] text) {
        this.cardPK = cardPK;
        this.name = name;
        this.text = text;
    }

    public Card(int cardTypeId, String name, byte[] text) {
        this.cardPK = new CardPK(cardTypeId);
        this.name = name;
        this.text = text;
    }

    public CardPK getCardPK() {
        return cardPK;
    }

    public void setCardPK(CardPK cardPK) {
        this.cardPK = cardPK;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public List<CardSet> getCardSetList() {
        return cardSetList;
    }

    public void setCardSetList(List<CardSet> cardSetList) {
        this.cardSetList = cardSetList;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    @XmlTransient
    public List<CardCollectionHasCard> getCardCollectionHasCardList() {
        return cardCollectionHasCardList;
    }

    public void setCardCollectionHasCardList(List<CardCollectionHasCard> cardCollectionHasCardList) {
        this.cardCollectionHasCardList = cardCollectionHasCardList;
    }

    @XmlTransient
    public List<CardHasCardAttribute> getCardHasCardAttributeList() {
        return cardHasCardAttributeList;
    }

    public void setCardHasCardAttributeList(List<CardHasCardAttribute> cardHasCardAttributeList) {
        this.cardHasCardAttributeList = cardHasCardAttributeList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cardPK != null ? cardPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Card)) {
            return false;
        }
        Card other = (Card) object;
        return !((this.cardPK == null && other.cardPK != null) 
                || (this.cardPK != null && !this.cardPK.equals(other.cardPK)));
    }

    @Override
    public String toString() {
        return "dreamer.card.game.storage.database.persistence.Card[ cardPK=" + cardPK + " ]";
    }

    public byte[] getText() {
        return text;
    }

    public void setText(byte[] text) {
        this.text = text;
    }

    @Override
    public String getCardId() {
        return getId();
    }

    @Override
    public int compareTo(Object o) {
        return equals(o) ? 0 : -1;
    }

  /**
   * @return the id
   */
  public String getId()
  {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id)
  {
    this.id = id;
  }
}
