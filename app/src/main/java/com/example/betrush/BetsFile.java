package com.example.betrush;

import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class BetsFile extends AppCompatActivity {
    private static final String FILE_NAME = "/BetRush/bets.json";
    private static final File betsFile = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
    private ArrayList<Bet> bets;
    private JSONObject betsFileContent;
    private String lastId = "0";

    public BetsFile() throws IOException, JSONException{
        bets = new ArrayList<>();
        betsFileContent = new JSONObject();
        loadFile();
        bets = getObjects();
        setLastId();
    }

    public void createBet(int value, String forecast, int bettor, String matchWinner) throws IOException, JSONException{
        lastId = ""+(Integer.valueOf(lastId) + 1);
        SimpleDateFormat format =
                new SimpleDateFormat("dd/MM/YYYY H:m", new Locale("es", "CO"));
        String date = format.format(new Date());
        Bet newBet = new Bet(lastId, date, value, forecast, bettor, matchWinner);
        addMatch(newBet);
    }

    private void addMatch(Bet bet) throws IOException, JSONException{
        JSONObject betJSON = new JSONObject();
        betJSON.put("id", bet.id);
        betJSON.put("date", bet.date);
        betJSON.put("value", bet.value);
        betJSON.put("forecast", bet.forecast);
        betJSON.put("bettor", bet.bettor);
        betJSON.put("matchWinner", bet.matchWinner);
        betsFileContent.put(bet.id+"", betJSON);
        bets.add(bet);
        saveFile();
    }

    public void removeBet(String betId) throws IOException{
        betsFileContent.remove(getBet(betId).id+"");
        bets.remove(getBet(betId));
        setLastId();
        saveFile();
    }

    public ArrayList<String> getBetsIds(){
        ArrayList<String> betsIds = new ArrayList<>();
        for (Bet bet : bets){
            if (bet.bettor == MainActivity.insertedId){
                betsIds.add(bet.id);
            }
        }
        return betsIds;
    }

    public ArrayList<String> getWonBetsDates(){
        ArrayList<String> wonBetsDates = new ArrayList<>();
        for (Bet bet : bets){
            if (bet.hasWon() && bet.bettor == MainActivity.insertedId){
                wonBetsDates.add(bet.date);
            }
        }
        return wonBetsDates;
    }

    public String getSummary(){
        String summary = "ACIERTOS TOTALES\t\t" + countVictories() + "\n\n";
        summary += "FRACASOS TOTALES\t\t" + countLosses() + "\n\n";
        summary += "GANANCIAS TOTALES\t\t" + totalBetsLosses() + "\n\n";
        summary += "PÉRDIDAS TOTALES\t\t" + totalBetsProfit();
        return summary;
    }

    public ArrayList<String> getLostBetsDates(){
        ArrayList<String> lostBetsDates = new ArrayList<>();
        for (Bet bet : bets){
            if (!bet.hasWon() && bet.bettor == MainActivity.insertedId){
                lostBetsDates.add(bet.date);
            }
        }
        return lostBetsDates;
    }

    public ArrayList<String> getBetsDates(){
        ArrayList<String> betsDates = new ArrayList<>();
        for (Bet bet : bets){
            if (bet.bettor == MainActivity.insertedId) {
                betsDates.add(bet.date);
            }
        }
        return betsDates;
    }

    public Bet getBet(String betId){
        for (Bet bet : bets){
            if (bet.id.equals(betId)){
                return bet;
            }
        }
        throw new InvalidParameterException("Error: no existe una apuesta con esa Id");
    }

    private int countVictories(){
        int totalVictories = 0;
        for(Bet bet : bets){
            if (bet.hasWon() && bet.bettor == MainActivity.insertedId){
                totalVictories ++;
            }
        }
        return totalVictories;
    }

    private int countLosses(){
        int totalLosses = 0;
        for(Bet bet : bets){
            if (!bet.hasWon() && bet.bettor == MainActivity.insertedId){
                totalLosses ++;
            }
        }
        return totalLosses;
    }

    private int totalBetsProfit(){
        int totalProfits = 0;
        for(Bet bet : bets){
            if (!bet.hasWon() && bet.bettor == MainActivity.insertedId){
                totalProfits += bet.value;
            }
        }
        return totalProfits;
    }

    private int totalBetsLosses(){
        int totalLosses = 0;
        for(Bet bet : bets){
            if (bet.hasWon() && bet.bettor == MainActivity.insertedId){
                totalLosses += bet.value;
            }
        }
        return totalLosses;
    }

    private void setLastId() {
        for (Bet bet : bets){
            int lastIdAsInt = Integer.valueOf(lastId);
            if (lastIdAsInt <= Integer.valueOf(bet.id)){
                lastId = bet.id;
            }
        }
    }

    private ArrayList<Bet> getObjects() throws JSONException {
        ArrayList<Bet> bets = new ArrayList<>();
        Iterator<String> betsFileKeys = betsFileContent.keys();
        while (betsFileKeys.hasNext()){
            String id = betsFileKeys.next();
            JSONObject betJSON =  betsFileContent.getJSONObject(id);
            String date = betJSON.getString("date");
            int value = betJSON.getInt("value");
            String forecast = betJSON.getString("forecast");
            int bettor = betJSON.getInt("bettor");
            String matchWinner = betJSON.getString("matchWinner");
            Bet bet = new Bet(id, date, value, forecast, bettor, matchWinner);
            bets.add(bet);
        }
        return bets;
    }

    private void saveFile() throws IOException {
        if (!betsFile.exists()){
            new File(Environment.getExternalStorageDirectory() + "/BetRush/").mkdirs();
            betsFile.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(betsFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(betsFileContent.toString());
        bufferedWriter.close();
    }

    private void loadFile() throws JSONException, IOException {
        if (betsFile.exists()){
            StringBuilder output = new StringBuilder();
            FileReader fileReader = new FileReader(betsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null){
                output.append(line);
            }
            betsFileContent = new JSONObject(output.toString());
            bufferedReader.close();
        }
    }
}
