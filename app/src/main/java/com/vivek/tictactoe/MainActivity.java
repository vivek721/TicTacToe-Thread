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
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final int PLAYER_ONE_TAG = 1;
    private final int PLAYER_TWO_TAG = 2;
    private final int NEXTTURN = 3;
    public Handler playerOneHandler;
    public Handler playerTwoHandler;
    public ArrayList<Integer> randomSelector = new ArrayList<Integer>();
    ArrayList<Integer> movesPlayed = new ArrayList<Integer>();
    int rowVal = 0;
    private int roundCount;
    private int randomNumberIndexSelector;
    private int greedyNumber;
    //    private ArrayList<Integer> randomArray = new ArrayList<>();
    private int[][] winningCombo = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},  //row
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},  //column
            {0, 4, 8}, {2, 5, 6}              //diagonal
    };
    private Button[] buttons = new Button[9];
    private TextView currentPlayer;
    private Button resetButton;
    private Thread playerOneThread;
    private Thread playerTwoThread;
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
                    changeMoveOnBoard(index, PLAYER_ONE_TAG);
                    break;
                }
                case PLAYER_TWO_TAG: {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "handleMessage: Thread Interrupted");
                    }
                    changeMoveOnBoard(index, PLAYER_TWO_TAG);
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
            //resets the game to initial stage
            resetGame();
            // start thread 1
            if (playerOneThread != null && playerOneThread.isAlive()) {
                Log.i(TAG + " : ThreadStatus", "Thread 1 is alive");
                /* The message queues of the handlers are getting cleared
                as New Game is to be started */
                playerOneHandler.removeCallbacksAndMessages(null);
                playerOneThread.interrupt();
                uiHandler.removeCallbacksAndMessages(null);
                // Starting Thread 1 again for new game
            }
            // start thread 2
            if (playerTwoThread != null && playerTwoThread.isAlive()) {
                Log.i(TAG + " : ThreadStatus", "Thread 2 is alive");
                /* The message queues of the handlers are getting cleared as New Game is to be started */
                playerTwoHandler.removeCallbacksAndMessages(null);
                playerTwoThread.interrupt();
                uiHandler.removeCallbacksAndMessages(null);
                playerTwoHandler.getLooper().quit();

                // Starting Thread 2 again for New Game
            }

            playerOneThread = new Thread(new PlayerOneThread());
            playerOneThread.start();
            playerTwoThread = new Thread(new PlayerTwoThread());
            playerTwoThread.start();

        });

        currentPlayer = (TextView) findViewById(R.id.currentPlayer);
    }

    // This method resets the board and starts a new game between the threads.
    public void resetGame() {
        roundCount = 0;
        randomNumberIndexSelector = 0;
        currentPlayer.setText("");
        movesPlayed.clear();

        currentPlayer.setTextColor(Color.parseColor("#CDAA7D"));
        for (int i = 0; i <= 8; i++) {
            buttons[i].setText("");
            movesPlayed.add(2);
        }
    }

    private Boolean checkGameStatus() {
        boolean winnerResult = false;
        Log.i(TAG, "checkGameStatus: " + movesPlayed);
        for (int[] winningPosition : winningCombo) {
            if (movesPlayed.get(winningPosition[0]) == movesPlayed.get(winningPosition[1]) &&
                    movesPlayed.get(winningPosition[1]) == movesPlayed.get(winningPosition[2]) &&
                    movesPlayed.get(winningPosition[0]) != 2) {
                winnerResult = true;
            }
        }
        return winnerResult;
    }

    // This is player one strategy which returns a random number for player 1's move
    public int playerOneStrategy() {
        Random r = new Random();
        synchronized (r) {
            while (true) {
                rowVal = r.nextInt(9 - 0) + 0; // gets a random value between 0 and 9
                randomSelector.add(rowVal);
            /*if(!randomIndices.contains(rowVal))
                return random;*/

                if (movesPlayed.get(rowVal) == 2) {
                    break; // break the while loop if there is no value at the chosen
                }
            }
        }
        Log.i(TAG, "playerOneStrategy: random value = " + rowVal);
        return rowVal;
    }

    // This is player one strategy which uses minMax algorithm and returns number for player 1's move
    public int playerTwoStrategy() {
        StringBuilder gameString = new StringBuilder();
        for (int i : movesPlayed) {
            if (i == 1) {
                gameString.append("X ");
            } else if (i == 0) {
                gameString.append("O ");
            } else {
                gameString.append("b ");
            }
        }
        AI_MinMax minMax = new AI_MinMax(gameString.toString());
        greedyNumber = minMax.ans - 1;

        return greedyNumber;
    }

    // uiHandler calls this to handle the player
    public void changeMoveOnBoard(int index, int player) {
        roundCount++;
        Log.i(TAG, "changeMoveOnBoard: " + roundCount);
        // Player 1 uiHandler
        if (player == PLAYER_ONE_TAG) {
            movesPlayed.set(index, 0);
            buttons[index].setText("X");
            buttons[index].setTextColor(Color.parseColor("#70FF4A"));
            currentPlayer.setText("Player One Made A Move");
            if (checkGameStatus()) {
                Log.i(TAG, "changeMoveOnBoard: player 1 if condition ");
                currentPlayer.setText("Player one Wins");
                currentPlayer.setTextColor(Color.parseColor("#70FF4A"));
                Toast.makeText(getApplicationContext(), "Player one Wins !",
                        Toast.LENGTH_SHORT).show();
                endLoopers();
            } else if (roundCount >= 9) {
                Log.i(TAG, "changeMoveOnBoard: inside roundCount > 9 : ");
                if (!checkGameStatus()) {
                    currentPlayer.setText("Its a tie");
                    currentPlayer.setTextColor(Color.parseColor("#B960A7"));
                    Toast.makeText(getApplicationContext(), "Game Tied!",
                            Toast.LENGTH_SHORT).show();
                    endLoopers();
                }
            } else {
                Log.i(TAG, "changeMoveOnBoard: else part : ");
                Message mssg = playerTwoHandler.obtainMessage(NEXTTURN);
                playerTwoHandler.sendMessage(mssg);
            }
        }
        // Player 1 uiHandler
        if (player == PLAYER_TWO_TAG) {
            Log.i(TAG, "changeMoveOnBoard: " + index);
            if (index != -1) {
                movesPlayed.set(index, 1);
                buttons[index].setText("O");
                buttons[index].setTextColor(Color.parseColor("#FFC34A"));
                currentPlayer.setText("Player Two Made A Move");
            }
            if (checkGameStatus()) {
                currentPlayer.setText("Player Two Wins");
                currentPlayer.setTextColor(Color.parseColor("#FFC34A"));
                Toast.makeText(getApplicationContext(), "Player Two Wins !",
                        Toast.LENGTH_SHORT).show();
                endLoopers();
            } else if (roundCount >= 9) {
                if (!checkGameStatus()) {
                    currentPlayer.setText("Its a tie");
                    currentPlayer.setTextColor(Color.parseColor("#B960A7"));
                    Toast.makeText(getApplicationContext(), "Game Tied!",
                            Toast.LENGTH_SHORT).show();
                    endLoopers();
                }
            } else {
                Message mssg = playerOneHandler.obtainMessage(NEXTTURN);
                playerOneHandler.sendMessage(mssg);
            }
        }
    }

    //
    public void endLoopers() {
        playerOneHandler.getLooper().quit();
        playerTwoHandler.getLooper().quit();
    }

    // class for handling Thread one
    public class PlayerOneThread implements Runnable {
        public void run() {
            // Since worker threads do not have looper by default, the looper.prepare is called
            Looper.prepare();
            int index = playerOneStrategy();
            Message msg = uiHandler.obtainMessage(PLAYER_ONE_TAG);
            msg.arg1 = index;
            uiHandler.sendMessage(msg);
            Log.i(TAG, "run: thread 1 index = " + index);

            // thread 1 handler
            playerOneHandler = new Handler(Looper.myLooper()) {
                public void handleMessage(Message msg) {
                    if (msg.what == NEXTTURN) {
                        int index = playerOneStrategy();
                        Message mssg = uiHandler.obtainMessage(PLAYER_ONE_TAG);
                        mssg.arg1 = index;
                        uiHandler.sendMessage(mssg);
                    }
                }
            };

            // Starts the looper so that all the messages in the Message
            // Queue of playerOneHandler can be executed in sequence
            Looper.loop();
        }
    }

    // class for handling Thread two
    public class PlayerTwoThread implements Runnable {
        public void run() {
            // Since worker threads do not have looper by default, the looper.prepare is called
            Looper.prepare();

            //thread 2 handler
            playerTwoHandler = new Handler(Looper.myLooper()) {
                public void handleMessage(Message msg) {

                    if (msg.what == NEXTTURN) {
                        int index = playerTwoStrategy();
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