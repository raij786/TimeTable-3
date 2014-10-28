package uk.me.timlittle.tt1;

/********
 * This Class holds information about a school timetable in a SQLLite database 
 * 
 * 
* Copyright (c) 2014 Tim Little
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
*  documentation files (the "Software"), to deal in the Software without restriction, including without limitation
*   the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, 
*   and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
*  PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
*  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
*  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


import java.util.Map;
import java.util.HashMap;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.content.ContentValues;

public class TimeTable {
	/*
	 * Class variables
	 */

	private static final String TAG = "TimeTable";
    
    private static final String DATABASE_NAME = "TimeTableDB";
    private static final String DATABASE_TABLE = "lessons";
    private static final int DATABASE_VERSION = 1;
    public static final String KEY_LESSONCODE = "lessonCode";
    public static final String KEY_DAY = "day";
    public static final String KEY_WEEK = "week";
    public static final String KEY_PERIOD = "period";
    public static final String KEY_INFO1 = "info1";
    public static final String KEY_INFO2 = "info2";
    public static final String KEY_INFO3 = "info3";
    public static final int NOTES_PERIOD_KEY = -1; 


    private static final String DATABASE_CREATE =
        "create table " + DATABASE_TABLE + " ( " + KEY_LESSONCODE + " text primary key, "
    	+ KEY_DAY + " integer not null, "
    	+ KEY_WEEK + " integer not null, "  
    	+ KEY_PERIOD + " integer not null, "
    	+ KEY_INFO1 + " text, "
    	+ KEY_INFO2 + " text, "
    	+ KEY_INFO3 + " text "    	
    	+");";
        
    private final Context context;    
	private Map<String, Lesson> lessonList  = new HashMap<String, Lesson>();
	
	public TimeTable (Context ctx) {
	    DatabaseHelper DBHelper;
	    SQLiteDatabase db;
		/*
		 * Constructor. reads all the stored lessons into the local hash table
		 */
		Cursor mCursor;
		Lesson lesson;
		
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
        
        db = DBHelper.getWritableDatabase();
        
        mCursor = db.query(DATABASE_TABLE, new String[] {KEY_DAY, KEY_WEEK, KEY_PERIOD, KEY_INFO1, KEY_INFO2, KEY_INFO3}, null, null, null, null, null);
        
        if (mCursor != null){
        	mCursor.moveToFirst();
     
        	if (mCursor.getCount() > 0) {
        		
        		do {
        			lesson = new Lesson ();
        			lesson.dayNo = mCursor.getInt(0);
        			lesson.weekNo = mCursor.getInt(1);
        			lesson.lessonNo = mCursor.getInt(2);
        			lesson.info1 = mCursor.getString(3);
        			lesson.info2 = mCursor.getString(4);
        			lesson.info3 = mCursor.getString(5);
        		
        			lessonList.put(lesson.getKey(), lesson);
        		
        		} while (mCursor.moveToNext());

        	}
        }
        
        db.close();
        
	}
	
	public void resetTimeTable (int newWeekCount, int newPeriodCount) {
		
	}

	public String [] getInfo1Vals(){
		Cursor mCursor;
		int rowCounter;
	    String[] info1Vals = new String[0];
	    DatabaseHelper DBHelper;
	    SQLiteDatabase db;

        DBHelper = new DatabaseHelper(context);
        
        db = DBHelper.getWritableDatabase();
	    
		mCursor = db.query(true, DATABASE_TABLE, new String[] {KEY_INFO1}, null, null, KEY_INFO1, null, null, null);
        
        if (mCursor != null){
        	info1Vals = new String[mCursor.getCount()];
        	
        	mCursor.moveToFirst();
        	
        	for (rowCounter = 0; rowCounter < mCursor.getCount(); rowCounter++ ){
        		info1Vals[rowCounter] = mCursor.getString(0);
        		mCursor.moveToNext();
        	}
        	
        }

        db.close();
        
		return info1Vals;
	}

	public String [] getInfo2Vals(){
		Cursor mCursor;
		int rowCounter;
	    String[] info2Vals = new String[0];
	    DatabaseHelper DBHelper;
	    SQLiteDatabase db;

        DBHelper = new DatabaseHelper(context);
        
        db = DBHelper.getWritableDatabase();

		mCursor = db.query(true, DATABASE_TABLE, new String[] {KEY_INFO2}, null, null, KEY_INFO2, null, null, null);
        
        if (mCursor != null){
        	info2Vals = new String[mCursor.getCount()];
        	
        	mCursor.moveToFirst();
        	
        	for (rowCounter = 0; rowCounter < mCursor.getCount(); rowCounter++ ){
        		info2Vals[rowCounter] = mCursor.getString(0);
        		mCursor.moveToNext();
        	}
        	
        }
        db.close();
        
		return info2Vals;
	}
	
	public String [] getInfo3Vals(){
		Cursor mCursor;
		int rowCounter;
	    String[] info3Vals = new String[0];
	    DatabaseHelper DBHelper;
	    SQLiteDatabase db;

        DBHelper = new DatabaseHelper(context);
        
        db = DBHelper.getWritableDatabase();

		mCursor = db.query(true, DATABASE_TABLE, new String[] {KEY_INFO3}, null, null, KEY_INFO3, null, null, null);
        
        if (mCursor != null){
        	info3Vals = new String[mCursor.getCount()];
        	
        	mCursor.moveToFirst();
        	
        	for (rowCounter = 0; rowCounter < mCursor.getCount(); rowCounter++ ){
        		info3Vals[rowCounter] = mCursor.getString(0);
        		mCursor.moveToNext();
        	}
        	
        }
        
        db.close();
        
		return info3Vals;
	}
	
	public String getLessonInfo (int weekNo, int dayNo, int lessonNo) {
		String infoString = "";
		String key;
		Lesson lesson = new Lesson();
		
		key = lesson.calcKey(weekNo, dayNo, lessonNo);
		
		if (lessonList.containsKey(key)){
			infoString = lessonList.get(key).getInfo();
		} else {
			infoString = "";
		}
		
		return infoString;
	}
	
	//A bit of a kludge, but use a lesson record to hold any notes for the day
	//But the kludge is hidden so it could be fixed at a later date
	public String getDayNotes (int weekNo, int dayNo) {
		String notes = getLessonInfo (weekNo, dayNo, NOTES_PERIOD_KEY);
		
		notes = notes.trim();
		
		return notes;
	}
	
	public void saveDayNotes(int weekNo, int dayNo, String notes) {
		addLesson (weekNo, dayNo, NOTES_PERIOD_KEY, notes, "", "");
	}
	
	public Lesson getLesson (int weekNo, int dayNo, int lessonNo) {
		String key;
		Lesson lesson = new Lesson();
		
		key = lesson.calcKey(weekNo, dayNo, lessonNo);
		
		if (lessonList.containsKey(key)){
			lesson = lessonList.get(key);
		}
		
		return lesson;
		
	}
		
	public void addLesson (int weekNo, int dayNo, int lessonNo, String info1, String info2, String info3){
		boolean exists = false;
		Lesson newLesson = new Lesson(weekNo, dayNo, lessonNo, info1, info2, info3);
	    DatabaseHelper DBHelper;
	    SQLiteDatabase db;

        DBHelper = new DatabaseHelper(context);
        
        db = DBHelper.getWritableDatabase();

		if (lessonList.containsKey(newLesson.getKey()))
			exists = true;
				
		lessonList.put(newLesson.getKey(), newLesson);
		
		ContentValues args = new ContentValues();
		
		args.put(KEY_DAY, dayNo);
		args.put(KEY_WEEK,weekNo);
		args.put(KEY_PERIOD,lessonNo);
		args.put(KEY_INFO1,info1);
		args.put(KEY_INFO2,info2);
		args.put(KEY_INFO3,info3);
		
		if (exists) {
			db.update(DATABASE_TABLE, args, KEY_LESSONCODE + "= '" + newLesson.getKey() + "'", null );
		} else {
			args.put(KEY_LESSONCODE, newLesson.getKey());
			db.insert(DATABASE_TABLE, null, args);
		}
		db.close();
	}
	
