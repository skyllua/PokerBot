package com.skyll.dev;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Table {
    private boolean isCreated;
    private boolean isStarted;
    private boolean isFinished;

    public Message titleMessage;
    public Message callPlayerMsg;
    public Player whoAreStarter;
    public long id;
    public List<Player> players;
    public List<Bank> banks;
    public Deck deck;
    public int minBet;
    public int nowMaxBet;
    public int countAgreedPlayers = 0;
    public int nowPlayerTurn = 0;
    public ArrayList<Card> flop;
    public Card turn;
    public Card river;

    public Table() {
        this.players = new ArrayList<Player>();
        this.banks = new ArrayList<Bank>();
        id = (long)(Math.random()*Long.MAX_VALUE);
        deck = new Deck();
        deck.shuffle();
        minBet = 100;
        isCreated = false;
        isStarted = false;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public Table(long id) {
        this.players = new ArrayList<Player>();
        this.banks = new ArrayList<Bank>();
        flop = new ArrayList<Card>();
        this.id = id;
        deck = new Deck();
        deck.shuffle();
        minBet = 100;
        isCreated = false;
        isStarted = false;
        isFinished = false;
    }

    public void dealCards() {
        for (int i = 0; i < 2; i++) {
            for (Player player : players) {
                player.setCard(deck.getCard());
            }
        }
    }

    public Player getPlayer(long id) {
        Player player = null;

        for (Player pl : players) {
            if (pl.getId() == id) {
                player = pl;
                break;
            }
        }

        return player;
    }

    public Player getNextPlayerTurn() {
        Player player = null;
        if (nowPlayerTurn + 1  < players.size()) {
            player = players.get(++nowPlayerTurn);
        } else {
            nowPlayerTurn = -1;
            player = getNextPlayerTurn();
        }

        return player;
    }

    public Player getNowPlayerTurn() {
        Player player = null;
        player = players.get(nowPlayerTurn);

        return player;
    }

    public boolean removePlayer(long id) {
        boolean isRemoved = false;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId() == id) {
                players.remove(i);
                isRemoved = true;
                break;
            }
        }

        return isRemoved;
    }

    public String getPlayersNames(){
        String playersNames = "";
        for (Player pl : players) {
            playersNames += "@" + pl.getName() + "\n";
        }

        return playersNames;
    }

    public Table clear() {
       return new Table(id);
    }

    public void shufflePlayers() {
        Collections.shuffle(players);
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public boolean isCreated() {
        return isCreated;
    }

    public void setCreated(boolean created) {
        isCreated = created;
    }

    public ArrayList<Card> getCardsOnTable() {
        ArrayList<Card> cards = new ArrayList<Card>(flop);
        cards.add(turn);
        cards.add(river);

        return cards;
    }

}
