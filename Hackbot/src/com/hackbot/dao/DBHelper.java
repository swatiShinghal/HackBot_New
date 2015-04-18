package com.hackbot.dao;

import java.util.Date;


import com.hackbot.entity.EventRunning;
import com.hackbot.entity.EventsTracked;
import com.hackbot.entity.HackBotEvent;
import com.hackbot.utility.Constants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;



/**
 * Created by SwatiSh on 4/8/2015.
 */
public class DBHelper extends SQLiteOpenHelper
{

	private static final String LOG = "DBHelper";
	
	public static final String DATABASE_NAME = "HackBotDB";
    public static final String KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER = "time_to_trigger";
    public static final String KEY_HACK_BOT_EVENTS_FIRST_OCCURRENCE = "first_occurrence";
    public static final String KEY_HACK_BOT_EVENTS_LAST_OCCURRENCE = "last_occurrence";
    public static final String KEY_HACK_BOT_EVENTS_TIMES_OCCURRED = "times_occurred";
    public static final String KEY_HACK_BOT_EVENTS_PROBABILITY = "probability";
    public static final String KEY_HACK_BOT_EVENTS_DURATION = "duration";
    public static final String KEY_HACK_BOT_EVENTS_PATTERN = "pattern";
    public static final String KEY_HACK_BOT_EVENTS_REPEATED_WEEKLY = "repeated_weekly";
    public static final String KEY_HACK_BOT_EVENTS_REPEATED_DAYS = "repeat_in_days";
    public static final String KEY_HACK_BOT_EVENTS_DAYS_TRACKED = "days_tracked";
    public static final String KEY_HACK_BOT_EVENTS_IS_EXECUTING = "is_executing";
    public static final String KEY_HACK_BOT_EVENTS_IS_LEARNED = "is_learned";
    public static final String KEY_HACK_BOT_EVENTS_DAYS_FULFILLED = "days_fulfilled";
    public static final String KEY_HACK_BOT_EVENTS_VALUE = "value";
    
    
    //table names
    private static final String TABLE_EVENTS_TRACKED = "events_tracked";
    private static final String TABLE_EVENTS = "events";					
    public static final String TABLE_HACK_BOT_EVENTS = "hack_bot_events";
    public static final String TABLE_EVENTS_RUNNING = "events_running";
    
    //common columns
    private static final String KEY_ID = "id";
    public static final String KEY_EVENTS_ID = "eventId";
    
    //TABLE_EVENTS_TRACKED columns
    private static final String KEY_EVENTS_TRACKED_HBEID = "hbe_id";
    
    //TABLE_EVENTS columns
    private static final String KEYS_EVENTS_EVENTS_NAME = "events_name";
    private static final String KEYS_EVENTS_IS_ENABLED = "is_enabled";
    
    //TABLE_EVENTS_RUNNING
    private static final String KEYS_EVENTS_RUNNING_TIME_STARTED = "time_started";
    
    //create table statements
    private static final String CREATE_EVENTS_TRACKED = "CREATE TABLE "
            + TABLE_EVENTS_TRACKED + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_EVENTS_ID
            + " INTEGER, " +KEY_EVENTS_TRACKED_HBEID+ " INTEGER)";
    
    private static final String CREATE_EVENTS = "CREATE TABLE "
            + TABLE_EVENTS + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEYS_EVENTS_EVENTS_NAME
            + " TEXT, " +KEYS_EVENTS_IS_ENABLED+ " INTEGER)";
    
    private static final String CREATE_EVENTS_RUNNING = "CREATE TABLE "
            + TABLE_EVENTS_RUNNING + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_EVENTS_ID
            + " INTEGER, " +KEY_EVENTS_TRACKED_HBEID+ " INTEGER, "+KEYS_EVENTS_RUNNING_TIME_STARTED+" INTEGER)";
    
