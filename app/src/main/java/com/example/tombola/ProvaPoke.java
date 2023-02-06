package com.example.tombola;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProvaPoke extends AppCompatActivity {

    Button button;
    String playerName = "";
    String roomName = "";
    String role = "";
    String message ="";

    FirebaseDatabase database;
    DatabaseReference messageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prova_poke);

        button = findViewById(R.id.Poke);
        button.setEnabled(false);

        database = FirebaseDatabase.getInstance();

        SharedPreferences preferences = getSharedPreferences("PREPS", 0);
        playerName = preferences.getString("playerName", "");

        Bundle extras = getIntent().getExtras();
        //setta host o guest in base a race conditions
        if (extras != null){
            roomName = extras.getString("roomName");
            if (roomName.equals(playerName)){
                role = "host";
            }else{
                role = "guest";
            }
        }
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            //invio del messaggio
                button.setEnabled(false);
                message = role + "Poked!";
                messageRef.setValue(message);
            }
        }

        );
        //in ascolto per i messaggi in ingresso
        messageRef = database.getReference("rooms/" + roomName + "/message");
        message = role + "Poked!";
        messageRef.setValue(message);
        addRoomEventListener();
    }

    private void addRoomEventListener(){
        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //mesaggio ricevuto
                if(role.equals("host")){
                    if (snapshot.getValue(String.class).contains("guest: ")){
                        button.setEnabled(true);
                        Toast.makeText(ProvaPoke.this,
                                "" + snapshot.getValue(String.class).replace("guest:", ""), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if (snapshot.getValue(String.class).contains("host: ")){
                        button.setEnabled(true);
                        Toast.makeText(ProvaPoke.this,
                                "" + snapshot.getValue(String.class).replace("host:", ""), Toast.LENGTH_SHORT).show();
                }
            }
        }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //brench errore >>>> riprova
            }
            });
    }
}