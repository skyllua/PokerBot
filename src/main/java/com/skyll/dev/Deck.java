package com.skyll.dev;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards = new ArrayList<Card>();
    private int index;

    public Deck() {
        index = 0;
        for (int i = 2; i < 15; i++) {
            for (int j = 0; j < 4; j++) {
                String value = "";
                String suit = "";

                switch (j) {
                    case 0: suit = "C";
                        break;
                    case 1: suit = "H";
                        break;
                    case 2: suit = "S";
                        break;
                    case 3: suit = "D";
                        break;
                }
                switch (i) {
                    case 11: value = "J";
                        break;
                    case 12: value = "Q";
                        break;
                    case 13: value = "K";
                        break;
                    case 14: value = "A";
                        break;
                    default: value = String.valueOf(i);
                }
                cards.add(new Card(value, suit));
            }
        }
    }

    public void shuffle() {
        int count = (int) (Math.round(Math.random()%5 * 10) + 1);
        for (int i = 0; i < count; i++) Collections.shuffle(cards);
    }

    public Card getCard() {
        if (index < 52) return cards.get(index++);

        return null;
    }

    public void dropCard() {
        index++;
    }

    public int getCountCards() {
        return 52 - index;
    }

    public List<Card> getCards() {
        return cards;
    }

}
