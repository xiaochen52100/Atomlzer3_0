package com.example.atomlzer30;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BooTBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent newIntent = new Intent(context,MainActivity.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newIntent);
        }

    }

}

