package com.example.betrush;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText txtId;
    private BettorsFile bettors;
    private String bettorName = "";
    public static int insertedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connect();
        try{
            bettors = new BettorsFile();
        }
        catch (JSONException e){
            Toast.makeText(this, "Ocurrió un error leyendo el archivo de apostadores", Toast.LENGTH_LONG);
        }
        catch(IOException e){
            askForPermissions();
        }
    }

    private void connect(){
        txtId = findViewById(R.id.txtId);
        Button btnInsert = findViewById(R.id.btnInsert);
        btnInsert.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        insertedId = Integer.valueOf(txtId.getText().toString());
        if (bettors.bettorExists(insertedId)){
            bettorName = bettors.getBettor(insertedId).name;
            successfulLogin();
        }
        else{
            generateNameInputDialog();
        }
    }

    private void generateNameInputDialog() {
        final AlertDialog.Builder dialogWindow = new AlertDialog.Builder(MainActivity.this);
        final EditText txtName = new EditText(MainActivity.this);
        txtName.setInputType(InputType.TYPE_CLASS_TEXT);
        dialogWindow.setView(txtName);
        dialogWindow.setMessage("Ingresa tu nombre para registrarte")
                .setCancelable(true)
                .setPositiveButton("Registrarme",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                bettorName = txtName.getText().toString();
                                if (!bettorName.isEmpty()){
                                    generateConfirmationDialog();
                                }
                                else{
                                    unsuccessfulLogin();
                                }
                            }
                        })
                .setTitle("Regístrate");
        dialogWindow.create();
        dialogWindow.show();
    }

    private void generateConfirmationDialog() {
        AlertDialog.Builder dialogWindow = new AlertDialog.Builder(MainActivity.this);
        dialogWindow.setMessage("¿Está seguro que desea registrarse como " + bettorName + "?")
                .setCancelable(true)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        unsuccessfulLogin();
                    }
                })
                .setPositiveButton("Registrar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    bettorName = bettorName.substring(0, 1).toUpperCase() + bettorName.substring(1);
                                    bettors.createBettor(insertedId, bettorName);
                                    successfulLogin();
                                }
                                catch(Exception e){
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

    private void unsuccessfulLogin() {
        Toast.makeText(this, "Debes ingresar tu nombre para continuar", Toast.LENGTH_LONG).show();
        generateNameInputDialog();
    }

    private void successfulLogin() {
        Toast.makeText(this, "Hola " + bettorName, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, TeamsPane.class);
        MainActivity.this.startActivity(intent);
    }
}
