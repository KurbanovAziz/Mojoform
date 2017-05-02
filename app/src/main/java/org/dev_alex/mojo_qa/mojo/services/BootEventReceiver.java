package org.dev_alex.mojo_qa.mojo.services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmService.scheduleAlarm(context);
    }
}
