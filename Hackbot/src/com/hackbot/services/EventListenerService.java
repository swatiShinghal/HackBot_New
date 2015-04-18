package com.hackbot.services;

import java.util.List;

import com.hackbot.businessLogic.Algo;
import com.hackbot.dao.DBHelper;
import com.hackbot.entity.Events;
import com.hackbot.utility.Enums;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class EventListenerService extends Service {

	EventBroadcastAudioReceiver receiverAudio;
	EventBroadcastDataReceiver receiverData;
	EventBroadcastBluetoothReceiver receiverBluetooth;
	private Algo algo ;
	
	private DBHelper dbHelper;
	
	private final IBinder mBinder = new LocalBinder(); 

	public class LocalBinder extends Binder { 
		public EventListenerService getService() { 
			// Return this instance of LocalService so clients can call public methods 
			return EventListenerService.this; 
		} 
	} 


	@Override
	public IBinder onBind(Intent arg0) {

		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		dbHelper= new DBHelper(this);
	}

	@Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        unregisterReceiver(receiverAudio);
        unregisterReceiver(receiverData);
        unregisterReceiver(receiverBluetooth);
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
		algo = new Algo (this);
		receiverAudio = new EventBroadcastAudioReceiver(algo);
		receiverData = new EventBroadcastDataReceiver(algo);
		receiverBluetooth = new EventBroadcastBluetoothReceiver(algo);
		
		return START_STICKY;
	}
	
	public void setBroadCastReciever(List<Events> events)
	{
		
		
		for (Events event : events)
		{
			switch(event.getId())
			{
			case 1:
				IntentFilter filter=new IntentFilter();
				filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
				registerReceiver(receiverAudio, filter);
				break;
			case 2:
				unregisterReceiver(receiverAudio);
				handleUnregisterOperation(event.getId());
				break;
			case 3:
				IntentFilter filter2=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
				registerReceiver(receiverData, filter2);
				break;
			case 4:
				unregisterReceiver(receiverData);
				handleUnregisterOperation(event.getId());
				break;
			case 5:		
				IntentFilter filter3=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
				registerReceiver(receiverBluetooth, filter3);
				break;
			case 6:
				unregisterReceiver(receiverBluetooth);
				handleUnregisterOperation(event.getId());
				break;
			}
		}
	}
	
	private void handleUnregisterOperation(int eventId)
	{
		if (eventId == 2)
		{
		algo.deleteEvents(Enums.EventIdConstant.AUDIO_ON);
		}
		else if (eventId == 4)
		{
			algo.deleteEvents(Enums.EventIdConstant.DATA_ON);
		}
		else if (eventId == 6)
		{
			algo.deleteEvents(Enums.EventIdConstant.BLUETOOTH_ON);
			
		}
			
	}

	
}
