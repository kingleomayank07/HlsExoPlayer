package com.example.samplehlsplayer.model;

import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class M3UParser {

    private static final String EXT_M3U = "#EXTM3U";
    private static final String EXT_INF = "#EXT-X-STREAM-INF:";
    private static final String EXT_PLAYLIST_NAME = "VIDEO=";
    private static final String EXT_LOGO = "tvg-logo";
    private static final String EXT_URL = "http://";

    private String convertStreamToString(InputStream is) {
        try {
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    public Iterator<String> readPlaylist(String string) {
        return Arrays.asList(string.split("\n")).iterator();
    }

    public M3UPlaylist parseFile(String inputStream) {
        M3UPlaylist m3UPlaylist = new M3UPlaylist();
        ArrayList<M3UItem> playlistItems = new ArrayList<>();
//        String stream = convertStreamToString(inputStream);
        String[] linesArray = inputStream.split(EXT_INF);
        for (String currLine : linesArray) {
            if (currLine.contains(EXT_M3U)) {
                //header of file
                if (currLine.contains(EXT_PLAYLIST_NAME)) {
                    String fileParams = currLine.substring(EXT_M3U.length(), currLine.indexOf(EXT_PLAYLIST_NAME));
                    String playListName = currLine.substring(currLine.indexOf(EXT_PLAYLIST_NAME) + EXT_PLAYLIST_NAME.length()).replace(":", "");
                    m3UPlaylist.setPlaylistName(playListName);
                    m3UPlaylist.setPlaylistParams(fileParams);
                } else {
                    m3UPlaylist.setPlaylistName("Noname Playlist");
                    m3UPlaylist.setPlaylistParams("No Params");
                }
            } else {
                M3UItem playlistItem = new M3UItem();
                String[] dataArray = currLine.split(",");
                if (dataArray[0].contains(EXT_LOGO)) {
                    String duration = dataArray[0].substring(0, dataArray[0].indexOf(EXT_LOGO)).replace(":", "").replace("\n", "");
                    String icon = dataArray[0].substring(dataArray[0].indexOf(EXT_LOGO) + EXT_LOGO.length()).replace("=", "").replace("\"", "").replace("\n", "");
                    playlistItem.setItemDuration(duration);
                    playlistItem.setItemIcon(icon);
                } else {
                    String duration = dataArray[0].replace(":", "").replace("\n", "");
                    playlistItem.setItemDuration(duration);
                    playlistItem.setItemIcon("");
                }
                try {
                    String url = dataArray[1].substring(dataArray[1].indexOf(EXT_URL)).replace("\n", "").replace("\r", "");
                    String name = dataArray[1].substring(0, dataArray[1].indexOf(EXT_URL)).replace("\n", "");
                    playlistItem.setItemName(name);
                    playlistItem.setItemUrl(url);
                } catch (Exception fdfd) {
                    Log.e("Google", "Error: " + fdfd.fillInStackTrace());
                }
                playlistItems.add(playlistItem);
            }
        }
        m3UPlaylist.setPlaylistItems(playlistItems);
        return m3UPlaylist;
    }
}