/*****************************************************************************************
 * Private classes
 */
	
	public class Lesson {
		/*
		 * Private class to handle information about individual lessons
		 */
		int weekNo; 
		int dayNo; 
		int lessonNo; 
		String info1; 
		String info2; 
		String info3;
		
		public Lesson () {	
		}
		
		public Lesson (int pweekNo, int pdayNo, int plessonNo, String pinfo1, String pinfo2, String pinfo3) {
			weekNo = pweekNo;
			dayNo = pdayNo;
			lessonNo = plessonNo;
			
			info1 = pinfo1; 
			info2 = pinfo2; 
			info3 = pinfo3;
			
			
		}
		
		public String calcKey (int pweekNo, int pdayNo, int plessonNo){
			String key;
			
			key = "W" + pweekNo + "D" + pdayNo + "L" + plessonNo;
			
			return key;
		}
		
		public String getKey () {
			
			return calcKey(weekNo, dayNo, lessonNo);
		}
		
		public String getInfo (){
			String info;
			
			if (info1.trim().length() == 0) {
				info1 = "Lesson " + lessonNo;
			}
	
			info = info1 + "\n" + info2 + "\n" + info3;
			
			return info;
		}
	}
	
	/*
	 * ------------------------------------------------------------------------
	 */
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
        	try {
        		db.execSQL(DATABASE_CREATE);	
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
        {
        	/*
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS lessons");
            onCreate(db);
            */
        }
    }    
	
	/*
	 * End of private classes
	 */
	
}
