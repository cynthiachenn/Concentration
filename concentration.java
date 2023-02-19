import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// represents a playing card
class Card {
  int rank;
  String suit; // Suit must be one of the following: "♣" "♦" "♥" "♠"
  boolean whichFace; // false is down, true is up
  Posn posn;
  boolean found; // has the pair of this card been found?
  boolean isRed;

  Card(int rank, String suit) {
    this.rank = rank;
    this.suit = suit;
    this.whichFace = false;
    this.found = false;
  }

  // converts the card's rank to a string value
  String rankToString() {
    if (this.rank == 1) {
      return "Ace";
    }
    if (this.rank == 11) {
      return "Jack";
    }
    if (this.rank == 12) {
      return "Queen";
    }
    if (this.rank == 13) {
      return "King";
    }
    else {
      return Integer.toString(this.rank);
    }
  }

  // Draws the correct color based on the suit ("♥" and "♦" are red, "♣" and "♠"
  // are black)
  public WorldImage drawSuit() {
    if (this.suit.equals("♦") || this.suit.equals("♥")) {
      return new TextImage(this.suit, 15, Color.red);
    }
    else {
      return new TextImage(this.suit, 15, Color.black);
    }
  }

  // Sets the color of the card (red is hearts and diamonds, black is spades and
  // clubs)
  void setRed() {
    if (this.suit.equals("♦") || this.suit.equals("♥")) {
      this.isRed = true;
    }
    else {
      this.isRed = false;
    }
  }

  // Assigns the card a position on the game board
  void assignValue(int i) {
    this.posn = new Posn((i % 13 * 50) + 50, (i / 13 * 100) + 100);
  }

  // Draws the card face down
  public WorldImage drawFaceDown() {
    return new RectangleImage(40, 60, "solid", Color.black);
  }

  // Draws the card face up
  public WorldImage drawFaceUp() {
    return new OverlayImage(new RectangleImage(40, 60, "outline", Color.black),
        new OverlayOffsetImage(new TextImage(this.rankToString(), 10, Color.black), 10.0, -30.0,
            this.drawSuit()));
  }

  // Erases the card from the game board by erasing it
  public WorldImage drawFound() {
    return new RectangleImage(42, 62, "solid", Color.white);
  }

  // Draws the card
  public WorldImage draw() {
    if (this.found) {
      return this.drawFound();
    }
    else if (this.whichFace) {
      return this.drawFaceUp();
    }
    else {
      return this.drawFaceDown();
    }
  }

  // Flips the card
  void flip() {
    this.whichFace = !whichFace;
  }

  // Checks if this Posn is within the bounds of a card
  boolean onCard(Posn posn) {
    return (posn.x >= this.posn.x - 20 && posn.x <= this.posn.x + 20 && posn.y >= this.posn.y - 30
        && posn.y <= this.posn.y + 30);
  }

  // Checks if the two cards are a match in rank
  boolean cardMatch(Card other) {
    if (this.rank == other.rank && this.suit == other.suit) {
      return false;
    }
    else {
      return (this.rank == other.rank && this.isRed == other.isRed);
    }
  }
}

// Utilities for Card class
class CardUtils {
  ArrayList<String> suits = new ArrayList<String>(Arrays.asList("♣", "♦", "♥", "♠"));
  ArrayList<Integer> ranks = new ArrayList<Integer>(
      Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13));
  ArrayList<Card> allCards = new ArrayList<Card>();

  // builds deck of cards
  ArrayList<Card> makeCards() {
    for (int s = 0; s < 4; s++) {
      for (int r = 0; r < 13; r++) {
        allCards.add(new Card(ranks.get(r), suits.get(s)));
      }
    }
    for (int i = 0; i < 52; i++) {
      allCards.get(i).assignValue(i);
      allCards.get(i).setRed();
    }
    return allCards;
  }

  // Shuffles deck of cards
  ArrayList<Card> shuffle(ArrayList<Card> original) {
    ArrayList<Card> randDeck = new ArrayList<Card>();
    Random rand = new Random();
    while (original.size() > 0) {
      int randIndex = rand.nextInt(original.size());
      randDeck.add(original.remove(randIndex));
    }
    for (int i = 0; i < 52; i++) {
      randDeck.get(i).assignValue(i);
      randDeck.get(i).setRed();
    }
    return randDeck;
  }
}

// To represent the concentration game world
class ConcentrationGame extends World {
  ArrayList<Card> cardDeck;
  ArrayList<Card> facingUp;
  int score;

