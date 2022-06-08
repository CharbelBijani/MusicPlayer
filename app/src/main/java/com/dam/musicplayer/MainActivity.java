package com.dam.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    /**  Variables globales    **/
     private static final String TAG = "MainActivity";



    /** ajout automatique de la lecture **/

    MediaPlayer mediaPlayer = new MediaPlayer();

    /*********** Methodes pour le fonctionnement de l'application ****************/
    public void play(View view){
        mediaPlayer.start();
        Log.i(TAG, "play: ");

    }

    public void pause(View view){
        mediaPlayer.pause();
        Log.i(TAG, "pause: ");
    }

    private void position(){
        SeekBar sbPosition = findViewById(R.id.sbPosition);
        // Definir la valeur max de la seekbar
        sbPosition.setMax(mediaPlayer.getDuration());

        // part one la gestion du deplacement du curseur par l'utilisateur
        sbPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, "Position dans le morceau : " + Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pause(sbPosition);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                play(sbPosition);
                mediaPlayer.seekTo(sbPosition.getProgress());

            }
        });

        // part two gestion du deplacement du curseru par l'application
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // deplacement automatique
                sbPosition.setProgress(mediaPlayer.getCurrentPosition());
            }
        },0, 300);


    }

    /*********** Cycles de vie **************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** Methode 1 lecture automatique du son
        mediaPlayer = MediaPlayer.create(this, R.raw.sound);
        mediaPlayer.start();
         **/

        /** Methode 2 avec les boutons **/
        mediaPlayer=MediaPlayer.create(this, R.raw.sound2);
        // Lancement des methodes
        //volume();
        position();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
    }
}