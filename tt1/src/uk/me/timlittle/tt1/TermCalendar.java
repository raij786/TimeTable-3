package uk.me.timlittle.tt1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import uk.me.timlittle.tt1.TermCalendar.Term;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.SparseArray;

public class TermCalendar {
    private static final String DATABASE_NAME = "CalendarDB";
    private static final String DATABASE_TABLE = "term_calendar";
    private static final int DATABASE_VERSION = 1;
    private static final String COL_TERM_ID = "termId";
    private static final String COL_TERM_NAME = "termName";
    private static final String COL_STARTDATE = "startDate";
    private static final String COL_ENDDATE = "endDate";
    private static final String COL_FIRSTWEEK = "firstWeek";
    private static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE + " ( " + COL_TERM_ID + " integer primary key, "
        	+ COL_TERM_NAME + " text, "
        	+ COL_STARTDATE + " text, "  
        	+ COL_ENDDATE + " text, "
        	+ COL_FIRSTWEEK + " integer "
        	+");";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
    private static Context localContext;
	private int termCount = 6;
	private SparseArray<Term> terms = new SparseArray();
	private int weekCount = 2; 
	private int endDay= Calendar.FRIDAY;

    /*
     * constructor, creates an array holding a term object for each term, 
     * and initialises some constants
     */
    public TermCalendar (Context ctx) {
		
        this.localContext = ctx;
    	// Get the number of terms

        
		SharedPreferences pref;
		String prefs_key;
		boolean includeWeekends = false;
		 		
		pref = PreferenceManager.getDefaultSharedPreferences(localContext);
		
		if (pref != null) {
			prefs_key = localContext.getResources().getString(R.string.pref_terms_key);
			termCount = Integer.parseInt(pref.getString(prefs_key, "6"));
		
			prefs_key = localContext.getResources().getString(R.string.pref_weeks_key);
			weekCount = Integer.parseInt(pref.getString(prefs_key, "2"));
			
			prefs_key = localContext.getResources().getString(R.string.pref_weekend_key);
			includeWeekends = pref.getBoolean(prefs_key, false);
			
			if (includeWeekends) {
				//Defaults to Monday and Friday
				endDay = Calendar.SATURDAY;
			}
			
		}
		
		for (int currentTerm = 1; currentTerm <= termCount; currentTerm++){
			terms.put(currentTerm, new Term(currentTerm));
		}
        
    }
    
    /*
     * Find whether the given date is week A (1) or B (2)
     */
    public int getCurrentWeek (Date weekDate) {
    	int weekNo = 1;
    	boolean foundTerm = false;
    	Term term;
    	int termId = 1;
    	int inTerm;
    	
    	while (!foundTerm && termId <= termCount) {
    		term = terms.get(termId);
    		inTerm = term.inTerm(weekDate);
    		
    		switch (inTerm) {
    			case -1: //Earlier
    				break;
    				
    			case 0: //found
        			foundTerm = true;
        			weekNo = term.currentWeek(weekDate);
    				break;
    			
    			case 1: //after so find the weekNo of the last week of the previous term
    				weekNo = term.lastWeek();
    				weekNo++;
    				if (weekNo > weekCount)
    					weekNo =1;
    				break;
    		}

    		termId++;
    		
    	}
    	
    	return weekNo;
    }
    
    public Term getTerm (Date weekDate) {
    	Term term;
    	int termId = 1;
    	
    	term = terms.get(termId);
    	
    	while (term.inTerm(weekDate) > 0) {
    		termId++;
    		term = terms.get(termId);
    	}

    	return term;
    }
    
    public Term getTerm (int termId) {
    	return terms.get(termId);
    }
    
    public void saveYear () {
    	for (int termId = 1; termId <= termCount; termId++) {
    		terms.get(termId).save();
    	}
    }
    
    public int getTermCount () {
    	return termCount;
    }
    
    /******************************************************************************
     * 
     * @author TJL-Dell
     * class to do term specific work 
     */
    
    public class Term {
    	private int id;
    	public String name;
    	private Date startDate;
    	private Date endDate;
    	private int firstWeek;
    	private boolean newRecord;
    	    	
    	public int termId () {
    		return id;
    	}
    	
    	public int lastWeek() {
			int weekNo = currentWeek(endDate);
			
			return weekNo;
		}

		public String getStartDate () {
    		String ret = "";
    		
    		if (startDate != null)
    			ret = dateFormat.format(startDate);
    		
    		return ret;
    	}

    	public void setStartDate (String newVal) {
    		
    		try {
				startDate = dateFormat.parse(newVal);
			} catch (ParseException e) {
				e.printStackTrace();
				startDate = null;
			}    		
    	}

    	public String getEndDate () {
    		String ret = "";
    		
    		if (endDate != null)
    			ret = dateFormat.format(endDate);
    		
    		return ret;
    	}

    	public void setEndDate (String newVal) {
    		
    		try {
				endDate = dateFormat.parse(newVal);
			} catch (ParseException e) {
				e.printStackTrace();
				endDate = null;
			}    		
    	}
    	
    	public boolean startsA () {
    		return firstWeek == 1 ? true: false;
    	}
    	
    	public void setStartWeek (boolean startsA) {
    		firstWeek = startsA ? 1 : 2; 
    	}
    	