  ConcentrationGame(ArrayList<Card> cardDeck) {
    this.cardDeck = cardDeck;
    this.score = 26;
    this.facingUp = new ArrayList<Card>();
  }

  // Draws the world scene
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(700, 500);
    for (int i = 0; i < 52; i++) {
      scene.placeImageXY(cardDeck.get(i).draw(), cardDeck.get(i).posn.x, cardDeck.get(i).posn.y);
    }
    scene.placeImageXY(new TextImage("Score: " + Integer.toString(score), 20, Color.black), 600,
        50);
    return scene;
  }

  // Mouse event key handler
  public void onMouseClicked(Posn posn, String buttonName) {
    for (int i = 0; i < 52; i++) {
      if (cardDeck.get(i).onCard(posn) && buttonName.equals("LeftButton")
          && cardDeck.get(i).found) {
        return;
      }
      else if (cardDeck.get(i).onCard(posn) && buttonName.equals("LeftButton")) {
        cardDeck.get(i).flip();
        this.facingUp.add(cardDeck.get(i));
        this.match();
      }
    }
  }

  // Key event handler : pressing r resets the game
  public void onKeyEvent(String ke) {
    if (ke.equals("r")) {
      this.cardDeck = new CardUtils().shuffle(cardDeck);
      this.score = 26;
      for (int i = 0; i < 52; i++) {
        cardDeck.get(i).whichFace = false;
      }
    }
  }

  // Removes cards if there is a match, flips cards back down if it is not a match
  void match() {
    if (this.facingUp.size() == 3) {
      if (this.facingUp.get(0).cardMatch(this.facingUp.get(1))) {
        this.score = this.score - 1;
        this.facingUp.get(0).found = true;
        this.facingUp.get(1).found = true;
        this.facingUp.remove(1);
        this.facingUp.remove(0);
        if (this.score == 0) {
          this.endOfWorld("You win!");
        }
      }
      else {
        this.facingUp.get(0).flip();
        this.facingUp.get(1).flip();
        this.facingUp.remove(1);
        this.facingUp.remove(0);
      }
    }
  }

  // Last winning scene shown to player
  public WorldScene lastScene(String msg) {
    WorldScene scene = new WorldScene(700, 500);
    scene.placeImageXY(new TextImage(msg, 50, Color.green), 350, 250);
    return scene;
  }
}

class ExampleConcentrationGame {
  ArrayList<Card> allCards;
  ConcentrationGame game1;

  void initCards() {
    allCards = new CardUtils().makeCards();
    game1 = new ConcentrationGame(allCards);
  }

  void testGame(Tester t) {
    ArrayList<Card> cards = new CardUtils().makeCards();
    ArrayList<Card> shuffledDeck = new CardUtils().shuffle(cards);
    ConcentrationGame cg = new ConcentrationGame(shuffledDeck);
    cg.bigBang(700, 500);
  }

  void testMakeCards(Tester t) {
    initCards();
    t.checkExpect(this.allCards.size(), 52);
    t.checkExpect(new CardUtils().makeCards().size(), 52);
  }

  void testRankToString(Tester t) {
    initCards();
    t.checkExpect(new Card(1, "♣").rankToString(), "Ace");
    t.checkExpect(new Card(2, "♦").rankToString(), "2");
    t.checkExpect(new Card(11, "♦").rankToString(), "Jack");
    t.checkExpect(new Card(12, "♦").rankToString(), "Queen");
    t.checkExpect(new Card(13, "♦").rankToString(), "King");
  }

  void testDrawSuit(Tester t) {
    initCards();
    t.checkExpect(new Card(2, "♣").drawSuit(), new TextImage("♣", 15, Color.black));
    t.checkExpect(new Card(2, "♦").drawSuit(), new TextImage("♦", 15, Color.red));
    t.checkExpect(new Card(2, "♥").drawSuit(), new TextImage("♥", 15, Color.red));
    t.checkExpect(new Card(2, "♠").drawSuit(), new TextImage("♠", 15, Color.black));
  }

  void testAssignValue(Tester t) {
    initCards();
    Card ex1 = new Card(1, "♣");
    t.checkExpect(ex1.posn, null);
    ex1.assignValue(0);
    t.checkExpect(ex1.posn, new Posn(50, 100));
    ex1.assignValue(5);
    t.checkExpect(ex1.posn, new Posn(300, 100));
    ex1.assignValue(13);
    t.checkExpect(ex1.posn, new Posn(50, 200));
    ex1.assignValue(52);
    t.checkExpect(ex1.posn, new Posn(50, 500));

  }

