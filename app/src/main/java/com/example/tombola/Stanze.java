package com.example.tombola;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.lights.LightState;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.internal.RootTelemetryConfigManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Stanze extends AppCompatActivity {


    ListView listView;
    Button button;


    List<String> roomsList;

    String playerName="";
    String roomName ="";

    FirebaseDatabase database;
    DatabaseReference roomRef;
    DatabaseReference roomsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stanze);
// prende il nome del gocatore e gli assegna una stanza con il suo nome

        database = FirebaseDatabase.getInstance();
        SharedPreferences preferences = getSharedPreferences("PREPS", 0);
        playerName = preferences.getString("playerName", "");
        roomName = playerName;

        listView = findViewById(R.id.listaStanze);
        button = findViewById(R.id.CreaStanza);
        // lista di tutte le stanze
        roomsList = new ArrayList<>();

        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //crea una stanza e inserisciti come player uno
                button.setText("CREA STANZA");
                button.setEnabled(false);
                roomName = playerName;
                roomRef = database.getReference("rooms/"+roomName+ "/player1");
                addRoomEventListener();
                roomRef.setValue(playerName);
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //entra in una stanza e settati as player2
                roomName = roomsList.get(position);
                roomRef = database.getReference("rooms/"+ roomName+ "player2");
                addRoomEventListener();
                roomRef.setValue(playerName);
            }
        });
        //mostra se una nuova stanza é disponibile
        addRoomsEventListener();  //event listener delle stanzE


    }
    private void addRoomEventListener(){
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //entra nella lobby (stanza)
                button.setText("CREATE ROOM");
                button.setEnabled(true);
                Intent intent= new Intent(getApplicationContext(), ProvaPoke.class);
                intent.putExtra("roomName", roomName);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // brench error
                button.setText("CREATE ROOM");
                button.setEnabled(true);
                Toast.makeText(Stanze.this, "Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addRoomsEventListener(){
        roomRef = database.getReference("rooms");
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //mostra la lista dlle stanze
                roomsList.clear();
                Iterable<DataSnapshot> rooms = snapshot.getChildren();
                for(DataSnapshot dataSnapshot : rooms){
                    roomsList.add(dataSnapshot.getKey());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(Stanze.this, android.R.layout.simple_list_item_1, roomsList);
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //brench errore
            }
        });
    }

}