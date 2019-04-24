package com.hamami.hamamimusicapp.Models;


import android.net.Uri;

import java.io.File;

public class Item {
    private String name;
    private Uri uri;
    private File file;

    public Item(String name, Uri uri,File file) {
        this.name = name;
        this.uri = uri;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