  void testDrawFaceUp(Tester t) {
    initCards();
    t.checkExpect(new Card(2, "♣").drawFaceUp(),
        new OverlayImage(new RectangleImage(40, 60, "outline", Color.black),
            new OverlayOffsetImage(new TextImage("2", 10, Color.black), 10.0, -30.0,
                new TextImage("♣", 15, Color.black))));
    t.checkExpect(new Card(1, "♦").drawFaceUp(),
        new OverlayImage(new RectangleImage(40, 60, "outline", Color.black),
            new OverlayOffsetImage(new TextImage("Ace", 10, Color.black), 10.0, -30.0,
                new TextImage("♦", 15, Color.red))));
    t.checkExpect(new Card(11, "♥").drawFaceUp(),
        new OverlayImage(new RectangleImage(40, 60, "outline", Color.black),
            new OverlayOffsetImage(new TextImage("Jack", 10, Color.black), 10.0, -30.0,
                new TextImage("♥", 15, Color.red))));
    t.checkExpect(new Card(12, "♠").drawFaceUp(),
        new OverlayImage(new RectangleImage(40, 60, "outline", Color.black),
            new OverlayOffsetImage(new TextImage("Queen", 10, Color.black), 10.0, -30.0,
                new TextImage("♠", 15, Color.black))));
    t.checkExpect(new Card(13, "♠").drawFaceUp(),
        new OverlayImage(new RectangleImage(40, 60, "outline", Color.black),
            new OverlayOffsetImage(new TextImage("King", 10, Color.black), 10.0, -30.0,
                new TextImage("♠", 15, Color.black))));
  }

  void testDrawFaceDown(Tester t) {
    initCards();
    t.checkExpect(new Card(2, "♣").drawFaceDown(),
        new RectangleImage(40, 60, "solid", Color.black));
    t.checkExpect(new Card(1, "♥").drawFaceDown(),
        new RectangleImage(40, 60, "solid", Color.black));
    t.checkExpect(new Card(11, "♦").drawFaceDown(),
        new RectangleImage(40, 60, "solid", Color.black));
    t.checkExpect(new Card(12, "♠").drawFaceDown(),
        new RectangleImage(40, 60, "solid", Color.black));
    t.checkExpect(new Card(13, "♣").drawFaceDown(),
        new RectangleImage(40, 60, "solid", Color.black));
  }

  void testFlip(Tester t) {
    initCards();
    t.checkExpect(this.allCards.get(0).whichFace, false);
    this.allCards.get(0).flip();
    t.checkExpect(this.allCards.get(0).whichFace, true);
    this.allCards.get(0).flip();
    t.checkExpect(this.allCards.get(0).whichFace, false);
  }

  void testDraw(Tester t) {
    initCards();
    t.checkExpect(this.allCards.get(0).draw(), this.allCards.get(0).drawFaceDown());
    t.checkExpect(this.allCards.get(14).draw(), this.allCards.get(4).drawFaceDown());
    this.allCards.get(0).flip();
    this.allCards.get(14).flip();
    t.checkExpect(this.allCards.get(0).draw(), this.allCards.get(0).drawFaceUp());
    t.checkExpect(this.allCards.get(14).draw(), this.allCards.get(14).drawFaceUp());
    Card testCard = new Card(1, "♣");
    t.checkExpect(testCard.draw(), testCard.drawFaceDown());
    testCard.whichFace = true;
    t.checkExpect(testCard.draw(), testCard.drawFaceUp());
    testCard.found = true;
    t.checkExpect(testCard.draw(), testCard.drawFound());
  }

  void testShuffle(Tester t) {
    initCards();
    t.checkExpect(new CardUtils().shuffle(this.allCards).size(), 52);
  }

