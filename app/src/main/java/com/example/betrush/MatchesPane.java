package com.example.betrush;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import org.json.JSONException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MatchesPane extends AppCompatActivity {
    private ListView listViewMatches;
    private MatchesFile matches;
    private Toolbar matchesToolbar;
    private String visitantTeam = "";
    private String matchDate = "";
    private final Calendar calendar = Calendar.getInstance();
    private SwipeRefreshLayout swipeRefreshMatches;
    private ArrayList<String> matchesDates = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String selectedTeam = "";
    private String[] teams;
    private Match selectedMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale locale = new Locale("es");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_matches_pane);
        connect();
        selectedTeam = getIntent().getStringExtra("team");
        teams = getIntent().getStringArrayExtra("teams");
        setSupportActionBar(matchesToolbar);
        getSupportActionBar().setTitle("Partidos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try{
            matches = new MatchesFile();
        }
        catch (JSONException e){
            Toast.makeText(this, "Ocurrió un error leyendo el archivo de partidos", Toast.LENGTH_LONG);
        }
        catch(IOException e){
            askForPermissions();
        }
        loadMatches();
        listViewMatches.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String matchDateSelected = listViewMatches.getItemAtPosition(position).toString();
                selectedMatch = matches.getMatchByDate(matchDateSelected);
                String matchAsString = getMatchAsString(selectedMatch);
                generateMatchDetailDialog(matchAsString);
            }
        });
        listViewMatches.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                generateDeleteConfirmationDialog(position);
                return true;
            }
        });
        swipeRefreshMatches.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMatches();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.matches_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuNewMatch) {
            generateMatchInputDialog();
        }
        else if(item.getItemId() == R.id.menuFilterWinMatches){
            swipeRefreshMatches.setRefreshing(true);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, matches.getWinMatchesDates(selectedTeam));
            listViewMatches.setAdapter(adapter);
            swipeRefreshMatches.setRefreshing(false);
        }
        if (item.getItemId() == R.id.menuTeamStatistics) {
            generateTeamStatisticsDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private String getMatchAsString(Match match) {
        String matchAsString = "";
        if (match.played){
            matchAsString = "RESULTADO\t\t" + match.results[0] + "\t  - \t" + match.results[1] + "\n\n";
            matchAsString += "GANADOR\t\t" + match.winner.toUpperCase();
        }
        return matchAsString;
    }

    private void connect() {
        listViewMatches = findViewById(R.id.listViewMatches);
        matchesToolbar = findViewById(R.id.matchesToolbar);
        swipeRefreshMatches = findViewById(R.id.swipeRefreshMatches);
    }

    private void generateSelectBetTeamsDialog(Match selectedMatch){
        final String[] matchTeams = selectedMatch.teams;
        final AlertDialog.Builder dialogWindow = new AlertDialog.Builder(MatchesPane.this);
        dialogWindow.setItems(matchTeams, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String forecast = matchTeams[which];
                if (forecast.isEmpty()){
                    loadMatches();
                }
                else{
                    generateAskBetValueDialog(dialogWindow, forecast);
                }
            }
        });
        dialogWindow.setCancelable(true)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadMatches();
                    }
                })
                .setTitle("Selección de Equipo");
        dialogWindow.show();
    }

    private void generateTeamStatisticsDialog() {
        AlertDialog.Builder dialogWindow = new AlertDialog.Builder(MatchesPane.this);
        dialogWindow.setMessage(matches.getTeamStatistics(selectedTeam))
                .setCancelable(true);
        dialogWindow.setTitle(selectedTeam.toUpperCase());
        dialogWindow.create();
        dialogWindow.show();
    }

    private void generateAskBetValueDialog(AlertDialog.Builder dialogWindow, String forecast){
        final String finalForecast = forecast;
        final EditText txtValue = new EditText(MatchesPane.this);
        txtValue.setInputType(InputType.TYPE_CLASS_NUMBER);
        dialogWindow.setView(txtValue);
        dialogWindow.setMessage("Ingresa el valor a apostar")
                .setCancelable(true)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadMatches();
                    }
                })
                .setPositiveButton("Ingresar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int betValue = Integer.valueOf(txtValue.getText().toString());
                                if (betValue > 0){
                                    generateCreateBetConfirmationDialog(finalForecast, betValue);
                                }
                                else{
                                    loadMatches();
                                }
                            }
                        })
                .setTitle("Inserción de Equipo");
        dialogWindow.create();
        dialogWindow.show();
    }

    private void generateCreateBetConfirmationDialog(String forecast, int betValue) {
        final String finalForecast = forecast;
        final int finalBetValue = betValue;
        AlertDialog.Builder dialogWindow = new AlertDialog.Builder(MatchesPane.this);
        dialogWindow.setMessage("¿Desea generar esta apuesta?")
                .setCancelable(true)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadMatches();
                    }
                })
                .setPositiveButton("Generar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    BetsFile bets = new BetsFile();
                                    matches.playMatch(selectedMatch);
                                    bets.createBet(finalBetValue, finalForecast, MainActivity.insertedId, selectedMatch.winner);
                                    successfulBetInsertion();
                                }
                                catch(JSONException e){
                                    unsuccessfulMatchInsertion();
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

    private void generateMatchDetailDialog(String matchAsString) {
        AlertDialog.Builder dialogWindow = new AlertDialog.Builder(MatchesPane.this);
        dialogWindow.setMessage(matchAsString)
                .setCancelable(true);
        if (!selectedMatch.played){
            dialogWindow.setPositiveButton("Apostar",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            generateSelectBetTeamsDialog(selectedMatch);
                        }
                    });
        }
        dialogWindow.setTitle(selectedMatch.teams[0] + " VS " + selectedMatch.teams[1]);
        dialogWindow.create();
        dialogWindow.show();
    }

    private void generateMatchInputDialog() {
        AlertDialog.Builder dialogWindow = new AlertDialog.Builder(MatchesPane.this);
        dialogWindow.setItems(teams, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                visitantTeam = teams[which];
                if (!selectedTeam.isEmpty() && !visitantTeam.isEmpty()){
                    generateMatchDatePicker();
                }
                else{
                    loadMatches();
                }
            }
        });
        dialogWindow.setCancelable(true)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadMatches();
                    }
                })
                .setTitle("Selecciona el Contrincante");
        dialogWindow.show();
    }

    private void generateMatchDatePicker() {
        final TimePickerDialog timeDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String formattedHour =  (hourOfDay < 10)? "0" + hourOfDay : String.valueOf(hourOfDay);
                String formattedMinute = (minute < 10)? "0" + minute : String.valueOf(minute);
                String AM_PM;
                if(hourOfDay < 12) {
                    AM_PM = "a.m.";
                } else {
                    AM_PM = "p.m.";
                }
                generateCreateConfirmationDialog();
                matchDate += " " + formattedHour + ":" + formattedMinute + " " + AM_PM;
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        DatePickerDialog dateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                final int actualMonth = month + 1;
                String formattedDay = (dayOfMonth < 10)? 0 + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                String formattedMonth = (actualMonth < 10)? 0 + String.valueOf(actualMonth):String.valueOf(actualMonth);
                matchDate = formattedDay + "/" + formattedMonth + "/" + year;
                timeDialog.show();
            }
        },calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dateDialog.setTitle("Escoja la fecha del encuentro");
        dateDialog.show();
    }

    private void generateCreateConfirmationDialog() {
        AlertDialog.Builder dialogWindow = new AlertDialog.Builder(MatchesPane.this);
        dialogWindow.setMessage("¿Desea generar este partido?")
                .setCancelable(true)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadMatches();
                    }
                })
                .setPositiveButton("Generar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    matches.createMatch(new String[]{selectedTeam, visitantTeam}, matchDate);
                                    successfulMatchInsertion();
                                }
                                catch(InvalidParameterException e){
                                    unsuccessfulMatchDateInsertion();
                                }
                                catch(JSONException e){
                                    unsuccessfulMatchInsertion();
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

    private void generateDeleteConfirmationDialog(int position) {
        AlertDialog.Builder dialogWindow = new AlertDialog.Builder(MatchesPane.this);
        final int final_position = position;
        dialogWindow.setMessage("¿Desea remover este partido?")
                .setCancelable(true)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadMatches();
                    }
                })
                .setPositiveButton("Remover",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try{
                                    matches.removeMatch(matches.getMatchByDate(listViewMatches.getItemAtPosition(final_position).toString()).id);
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

    private void successfulMatchInsertion() {
        Toast.makeText(this, "Partido ingresado", Toast.LENGTH_SHORT).show();
        refreshMatchesList();
    }

    private void successfulBetInsertion() {
        Toast.makeText(this, "Apuesta ingresada", Toast.LENGTH_SHORT).show();
        refreshMatchesList();
    }

    private void unsuccessfulMatchInsertion() {
        Toast.makeText(this, "No existen uno o ningún equipo", Toast.LENGTH_LONG).show();
        refreshMatchesList();
    }

    private void unsuccessfulMatchDateInsertion() {
        Toast.makeText(this, "Este equipo ya tiene un partido programado en esa fecha", Toast.LENGTH_LONG).show();
    }

    private void askForPermissions(){
        Toast.makeText(this, "Se necesitan permisos para acceder al almacenamiento", Toast.LENGTH_LONG).show();
    }

    private void successfulDeletion() {
        Toast.makeText(this, "Partido removido", Toast.LENGTH_SHORT).show();
        loadMatches();
    }

    private void unsuccessfulDeletion() {
        Toast.makeText(this, "No existe ese partido", Toast.LENGTH_LONG).show();
    }

    private void refreshMatchesList() {
        swipeRefreshMatches.setRefreshing(true);
        finish();
        startActivity(getIntent());
        swipeRefreshMatches.setRefreshing(false);
    }

    private void loadMatches(){
        swipeRefreshMatches.setRefreshing(true);
        try{
            matchesDates = matches.getMatchesDates(selectedTeam);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, matchesDates);
        listViewMatches.setAdapter(adapter);
        swipeRefreshMatches.setRefreshing(false);
    }
}
