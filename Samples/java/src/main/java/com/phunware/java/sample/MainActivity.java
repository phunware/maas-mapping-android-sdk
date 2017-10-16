package com.phunware.java.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity implements DemoAdapter.DemoOnClickListener {
    private static final int REQUEST_PERMISSION_LOCATION_FINE = 1;
    private DemoAdapter demoAdapter;
    private RelativeLayout content;
    private Snackbar permissionsSnackbar;
    private Demo clickedDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        content = findViewById(R.id.content);

        // Instantiate all demo info
        DemoDetailsList demoDetails = new DemoDetailsList(this);

        // Create list of all demos
        RecyclerView demoRecyclerView = findViewById(R.id.demo_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        demoRecyclerView.setLayoutManager(layoutManager);
        demoAdapter = new DemoAdapter(demoDetails.getDemos(), this);
        demoRecyclerView.setAdapter(demoAdapter);

        checkPermissions(this);
    }

    @Override
    public void onItemClicked(String title) {
        clickedDemo = demoAdapter.getItem(title);
        if (clickedDemo != null) {
            if (canAccessLocation()) {
                startDemo(clickedDemo);
            } else {
                checkPermissions(this);
            }
        }
    }

    private void startDemo(Demo demo) {
        startActivity(new Intent(this, demo.getActivityClass()));
    }

    /**
     * Permission Checks
     **/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_LOCATION_FINE) {
            if (canAccessLocation()) {
                if (permissionsSnackbar != null) {
                    permissionsSnackbar.dismiss();
                }
                if (clickedDemo != null) {
                    startDemo(clickedDemo);
                    clickedDemo = null;
                }
            } else if (!canAccessLocation() && content != null) {
                permissionsSnackbar = Snackbar.make(content, R.string.permission_snackbar_message,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivityForResult(
                                        new Intent(android.provider.Settings.ACTION_SETTINGS),
                                        REQUEST_PERMISSION_LOCATION_FINE);
                            }
                        });
                permissionsSnackbar.show();
            }
        }
    }

    private boolean checkPermissions(Activity activity) {
        if (activity != null && !canAccessLocation()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_PHONE_STATE},
                        REQUEST_PERMISSION_LOCATION_FINE);
                return false;
            }
        }
        return true;
    }

    private boolean canAccessLocation() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        //noinspection SimplifiableIfStatement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
        }
        return true;
    }
}
