package com.phunware.javasample;

import android.support.v7.app.AppCompatActivity;

class Demo {
    private String title;
    private String description;
    private final Class<? extends AppCompatActivity> activityClass;

    Demo(String title, String description,
         Class<? extends AppCompatActivity> activityClass) {
        this.title = title;
        this.description = description;
        this.activityClass = activityClass;
    }

    String getTitle() {
        return title;
    }

    String getDescription() {
        return description;
    }

    Class<? extends AppCompatActivity> getActivityClass() {
        return activityClass;
    }
}
