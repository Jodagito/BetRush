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
import java.util.ArrayList;
import java.util.Iterator;

public class TeamsFile extends AppCompatActivity{
    private static final String FILE_NAME = "/BetRush/teams.json";
    private final File teamsFile = new File(Environment.getExternalStorageDirectory() + FILE_NAME);
    private ArrayList<Team> teams;
    private JSONObject teamsFileContent;

    public TeamsFile() throws IOException, JSONException{
        teams = new ArrayList<>();
        teamsFileContent = new JSONObject();
        loadFile();
        teams = getObjects();
    }

    public String removeTeam(String name) throws IOException, JSONException{
        String teamToRemove = getTeam(name).name;
        teamsFileContent.remove(teamToRemove);
        saveFile();
        teams = getObjects();
        return teamToRemove;
    }

    public Team getTeam(String name){
        String cleanedName = cleanString(name);
        for (Team team : teams){
            String cleanedTeamName = cleanString(team.name);
            if (cleanedName.equals(cleanedTeamName)){
                return team;
            }
        }
        throw new Resources.NotFoundException("Error: no se encontró un equipo con ese nombre");
    }

    public ArrayList<String> getTeamsNames(){
        ArrayList<String> teamsNames = new ArrayList<>();
        for (Team team : teams){
            teamsNames.add(team.name);
        }
        return teamsNames;
    }

    public void createTeam(String name) throws JSONException, IOException{
        Team newTeam = new Team(name);
        addTeam(newTeam);
    }

    public Boolean teamExists(String name){
        String cleanedName = cleanString(name);
        for (Team team : teams){
            String cleanedTeamName = cleanString(team.name);
            if (cleanedTeamName.equals(cleanedName)){
                return true;
            }
        }
        return false;
    }

    private String cleanString(String string) {
        string = (string.toLowerCase().replace(" ", "")
                .replace("á", "a").replace("é", "e")
                .replace("í", "i").replace("ó", "o")
                .replace("ú", "u"));
        return string;
    }

    private void addTeam(Team team) throws JSONException, IOException{
        teamsFileContent.put(team.name, team.name);
        teams.add(team);
        saveFile();
    }

    private ArrayList<Team> getObjects() throws JSONException {
        ArrayList<Team> teams = new ArrayList<>();
        Iterator<String> teamsFileKeys = teamsFileContent.keys();
        while (teamsFileKeys.hasNext()) {
            String name = teamsFileContent.getString(teamsFileKeys.next());
            Team team = new Team(name);
            teams.add(team);
        }
        return teams;
    }

    private void saveFile() throws IOException {
        if (!teamsFile.exists()){
            new File(Environment.getExternalStorageDirectory() + "/BetRush/").mkdirs();
            teamsFile.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(teamsFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(teamsFileContent.toString());
        bufferedWriter.close();
    }

    private void loadFile() throws JSONException, IOException {
        if (teamsFile.exists()){
            StringBuilder output = new StringBuilder();
            FileReader fileReader = new FileReader(teamsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null){
                output.append(line);
            }
            teamsFileContent = new JSONObject(output.toString());
            bufferedReader.close();
        }
    }
}
