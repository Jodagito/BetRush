package com.example.betrush;

import android.content.res.Resources;
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
import java.util.ArrayList;
import java.util.Iterator;

public class MatchesFile extends AppCompatActivity{
    private static final String FILE_NAME = "/BetRush/matches.json";
    private final File matchesFile = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
    private String lastId = "0";
    private ArrayList<Match> matches;
    private JSONObject matchesFileContent;

    public MatchesFile() throws IOException, JSONException{
        matches = new ArrayList<>();
        matchesFileContent = new JSONObject();
        loadFile();
        matches = getObjects();
        setLastId();
    }

    public void createMatch(String[] teams, String date) throws InvalidParameterException, IOException, JSONException{
        if(getMatchByDate(date) != null){
            throw new InvalidParameterException("Error: ya existe un partido en esta fecha");
        }
        lastId = ""+(Integer.valueOf(lastId) + 1);
        Match newMatch = new Match(lastId, teams, date);
        addMatch(newMatch);
    }

    private void addMatch(Match match) throws IOException, JSONException{
        JSONObject matchJSON = new JSONObject();
        matchJSON.put("id", match.id);
        matchJSON.put("date", match.date);
        matchJSON.put("local_team", match.teams[0]);
        matchJSON.put("visitant_team", match.teams[1]);
        matchJSON.put("played", match.played);
        matchesFileContent.put(match.id+"", matchJSON);
        matches.add(match);
        saveFile();
    }

    public void playMatch(Match match) throws IOException, JSONException{
        match.playMatch();
        JSONObject matchJSON = new JSONObject();
        matchJSON.put("id", match.id);
        matchJSON.put("date", match.date);
        matchJSON.put("local_team", match.teams[0]);
        matchJSON.put("visitant_team", match.teams[1]);
        matchJSON.put("played", match.played);
        matchJSON.put("local_result", match.results[0]);
        matchJSON.put("visitant_result", match.results[1]);
        matchJSON.put("winner", match.winner);
        matchesFileContent.put(match.id+"", matchJSON);
        matches.add(match);
        saveFile();

    }

    public void removeMatch(String id) throws IOException{
        matchesFileContent.remove(getMatch(id).id+"");
        matches.remove(getMatch(id));
        setLastId();
        saveFile();
    }

    public String getTeamStatistics(String name){
        int totalMatches = getPlayedMatchesByTeam(name).size();
        int totalVictories = getWinMatchesDates(name).size();
        int totalLosses = getDefeatMatchesDates(name).size();
        String teamStatistics = "PARTIDOS JUGADOS\t\t" + totalMatches + "\n\n";
        teamStatistics += "PARTIDOS GANADOS\t\t" + totalVictories + "\n\n";
        teamStatistics += "PARTIDOS PERDIDOS\t\t" + totalLosses;
        return teamStatistics;
    }

    public ArrayList<String> getWinMatchesDates(String localTeam){
        ArrayList<String> matchesFilteredByWins = new ArrayList<>();
        for (Match match : matches){
            if (match.played && localTeam.equals(match.winner)){
                matchesFilteredByWins.add(match.date);
            }
        }
        return matchesFilteredByWins;
    }

    public ArrayList<String> getDefeatMatchesDates(String localTeam){
        ArrayList<String> matchesFilteredByDefeats = new ArrayList<>();
        for (Match match : matches){
            if (match.played && !localTeam.equals(match.winner) && localTeam.equals(match.teams[0])){
                matchesFilteredByDefeats.add(match.date);
            }
        }
        return matchesFilteredByDefeats;
    }

    public ArrayList<Match> getPlayedMatchesByTeam(String team){
        ArrayList<Match> matchesFilteredByTeam = new ArrayList<>();
        for (Match match : matches){
            if (match.played && match.teams[0].equals(team) || match.teams[1].equals(team)){
                matchesFilteredByTeam.add(match);
            }
        }
        return matchesFilteredByTeam;
    }

    public ArrayList<Match> getMatchesByTeam(String team){
        ArrayList<Match> matchesFilteredByTeam = new ArrayList<>();
        for (Match match : matches){
            if (match.teams[0].equals(team) || match.teams[1].equals(team)){
                matchesFilteredByTeam.add(match);
            }
        }
        return matchesFilteredByTeam;
    }

    public ArrayList<String> getMatchesDates(String team){
        ArrayList<String> matchesDates = new ArrayList<>();
        for (Match match : getMatchesByTeam(team)){
            matchesDates.add(match.date);
        }
        return matchesDates;
    }

    public Match getMatchByDate(String date){
        Match matchFilteredByDate = null;
        for (Match match : matches){
            if (match.date.equals(date)){
                matchFilteredByDate = match;
            }
        }
        return matchFilteredByDate;
    }

    private Match getMatch(String id){
        for (Match match : matches){
            if (match.id.equals(id)){
                return match;
            }
        }
        throw new Resources.NotFoundException("Error: no se encontró un partido con esa ID");
    }

    private void setLastId() {
        for (Match match : matches){
            int lastIdAsInt = Integer.valueOf(lastId);
            if (lastIdAsInt <= Integer.valueOf(match.id)){
                lastId = match.id;
            }
        }
    }

    private ArrayList<Match> getObjects() throws JSONException {
        ArrayList<Match> matches = new ArrayList<>();
        Iterator<String> matchesFileKeys = matchesFileContent.keys();
        while (matchesFileKeys.hasNext()){
            String id = matchesFileKeys.next();
            JSONObject matchJSON =  matchesFileContent.getJSONObject(id);
            String date = matchJSON.getString("date");
            String[] teams = new String[]{matchJSON.getString("local_team"), matchJSON.getString("visitant_team")};
            boolean played = matchJSON.getBoolean("played");
            Match match = new Match(id, teams, date);
            if (played){
                int[] results = new int[]{matchJSON.getInt("local_result"), matchJSON.getInt("visitant_result")};
                String winner = matchJSON.getString("winner");
                match = new Match(id, date, teams, results, winner, true);
            }
            matches.add(match);
        }
        return matches;
    }

    private void saveFile() throws IOException {
        if (!matchesFile.exists()){
            new File(Environment.getExternalStorageDirectory() + "/BetRush/").mkdirs();
            matchesFile.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(matchesFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(matchesFileContent.toString());
        bufferedWriter.close();
    }

    private void loadFile() throws JSONException, IOException {
        if (matchesFile.exists()){
            StringBuilder output = new StringBuilder();
            FileReader fileReader = new FileReader(matchesFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null){
                output.append(line);
            }
            matchesFileContent = new JSONObject(output.toString());
            bufferedReader.close();
        }
    }
}
