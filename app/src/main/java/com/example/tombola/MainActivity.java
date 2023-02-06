package com.example.tombola;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.browse.MediaBrowser;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {
/*
Alias: AndroidDebugKey
MD5: B7:EF:B1:EE:AC:56:7C:1E:6D:79:7B:84:64:77:A9:13
SHA1: 76:B0:16:11:D4:D4:1D:39:38:79:31:D2:E1:86:8D:FA:84:67:5D:B3
SHA-256: A7:8D:2E:CF:D7:74:AC:BC:39:F4:93:14:E0:57:8B:2F:2D:28:7D:1A:F4:57:8A:C2:67:F1:C1:F9:C6:7D:EC:69
Valid until: mercoled├¼ 8 gennaio 2053

 */

    //INIZIO PROVA POKE TRA UTENTI

    EditText editText;
    Button button;

    String playerName = "";
    FirebaseDatabase database;
    DatabaseReference playerRef;


    // FINE PROVA

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prima_pagina);

        //PROVA POKE
        editText = findViewById(R.id.NomeGiocatoreEditText);
        button = findViewById(R.id.BottoneStart);

        database = FirebaseDatabase.getInstance();

        SharedPreferences preferences = getSharedPreferences("PREPS", 0);
        playerName = preferences.getString("PlayerName", "");

        if(!playerName.equals("")) {
            playerRef = database.getReference("players/" + playerName);
            addEventListener();
            playerRef.setValue("");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerName = editText.getText().toString();
                editText.setText("");
                if(!playerName.equals("")){
                    button.setText("REGISTRATI");
                    button.setEnabled(false);
                    playerRef = database.getReference("players/" + playerName);
                    addEventListener();
                    playerRef.setValue("");
                }
            }
        });


        }