    	public Term (int pId) {
    	    DatabaseHelper DBHelper;
    	    SQLiteDatabase db;
    		/*
    		 * Constructor
    		 */
    		Cursor mCursor;
            DBHelper = new DatabaseHelper(localContext);
            
            db = DBHelper.getWritableDatabase();
            
            id = pId;
            
            mCursor = db.query(DATABASE_TABLE, new String[] {COL_TERM_ID, COL_TERM_NAME, COL_STARTDATE, COL_ENDDATE, COL_FIRSTWEEK}, COL_TERM_ID + "=\"" + id + "\" ", null, null, null, null);
            
            if (mCursor != null && mCursor.getCount() > 0){
            	//If the term details were found copy them into the local variables
            	mCursor.moveToFirst();
            	
            	if (!mCursor.isNull(1))
            		name = mCursor.getString(1);
            	
            	
            	try {
					startDate = dateFormat.parse(mCursor.getString(2));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					startDate = null;
				}

            	try {
					endDate = dateFormat.parse(mCursor.getString(3));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					endDate = null;
				}
            	
            	firstWeek = mCursor.getInt(4);
            	newRecord = false;
            	
            } else {
            	//Default the name
            	
            	//If they weren't either give defaults
            	if (id != 1 ) {
                	name = localContext.getResources().getString(R.string.pref_termyear_key) + " " + id;
            		startDate = null;
            		endDate = null;
            		firstWeek = 1;
            		newRecord = true;
            	} else {
            		//Or read the v1 values from v1 preferences
        			//Read any existing values
                	Calendar rightNow = Calendar.getInstance();
                	int year = rightNow.get(Calendar.YEAR);
                	int month = Calendar.SEPTEMBER;
                	int day = 1;
                	boolean startA = true;

            		SharedPreferences pref;
        			String prefs_key;
        			 		
        			pref = PreferenceManager.getDefaultSharedPreferences(localContext);
        			
        			if (pref != null) {
        				prefs_key = localContext.getResources().getString(R.string.pref_termyear_key);
        				year = pref.getInt(prefs_key, year);
        				
        				prefs_key = localContext.getResources().getString(R.string.pref_termmonth_key);
        				month = pref.getInt(prefs_key, month);

        				prefs_key = localContext.getResources().getString(R.string.pref_termday_key);
        				day = pref.getInt(prefs_key, day);
        				
        				prefs_key = localContext.getResources().getString(R.string.pref_termstartweek_key);
        				startA = pref.getBoolean(prefs_key, true);

        			}

                	name = localContext.getResources().getString(R.string.term) + " " + id;
                	Calendar termStartDate = Calendar.getInstance();            			
            		termStartDate.set(year, month, day);
            		
            		startDate =  termStartDate.getTime();
            		termStartDate.add(Calendar.YEAR, 1);
            		endDate = termStartDate.getTime();
            		firstWeek = startA ? 1 : 2;
            		newRecord = true;
            	}
            	
            }
    	}
    	
    	/*
    	 * Check to see if a particular date is within the current term
    	 * -1 means before
    	 * 0 means during
    	 * 1 means after
    	 */
    	public int inTerm (Date pCheck) {
    		int ret = 0;
    		
    		if (startDate != null && endDate != null) {
    			
    			if (startDate.before(pCheck) && endDate.after(pCheck)) {
    				ret = 0;
    			} else if (endDate.before(pCheck)) {
    				ret = 1;
    			} else {
    				ret = -1;
    			}
    		}
    		
    		return ret;
    	}
    	
    	/*
    	 * Check which week it is currently
    	 */
    	public int currentWeek (Date pCheck){
    		int weekNo = 1;
    		Calendar termStartDate = Calendar.getInstance();
    		termStartDate.setTime(startDate);
    		
    		if (startDate != null && endDate != null) {
				while (termStartDate.get(Calendar.DAY_OF_WEEK) < endDay)
					termStartDate.add(Calendar.DATE, 1);
				
				weekNo = firstWeek;
				while (termStartDate.before(pCheck)) {
					termStartDate.add(Calendar.DATE, 7);
					weekNo++;
					if (weekNo > weekCount)
						weekNo = 1;
				}
    		}
    		
    		return weekNo;
    	}
    	
    	public void save () {
    	    DatabaseHelper DBHelper;
    	    SQLiteDatabase db;
    	    ContentValues args = new ContentValues();

            DBHelper = new DatabaseHelper(localContext);
            
            db = DBHelper.getWritableDatabase();
            
            args.put(COL_TERM_NAME, name);
            
            if (startDate != null)
            	args.put(COL_STARTDATE, dateFormat.format(startDate) );
            else
            	args.put(COL_STARTDATE, "" );

            if (endDate != null)
            	args.put(COL_ENDDATE, dateFormat.format(endDate) );
            else
            	args.put(COL_ENDDATE, "" );
            
            args.put(COL_FIRSTWEEK, firstWeek);
            
            if (newRecord) {
            	
            	args.put(COL_TERM_ID, id);
            	db.insert(DATABASE_TABLE, null, args);
            	newRecord = false;
            } else {
            	db.update(DATABASE_TABLE, args, COL_TERM_ID + "= " + id, null );
            }
    		
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

        		ContentValues args = new ContentValues();
        		String[] defaultTermNames = localContext.getResources().getStringArray(R.array.default_term_name);
        		String[] defaultStartDates = localContext.getResources().getStringArray(R.array.default_term_start);
        		String[] defaultEndDates = localContext.getResources().getStringArray(R.array.default_term_end);
        		String[] defaultStartWeeks = localContext.getResources().getStringArray(R.array.default_term_startweek);
        		
        		
        		for ( int id = 0; id < 6; id++){
        			args.put(COL_TERM_ID, id + 1);
        			args.put(COL_TERM_NAME, defaultTermNames[id]);
        			args.put(COL_STARTDATE, defaultStartDates[id]);
        			args.put(COL_ENDDATE, defaultEndDates[id]);
        			args.put(COL_FIRSTWEEK, Integer.parseInt(defaultStartWeeks[id]));
        			
        			db.insert(DATABASE_TABLE, null, args);
        			
        			args.clear();
        		}
        		
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
