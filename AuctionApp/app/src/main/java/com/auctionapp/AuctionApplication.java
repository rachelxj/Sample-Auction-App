package com.auctionapp;

import android.app.Application;

import com.auctionapp.model.User;

/**
 * Created by apple on 2/25/16.
 */
public class AuctionApplication extends Application {
    private User loggedUser;

    public User getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(User loggedUser, boolean rememberMe) {
        this.loggedUser = loggedUser;
        if (rememberMe && loggedUser != null) {
            new Preferences(this).saveLoggedUserId(loggedUser.getId());
        }
    }
}
