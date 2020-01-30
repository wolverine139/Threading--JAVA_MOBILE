package com.example.project_4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class gridview extends AppCompatActivity {

    private ListView player1;
    private ListView player2;
    private TextView methodTextView;
    private TextView guessNumber;

    private ArrayAdapter<String> player1Adapter;
    private ArrayAdapter<String> player2Adapter;
    private ArrayList<String> player1update;
    private ArrayList<String> player2update;

    private Handler mainHandler;
    private Handler p1Handler;
    private Handler p2Handler;

    private String method;
    private ArrayList<Integer> key;

    private ArrayList<ArrayList<Integer>> player1data;
    private ArrayList<ArrayList<Integer>> player2data;

    private boolean gameRunning;
    private boolean p1Won;
    private boolean p2Won;

    public static final int P1DISPLAY = 1;
    public static final int P2DISPLAY = 2;
    public static final int GAMEOVER = 3;
    public static final int NEXTTURN = 4;
    public final long wait = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);
        player1 = findViewById(R.id.player1_ListView);
        player2 = findViewById(R.id.player2_ListView);
        methodTextView = findViewById(R.id.method_tv);
        guessNumber = findViewById(R.id.random_tv);
        method = getIntent().getStringExtra("key");
        key = new ArrayList<>();
        key = getrandom();

        mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int message = msg.what;
                switch (message) {
                    case P1DISPLAY:
                        String responses1 = details(key, player1data.get(player1data.size() - 1));
                        if(responses1 == "Complete miss") {
                            responses1 = disaster(player1data, player2data, player1data.get(player1data.size() - 1), responses1);
                        }
                        player1update.add("Move: " + (player1data.get(player1data.size() - 1).get(0)) + " X " + (player1data.get(player1data.size() - 1).get(1))
                        + "\n" + responses1);
                        player1Adapter.notifyDataSetChanged();
                        player1.setSelection(player1Adapter.getCount() - 1);
                        p1Won = winner(player1data.get(player1data.size() - 1), key);
                        if (p1Won) {
                            mainHandler.sendMessageAtFrontOfQueue(mainHandler.obtainMessage(gridview.GAMEOVER));
                        } else {
                            p2Handler.sendMessage(p2Handler.obtainMessage(gridview.NEXTTURN));
                        }
                        break;
                    case P2DISPLAY:
                        String responses2 = details(key, player2data.get(player2data.size() - 1));
                        if(responses2 == "Complete miss") {
                            responses2 = disaster(player1data, player2data, player2data.get(player2data.size() - 1), responses2);
                        }
                        player2update.add("Move: " + (player2data.get(player2data.size() - 1).get(0)) + " X " + (player2data.get(player2data.size() - 1).get(1))+ "\n" + responses2);
                        player2Adapter.notifyDataSetChanged();
                        player2.setSelection(player2Adapter.getCount() - 1);
                        p2Won = winner(player2data.get(player2data.size() - 1), key);
                        if (p2Won) {
                            mainHandler.sendMessageAtFrontOfQueue(mainHandler.obtainMessage(gridview.GAMEOVER));
                        } else {
                            p1Handler.sendMessage(p1Handler.obtainMessage(gridview.NEXTTURN));
                        }
                        break;
                    case GAMEOVER:
                        p1Handler.sendMessageAtFrontOfQueue(p1Handler.obtainMessage(gridview.GAMEOVER));
                        p2Handler.sendMessageAtFrontOfQueue(p2Handler.obtainMessage(gridview.GAMEOVER));
                        gameRunning = false;
                        AlertDialog.Builder alertbox = new AlertDialog.Builder(gridview.this, android.R.style.Theme_Material_Dialog_Alert);
                        String alert_msg = "";
                        if (p1Won)
                            alert_msg = "Success : Player-1 Won";
                        else
                            alert_msg = "Success : Player-2 Won";
                        alertbox.setMessage(alert_msg).setPositiveButton("Restart Game", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(gridview.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }).show();
                }
            }
        };

        if (method != null) {
            methodTextView.setText(method);
            guessNumber.setText("   Row: " + key.get(0) + " Column: " + key.get(1));
        }

        if (gameRunning) {
            p1Handler.sendMessageAtFrontOfQueue(p1Handler.obtainMessage(gridview.GAMEOVER));
            p2Handler.sendMessageAtFrontOfQueue(p2Handler.obtainMessage(gridview.GAMEOVER));
        }
        gameRunning = false;
        player1update = new ArrayList<>();
        player1data = new ArrayList<>();
        player1Adapter = new ArrayAdapter<>(gridview.this, android.R.layout.simple_list_item_1, player1update);
        player1.setAdapter(player1Adapter);
        p1Won = false;
        player2update = new ArrayList<>();
        player2data = new ArrayList<>();
        player2Adapter = new ArrayAdapter<>(gridview.this, android.R.layout.simple_list_item_1, player2update);
        player2.setAdapter(player2Adapter);
        p2Won = false;
        Thread p1 = new Thread(new player1Thread());
        Thread p2 = new Thread(new player2Thread());
        p1.start();
        p2.start();
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        p1Handler.sendMessage(p1Handler.obtainMessage(gridview.NEXTTURN));

    }



    public class player1Thread implements Runnable
    {
        @Override
        public void run() {
            Looper.prepare();
            p1Handler = new Handler(){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    int msg_what = msg.what;
                    switch (msg_what){
                        case NEXTTURN:
                            try {
                                Thread.sleep(wait);
                                gameRunning = true;
                                ArrayList<Integer> temp = new ArrayList<>();
                                if(method.equals("Continuous") && player1data.size()!=0) {
                                    temp = heuristic(player1data, key, player1data.get(player1data.size() - 1));
                                }
                                else
                                    temp = getrandom();
                                player1data.add(temp);
                                Message reply = mainHandler.obtainMessage(gridview.P1DISPLAY);
                                mainHandler.sendMessageAtFrontOfQueue(reply);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            break;
                        case GAMEOVER:
                            gameRunning = false;
                            getLooper().quit();
                            removeCallbacksAndMessages(null);
                            Thread.currentThread().interrupt();
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }

    public class player2Thread implements Runnable
    {
        @Override
        public void run() {
            Looper.prepare();
            p2Handler = new Handler(){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    int msg_what = msg.what;
                    switch (msg_what){
                        case NEXTTURN:
                            try {
                                Thread.sleep(wait);
                                gameRunning = true;
                                ArrayList<Integer> temp = new ArrayList<>();
                                if(method.equals("Continuous") && player2data.size()!=0) {
                                    temp = heuristic(player2data, key, player2data.get(player2data.size() - 1));
                                }
                                else
                                temp = getrandom();
                                player2data.add(temp);
                                Message reply = mainHandler.obtainMessage(gridview.P2DISPLAY);
                                mainHandler.sendMessageAtFrontOfQueue(reply);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        case GAMEOVER:
                            gameRunning = false;
                            getLooper().quit();
                            removeCallbacksAndMessages(null);
                            Thread.currentThread().interrupt();
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }

    public ArrayList<Integer> getrandom()
    {
        ArrayList<Integer> temp = new ArrayList<>();
       int x = (int)(Math.random() * (11 - 1) + 1);
       int y = (int)(Math.random() * (11 - 1) + 1);
       temp.add(x);
       temp.add(y);
       return temp;
    }

    public boolean winner(ArrayList<Integer> data, ArrayList<Integer> goal)
    {
        if((data.get(0) == goal.get(0)) &&(data.get(1) == goal.get(1)))
            return true;
        else
            return false;
    }

    public String details(ArrayList<Integer> data, ArrayList<Integer> goal)
    {
        String temp = "";
        if((goal.get(0) == (data.get(0) - 1) && ((goal.get(1) == data.get(1)) || (goal.get(1) == data.get(1) -1) || (goal.get(1) == data.get(1) +1))) ||
                ((goal.get(0) == data.get(0)) && ((goal.get(1) == data.get(1) - 1) || (goal.get(1) == data.get(1) + 1))) ||
                (goal.get(0) == (data.get(0) + 1) && ((goal.get(1) == data.get(1)) || (goal.get(1) == data.get(1) -1) || (goal.get(1) == data.get(1) +1))))
        {
            temp = "Near miss";
        }
        else if((goal.get(0) == (data.get(0) - 2) && ((goal.get(1) == data.get(1)) || (goal.get(1) == data.get(1) - 2) || (goal.get(1) == data.get(1) + 2))) ||
                ((goal.get(0) == data.get(0)) && ((goal.get(1) == data.get(1) - 2) || (goal.get(1) == data.get(1) + 2))) ||
                (goal.get(0) == (data.get(0) + 2) && ((goal.get(1) == data.get(1)) || (goal.get(1) == data.get(1) - 2) || (goal.get(1) == data.get(1) + 2))))
        {
            temp = "Close guess";
        }
        else
        {
            temp = "Complete miss";
        }
        return temp;
    }

    public String disaster(ArrayList<ArrayList<Integer>> p1, ArrayList<ArrayList<Integer>> p2, ArrayList<Integer> goal, String temp)
    {
        if (temp == "Complete miss")
        {
            if(p1.size() != 0 && p2.size() != 0)
            {
                int i = 0;
                int j = 0;
                for(ArrayList<Integer> x : p1)
                {
                    if(i++ == p1.size() - 1)
                        break;
                    if(x.get(0) == goal.get(0) && x.get(1) == goal.get(1))
                        temp = "Disaster";
                }
                for(ArrayList<Integer> x : p2)
                {
                    if(j++ == p1.size() - 1)
                        break;
                    if(x.get(0) == goal.get(0) && x.get(1) == goal.get(1))
                        temp = "Disaster";
                }
            }
        }
        else
            temp = "Complete miss";
        return temp;
    }

    public ArrayList<Integer> heuristic(ArrayList<ArrayList<Integer>> x, ArrayList<Integer> data, ArrayList<Integer> goal)
    {
        String prev = "";
        ArrayList<Integer> temp = new ArrayList<>();
        if(x.size() != 0)
        {

            if((goal.get(0) == (data.get(0) - 1) && ((goal.get(1) == data.get(1)) || (goal.get(1) == data.get(1) -1) || (goal.get(1) == data.get(1) +1))) ||
                    ((goal.get(0) == data.get(0)) && ((goal.get(1) == data.get(1) - 1) || (goal.get(1) == data.get(1) + 1))) ||
                    (goal.get(0) == (data.get(0) + 1) && ((goal.get(1) == data.get(1)) || (goal.get(1) == data.get(1) -1) || (goal.get(1) == data.get(1) +1))))
            {
                prev = "Near miss";
            }
            else if((goal.get(0) == (data.get(0) - 2) && ((goal.get(1) == data.get(1)) || (goal.get(1) == data.get(1) - 2) || (goal.get(1) == data.get(1) + 2))) ||
                    ((goal.get(0) == data.get(0)) && ((goal.get(1) == data.get(1) - 2) || (goal.get(1) == data.get(1) + 2))) ||
                    (goal.get(0) == (data.get(0) + 2) && ((goal.get(1) == data.get(1)) || (goal.get(1) == data.get(1) - 2) || (goal.get(1) == data.get(1) + 2))))
            {
                prev = "Close guess";
            }
            else
            {
                prev = "Complete miss";
            }

        }
        if(prev == "Near miss" || prev == "Close guess" && x.size() != 0)
        {
            temp = data;
            return temp;
        }
        else {
            return getrandom();
        }
    }
}
