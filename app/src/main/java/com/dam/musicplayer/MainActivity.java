package com.dam.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    /**
     * Variables globales
     **/
    private static final String TAG = "MainActivity";
    /**
     * ajout automatique de la lecture
     **/
    private MediaPlayer mediaPlayer;

    private SeekBar sbPosition;

    private RecyclerView recyclerView;
    private TextView tvCurrentPos, tvTotalDuration, tvSongTitle;
    private ImageView btnPrev, btnPlay, btnNext;

    private ArrayList<ModelSong> songArrayList;

    public static final int PERMISSION_READ = 0;

    private int songIndex = 0;

    double currentPos, totalDuration;


    // Méthode d'initialisation
    private void init() {
        // init UI
        sbPosition = findViewById(R.id.sbPosition);
        tvCurrentPos = findViewById(R.id.tvCurrentPosition);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        tvSongTitle = findViewById(R.id.id_songTitle);
        btnPrev = findViewById(R.id.id_btnPrevious);
        btnPlay = findViewById(R.id.id_btPlay);
        btnNext = findViewById(R.id.id_btn_next);
        recyclerView = findViewById(R.id.recyclerView);

        // init
        mediaPlayer = new MediaPlayer();
        songArrayList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    /*********** Methodes pour le fonctionnement de l'application ****************/
    // Méthode pour verifier les permissions de l'application
    public boolean checkPermission() {
        int READ_INTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (READ_INTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_READ: {
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "Please allow storage excess permission", Toast.LENGTH_SHORT).show();
                    } else {
                        // lancement de l'appli
                        setSong();
                    }
                }
            }
        }
    }

    public void getAudioFiles() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        // loop au travers de toutes les lignes et ajout dans le ArrayList
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Récupération des données pour injection dans le tableau
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                long album_id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                Uri coverFolder = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(coverFolder, album_id);

                // Utilisation du model pour remplir les valeurs dans le tableau
                ModelSong modelSong = new ModelSong();
                modelSong.setSongTitle(title);
                modelSong.setSongArtist(artist);
                modelSong.setSongUri(Uri.parse(url));
                modelSong.setSongcover(albumArtUri);
                modelSong.setSongDuration(duration);

                // Ajout de ces données dans le ArrayList
                songArrayList.add(modelSong);

            } while (cursor.moveToNext());
        }
        // Ajout de l'adapter qui va permettre l'affichage des données recuperées dans songArrayList
        AdapterSong adapterSong = new AdapterSong(this, songArrayList);
        // Adapatation des données de ArrayList au Recycler
        recyclerView.setAdapter(adapterSong);

        adapterSong.setOnItemClickListener(new AdapterSong.OnItemClickListener() {
            @Override
            public void OnItemClick(int position, View view) {
                playSong(position);

            }
        });
    }

    private void playSong (int pos){
        try {
            mediaPlayer.reset(); // pour commencer sur de bonnes bases
            mediaPlayer.setDataSource(this, songArrayList.get(pos).getSongUri());// Le chemin vers la chanson
            mediaPlayer.prepare();// cache mémoire pour la lecture
            mediaPlayer.start();

            // Affichage du bouton pause
            btnPlay.setImageResource(R.drawable.ic_pause_48_w);
            // Affichage du titre
            tvSongTitle.setText(songArrayList.get(pos).getSongTitle());
            // Position dans la liste pour le deplacement avec next et previous
            songIndex = pos;
        } catch (Exception e){
            e.printStackTrace();
        }
        // On lance la methode pour le deplacement de la seekbar et la gestion du temps des TV
        setSongProgress();

    }

    private void setSongProgress() {
        // recuperation des données du médiaPlayer
        currentPos = mediaPlayer.getCurrentPosition();
        totalDuration = mediaPlayer.getDuration(); // resultat en millis

        // Assignation des valeurs aux TV de gestion du temps
        tvCurrentPos.setText(timerConvertion((long) currentPos));
        tvTotalDuration.setText(timerConvertion((long) totalDuration));

        // gestion de la seekbar
        sbPosition.setMax((int) totalDuration);

        // gestion du thread qui ve s'occuper du temps "reel"
        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    currentPos = mediaPlayer.getCurrentPosition();
                    tvCurrentPos.setText(timerConvertion((long) currentPos));
                    sbPosition.setProgress((int) currentPos);
                    handler.postDelayed(this, 1000);
                } catch (IllegalStateException ed){
                    ed.printStackTrace();
                }
             }
        };
        handler.postDelayed(runnable, 1000);

    }

    public String timerConvertion(long value){
        String songDuration;
        int dur = (int) value; // la duree totale en millis
        int hrs = dur / 3600000;
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if(hrs > 0) {
            songDuration = String.format("%02d:%02d:%02d:", hrs, mns, scs);
        } else {
            songDuration = String.format("%02d:%02d:", mns, scs);
        }
        return songDuration;
    }


    private void setSong() {
    }

    public void play(View view) {
        mediaPlayer.start();
        Log.i(TAG, "play: ");

    }

    public void pause(View view) {
        mediaPlayer.pause();
        Log.i(TAG, "pause: ");
    }

    private void position() {
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
        }, 0, 300);


    }

    /*********** Cycles de vie **************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        checkPermission();
        getAudioFiles();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
    }
}