package com.example.tombola;
//prova pull
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class Gioco extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prima_pagina);


        final EditText NomeGiocatoreEditText = findViewById(R.id.NomeGiocatoreEditText);
        final AppCompatButton BottoneStart = findViewById(R.id.BottoneStart);

        BottoneStart.setOnClickListener(view -> {
            final String getPlayerName = NomeGiocatoreEditText.getText().toString();

            if(getPlayerName.isEmpty()){
                Toast.makeText(Gioco.this, "Inserisci il tuo nickname", Toast.LENGTH_SHORT).show();
            }
            else{
                //creo L'Intent che apre la main activity
                Intent intent = new Intent (Gioco.this, MainActivity.class);

                //aggiungo il nome utente all'Intent
                intent.putExtra("playername", getPlayerName);

                //apro la main
                startActivity(intent);

                //destroy this activity (PlayerName)
                finish();
            }
        });
    }
}