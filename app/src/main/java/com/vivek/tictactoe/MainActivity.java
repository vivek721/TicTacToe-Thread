package com.vivek.tictactoe;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "MainActivity";
    private Handler handler = new Handler(Looper.getMainLooper());
    private Button resetButton;
    private Button[] buttons = new Button[9];
    private TextView resultDisplay;


    private Boolean activePlayer;   // true for player1 and false for player2

    private int[][] winningCombo = {
            {1, 2, 3}, {4, 5, 6}, {7, 8, 9},  //row
            {1, 4, 7}, {2, 5, 8}, {3, 6, 9},  //column
            {1, 5, 9}, {3, 5, 7}              //diagonal
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        resetButton = this.findViewById(R.id.ResetButton);
        for (int i = 0; i <= 8; i++) {
            String idString = "btn_" + (i + 1);
            Log.i(TAG, "initializeButtons: " + idString);
            int buttonId = getResources().getIdentifier(idString, "id", getPackageName());
            buttons[i] = this.findViewById(buttonId);
//            buttons[i].setOnClickListener(this);
        }


        resetButton.setOnClickListener(v -> {
            resetGame();
            Thread t1 = new Thread(new threadOneRunnable()) ;
            t1.start();
        });
        activePlayer = true;
    }

    private void resetGame() {
        for (int i = 0; i <= 8; i++) {
            buttons[i].setText("");
        }
    }


    public void onThreadMove(View v) {
//        Log.i(TAG, "onClick");
//        if (!((Button) v).getText().equals("")) {
//            Toast.makeText(this, "Select Different Box", Toast.LENGTH_SHORT).show();
//            return;
//        }
////        String buttonId = v.getResources().getResourceEntryName(v.getId());
//        if (activePlayer) {
//            ((Button) v).setText("X");
//            ((Button) v).setTextColor(Color.parseColor("#FFC34A"));
//            activePlayer = false;
//        } else {
//            ((Button) v).setText("0");
//            ((Button) v).setTextColor(Color.parseColor("#70FF4A"));
//            activePlayer = true;
//        }
    }

    private class threadOneRunnable implements Runnable {
        @Override
        public void run() {

        }
    }
}