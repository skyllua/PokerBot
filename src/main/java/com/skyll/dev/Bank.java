package com.skyll.dev;

import java.util.ArrayList;
import java.util.List;

public class Bank {
    private int chips;
    private List<Player> players;

    public Bank(int chips, List<Player> players) {
        this.chips = chips;
        this.players = players;
    }

    public int getChips() {
        return chips;
    }

    public void setChips(int chips) {
        this.chips = chips;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void removePlayer(long id) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId() == id) {
                players.remove(i);
                break;
            }
        }

    }
}
