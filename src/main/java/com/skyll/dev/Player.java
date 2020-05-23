package com.skyll.dev;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Player {
    private int chips;
    private String name;
    private long id;
    private ArrayList<Card> hand;
    private String status;
    private Message msgToClearHands;
    private int bet;
    private ArrayList<Card> winningCombination;

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public Message getMsgToClearHands() {
        return msgToClearHands;
    }

    public void setMsgToClearHands(Message msgToClearHands) {
        this.msgToClearHands = msgToClearHands;
    }

    public Player(long id, String name, int chips) {
        this.id = id;
        this.name = name;
        this.chips = chips;
        hand = new ArrayList<Card>();
        status = "";
        bet = 0;
    }

    public void setCard(Card card) {
        hand.add(card);
    }

    public void fold() {
        hand.clear();
        status = "fold";
    }

    public long getId() {
        return id;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public int getChips(int count) {
        if (chips - count >= 0) {
            chips = chips - count;
            return count;
        }
        return 0;
    }

    public int countChips() { return chips;}

    public void setChips(int chips) {
        this.chips = chips;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Card> getWinningCombination() {
        return winningCombination;
    }

    public void setWinningCombination(ArrayList<Card> winningCombination) {
        this.winningCombination = winningCombination;
    }

    public int getRankCombination(int numCard) {
        return winningCombination.get(numCard).getValueRank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id == player.id &&
                Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }
}
