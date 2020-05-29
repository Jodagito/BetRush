package com.example.betrush;

import java.util.Random;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Match extends RealmObject {
    @PrimaryKey
    public int id;
    private String date;
    private RealmList<Team> teams;
    private RealmList<Integer> results;
    private String winner;
    private Boolean played = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public RealmList<Team> getTeams() {
        return teams;
    }

    public void setTeams(RealmList<Team> teams) {
        this.teams = teams;
    }

    public RealmList<Integer> getResults() {
        return results;
    }

    public void setResults(RealmList<Integer> results) {
        this.results = results;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public Boolean getPlayed() {
        return played;
    }

    public void setPlayed(Boolean played) {
        this.played = played;
    }

    private void setWinner(int winner){
        this.winner = teams.get(winner).name;
    }

    public void playMatch(){
        Random random = new Random();
        int winner = -1;
        results.add(random.nextInt(6));
        results.add(random.nextInt(6));
        if (results.get(0) > results.get(1)){
            winner++;
        }
        else if(results.get(0) < results.get(1)){
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
}
