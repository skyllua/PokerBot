package com.skyll.dev;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Evaluation {
    private static final String[] values = new String[] {"A?", "K?", "Q?", "J?", "10?", "9?", "8?", "7?", "6?", "5?", "4?", "3?", "2?"};
    private static final String[] values_simple = new String[] {"A", "K", "Q", "J", "10", "9", "8", "7", "6", "5", "4", "3", "2"};
    private static final String[] suits = new String[] {"?S", "?C", "?H", "?D"};
    private static final String[] suits_simple = new String[] {"S", "C", "H", "D"};

    public static ArrayList<Card> getRoyalFlush(ArrayList<Card> table, ArrayList<Card> player) {
        ArrayList<Card> matchup = new ArrayList<Card>();
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.addAll(table);
        cards.addAll(player);
        ArrayList<String[]> pairs = new ArrayList<String[]>();

        for (String suit : suits_simple) {
            pairs.add(new String[]{values_simple[0] + suit, values_simple[1] + suit, values_simple[2] + suit, values_simple[3] + suit, values_simple[4] + suit});
        }

        for (String[] card : pairs) {
            boolean isStraight = false;
            for (int i = 0; i < card.length; i++) {
                boolean isFind = false;
                for (int j = 0; j < cards.size(); j++) {
                    if (cards.get(j).equals(card[i])) {
                        matchup.add(cards.get(j));
                        isFind = true;
                        if (matchup.size() == 5) {
                            isStraight = true;
                        }
                        break;
                    }
                }

                if (!isFind) {
                    matchup.clear();
                    break;
                } else if (isStraight) break;

            }
            if (isStraight) break;
        }


        return matchup;
    }

    public static ArrayList<Card> getStraightFlush(ArrayList<Card> table, ArrayList<Card> player) {
        ArrayList<Card> matchup = new ArrayList<Card>();
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.addAll(table);
        cards.addAll(player);
        ArrayList<String[]> pairs = new ArrayList<String[]>();

        for (int i = 0; i < values_simple.length; i++) {
            for (String suit : suits_simple) {
                if (values_simple[i].equals("5")) {
                    pairs.add(new String[]{values_simple[i] + suit, values_simple[i + 1] + suit, values_simple[i + 2] + suit, values_simple[i + 3] + suit, values_simple[0] + suit});
                } else
                    pairs.add(new String[]{values_simple[i] + suit, values_simple[i + 1] + suit, values_simple[i + 2] + suit, values_simple[i + 3] + suit, values_simple[i + 4] + suit});
            }
            if (values_simple[i].equals("5"))
                break;
        }

        for (String[] card : pairs) {
            boolean isStraight = false;
            for (int i = 0; i < card.length; i++) {
                boolean isFind = false;
                for (int j = 0; j < cards.size(); j++) {
                    if (cards.get(j).equals(card[i])) {
                        matchup.add(cards.get(j));
                        isFind = true;
                        if (matchup.size() == 5) {
                            isStraight = true;
                        }
                        break;
                    }
                }

                if (!isFind) {
                    matchup.clear();
                    break;
                } else if (isStraight) break;

            }
            if (isStraight) break;
        }


        return matchup;
    }

    public static ArrayList<Card> getFourOfAKind(ArrayList<Card> table, ArrayList<Card> player) {
        ArrayList<Card> matchup = new ArrayList<Card>();
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.addAll(table);
        cards.addAll(player);

        for (String value : values) {
            for (Card card : cards) {
                if (card.equals(value)) {
                    matchup.add(card);
                }
            }
            if (matchup.size() == 4) break;
            else matchup.clear();
        }

        // add Highest cards
        if (matchup.size() > 0) {
            for (Card highCard : getHighestCards(table, player)) {
                if (!matchup.contains(highCard)) {
                    matchup.add(highCard);
                }
                if (matchup.size() == 5)
                    break;
            }
        } else matchup.clear();

        return matchup;
    }

    public static ArrayList<Card> getFullHouse(ArrayList<Card> table, ArrayList<Card> player) {
        ArrayList<Card> matchup = new ArrayList<Card>();
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.addAll(table);
        cards.addAll(player);
        ArrayList<Card> threeOfAKind = getThreeOfAKind(table, player);

        if (threeOfAKind.size() > 0) {
            for (Card card1 : cards) {
                for (Card card2 : cards) {
                    if (card1.getValue().equals(card2.getValue()) && !card1.equals(card2) && !card1.getValue().equals(threeOfAKind.get(0).getValue())) {
                        for (int i = 0; i < 3; i++) matchup.add(threeOfAKind.get(i));
                        matchup.add(card1);
                        matchup.add(card2);
                        break;
                    }
                }
                if (matchup.size() == 5) break;
            }
        }

        return matchup;
    }

    public static ArrayList<Card> getFlush(ArrayList<Card> table, ArrayList<Card> player) {
        ArrayList<Card> matchup = new ArrayList<Card>();
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.addAll(table);
        cards.addAll(player);

        for (String suit : suits) {
            for (Card card : cards) {
                if (card.equals(suit)) {
                    matchup.add(card);
                }
            }
            if (matchup.size() < 5)
                matchup.clear();
            else break;
        }

        ArrayList<Card> list = new ArrayList<Card>(matchup);
        matchup.clear();
        for (String value : values) {
            for (Card card : list) {
                if (card.equals(value)) matchup.add(card);
            }
        }


        return matchup;
    }

    public static ArrayList<Card> getStraight(ArrayList<Card> table, ArrayList<Card> player) {
        ArrayList<Card> matchup = new ArrayList<Card>();
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.addAll(table);
        cards.addAll(player);
        ArrayList<String[]> pairs = new ArrayList<String[]>();

        for (int i = 0; i < values.length; i++) {
            if (values[i].equals("5?")) {
                pairs.add(new String[] {values[i], values[i+1], values[i+2], values[i+3], values[0]});
                break;
            }
            pairs.add(new String[] {values[i], values[i+1], values[i+2], values[i+3], values[i+4]});
        }

        for (String[] card : pairs) {
            boolean isStraight = false;
            for (int i = 0; i < card.length; i++) {
                boolean isFind = false;
                for (int j = 0; j < cards.size(); j++) {
                    if (cards.get(j).equals(card[i])) {
                        matchup.add(cards.get(j));
                        isFind = true;
                        if (matchup.size() == 5) {
                            isStraight = true;
                        }
                        break;
                    }
                }

                if (!isFind) {
                    matchup.clear();
                    break;
                } else if (isStraight) break;

            }
            if (isStraight) break;
        }


        return matchup;
    }

    public static ArrayList<Card> getThreeOfAKind(ArrayList<Card> table, ArrayList<Card> player) {
        ArrayList<Card> matchup = new ArrayList<Card>();
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.addAll(table);
        cards.addAll(player);

        for (String value : values) {
            for (Card card : cards) {
                if (card.equals(value)) {
                    matchup.add(card);
                }
            }
            if (matchup.size() == 3) break;
            else matchup.clear();
        }

        // add Highest cards
        if (matchup.size() > 0) {
            for (Card highCard : getHighestCards(table, player)) {
                if (!matchup.contains(highCard)) {
                    matchup.add(highCard);
                }
                if (matchup.size() == 5)
                    break;
            }
        } else matchup.clear();

        return matchup;
    }

    public static ArrayList<Card> getTwoPair(ArrayList<Card> table, ArrayList<Card> player) {
        ArrayList<Card> matchup = new ArrayList<Card>();
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.addAll(table);
        cards.addAll(player);

        for (String value1 : values) {
            for (String value2 : values) {
                for (Card card_i : cards) {
                    for (Card card_j : cards) {
                        if ((card_i.equals(value1) && card_j.equals(value1)) && !card_i.equals(card_j) && !value1.equals(value2) && !matchup.contains(card_i) && !matchup.contains(card_j)) {
                            matchup.add(card_i);
                            matchup.add(card_j);
                            break;
                        }
                    }
                    if (matchup.size() > 2) break;
                }
                if (matchup.size() > 2) break;
            }
            if (matchup.size() > 2) break;
        }

        // add Highest cards
        if (matchup.size() > 2) {
            for (Card highCard : getHighestCards(table, player)) {
                if (!matchup.contains(highCard)) {
                    matchup.add(highCard);
                }
                if (matchup.size() == 5)
                    break;
            }
        } else matchup.clear();

        return matchup;
    }

    public static ArrayList<Card> getPair(ArrayList<Card> table, ArrayList<Card> player) {
        ArrayList<Card> matchup = new ArrayList<Card>();
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.addAll(table);
        cards.addAll(player);

        for (String value : values) {
            for (Card card : cards) {
                if (card.equals(value)) {
                    matchup.add(card);
                }
            }
            if (matchup.size() == 2) break;
            else matchup.clear();
        }

        // add Highest cards
        if (matchup.size() > 0) {
            for (Card highCard : getHighestCards(table, player)) {
                if (!matchup.contains(highCard)) {
                    matchup.add(highCard);
                }
                if (matchup.size() == 5)
                    break;
            }
        } else matchup.clear();

        return matchup;
    }

    public static ArrayList<Card> getHighestCards(ArrayList<Card> table, ArrayList<Card> player) {
        ArrayList<Card> matchup = new ArrayList<Card>();
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.addAll(table);
        cards.addAll(player);


        for (String value : values) {
            for (Card card : cards) {
                if (card.equals(value) && matchup.size() < 5) {
                    matchup.add(card);
                }
            }
        }

        return matchup;
    }
}
