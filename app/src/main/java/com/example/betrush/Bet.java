package com.example.betrush;

public class Bet {
    public final int value;
    public final String id;
    public final String date;
    public final String forecast;
    public final int bettor;
    public final String matchWinner;

    public Bet(String id, String date, int value, String forecast, int bettor, String matchWinner){
        this.id = id;
        this.date = date;
        this.value = value;
        this.forecast = forecast;
        this.bettor = bettor;
        this.matchWinner = matchWinner;
    }

    public Boolean hasWon(){
        return matchWinner.equals(forecast);
    }
}
