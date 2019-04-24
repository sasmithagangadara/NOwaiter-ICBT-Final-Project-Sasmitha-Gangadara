package com.example.noob.colombopizza.services;

import android.util.Log;

import com.example.noob.colombopizza.Common.Common;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class PizzaGetInstanceIDServices extends FirebaseInstanceIdService {

    String TAG = "PizzaGetInstanceIDServices";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */

    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(token);
        System.out.println("token --->"+token);
        Common.FCM_Token = token;
    }

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.d(TAG, refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        System.out.println("token --->"+refreshedToken);
        Common.FCM_Token = refreshedToken;
    }


}