    private static final String CREATE_HACK_BOT_EVENTS = "CREATE TABLE IF NOT EXISTS "+TABLE_HACK_BOT_EVENTS +
            " ("+KEY_ID 
            + " INTEGER PRIMARY KEY," + KEY_EVENTS_ID
    		+" INTEGER ," + KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER
            +" INTEGER,"+KEY_HACK_BOT_EVENTS_FIRST_OCCURRENCE
            +" INTEGER,"+KEY_HACK_BOT_EVENTS_LAST_OCCURRENCE
            +" INTEGER," + KEY_HACK_BOT_EVENTS_TIMES_OCCURRED
            +" TEXT,"+KEY_HACK_BOT_EVENTS_PROBABILITY
            +" INTEGER, "+ KEY_HACK_BOT_EVENTS_DURATION
            +" INTEGER, "+KEY_HACK_BOT_EVENTS_PATTERN
            +" TEXT, "+ KEY_HACK_BOT_EVENTS_REPEATED_WEEKLY
            +" INTEGER, "+KEY_HACK_BOT_EVENTS_REPEATED_DAYS
            +" INTEGER, " + KEY_HACK_BOT_EVENTS_IS_EXECUTING
            +" INTEGER, " + KEY_HACK_BOT_EVENTS_IS_LEARNED
            +" INTEGER, " + KEY_HACK_BOT_EVENTS_DAYS_TRACKED
            +" INTEGER,"+ KEY_HACK_BOT_EVENTS_DAYS_FULFILLED
            +" INTEGER,"+ KEY_HACK_BOT_EVENTS_VALUE
            +" INTEGER )";
    
    
    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
    	Log.d(LOG, "onCreate enter");
        db.execSQL(CREATE_HACK_BOT_EVENTS);
        db.execSQL(CREATE_EVENTS_TRACKED);
        db.execSQL(CREATE_EVENTS);
        db.execSQL(CREATE_EVENTS_RUNNING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	Log.d(LOG, "onUpgrade enter");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HACK_BOT_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_EVENTS_TRACKED) ;
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_EVENTS) ;
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_EVENTS_RUNNING) ;
        onCreate(db);
    }

    public boolean insertEvent  (long eventId, String eventName, Integer probability)
    {
    	Log.d(LOG, "insertEvent enter");
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_EVENTS_ID, eventId);
    //    contentValues.put(EVENTS_NAME, eventName);
        contentValues.put(KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER, (time.hour * 60)+time.minute);
        contentValues.put(KEY_HACK_BOT_EVENTS_FIRST_OCCURRENCE, time.toMillis(false));
        contentValues.put(KEY_HACK_BOT_EVENTS_LAST_OCCURRENCE, time.toMillis(false));
        contentValues.put(KEY_HACK_BOT_EVENTS_TIMES_OCCURRED, 1);
        contentValues.put(KEY_HACK_BOT_EVENTS_PROBABILITY, probability);
        contentValues.put(KEY_HACK_BOT_EVENTS_DURATION, 0);
        contentValues.put(KEY_HACK_BOT_EVENTS_PATTERN, 64);
        contentValues.put(KEY_HACK_BOT_EVENTS_REPEATED_WEEKLY, false);
        contentValues.put(KEY_HACK_BOT_EVENTS_REPEATED_DAYS, 1);

        db.insert(TABLE_HACK_BOT_EVENTS, null, contentValues);
        return true;
    }
    
    public void dropDataBaseHBE(){
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.execSQL("DROP TABLE IF EXISTS " + TABLE_HACK_BOT_EVENTS);
    }
    
    public HackBotEvent getData(long time, long range){
    	Log.d(LOG, "getData enter");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.query(TABLE_HACK_BOT_EVENTS, null, " BETWEEN ? AND ?", new String[] {Long.toString(time - range), Long.toString(time + range)}, null, null, null, null);
        HackBotEvent event = parseRecord(res);
        //Cursor res =  db.rawQuery( "select * from HackBotEvents where id="+id+" AND time="+time+"", null );

        return event;
    }

    public HackBotEvent getData(long id, long time, long range){
    	Log.d(LOG, "getData enter");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.query(TABLE_HACK_BOT_EVENTS, null, KEY_EVENTS_ID+ "? AND "+KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER+" BETWEEN ? AND ?", 
        		new String[] {Long.toString(id), Long.toString(time - range), Long.toString(time + range)}, null, null, null, null);
        HackBotEvent event = parseRecord(res);
        //Cursor res =  db.rawQuery( "select * from HackBotEvents where id="+id+" AND time="+time+"", null );

        return event;
    }
    
    public int numberOfRows(){
    	Log.d(LOG, "numberOfRows enter");
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_HACK_BOT_EVENTS);
        return numRows;
    }
    
    public boolean updateEvent (long id, long time, long range, long probability, boolean repeatedWeekly)
    {
    	Log.d(LOG, "updateEvent enter");
        Time last = new Time();
        last.setToNow();
        HackBotEvent event = getData(id, time, range);
        if (event == null) return  false;
        String pattern = event.getPattern();
        long noOfDays = getNoOfDays(last.toMillis(false), event.getFirstOccurrence());
        long updatedPattern = getUpdatedPattern(pattern, noOfDays);
        long repeatInDays = event.getRepeatInDays() + 1;
    //    long timesOccurred = event.getTimesOccurred() + 1;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_EVENTS_ID, id);
        contentValues.put(KEY_HACK_BOT_EVENTS_PROBABILITY, probability);
        contentValues.put(KEY_HACK_BOT_EVENTS_LAST_OCCURRENCE, last.toMillis(false));
        contentValues.put(KEY_HACK_BOT_EVENTS_REPEATED_WEEKLY, repeatedWeekly?"true":"false");
        contentValues.put(KEY_HACK_BOT_EVENTS_PATTERN, updatedPattern);
 //       contentValues.put(EVENTS_COLUMN_TIMES_OCCURRED, timesOccurred);
        contentValues.put(KEY_HACK_BOT_EVENTS_REPEATED_DAYS, repeatInDays);
        db.update(TABLE_HACK_BOT_EVENTS, contentValues, KEY_EVENTS_ID +"? AND "+KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER+" BETWEEN ? AND ?", new String[] {Long.toString(id), Long.toString(time - range), Long.toString(time + range)} );
        return true;
    }

    public boolean updateDurationEvent (long id, long range)
    {
    	Log.d(LOG, "updateDurationEvent enter");
        Time finish = new Time();
        finish.setToNow();
        long finishTime = (finish.hour * 60)+finish.minute;
        SQLiteDatabase db = this.getWritableDatabase();
        HackBotEvent event = getNearestRecord(id, finishTime);
        if (event == null) return false;
        long time = event.getTimeToTrigger();
        long duration = finishTime - time;
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_EVENTS_ID, id);
        contentValues.put(KEY_HACK_BOT_EVENTS_DURATION, duration);
        db.update(TABLE_HACK_BOT_EVENTS, contentValues, KEY_EVENTS_ID +"? AND "+KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER+" BETWEEN ? AND ?", new String[] {Long.toString(id), Long.toString(time - range), Long.toString(time + range)} );
        return true;
    }

    private HackBotEvent getNearestRecord(long id, long time)
    {
    	Log.d(LOG, "getNearestRecord enter");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HACK_BOT_EVENTS, null,
        		KEY_EVENTS_ID+ "?", new String[] {Long.toString(id)}, null, null,
                "abs(EVENTS_COLUMN_TIME_TO_TRIGGER - " + time + ")", "1");
        HackBotEvent event = parseRecord(cursor);
        return event;
    }


    public Integer deleteEvent (long id, long time, long range)
    {
    	Log.d(LOG, "deleteEvent enter");
        SQLiteDatabase db = this.getWritableDatabase();
        //return db.query(EVENTS_TABLE_NAME, null, " BETWEEN ? AND ?", new String[] {Long.toString(time - range), Long.toString(time + range)}, null, null, null, null);
        return 0;
    }

    private HackBotEvent parseRecord(Cursor cursor)			
    {
    	Log.d(LOG, "parseRecord enter");
        if (cursor.moveToFirst()) {
            HackBotEvent event = new HackBotEvent();
            event.setEventId(cursor.getInt(1));
            event.setTimeToTrigger(cursor.getInt(2));
            event.setFirstOccurrence(cursor.getInt(3));
            event.setLastOccurrence(cursor.getInt(4));
            event.setTimesOccurred(cursor.getString(5));
            event.setProbability(cursor.getInt(6));
            event.setDuration(cursor.getInt(7));
            event.setPattern(cursor.getString(8));
            event.setRepeatedWeekly(cursor.getInt(9));
            event.setRepeatInDays(cursor.getInt(10));
            event.setIsExecuting(cursor.getInt(11));
            event.setIsLearned(cursor.getInt(12));
            event.setDaysTracked(cursor.getInt(13));
            event.setDaysFulfilled(cursor.getInt(14));
            event.setValue(cursor.getInt(15));
            return event;
        }
        return null;
    }

    private long getUpdatedPattern(String pattern, long num)
    {
        long finalPattern = Long.parseLong(pattern, 2);
        if (num <= 7) {
            long patternInt = Long.parseLong(pattern, 2);
            finalPattern = patternInt | (2 ^ (6-num));
        }
        return finalPattern;
    }
    
    private long getNoOfDays(long lastDate, long firstDate)
    {
        Date last = new Date(lastDate);
        Date first = new Date(firstDate);
        long noOfDays = (long)( (last.getTime() - first.getTime()) / (1000 * 60 * 60 * 24));
        return noOfDays;
    }
    
    //EventRunning Table
    /**
     * returns 1 if the event is currently running else 0
     * @param eventId
     * @param timeTriggered
     * @param range
     * @return
     */
    public int isEventRunning(long eventId){			// get if this event is currently running or not
    	Log.d(LOG, "isEventRunning enter");
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT  * FROM " + TABLE_EVENTS_RUNNING + " WHERE "
                + KEY_EVENTS_ID + " = " + eventId;
     
        Cursor c = db.rawQuery(selectQuery, null);
        
        if (c.moveToFirst()){
        	EventRunning er = new EventRunning();
        	er.setEventId(c.getInt(c.getColumnIndex(KEY_EVENTS_ID)));
        	er.setHbeId(c.getInt(c.getColumnIndex(KEY_EVENTS_TRACKED_HBEID)));
        	er.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        	er.setTimeStarted(c.getInt(c.getColumnIndex(KEYS_EVENTS_RUNNING_TIME_STARTED)));
        	return 1;
        }
        else
        	return 0;
        	
    }
    
    
    //EventsTracked Table
    public EventsTracked getEventTrackedById(int eventId){
    	Log.d(LOG, "getEventTrackedById enter");
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT  * FROM " + TABLE_EVENTS_TRACKED + " WHERE "
                + KEY_EVENTS_ID + " = " + eventId;
     
        Cursor c = db.rawQuery(selectQuery, null);
        
        if (c.moveToFirst()){
        	EventsTracked et = new EventsTracked();
        	et.setEventId(c.getInt(c.getColumnIndex(KEY_EVENTS_ID)));
        	et.setHbeId(c.getInt(c.getColumnIndex(KEY_EVENTS_TRACKED_HBEID)));
        	et.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        	return et;
        }
        else
        	return null;
        	
    }
   
    //HBE Table
    
    /**
     * this deletes all the events of this eventId in the HBE table and returns there object
     * @param eventId
     * @return
     */
    public void deleteAllEventsByEventId(int eventId){

    	Log.d(LOG, "deleteAllEventsByEventId enter");
        SQLiteDatabase db = this.getWritableDatabase();
     
        db.delete(TABLE_HACK_BOT_EVENTS, KEY_EVENTS_ID + " = ?",
                new String[] { String.valueOf(eventId) });
    
    }
    
    
    public HackBotEvent getHbeById(int id){
    	Log.d(LOG, "getHbeById enter");
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT  * FROM " + TABLE_HACK_BOT_EVENTS + " WHERE "
                + KEY_ID + " = " + id;
     
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst())
        	{
        		HackBotEvent event = parseRecord(cursor);
        		return event;
        	}
        return null;
        
    }
    
    
    public int updateHackBotEvent(HackBotEvent hbe) {
    	Log.d(LOG, "updateHackBotEvent enter");
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_EVENTS_ID, hbe.getEventId());
        contentValues.put(KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER, hbe.getTimeToTrigger());
        contentValues.put(KEY_HACK_BOT_EVENTS_FIRST_OCCURRENCE, hbe.getFirstOccurrence());
        contentValues.put(KEY_HACK_BOT_EVENTS_LAST_OCCURRENCE, hbe.getLastOccurrence());
        contentValues.put(KEY_HACK_BOT_EVENTS_TIMES_OCCURRED, hbe.getTimesOccurred());
        contentValues.put(KEY_HACK_BOT_EVENTS_PROBABILITY, hbe.getProbability());
        contentValues.put(KEY_HACK_BOT_EVENTS_DURATION, hbe.getDuration());
        contentValues.put(KEY_HACK_BOT_EVENTS_PATTERN, hbe.getPattern());
        contentValues.put(KEY_HACK_BOT_EVENTS_REPEATED_WEEKLY, hbe.getRepeatedWeekly());
        contentValues.put(KEY_HACK_BOT_EVENTS_REPEATED_DAYS, hbe.getRepeatInDays());
        contentValues.put(KEY_HACK_BOT_EVENTS_DAYS_TRACKED, hbe.getDaysTracked());
        contentValues.put(KEY_HACK_BOT_EVENTS_IS_EXECUTING, hbe.getIsExecuting());
        contentValues.put(KEY_HACK_BOT_EVENTS_IS_LEARNED, hbe.getIsLearned());
        contentValues.put(KEY_HACK_BOT_EVENTS_DAYS_FULFILLED, hbe.getDaysFulfilled());
        contentValues.put(KEY_HACK_BOT_EVENTS_VALUE, hbe.getValue());
     
        // updating row
        return db.update(TABLE_HACK_BOT_EVENTS, contentValues, KEY_ID + " = ?",
                new String[] { String.valueOf(hbe.getId()) });
    }
    
    /**
     * This returns HBE object if it has to be unlearned
     * @param eventId
     * @param timeTriggeredOff
     * @return
     */
    public HackBotEvent getUnlearnEvent(int eventId, long timeTriggeredOff){			
    	Log.d(LOG, "getUnlearnEvent enter");
    	SQLiteDatabase db = this.getReadableDatabase();
    	
        String selectQuery = "SELECT  * FROM " + TABLE_EVENTS_RUNNING + " WHERE "
                + KEY_EVENTS_ID + " = " + eventId;
     
        Cursor c = db.rawQuery(selectQuery, null);
        
        if (c.moveToFirst()){
        	int hbeId = c.getInt(c.getColumnIndex(KEY_EVENTS_TRACKED_HBEID));
        	long timeStarted = c.getInt(c.getColumnIndex(KEYS_EVENTS_RUNNING_TIME_STARTED));
        	
        	HackBotEvent hbe = getHbeById(hbeId);
        	if(Math.abs(timeTriggeredOff-timeStarted-hbe.getDuration())>Constants.TIME_RANGE)
        		return hbe;
        }
        
        	return null;
        
    }
    
    /**
     * return 1 if it is a new event, which has no previous entry in the DB at this triggerTime
     * else return 0
     * @param eventId
     * @param timeTriggered
     * @return
     */
    public int isNewEvent(int eventId,long timeTriggered){
    	Log.d(LOG, "isNewEvent enter");
    	SQLiteDatabase db = this.getReadableDatabase();
    	
        String selectQuery = "SELECT  * FROM " + TABLE_HACK_BOT_EVENTS + " WHERE "
                + KEY_EVENTS_ID + " = " + eventId + " AND " + KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER 
                + " BETWEEN "+ (timeTriggered - Constants.TIME_RANGE) + " AND " + (timeTriggered + Constants.TIME_RANGE);
        Log.d(LOG, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        
        if (c.moveToFirst()){
        	int i = c.getInt(c.getColumnIndex(KEY_EVENTS_ID));
        	return 0;
        }
        else
        	return 1;
        
    }
    
    /**
     * will return 1 if it has to be learned if not then 0 and -1 if it has an entry in the EventsTracked table
     * It will be learned when an event already occurs at the same approx time and also the event is currently not in the EventsTracked Table
     * @param eventId
     * @param timeTriggered
     * @return
     */
    public int isEventToLearn(int eventId,long timeTriggered){			

    	Log.d(LOG, "isEventToLearn enter");
    	SQLiteDatabase db = this.getReadableDatabase();

    	EventsTracked et = getEventTrackedById(eventId);
    	if(et!=null){
    		return -1;
    	}
    	else{

    		String selectQuery = "SELECT  * FROM " + TABLE_HACK_BOT_EVENTS + " WHERE "
    				+ KEY_EVENTS_ID + " = " + eventId + " AND " + KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER 
    				+ " BETWEEN "+ (timeTriggered - Constants.TIME_RANGE) + " AND " + (timeTriggered + Constants.TIME_RANGE)
    				+ " AND ";
    		Log.d(LOG, selectQuery);
    		Cursor c = db.rawQuery(selectQuery, null);

    		if (c.moveToFirst()){
    			int i = c.getInt(c.getColumnIndex(KEY_EVENTS_ID));
    			return i;
    		}
    		else
    			return 0;
    	}
    }
    
    /**
     * Insert to HBE table		
     * @param hbe
     * @return
     */
    public long insertToHackBotEvents(HackBotEvent hbe)
    {
    	Log.d(LOG, "insertToHackBotEvents enter");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_EVENTS_ID, hbe.getEventId());
        contentValues.put(KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER, hbe.getTimeToTrigger());
        contentValues.put(KEY_HACK_BOT_EVENTS_FIRST_OCCURRENCE, hbe.getFirstOccurrence());
        contentValues.put(KEY_HACK_BOT_EVENTS_LAST_OCCURRENCE, hbe.getLastOccurrence());
        contentValues.put(KEY_HACK_BOT_EVENTS_TIMES_OCCURRED, hbe.getTimesOccurred());
        contentValues.put(KEY_HACK_BOT_EVENTS_PROBABILITY, hbe.getProbability());
        contentValues.put(KEY_HACK_BOT_EVENTS_DURATION, hbe.getDuration());
        contentValues.put(KEY_HACK_BOT_EVENTS_PATTERN, hbe.getPattern());
        contentValues.put(KEY_HACK_BOT_EVENTS_REPEATED_WEEKLY, hbe.getRepeatedWeekly());
        contentValues.put(KEY_HACK_BOT_EVENTS_REPEATED_DAYS, hbe.getRepeatInDays());
        contentValues.put(KEY_HACK_BOT_EVENTS_DAYS_TRACKED, hbe.getDaysTracked());
        contentValues.put(KEY_HACK_BOT_EVENTS_IS_EXECUTING, hbe.getIsExecuting());
        contentValues.put(KEY_HACK_BOT_EVENTS_IS_LEARNED, hbe.getIsLearned());
        contentValues.put(KEY_HACK_BOT_EVENTS_DAYS_FULFILLED, hbe.getDaysFulfilled());

        return db.insert(TABLE_HACK_BOT_EVENTS, null, contentValues);
    }
    
    //EVENTS_TRACKED Table
    public long insertEventsTracked(EventsTracked et){
    	Log.d(LOG, "insertEventsTracked enter");
    	SQLiteDatabase db = this.getWritableDatabase();
    	
    	ContentValues values = new ContentValues();
        values.put(KEY_EVENTS_ID, et.getEventId());
        values.put(KEY_EVENTS_TRACKED_HBEID, et.getHbeId());
     
        // insert row
        long id = db.insert(TABLE_EVENTS_TRACKED, null, values);
        
        return id;
    }
    
    public EventsTracked isEventTracked(int eventId){
    	Log.d(LOG, "isEventTracked enter");
    	SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_EVENTS_TRACKED + " WHERE "
                + KEY_EVENTS_ID + " = " + eventId;
        Cursor c = db.rawQuery(selectQuery, null);
        
        if (c != null)
            c.moveToFirst();
        
        EventsTracked et = new EventsTracked();
        et.setEventId(c.getInt(c.getColumnIndex(KEY_EVENTS_ID)));
        et.setHbeId(c.getInt(c.getColumnIndex(KEY_EVENTS_TRACKED_HBEID)));
        et.setId(c.getColumnIndex(KEY_ID));
        
        return et;
    }
    
    public void deleteEventsTracked(EventsTracked et) {
    	Log.d(LOG, "deleteEventsTracked enter");
        SQLiteDatabase db = this.getWritableDatabase();
     
        db.delete(TABLE_EVENTS_TRACKED, KEY_ID + " = ?",
                new String[] { String.valueOf(et.getId()) });
    }

}