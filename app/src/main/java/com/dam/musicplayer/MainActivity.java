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
    // START Volume
  /** private void volume(){
        // association de la seekbar au java
        SeekBar sbVolume = findViewById(R.id.sbVolume);

        // initialiser le manager en tant que service
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Volume max du terminal.
        int volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // valorisation de cette valeur au max de la seekbar
        sbVolume.setMax(volumeMax);

        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // ajustement de la position du curseur
        sbVolume.setProgress(currentVolume);

        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, "onProgressChanged: Volume = " + Integer.toString(progress));
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress,0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }**/
    // END Volume

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
        mediaPlayer=MediaPlayer.create(this, R.raw.sound1);
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