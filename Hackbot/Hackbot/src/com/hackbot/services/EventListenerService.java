package com.hackbot.services;

import java.util.ArrayList;
import java.util.List;

import com.hackbot.businessLogic.Algo;
import com.hackbot.dao.DBHelper;
import com.hackbot.entity.EventIdConstant;
import com.hackbot.entity.Events;
import com.hackbot.utility.Enums;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class EventListenerService extends Service {

	EventBroadcastAudioReceiver receiverAudio;
	EventBroadcastDataReceiver receiverData;
	EventBroadcastBluetoothReceiver receiverBluetooth;
	private static List<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>();
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
        algo.destroy();
        for (BroadcastReceiver receiver : receivers)
        {
        	unregisterReceiver(receiver);
        }

    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
		algo = new Algo (this);
		receiverAudio = new EventBroadcastAudioReceiver();
		receiverData = new EventBroadcastDataReceiver();
		receiverBluetooth = new EventBroadcastBluetoothReceiver();
		
		return START_STICKY;
	}
	
	public void setBroadCastReciever(List<Events> events)
	{	
		
		for (Events event : events)
		{
			Log.d("listener", "Event Id got :"+event.getId());
			switch(event.getId())
			{
			case EventIdConstant.AUDIO_ON:
				if (!(receivers.contains(receiverAudio)))
				{
					IntentFilter filter=new IntentFilter();
					filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
					registerReceiver(receiverAudio, filter);
					receivers.add(receiverAudio);
				}
				break;
			case EventIdConstant.AUDIO_OFF:
				if (receivers.contains(receiverAudio))
				{
					unregisterReceiver(receiverAudio);
					handleUnregisterOperation(event.getId());
					receivers.remove(receiverAudio);
				}
				break;
			case 3:
				if (!(receivers.contains(receiverData)))
				{
					IntentFilter filter2=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
					registerReceiver(receiverData, filter2);
					receivers.add(receiverData);
				}
				break;
			case 4:
				if (receivers.contains(receiverData))
				{
					unregisterReceiver(receiverData);
					handleUnregisterOperation(event.getId());
					receivers.remove(receiverData);
				}
				break;
			case 5:	
				if (!(receivers.contains(receiverBluetooth)))
				{
					IntentFilter filter3=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
					registerReceiver(receiverBluetooth, filter3);
					receivers.add(receiverBluetooth);
				}
				break;
			case 6:
				if (receivers.contains(receiverBluetooth))
				{
					unregisterReceiver(receiverBluetooth);
					handleUnregisterOperation(event.getId());
					receivers.remove(receiverBluetooth);
				}
				break;
			}
		}
	}

	private void handleUnregisterOperation(int eventId)
	{
		if (eventId == EventIdConstant.AUDIO_OFF)
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
