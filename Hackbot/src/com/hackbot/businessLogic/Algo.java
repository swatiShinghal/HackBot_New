package com.hackbot.businessLogic;

import java.util.Calendar;
import java.util.Date;

import com.hackbot.dao.DBHelper;
import com.hackbot.entity.EventsTracked;
import com.hackbot.entity.HackBotEvent;
import com.hackbot.services.ActionService;
import com.hackbot.utility.Constants;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;



public class Algo {
	
	private Context context;
	private DBHelper dbh;
	private ActionService mService;
	boolean mBound = true;
	
	
	public Algo(Context context){
		this.context = context;
		this.dbh = new DBHelper(this.context);
		Intent intent = new Intent(context, ActionService.class); 
		context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		
	}
	
	public void writeToDB(int eventId, long timeTriggered, int value){
		Log.d("Algo Start:", "Starting the algo...");
		HackBotEvent hbe=null;
		
		if(dbh.isNewEvent(eventId,timeTriggered)==1){		//this is called when its is the first event of its type
															//and has no related entry in the DB.
			Log.d("Algo:", "This is a new event");
			hbe = new HackBotEvent();
			
			String patternArray[] = {"0","0","0","0","0","0","0"};
			StringBuilder pattern = new StringBuilder();
			hbe.setEventId(eventId);
			hbe.setFirstOccurrence(timeTriggered);
			hbe.setLastOccurrence(timeTriggered);
			hbe.setTimesOccurred("1");
			hbe.setDaysTracked(1);
			
			//pattern. It begins from Sunday, Monday ...
			Date d = new Date(timeTriggered);
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			patternArray[dayOfWeek-1] = "1";
			for (String str : patternArray) {
				pattern.append(str);
			}
			hbe.setPattern(pattern.toString());
			//pattern
			
			hbe.setRepeatedWeekly(-1);
			hbe.setRepeatInDays(-1);
			hbe.setDuration(0);			//at beginning set the duration 0
			
			//time to trigger
			long timeToTrigger = c.get(Calendar.HOUR_OF_DAY) * 60;
			timeToTrigger += c.get(Calendar.MINUTE);
			hbe.setTimeToTrigger(timeToTrigger);
			//time to trigger
			
//			if(eventId==1){	//the case when silent
//				hbe.setEventValueOn(-1);
//				hbe.setEventValueOff(-1);
//			}
//			else{	//when brightness and volume
//				hbe.setEventValueOn(eventValueOn);
//				hbe.setEventValueOff(eventValueOff);
//			}
			
			hbe.setProbability(100);
			hbe.setDaysFulfilled(setDaysFulfilled(hbe));
			hbe.setIsLearned(setLearned(hbe));
			hbe.setValue(value);
			long id = dbh.insertToHackBotEvents(hbe);
			
			//put this event in the EventsTracked table to get the duration next time
			EventsTracked et = new EventsTracked();
			et.setEventId(eventId);
			et.setHbeId((int)id);
			dbh.insertEventsTracked(et);
			

			
		}
		
		else if(dbh.isEventToLearn(eventId,timeTriggered)==1){	//this will be true when an event in found in the DB which 
								//has the same approx timeToTrigger and this event should not be in the EventsTracked table
			Log.d("Algo:", "this is more than one entry in the DB");
			int isEventRunning = dbh.isEventRunning(eventId);
			
			if(isEventRunning!=1){			//check if this event is already running by the PerformActionService
				 hbe = dbh.getData(eventId, timeTriggered, Constants.TIME_RANGE);
				hbe.setTimeToTrigger((hbe.getTimeToTrigger() + timeTriggered)/2);

				//RepeatedWeekly, when not set, second call
				if(hbe.getRepeatedWeekly()==-1){
					long timeDifference = timeTriggered - hbe.getLastOccurrence();

					if(timeDifference <= (7 * Constants.MILLISECONDS_A_DAY + (Constants.TIME_RANGE))){
						hbe.setRepeatedWeekly(1);
					}
					else
						hbe.setRepeatedWeekly(0);

				}

				//weekly pattern, when repeated weekly
				if(hbe.getRepeatedWeekly()==1){

					Date d = new Date(timeTriggered);
					Calendar c = Calendar.getInstance();
					c.setTime(d);
					int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

					StringBuilder patternSB = new StringBuilder();
					String pattern = hbe.getPattern();
					String patternArray[] = stringToArray(pattern);
					patternArray[dayOfWeek] = "1";

					for (String str : patternArray) {
						patternSB.append(str);
					}

					hbe.setPattern(patternSB.toString());
					hbe.setRepeatInDays(-1);
				}
				
				else{		//it is not a weekly pattern, repeated in more than 7 days
					long differenceInDays = (timeTriggered - hbe.getLastOccurrence() + Constants.TIME_RANGE)/(Constants.MILLISECONDS_A_DAY);
					if(hbe.getRepeatInDays()==-1){		//event occurred second time
						hbe.setRepeatInDays(differenceInDays);
						hbe.setPattern("0000000");
					}
					
					//there can be case when the 

					//TODO
				}

				String timesOccured = hbe.getTimesOccurred();
				String timesOccuredArray[] = stringToArray(timesOccured);
				if(timesOccuredArray.length<15){
					String timesOccuredArrayTemp[] = new String[timesOccuredArray.length+1];
					timesOccuredArrayTemp[0] = "1";
					System.arraycopy(timesOccuredArray, 0, timesOccuredArrayTemp, 1, timesOccuredArray.length);
					hbe.setTimesOccurred(convertArrayToString(timesOccuredArrayTemp));
				}
				else{
					for(int i=timesOccuredArray.length-1;i>=1;i++){
						timesOccuredArray[i] = timesOccuredArray[i-1];
					}
					timesOccuredArray[0] = "1";
					hbe.setTimesOccurred(convertArrayToString(timesOccuredArray));
				}


		//		hbe.setEventValueOn((hbe.getEventValueOn() + eventValueOn)/2);
		//		hbe.setEventValueOff((hbe.getEventValueOff() + eventValueOff)/2);
				hbe.setDaysTracked((hbe.getFirstOccurrence() - timeTriggered + Constants.TIME_RANGE) / Constants.MILLISECONDS_A_DAY);

				hbe.setProbability(calculateProbability(hbe.getTimesOccurred()));
				hbe.setDaysFulfilled(setDaysFulfilled(hbe));
				hbe.setIsLearned(setLearned(hbe));
				hbe.setLastOccurrence(timeTriggered);
				

			}
			
			
		}
		
		else if(dbh.isEventToLearn(eventId, timeTriggered)==-1){		//this will run when the incoming event has an entry in the events tracked table
																		//to update the duration
			Log.d("Algo:", "This is learning the duration");
			EventsTracked et = dbh.isEventTracked(eventId);
			
			 hbe = dbh.getHbeById(et.getHbeId());
			
			if(hbe!=null){
			if(hbe.getDuration()!=0){
				hbe.setDuration((hbe.getDuration() + timeTriggered - hbe.getLastOccurrence())/2);
			}
			else
				hbe.setDuration(timeTriggered - hbe.getLastOccurrence());
			
			dbh.updateHackBotEvent(hbe);
			
			//remove EventsTracked entry from the db
			dbh.deleteEventsTracked(et);
			}

		}
		
		else if((hbe = dbh.getUnlearnEvent(eventId, timeTriggered))!=null){		//unlearn event		
									//assume the event is identified which has to be unlearned,  
		//	HackBotEvent hbe = dbh.getUnlearnEvent(eventId, timeTriggered);
			Log.d("Algo:", "This is unlearn algo");
			//change the probability, timesOccured, keep duration same
			String timesOccuredArray[] = stringToArray(hbe.getTimesOccurred());
			if(timesOccuredArray.length<15){
				String timesOccuredArrayTemp[] = new String[timesOccuredArray.length+1];
				timesOccuredArrayTemp[0] = "0";
				System.arraycopy(timesOccuredArray, 0, timesOccuredArrayTemp, 1, timesOccuredArray.length);
				hbe.setTimesOccurred(convertArrayToString(timesOccuredArrayTemp));
			
			}
			else{
				for(int i=timesOccuredArray.length-1;i>=1;i++){
					timesOccuredArray[i] = timesOccuredArray[i-1];
				}
				timesOccuredArray[0] = "0";
				hbe.setTimesOccurred(convertArrayToString(timesOccuredArray));
			}
			
			//change the probability
			hbe.setProbability(calculateProbability(hbe.getTimesOccurred()));
			hbe.setIsLearned(setLearned(hbe));
			dbh.updateHackBotEvent(hbe);

			
		}
		publishToActionService(hbe);
	}
	
