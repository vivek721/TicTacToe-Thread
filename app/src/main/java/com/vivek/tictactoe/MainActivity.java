package com.vivek.tictactoe;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final int PLAYER_ONE_TAG = 1;
    private final int PLAYER_TWO_TAG = 2;
    private final int NEXTTURN = 3;

    public Handler playerOneHandler;
    public Handler playerTwoHandler;
    int[] gameState = new int[9];
    private int roundCount;
    private Button resetButton;
    private Button[] buttons = new Button[9];
    private TextView currentPlayer;
    private Thread playerOneThread;
    private Thread playerTwoThread;
    private ArrayList<Integer> movesArray = new ArrayList<>();
    private int[][] winningCombo = {
            {1, 2, 3}, {4, 5, 6}, {7, 8, 9},  //row
            {1, 4, 7}, {2, 5, 8}, {3, 6, 9},  //column
            {1, 5, 9}, {3, 5, 7}              //diagonal
    };
    private int randomNumberSelectorIndex;


    private Handler uiHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            Log.i(TAG, "inside UIHandler");
            int currentState = msg.what;
            int index = (int) msg.arg1; //arg1 contains the index of button
            switch (currentState) {
                case PLAYER_ONE_TAG: {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "handleMessage: Thread Interrupted");
                    }
                    gameState[index] = 0;
                    buttons[index].setText("X");
                    buttons[index].setTextColor(Color.parseColor("#70FF4A"));
                    currentPlayer.setText("Player One Made A Move");
                    roundCount++;
                    if (checkGameStatus()) {
                        currentPlayer.setText("Player one Wins");
                        currentPlayer.setTextColor(Color.parseColor("#70FF4A"));
                        Toast.makeText(getApplicationContext(), "Player one Wins !",
                                Toast.LENGTH_SHORT).show();
                        playerOneHandler.getLooper().quit();
                        playerTwoHandler.getLooper().quit();
                    } else if (roundCount > 9) {
                        if (!checkGameStatus()) {
                            currentPlayer.setText("Its a tie");
                            currentPlayer.setTextColor(Color.parseColor("#B960A7"));
                            Toast.makeText(getApplicationContext(), "Game Tied!",
                                    Toast.LENGTH_SHORT).show();
                            playerOneHandler.getLooper().quit();
                            playerTwoHandler.getLooper().quit();
                        }
                    } else {
                        Message mssg = playerTwoHandler.obtainMessage(NEXTTURN);
                        playerTwoHandler.sendMessage(mssg);
                    }
                    break;
                }
                case PLAYER_TWO_TAG: {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "handleMessage: Thread Interrupted");
                    }
                    gameState[index] = 1;
                    buttons[index].setText("O");
                    buttons[index].setTextColor(Color.parseColor("#FFC34A"));
                    currentPlayer.setText("Player Two Made A Move");
                    roundCount++;
                    if (checkGameStatus()) {
                        currentPlayer.setText("Player Two Wins");
                        currentPlayer.setTextColor(Color.parseColor("#FFC34A"));
                        Toast.makeText(getApplicationContext(), "Player Two Wins !",
                                Toast.LENGTH_SHORT).show();
                        playerOneHandler.getLooper().quit();
                        playerTwoHandler.getLooper().quit();
                    } else if (roundCount > 9) {
                        if (!checkGameStatus()) {
                            currentPlayer.setText("Its a tie");
                            currentPlayer.setTextColor(Color.parseColor("#B960A7"));
                            Toast.makeText(getApplicationContext(), "Game Tied!",
                                    Toast.LENGTH_SHORT).show();
                            playerOneHandler.getLooper().quit();
                            playerTwoHandler.getLooper().quit();
                        }
                    } else {
                        Message mssg = playerOneHandler.obtainMessage(NEXTTURN);
                        playerOneHandler.sendMessage(mssg);
                    }
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int i = 0; i <= 8; i++) {
            String idString = "btn_" + (i + 1);
            int buttonId = getResources().getIdentifier(idString, "id", getPackageName());
            buttons[i] = this.findViewById(buttonId);
        }

        resetButton = this.findViewById(R.id.ResetButton);
        resetButton.setOnClickListener(v -> {
            resetGame();
            playerOneThread = new Thread(new PlayerOneThread());
            playerOneThread.start();
            playerTwoThread = new Thread(new PlayerTwoThread());
            playerTwoThread.start();
        });

        currentPlayer = (TextView) findViewById(R.id.currentPlayer);
        roundCount = 0;
    }

    // This method resets the board
    public void resetGame() {
        roundCount = 0;
        movesArray.clear();
        randomNumberSelectorIndex = 0;
        currentPlayer.setText("");

        currentPlayer.setTextColor(Color.parseColor("#CDAA7D"));
        for (int i = 0; i <= 8; i++) {
            buttons[i].setText("");
            gameState[i] = 2;
            movesArray.add(i);
        }
        Collections.shuffle(movesArray);
    }

    private Boolean checkGameStatus() {
        boolean winnerResult = false;

        for (int[] winningPosition : winningCombo) {
            if (gameState[winningPosition[0] - 1] == gameState[winningPosition[1] - 1] &&
                    gameState[winningPosition[1] - 1] == gameState[winningPosition[2] - 1] &&
                    gameState[winningPosition[0] - 1] != 2) {
                winnerResult = true;
            }
        }
        return winnerResult;
    }

    public int randomSelector() {
        Log.i(TAG, "randomGenerator: thread : " + movesArray);
        int randomNumber = 0;
        if (randomNumberSelectorIndex < 9)
            randomNumber = movesArray.get(randomNumberSelectorIndex);
        randomNumberSelectorIndex++;
        return randomNumber;
    }

    public class PlayerOneThread implements Runnable {
        public void run() {
            // Since worker threads do not have looper by default, the looper.prepare is called
            Looper.prepare();
            int index = randomSelector();
            Message msg = uiHandler.obtainMessage(PLAYER_ONE_TAG);
            msg.arg1 = index;
            uiHandler.sendMessage(msg);
            Log.i(TAG, "run: thread 1 index = " + index);
            playerOneHandler = new Handler(Looper.myLooper()) {
                public void handleMessage(Message msg) {
                    if (msg.what == NEXTTURN) {
                        int index = randomSelector();
                        Message mssg = uiHandler.obtainMessage(PLAYER_ONE_TAG);
                        mssg.arg1 = index;
                        uiHandler.sendMessage(mssg);
                    }
                }
            };

            // Starts the looper so that all the messages in the Message
            //    Queue of playerOneHandler can be executed in sequence
            Looper.loop();
        }
    }

    public class PlayerTwoThread implements Runnable {
        public void run() {
            // Since worker threads do not have looper by default, the looper.prepare is called
            Looper.prepare();
            playerTwoHandler = new Handler(Looper.myLooper()) {
                public void handleMessage(Message msg) {
                    if (msg.what == NEXTTURN) {
                        int index = randomSelector();
                        Message mssg = uiHandler.obtainMessage(PLAYER_TWO_TAG);
                        mssg.arg1 = index;
                        uiHandler.sendMessage(mssg);
                    }
                }
            };

            // Starts the looper so that all the messages in the Message
            //    Queue of playerOneHandler can be executed in sequence
            Looper.loop();
        }
    }

}