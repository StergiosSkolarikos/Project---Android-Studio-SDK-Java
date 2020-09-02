package com.myapp.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Statistics extends AppCompatActivity {

    TextView General;
    TextView NoOfNots;
    TextView NoOfFall;
    TextView NoOfFire;

    TextView PrOfFall;
    TextView PrOfFire;
    TextView CancelAlert;

    static List<String> Situation;
    static List<String> Position;
    static List<String> Timestamp;
    static List<String> Notifications;

    int counterFire=0;
    int counterFall=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        General=findViewById(R.id.textView0);
        NoOfNots=findViewById(R.id.textView1);
        NoOfFall=findViewById(R.id.textView2);
        NoOfFire=findViewById(R.id.textView3);

        PrOfFall=findViewById(R.id.textView4);
        PrOfFire=findViewById(R.id.textView5);
        General.setText(R.string.Gs);

        CancelAlert=findViewById(R.id.textView6);
        Situation=new ArrayList<>();
        Position=new ArrayList<>();
        Timestamp=new ArrayList<>();
        Notifications=new ArrayList<>();
        MainActivity.Notifications.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String not=ds.getValue().toString();
                    String position= ds.child("position").getValue().toString();
                    String situation= ds.child("situation").getValue().toString();
                    String timestamp= ds.child("timestamp").getValue().toString();
                    Situation.add(situation);
                    Position.add(position);
                    Timestamp.add(timestamp);
                    Notifications.add(not);

                }
                showStatistics(Situation,Position,Timestamp,Notifications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        MainActivity.CancelAlert.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int number= (int) dataSnapshot.getChildrenCount();
                ShowCancelAlerts(number);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


    public void showStatistics(List<String> Situation, List<String> Position, List<String> TimeStamp, List<String> Notifications){
        for (String sit: Situation) {
          if(sit.equals("Fall")){
              counterFall++;
          }
          else if (sit.equals("Fire"))
          {
             counterFire++;
          }
        }
        DecimalFormat df=new DecimalFormat("##.#");
        double percentageFall=((double)counterFall/Notifications.size())*100;
        double percentageFire=((double)counterFire/Notifications.size())*100;

        NoOfNots.setText(getString(R.string.NoOfNots)+": "+String.format("%d",Notifications.size()));
        NoOfFall.setText(getString(R.string.NoOfFall)+": "+String.format("%d",counterFall));
        NoOfFire.setText(getString(R.string.NoOfFire)+": "+String.format("%d",counterFire));
        PrOfFall.setText(getString(R.string.PerOfFall)+": "+String.format(df.format(percentageFall))+"%");
        PrOfFire.setText(getString(R.string.PerOfFire)+": "+String.format(df.format(percentageFire))+"%");
    }
    public void ShowCancelAlerts(int number){
        CancelAlert.setText(getString(R.string.cancelAlert)+": "+String.format("%d",number));
    }


}