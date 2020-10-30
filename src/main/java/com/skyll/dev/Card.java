package com.skyll.dev;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Card {
    private static final String[] values_simple = new String[] {"A", "K", "Q", "J", "10", "9", "8", "7", "6", "5", "4", "3", "2"};
    private String value;
    private String suit;

    public Card(String value, String suit) {
        this.value = value;
        this.suit = suit;
    }
    public Card(String vs) {
        if (vs.length() == 3) {
            value = vs.substring(0, 2);
            suit = vs.substring(2);
        } else if (vs.length() == 2) {
            value = vs.substring(0, 1);
            suit = vs.substring(1);
        }
    }

    public String getValue() {
        return value;
    }

    public String getSuit() {
        return suit;
    }

    public BufferedImage getPic() {
        try {
            System.out.println(Methods.getResourcesPath());
            System.out.println(new File(Methods.getCardsPath() + value + suit + ".png").exists());
            System.out.println(Methods.getCardsPath() + value + suit +".png");
            return ImageIO.read(new File(Methods.getCardsPath() + value + suit +".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String view() {

        return "|" + value + (!value.equals("10") ? " ": "") + suitToString() + "|";
    }

    private String suitToString() {
        switch (suit) {
            case "H": return "♥";
            case "D": return "♦";
            case "C": return "♣";
            case "S": return "♠";
        }

        return null;
    }

    @Override
    public String toString() {
        return value + suit;
    }

    public boolean equals(Card card) {
        boolean result = false;

        if ((value.equals(card.getValue()) || card.getValue().equals("?")) && (suit.equals(card.getSuit()) || card.getSuit().equals("?")))
            result = true;

        return result;
    }

    public boolean equals(String vs) {
        Card tmp = null;
        if (vs.length() == 2)
            tmp = new Card(vs.substring(0, 1), vs.substring(1));
        if (vs.length() == 3)
            tmp = new Card(vs.substring(0, 2), vs.substring(2));

        return equals(tmp);
    }

    public static int getValueRank(String val) {
        int rank = 0;

        for (int i = 0; i < values_simple.length; i++) {
            if (val.equals(values_simple[i])) {
                rank = i;
                break;
            }
        }

        return rank;
    }

    public  int getValueRank() {
        int rank = 0;

        for (int i = 0; i < values_simple.length; i++) {
            if (value.equals(values_simple[i])) {
                rank = i;
                break;
            }
        }

        return rank;
    }
}
