package com.example.betrush;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class BetsPane extends AppCompatActivity {
    private ListView listViewBets;
    private BetsFile bets;
    private Toolbar betsToolbar;
    private ArrayList<String> betsIds;
    private SwipeRefreshLayout swipeRefreshBets;
    private ArrayList<String> betsDates;
    private ArrayAdapter<String> adapter;
    private Bet selectedBet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        betsIds = new ArrayList<>();
        betsDates = new ArrayList<>();
        Locale locale = new Locale("es");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_bets_pane);
        connect();
        setSupportActionBar(betsToolbar);
        getSupportActionBar().setTitle("Apuestas");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            bets = new BetsFile();
        }
        catch (JSONException e){
            Toast.makeText(this, "Ocurrió un error leyendo el archivo de apuestas", Toast.LENGTH_LONG);
        }
        catch(IOException e){
            askForPermissions();
        }
        loadBets();
        listViewBets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedBet = bets.getBet(betsIds.get(position));
                String betAsString = getBetAsString(selectedBet);
                generateBetDetailDialog(betAsString);
            }
        });
        listViewBets.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                generateDeleteConfirmationDialog(position);
                return true;
            }
        });
        swipeRefreshBets.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadBets();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bets_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuFilterWonBets) {
            swipeRefreshBets.setRefreshing(true);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bets.getWonBetsDates());
            listViewBets.setAdapter(adapter);
            swipeRefreshBets.setRefreshing(false);
        }
        else if(item.getItemId() == R.id.menuFilterLostBets){
            swipeRefreshBets.setRefreshing(true);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bets.getLostBetsDates());
            listViewBets.setAdapter(adapter);
            swipeRefreshBets.setRefreshing(false);
        }
        else if(item.getItemId() == R.id.menuGetSummary){
            generateBetsSummaryDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private String getBetAsString(Bet bet) {
        String betAsString = "PRONÓSTICO\t\t" + bet.forecast + "\n\n";
        betAsString += "VALOR\t\t" + bet.value + "\n\n";
        betAsString += "GANADOR\t\t" + bet.matchWinner.toUpperCase();
        return betAsString;
    }

    private void connect() {
        listViewBets = findViewById(R.id.listViewBets);
        betsToolbar = findViewById(R.id.betsToolbar);
        swipeRefreshBets = findViewById(R.id.swipeRefreshBets);
    }

    private void generateBetDetailDialog(String betAsString) {
        AlertDialog.Builder dialogWindow = new AlertDialog.Builder(BetsPane.this);
        dialogWindow.setMessage(betAsString)
                .setCancelable(true);
        dialogWindow.setTitle(selectedBet.date);
        dialogWindow.create();
        dialogWindow.show();
    }

    private void generateBetsSummaryDialog() {
        AlertDialog.Builder dialogWindow = new AlertDialog.Builder(BetsPane.this);
        dialogWindow.setMessage(bets.getSummary())
                .setCancelable(true);
        dialogWindow.setTitle("Resúmen Apuestas");
        dialogWindow.create();
        dialogWindow.show();
    }

    private void generateDeleteConfirmationDialog(int position) {
        AlertDialog.Builder dialogWindow = new AlertDialog.Builder(BetsPane.this);
        final int final_position = position;
        dialogWindow.setMessage("¿Desea remover esta apuesta?")
                .setCancelable(true)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadBets();
                    }
                })
                .setPositiveButton("Remover",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try{
                                    bets.removeBet(betsIds.get(final_position));
                                    successfulDeletion();
                                }
                                catch(Resources.NotFoundException e){
                                    unsuccessfulDeletion();
                                }
                                catch(IOException e){
                                    askForPermissions();
                                }
                            }
                        })
                .setTitle("Confirmación");
        dialogWindow.create();
        dialogWindow.show();
    }

    private void askForPermissions(){
        Toast.makeText(this, "Se necesitan permisos para acceder al almacenamiento", Toast.LENGTH_LONG).show();
    }

    private void successfulDeletion() {
        Toast.makeText(this, "Apuesta removida", Toast.LENGTH_SHORT).show();
        loadBets();
    }

    private void unsuccessfulDeletion() {
        Toast.makeText(this, "No existe esa apuesta", Toast.LENGTH_LONG).show();
    }

    private void loadBets(){
        swipeRefreshBets.setRefreshing(true);
        try{
            betsDates = bets.getBetsDates();
            betsIds = bets.getBetsIds();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, betsDates);
        listViewBets.setAdapter(adapter);
        swipeRefreshBets.setRefreshing(false);
    }
}
