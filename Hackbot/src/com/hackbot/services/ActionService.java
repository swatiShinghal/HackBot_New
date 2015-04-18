package com.hackbot.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.hackbot.entity.HackBotEvent;
import com.hackbot.services.EventListenerService.LocalBinder;
import com.hackbot.utility.Enums.EventIdConstant;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class ActionService extends Service {

	private List<HackBotEvent> eventsListened = new ArrayList<HackBotEvent>();
	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		public ActionService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return ActionService.this;
		}
	}

	@Override
	public void onCreate() {

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Toast.makeText(this, "Action Service Started", Toast.LENGTH_LONG)
				.show();

		final List<HackBotEvent> eventList = new ArrayList<HackBotEvent>();

		Timer timer = new Timer();
		TimerTask hourlyTask = new TimerTask() {
			@Override
			public void run() {

				for (HackBotEvent bot : eventList) {
					// if(bot.getTimeToTrigger())
					// {
					switch (bot.getEventId()) {
					case EventIdConstant.AUDIO_OFF:
					case EventIdConstant.AUDIO_ON:
						audioChange(bot.getValue());
						break;
						
					case EventIdConstant.BLUETOOTH_OFF:
					case EventIdConstant.BLUETOOTH_ON:
						bluetoothChange(bot.getValue());
						break;
						
					case EventIdConstant.DATA_OFF:
					case EventIdConstant.DATA_ON:
						dataChange(bot.getValue());
						break;

					// }
					}
				}
			}
		};

		// schedule the task to run starting now and then every minute...
		timer.schedule(hourlyTask, 0l, 1000 * 1 * 60);
		return START_STICKY;
	}

	private void audioChange(int valueSet) {
		AudioManager am;
		am = (AudioManager) getBaseContext().getSystemService(
				Context.AUDIO_SERVICE);
		if(valueSet==1)
			am.setRingerMode(2);
		else
			am.setRingerMode(0);
	}

	private void bluetoothChange(int valueSet) {
		BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
		if (valueSet==1)
			bluetooth.enable();
		else
			bluetooth.disable();
	}

	private void dataChange(int valueSet) {
		ConnectivityManager dataManager;
		boolean value=false;
		if(valueSet==1)
			value=true;
		dataManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		Method dataMtd;
		try {
			dataMtd = ConnectivityManager.class.getDeclaredMethod(
					"setMobileDataEnabled", boolean.class);
			dataMtd.setAccessible(value);
			try {
				dataMtd.invoke(dataManager, value);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void fillListenedEventList(HackBotEvent updatedEvent) {
		boolean isNew = true;
		if (updatedEvent != null && updatedEvent.getId() != 0) {
			for (Iterator iterator = eventsListened.iterator(); iterator
					.hasNext();) {
				HackBotEvent event = (HackBotEvent) iterator.next();
				if (updatedEvent.getId() == event.getId()) {
					if (updatedEvent.getIsLearned() == 0)
						eventsListened.remove(event);
					else
						event = updatedEvent;
					isNew = false;
					break;
				}
			}
			if (isNew)
				eventsListened.add(updatedEvent);
		} else {
			for (Iterator iterator = eventsListened.iterator(); iterator
					.hasNext();) {
				HackBotEvent event = (HackBotEvent) iterator.next();
				if (updatedEvent.getEventId() == event.getEventId())
					eventsListened.remove(event);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return true;
	}
}