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

public class BettorsFile extends AppCompatActivity{
    private static final String FILE_NAME = "/BetRush/bettors.json";
    private final File bettorsFile = new File(Environment.getExternalStorageDirectory() + FILE_NAME);
    private JSONObject bettorsFileContent;
    private ArrayList<Bettor> bettors;

    public BettorsFile() throws IOException, JSONException{
        bettors = new ArrayList<>();
        bettorsFileContent = new JSONObject();
        loadFile();
        bettors = getObjects();
    }

    public void createBettor(int id, String name) throws JSONException{
        Bettor newBettor = new Bettor(id, name);
        addBettor(newBettor);
    }

    public Boolean bettorExists(int id){
        for (Bettor bettor : bettors){
            if (bettor.id == id){
                return true;
            }
        }
        return false;
    }

    public Bettor getBettor(int id){
        for (Bettor bettor : bettors){
            if (bettor.id == id){
                return bettor;
            }
        }
        throw new Resources.NotFoundException("Error: no se encontró un apostador con esa identificación");
    }

    private void addBettor(Bettor bettor) throws JSONException{
        bettorsFileContent.put(bettor.id+"", bettor.name);
        try{
            saveFile();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private ArrayList<Bettor> getObjects() throws JSONException {
        Iterator<String> bettorsFileKeys = bettorsFileContent.keys();
        while (bettorsFileKeys.hasNext()) {
            int id = Integer.valueOf(bettorsFileKeys.next());
            String name = bettorsFileContent.getString(id+"");
            Bettor bettor = new Bettor(id, name);
            bettors.add(bettor);
        }
        return bettors;
    }

    private void saveFile() throws IOException {
        if (!bettorsFile.exists()){
            new File(Environment.getExternalStorageDirectory() + "/BetRush/").mkdirs();
            bettorsFile.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(bettorsFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(bettorsFileContent.toString());
        bufferedWriter.close();
    }

    private void loadFile() throws JSONException, IOException {
        if (bettorsFile.exists()){
            StringBuilder output = new StringBuilder();
            FileReader fileReader = new FileReader(bettorsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null){
                output.append(line);
            }
            bettorsFileContent = new JSONObject(output.toString());
            bufferedReader.close();
        }
    }
}
