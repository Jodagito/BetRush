package com.example.betrush;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import io.realm.Realm;
import io.realm.RealmResults;

public class TeamsPane extends AppCompatActivity {
    private ListView listViewTeams;
    private Toolbar toolbar;
    private String teamName = "";
    private SwipeRefreshLayout swipeRefreshTeams;
    private ArrayList<String> teamsNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams_pane);
        connect();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Equipos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadTeams();
        listViewTeams.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(TeamsPane.this, MatchesPane.class);
                teamName = listViewTeams.getItemAtPosition(position).toString();
                intent.putExtra("team", teamName);
                intent.putExtra("teams", teamsNamesAsArray());
                TeamsPane.this.startActivity(intent);
            }
        });
        listViewTeams.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                generateDeleteConfirmationDialog(position);
                return true;
            }
        });
        swipeRefreshTeams.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTeams();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.teams_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuNewTeam) {
                generateTeamInputDialog();
        }
        else if(item.getItemId() == R.id.menuBets){
            Intent intent = new Intent(TeamsPane.this, BetsPane.class);
            TeamsPane.this.startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private String[] teamsNamesAsArray(){
        ArrayList<String> teamsNames = teams.getTeamsNames();
        teamsNames.remove(teamName);
        String[] teamsNamesAsArray = new String[teamsNames.size()];
        for (String name : teamsNames){
            teamsNamesAsArray[teamsNames.indexOf(name)] = name;
        }
        return teamsNamesAsArray;
    }

    private void connect() {
        listViewTeams = findViewById(R.id.listViewTeams);
        toolbar = findViewById(R.id.teamsToolbar);
        swipeRefreshTeams = findViewById(R.id.swipeRefreshTeams);
    }

    private void generateTeamInputDialog() {
        AlertDialog.Builder dialogWindow = new AlertDialog.Builder(TeamsPane.this);
        final EditText txtName = new EditText(TeamsPane.this);
        txtName.setInputType(InputType.TYPE_CLASS_TEXT);
        dialogWindow.setView(txtName);
        dialogWindow.setMessage("Ingresa el nombre del equipo")
                .setCancelable(true)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadTeams();
                    }
                })
                .setPositiveButton("Ingresar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                teamName = txtName.getText().toString();
                                if (!teamName.isEmpty() && !teams.teamExists(teamName)){
                                    generateCreateConfirmationDialog();
                                }
                                else if(teams.teamExists(teamName)){
                                    TeamsPane.this.adapter.getFilter().filter(teams.getTeam(teamName).name);
                                }
                                else{
                                    loadTeams();
                                }
                            }
                        })
                .setTitle("Inserción de Equipo");
        dialogWindow.create();
        dialogWindow.show();
    }

    private void generateCreateConfirmationDialog() {
        AlertDialog.Builder dialogWindow = new AlertDialog.Builder(TeamsPane.this);
        dialogWindow.setMessage("¿Desea ingresar el equipo " + teamName +"?")
                .setCancelable(true)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadTeams();
                    }
                })
                .setPositiveButton("Ingresar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                teamName = teamName.substring(0, 1).toUpperCase() + teamName.substring(1);
                                realm.beginTransaction();
                                realm.createObject(Team.class, teamName);
                                realm.commitTransaction();
                                successfulInsertion();
                            }
                        })
                .setTitle("Confirmación");
        dialogWindow.create();
        dialogWindow.show();
    }

    private void generateDeleteConfirmationDialog(int position) {
        AlertDialog.Builder dialogWindow = new AlertDialog.Builder(TeamsPane.this);
        final int final_position = position;
        dialogWindow.setMessage("¿Desea remover este equipo ?")
                .setCancelable(true)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadTeams();
                    }
                })
                .setPositiveButton("Remover",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String removedTeam = "";
                                removedTeam = listViewTeams.getItemAtPosition(final_position).toString();
                                RealmResults<Team> teamToRemove = realm.where(Team.class).equalTo("name", removedTeam).findAll();
                                realm.beginTransaction();
                                teamToRemove.deleteAllFromRealm();
                                realm.commitTransaction();
                                successfulDeletion(removedTeam);
                            }
                        })
                .setTitle("Confirmación");
        dialogWindow.create();
        dialogWindow.show();
    }

    private void successfulInsertion() {
        Toast.makeText(this, teamName + " ingresado", Toast.LENGTH_SHORT).show();
        refreshTeamsList();
    }

    private void askForPermissions(){
        Toast.makeText(this, "Se necesitan permisos para acceder al almacenamiento", Toast.LENGTH_LONG).show();
    }

    private void successfulDeletion(String removedTeam) {
        Toast.makeText(this, removedTeam + " removido", Toast.LENGTH_SHORT).show();
        loadTeams();
    }

    private void unsuccessfulDeletion(String removedTeam) {
        Toast.makeText(this, "No existe un equipo llamado " + removedTeam, Toast.LENGTH_SHORT).show();
    }

    private void refreshTeamsList() {
        swipeRefreshTeams.setRefreshing(true);
        finish();
        startActivity(getIntent());
        swipeRefreshTeams.setRefreshing(false);
    }

    private void loadTeams(){
        swipeRefreshTeams.setRefreshing(true);
        teamsNames = new ArrayList<>();
        RealmResults<Team> teams = realm.where(Team.class).findAll();
        for (Team team : teams) {
            teamsNames.add(team.name);
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, teamsNames);
        listViewTeams.setAdapter(adapter);
        swipeRefreshTeams.setRefreshing(false);
    }
}
