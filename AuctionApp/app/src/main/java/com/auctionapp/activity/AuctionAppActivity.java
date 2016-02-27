package com.auctionapp.activity;

import android.support.v7.app.AppCompatActivity;

import com.auctionapp.AuctionApplication;
import com.auctionapp.model.User;

/**
 * Created by apple on 2/25/16.
 */
public class AuctionAppActivity extends AppCompatActivity {
    public AuctionApplication getApplicationContext() {
        return (AuctionApplication) getApplication();
    }

    public User getUser() {
        return getApplicationContext().getLoggedUser();
    }
}
