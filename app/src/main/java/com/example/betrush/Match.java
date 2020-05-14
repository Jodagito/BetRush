package com.example.betrush;

import java.util.Random;

public class Match {
    public final String id;
    public final String date;
    public final String[] teams;
    public final int[] results;
    public String winner;
    public Boolean played;

    public Match(String id, String date, String[] teams, int[] results, String winner, Boolean played){
        this.id = id;
        this.date = date;
        this.teams = teams;
        this.results = results;
        this.winner = winner;
        this.played = played;
    }

    public Match(String id, String[] teams, String date){
        this.id = id;
        this.date = date;
        this.teams = teams;
        played = false;
        results = new int[2];
    }

    public void playMatch(){
        Random random = new Random();
        int winner = -1;
        results[0] = random.nextInt(6);
        results[1] = random.nextInt(6);
        if (results[0] > results[1]){
            winner++;
        }
        else if(results[0] < results[1]){
            winner += 2;
        }
        else if(winner < 0){
            this.winner = "Empate";
            played = true;
            return;
        }
        setWinner(winner);
        played = true;
    }

    private void setWinner(int winner){
        this.winner = teams[winner];
    }
}
