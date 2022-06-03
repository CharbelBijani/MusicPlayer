package com.dam.musicplayer;

import android.net.Uri;

public class ModelSong {

    private String songTitle;
    private String songArtist;
    private String songDuration;
    private Uri songcover;
    private Uri songUri;

    public ModelSong() {
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(String songDuration) {
        this.songDuration = songDuration;
    }

    public Uri getSongcover() {
        return songcover;
    }

    public void setSongcover(Uri songcover) {
        this.songcover = songcover;
    }

    public Uri getSongUri() {
        return songUri;
    }

    public void setSongUri(Uri songUri) {
        this.songUri = songUri;
    }
}