	private void publishToActionService(HackBotEvent hackBotEvent){
    	if((hackBotEvent != null) && (hackBotEvent.getIsLearned() != -1) && (mBound))
		{
			mService.fillListenedEventList(hackBotEvent);
		}
	}
		


	
	
	private String convertArrayToString(String[] arr){
		
		StringBuilder patternSB = new StringBuilder();
		for (String str : arr) {
			patternSB.append(str);
		}
		return patternSB.toString();
	}
	
	private int calculateProbability(String timesOccured){
		int length = timesOccured.length();
		
		String timesOccuredArr[] = stringToArray(timesOccured);
		int noOfOnes = 0;
		
		for (String str : timesOccuredArr) {
			if(str.trim().equals("1"))
				noOfOnes++;
		}
		
		return noOfOnes/length;
		
	}
	
	private String[] stringToArray(String str){
		String arr[] = str.split("");  
		String patternArray[] = new String[arr.length-1];
		System.arraycopy(arr, 1, patternArray, 0, patternArray.length);
		return patternArray;
	}
	
	public static int setDaysFulfilled(HackBotEvent hbe){				
		
		if(hbe.getDaysTracked()>=7 && hbe.getTimesOccurred().length()>=4 && hbe.getRepeatedWeekly()==1)
			return 1;
		else if(hbe.getDaysTracked()>=7 && hbe.getTimesOccurred().length()>=3 && hbe.getRepeatedWeekly()==0)
			return 1;
		else
			return 0;
		
	}
	
//	public static int triggerEvent(int eventId, DBHelper dbh){			//to know if to trigger this event automatically
//		
//		HackBotEvent hbe = dbh.getLearnedEvents(eventId);		 		//this eventId is the unique Id for all the events in the HBE table
//		if(hbe.getProbability()>=75)
//			return 1;
//		else
//			return 0;
//		
//	}
	
	public int setLearned(HackBotEvent hbe){
		if(hbe.getDaysFulfilled()==1 && hbe.getProbability()>=75)
			return 1;
		else if(hbe.getDaysFulfilled()==1 && hbe.getProbability()<75)
			return 0;
		else
			return -1;
		
	}
	
	public void deleteEvents(int eventId){		//return all the events of this eventId that have been deleted
		
		dbh.deleteAllEventsByEventId(eventId);
		HackBotEvent hbe = new HackBotEvent();
		hbe.setEventId(eventId);
		mService.fillListenedEventList(hbe);
		
	}
	
	private ServiceConnection mConnection = new ServiceConnection() { 


        @Override 
        public void onServiceConnected(ComponentName className, 
                                       IBinder service) { 
            // We've bound to LocalService, cast the IBinder and get LocalService instance 
        	ActionService.LocalBinder binder = (ActionService.LocalBinder) service; 
            mService = binder.getService(); 
            mBound = true; 
        } 


        @Override 
        public void onServiceDisconnected(ComponentName arg0) { 
            mBound = false; 
        } 
    }; 

}