//PROVA NOTIFICA
    private void addEventListener(){
//legge dal db
        playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //successo, continua fino al punto due
                if(!playerName.equals("")){
                    SharedPreferences preferences = getSharedPreferences("PREPS", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("player name", playerName);
                    editor.apply();

                    startActivity(new Intent(getApplicationContext(), Stanze.class));
                    finish();
                }
            }
            //
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //successo
                button.setText("LOGIN");
                button.setEnabled(true);
                Toast.makeText(MainActivity.this, "ERROREEE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    }


/*
public class MainActivity extends AppCompatActivity {
    private LinearLayout player1Layout, player2Layout;
    private ImageView image1,image2,image3,image4,image5,image6,image7,image8,image9;
    private TextView player1TV, player2TV;
    //combinazioni vincenti
    private final List<int[]> combinationsList = new ArrayList<>();
    private final List<String> doneBoxes = new ArrayList<>(); // box gia giocate


    //id univoci per i player
    private String playerUniqueID ="0";
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://tictactoe0-38421-default-rtdb.firebaseio.com/");

    private boolean opponentFound = false;

    private String opponentUniqueID ="0";
    // matching o waiting, una delle due a seconda della stato
    private String status = "matching";

    private String playerTurn = "";

    private String connectionId =""; // connessione in cui si entra per giocare

    // genero event listener per firebase
    ValueEventListener turnsEventListener, wonEventListener;

    // box selezionate dai giocatori. I campi vuoti vengono riempiti dagli id corrispondenti alle mosse
    private final String[] boxesSelectedBy = {"","","","","","","","",""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        player1Layout = findViewById(R.id.player1Layout);
        player2Layout = findViewById(R.id.player2Layout);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);
        image5 = findViewById(R.id.image5);
        image6 = findViewById(R.id.image6);
        image7 = findViewById(R.id.image7);
        image8 = findViewById(R.id.image8);
        image9 = findViewById(R.id.image9);

        player1TV = findViewById(R.id.player1TV);
        player2TV = findViewById(R.id.player2TV);
        //prendo il nome utente dal file PlayerName.class
        final String getPlayerName = getIntent().getStringExtra("playerName");

        // genero le combinazioni vincenti
        combinationsList.add(new int[]{0,1,2});
        combinationsList.add(new int[]{3,4,5});
        combinationsList.add(new int[]{6,7,8});
        combinationsList.add(new int[]{0,3,6});
        combinationsList.add(new int[]{1,4,7});
        combinationsList.add(new int[]{2,5,8});
        combinationsList.add(new int[]{2,4,6});
        combinationsList.add(new int[]{0,4,8});

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Waiting for opponent");
        progressDialog.show();

        //generazione dell'id univoco per il player
        playerUniqueID = String.valueOf(System.currentTimeMillis());

        // setting player name to TextView
        player1TV.setText(getPlayerName);

        databaseReference.child("connections").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(opponentFound){


                    // controllo tutte le connessioni nel caso altri utenti stiano gia aspettando qualcuno
                    if(snapshot.hasChildren()){

                        //controllo quindi tutte le connessioni
                        for(DataSnapshot connections :snapshot.getChildren()){

                            // prendo gli id delle connessioni
                            String conId = (connections.getKey());

                            //servono 2 giocatori per iniziare
                            //se getPlayersCount è 1 allora siamo in attesa, se 2 allora lesssgo
                            int getPlayersCount = (int)connections.getChildrenCount();
                            // dopo aver creato la connessione mi metto in attesa di altri player
                            if(status.equals("waiting")){
                                if(getPlayersCount == 2){
                                    playerTurn = playerUniqueID;
                                    applyPlayerTurn(playerTurn);

                                    // true quando l'avversario viene trovato
                                    boolean playerFound = false;

                                    // getting players in connessione
                                    for(DataSnapshot players : connections.getChildren()){

                                        String getPlayerUniqueId = players.getKey();

                                        //controllo che il player id sia uguale a quello del player
                                        //che ha creato la connessione(this user). Se si allora
                                        // prendo i dati dell'avversario
                                        if(getPlayerUniqueId.equals(playerUniqueID)){
                                            playerFound = true;
                                        }
                                        else if (playerFound){
                                            String getOpponentPlayerName = players.child("player_name").getValue(String.class);
                                            opponentUniqueID = players.getKey();

                                            // setto la TextView dell'avversario
                                            player2TV.setText(getOpponentPlayerName);

                                            // assining connection id
                                            connectionId = conId;
                                            opponentFound = true;
                                            // adding turn listeners and won listener to the db reference
                                            databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                            databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);

                                            // tolgo la progress dialog se messa in evidenza
                                            if(progressDialog.isShowing()){
                                                progressDialog.dismiss();
                                            }
                                            // una volta che la connessione ha rimosso il connectionListener dal database reference
                                            databaseReference.child("connections").removeEventListener(this);

                                        }
                                    }
                                }

                            } // nel caso un utente non abbia creato nessuna connessione perchè ce ne sono gia altre disponibili per entrarci
                            else {

                                if(getPlayersCount == 1){
                                    // aggiungo il player alla connessione
                                    connections.child(playerUniqueID).child("player_name").getRef().setValue(getPlayerName);
                                    // prendo entrambi i giocatori
                                    for(DataSnapshot players : connections.getChildren()){

                                        String getOpponentName = players.child("player_name").getValue(String.class);
                                        opponentUniqueID = players.getKey();

                                        // il primo turno va sempre a chi ha creato la lobby
                                        playerTurn = opponentUniqueID;
                                        applyPlayerTurn(playerTurn);

                                        player2TV.setText(getOpponentName);
                                        // assining connection id
                                        connectionId = conId;
                                        opponentFound = true;

                                        // adding turn listeners and won listener to the db reference
                                        databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                        databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);

                                        // tolgo la progress dialog se messa in evidenza
                                        if(progressDialog.isShowing()){
                                            progressDialog.dismiss();
                                        }
                                        // una volta che la connessione ha rimosso il connectionListener dal database reference
                                        databaseReference.child("connections").removeEventListener(this);

                                        break;
                                    }
                                }
                            }
                        }
                        // controllo se un avversario non è stato trovato e il giocatore non sta piu aspettando. Quindi creo una nuova connessione
                        if(!opponentFound && !status.equals("waiting")){

                            //genero l'id della lobby
                            String connectionUniqueId = String.valueOf(System.currentTimeMillis());

                            // aggiungo il primo giocatore alla connessione/lobby in attesa del secondo
                            snapshot.child(connectionUniqueId).child(playerUniqueID).child("player_name").getRef().setValue(getPlayerName);

                            status = "waiting";

                        }
                    }
                    // se non cè ancora nessuna connessione allora creala (lobby)
                    else{

                        //genero l'id della lobby
                        String connectionUniqueId = String.valueOf(System.currentTimeMillis());

                        // aggiungo il primo giocatore alla connessione/lobby in attesa del secondo
                        snapshot.child(connectionUniqueId).child(playerUniqueID).child("player_name").getRef().setValue(getPlayerName);

                        status = "waiting";
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        turnsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //getting all turns della connessione
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                    if(dataSnapshot.getChildrenCount() == 2){
                        // prendo la posizione della box scelta dall'utente
                        final int getBoxPosition = Integer.parseInt(dataSnapshot.child("box_position").getValue(String.class));

                        // prendo l'id del giocatore che ha fatto la mossa
                        final String getPlayerId = dataSnapshot.child("player_id").getValue(String.class);

                        // controllo se il giocatore non ha gia selezionato la box
                        if(!doneBoxes.contains(String.valueOf(getBoxPosition))){

                            doneBoxes.add(String.valueOf(getBoxPosition));

                            if(getBoxPosition == 1){
                                selectBox(image1, getBoxPosition, getPlayerId);

                            }else if (getBoxPosition == 2) {
                                selectBox(image2, getBoxPosition, getPlayerId);

                            }else if (getBoxPosition == 3) {
                                selectBox(image3, getBoxPosition, getPlayerId);

                            }else if (getBoxPosition == 4) {
                                selectBox(image4, getBoxPosition, getPlayerId);

                            }else if (getBoxPosition == 5) {
                                selectBox(image5, getBoxPosition, getPlayerId);

                            }else if (getBoxPosition == 6) {
                                selectBox(image6, getBoxPosition, getPlayerId);

                            }else if (getBoxPosition == 7) {
                                selectBox(image7, getBoxPosition, getPlayerId);

                            }else if (getBoxPosition == 8) {
                                selectBox(image8, getBoxPosition, getPlayerId);

                            }else if (getBoxPosition == 9) {
                                selectBox(image9, getBoxPosition, getPlayerId);

                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        wonEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // vediamo se qualcuno ha vinto
                if (snapshot.hasChild("player_id")){

                    String getWinPlayerId = snapshot.child("player_id").getValue(String.class);

                    final WinDialog winDialog;

                    if (getWinPlayerId.equals(playerUniqueID)){
                        //show in dialog
                        winDialog = new WinDialog(MainActivity.this, "hai vinto la partita");
                    }
                    else{
                        //show in dialog
                        winDialog = new WinDialog(MainActivity.this, "L'avversario ha vinto");
                    }
                    winDialog.setCancelable(false);
                    winDialog.show();

                    // remove Listeners from db
                    databaseReference.child("turns").child(connectionId).removeEventListener(turnsEventListener);
                    databaseReference.child("won").child(connectionId).removeEventListener(wonEventListener);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if the box is not selected before and current user's player turn
                if (!doneBoxes.contains("1") && playerTurn.equals(playerUniqueID)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);
                    // mando la box selezionata e da chi a firebase
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("1");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueID);

                    // change player turn
                    playerTurn = opponentUniqueID;
                }
            }
        });

        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if the box is not selected before and current user's player turn
                if (!doneBoxes.contains("2") && playerTurn.equals(playerUniqueID)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);
                    // mando la box selezionata e da chi a firebase
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("2");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueID);

                    // chage player turn
                    playerTurn = opponentUniqueID;
                }
            }
        });

        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if the box is not selected before and current user's player turn
                if (!doneBoxes.contains("3") && playerTurn.equals(playerUniqueID)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);
                    // mando la box selezionata e da chi a firebase
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("3");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueID);

                    // chage player turn
                    playerTurn = opponentUniqueID;
                }
            }
        });

        image4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if the box is not selected before and current user's player turn
                if (!doneBoxes.contains("4") && playerTurn.equals(playerUniqueID)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);
                    // mando la box selezionata e da chi a firebase
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("4");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueID);

                    // chage player turn
                    playerTurn = opponentUniqueID;
                }
            }
        });

        image5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if the box is not selected before and current user's player turn
                if (!doneBoxes.contains("5") && playerTurn.equals(playerUniqueID)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);
                    // mando la box selezionata e da chi a firebase
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("5");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueID);

                    // chage player turn
                    playerTurn = opponentUniqueID;
                }
            }
        });

        image6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if the box is not selected before and current user's player turn
                if (!doneBoxes.contains("6") && playerTurn.equals(playerUniqueID)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);
                    // mando la box selezionata e da chi a firebase
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("6");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueID);

                    // chage player turn
                    playerTurn = opponentUniqueID;
                }
            }
        });

        image7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if the box is not selected before and current user's player turn
                if (!doneBoxes.contains("7") && playerTurn.equals(playerUniqueID)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);
                    // mando la box selezionata e da chi a firebase
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("7");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueID);

                    // chage player turn
                    playerTurn = opponentUniqueID;
                }
            }
        });

        image8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if the box is not selected before and current user's player turn
                if (!doneBoxes.contains("8") && playerTurn.equals(playerUniqueID)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);
                    // mando la box selezionata e da chi a firebase
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("8");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueID);

                    // chage player turn
                    playerTurn = opponentUniqueID;
                }
            }
        });

        image9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if the box is not selected before and current user's player turn
                if (!doneBoxes.contains("9") && playerTurn.equals(playerUniqueID)){
                    ((ImageView)v).setImageResource(R.drawable.cross_icon);
                    // mando la box selezionata e da chi a firebase
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("9");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueID);

                    // chage player turn
                    playerTurn = opponentUniqueID;
                }
            }
        });
    }
    private void applyPlayerTurn(String playerUniqueID2){
        if (playerUniqueID2.equals(playerUniqueID)){
            player1Layout.setBackgroundResource(R.drawable.round_back_dark_blue_stroke);
            player2Layout.setBackgroundResource(R.drawable.round_back_dark_blue_20);
        }
        else{
            player2Layout.setBackgroundResource(R.drawable.round_back_dark_blue_stroke);
            player1Layout.setBackgroundResource(R.drawable.round_back_dark_blue_20);
        }

    }
    private void selectBox(ImageView imageView, int selectedBoxPosition, String selectedByPlayer){

        boxesSelectedBy[selectedBoxPosition - 1] = selectedByPlayer;

        if (selectedByPlayer.equals(playerUniqueID)){

            imageView.setImageResource(R.drawable.cross_icon);
            playerTurn = opponentUniqueID;
        }
        else {
            imageView.setImageResource(R.drawable.zero_icon);
            playerTurn = playerUniqueID;
        }
        applyPlayerTurn(playerTurn);

        if(checkPlayerWin(selectedByPlayer)){
            // mando l'id del giocatore vincente a firebase
            databaseReference.child("won").child(connectionId).child("player_id").setValue(selectedByPlayer);
        }
        // se nessuno ha vinto e sono finite le caselle
        if(doneBoxes.size() == 9){
            final WinDialog winDialog = new WinDialog( MainActivity.this, "nessun vincitore");
            winDialog.setCancelable(false);
            winDialog.show();
        }
    }
    private boolean checkPlayerWin(String playerId){

        boolean isPlayerWon = false;

        // compare player turns with every wining combinations
        for(int i = 0; i< combinationsList.size(); i++){
            final int[] combination = combinationsList.get(i);

            // checking last three turns of user
            if (boxesSelectedBy[combination[0]].equals(playerId) &&
                    boxesSelectedBy[combination[1]].equals(playerId) &&
                    boxesSelectedBy[combination[2]].equals(playerId)){

                isPlayerWon = true;
            }
        }
        return isPlayerWon;
    }
}

 */