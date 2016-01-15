package com.meljin.maprun;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by s_jin01 on 1/6/16.
 */
public class EndGame extends FragmentActivity {
    private TextView message;
    private TextView score;
    private TextView time;
    private Button menu;
    private Button playAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_game);

        message = (TextView) findViewById(R.id.message);
        if(MapsActivity.endGameCond == 0) {
            endOnWin(message);
        }
        else if(MapsActivity.endGameCond == 1){
            endOnLose(message);
        }
        else{
            endOnButtonPress(message);
        }

        score = (TextView) findViewById(R.id.score);
        score.setText("Score: " + Player.score);

        time = (TextView) findViewById(R.id.time);
        time.setText("Duration: " + MapsActivity.timerVal);
        menu = (Button) findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), Menu.class);
                startActivity(intent);
                finish();
            }
        });

        playAgain = (Button) findViewById(R.id.play_again);
        playAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), MapsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public static void endOnWin(TextView t){
        t.setText("Congratulations! You collected all the points!");
    }

    public static void endOnLose(TextView t){
        t.setText("Oh no! You were caught by a ghost!");
    }

    public static void endOnButtonPress(TextView t){
        t.setText("DON'T BE LAZY, KEEP RUNNING!");
    }
}