  void testOnCard(Tester t) {
    initCards();
    t.checkExpect(this.allCards.get(0).onCard(new Posn(0, 0)), false);
    t.checkExpect(this.allCards.get(0).onCard(new Posn(50, 100)), true);
    t.checkExpect(this.allCards.get(0).onCard(new Posn(29, 100)), false);
    t.checkExpect(this.allCards.get(0).onCard(new Posn(71, 100)), false);
    t.checkExpect(this.allCards.get(0).onCard(new Posn(50, 69)), false);
    t.checkExpect(this.allCards.get(0).onCard(new Posn(50, 131)), false);
    t.checkExpect(this.allCards.get(0).onCard(new Posn(70, 130)), true);
    t.checkExpect(this.allCards.get(0).onCard(new Posn(30, 70)), true);
    t.checkExpect(this.allCards.get(51).onCard(new Posn(650, 400)), true);
    t.checkExpect(this.allCards.get(51).onCard(new Posn(670, 430)), true);
    t.checkExpect(this.allCards.get(51).onCard(new Posn(630, 370)), true);
    t.checkExpect(this.allCards.get(51).onCard(new Posn(671, 400)), false);
    t.checkExpect(this.allCards.get(51).onCard(new Posn(629, 400)), false);
    t.checkExpect(this.allCards.get(51).onCard(new Posn(650, 431)), false);
    t.checkExpect(this.allCards.get(51).onCard(new Posn(650, 369)), false);
  }

  void testOnMouseClicked(Tester t) {
    initCards();
    t.checkExpect(allCards.get(0).whichFace, false);
    game1.onMouseClicked(new Posn(0, 0), "LeftButton");
    t.checkExpect(allCards.get(0).whichFace, false);
    game1.onMouseClicked(new Posn(50, 100), "RightButton");
    t.checkExpect(allCards.get(0).whichFace, false);
    game1.onMouseClicked(new Posn(50, 100), "LeftButton");
    t.checkExpect(allCards.get(0).whichFace, true);
  }

  void testOnKeyEvent(Tester t) {
    initCards();
    t.checkExpect(this.game1.cardDeck.get(0).whichFace, false);
    game1.onKeyEvent("space");
    game1.onMouseClicked(new Posn(50, 100), "LeftButton");
    t.checkExpect(this.game1.cardDeck.get(0).whichFace, true);
    game1.onKeyEvent("r");
    t.checkExpect(this.game1.cardDeck.get(0).whichFace, false);
    t.checkExpect(game1.score, 26);
  }

  void testCardMatch(Tester t) {
    initCards();
    t.checkExpect(this.allCards.get(0).cardMatch(this.allCards.get(1)), false);
    t.checkExpect(this.allCards.get(0).cardMatch(this.allCards.get(0)), false);
    t.checkExpect(this.allCards.get(0).cardMatch(this.allCards.get(13)), false);
    t.checkExpect(this.allCards.get(0).cardMatch(this.allCards.get(26)), false);
    t.checkExpect(this.allCards.get(0).cardMatch(this.allCards.get(39)), true);
    t.checkExpect(this.allCards.get(13).cardMatch(this.allCards.get(26)), true);
    t.checkExpect(this.allCards.get(1).cardMatch(this.allCards.get(13)), false);
    t.checkExpect(this.allCards.get(1).cardMatch(this.allCards.get(14)), false);
    t.checkExpect(this.allCards.get(14).cardMatch(this.allCards.get(27)), true);
    t.checkExpect(this.allCards.get(1).cardMatch(this.allCards.get(40)), true);
  }

  void testDrawFound(Tester t) {
    initCards();
    Card testCard = new Card(1, "♣");
    testCard.found = true;
    t.checkExpect(testCard.drawFound(), new RectangleImage(42, 62, "solid", Color.white));
    testCard.rank = 13;
    t.checkExpect(testCard.drawFound(), new RectangleImage(42, 62, "solid", Color.white));
    testCard.suit = "♥";
    t.checkExpect(testCard.drawFound(), new RectangleImage(42, 62, "solid", Color.white));
  }

  void testMatch(Tester t) {
    initCards();

    game1.facingUp.add(allCards.get(0));
    game1.facingUp.add(allCards.get(1));
    game1.facingUp.add(allCards.get(13));
    t.checkExpect(game1.facingUp.get(0).found, false);
    t.checkExpect(game1.facingUp.get(1).found, false);
    t.checkExpect(game1.facingUp.get(2).found, false);
    game1.match();
    t.checkExpect(game1.facingUp.get(0).found, false);
    game1.facingUp.add(allCards.get(26));
    game1.facingUp.add(allCards.get(39));
    game1.match();
    t.checkExpect(game1.cardDeck.get(13).found, true);
    t.checkExpect(game1.cardDeck.get(26).found, true);
    t.checkExpect(game1.cardDeck.get(39).found, false);
  }

  void testLastScene(Tester t) {
    initCards();
    WorldScene scene = new WorldScene(700, 500);
    scene.placeImageXY(new TextImage("You win!", 50, Color.green), 350, 250);
    t.checkExpect(game1.lastScene("You win!"), scene);
  }

}