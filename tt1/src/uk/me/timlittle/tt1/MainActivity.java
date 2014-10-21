package uk.me.timlittle.tt1;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import uk.me.timlittle.tt1.R;
import uk.me.timlittle.tt1.SettingsActivity;
import uk.me.timlittle.tt1.SimpleGestureFilter.SimpleGestureListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
//import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.Point;


public class MainActivity extends Activity implements SimpleGestureListener 
{
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
	private SimpleGestureFilter detector;
	
	private int weekCount = 2;
	private int lessonCount = 5;
	private int currentDay;
	private int currentWeek = 1;
	private Calendar rightNow = Calendar.getInstance();
	private TimeTable timeTable;
	private TableLayout layout;
	private Map<String, Integer> viewMap = new HashMap<String, Integer>();
	private boolean includeWeekends = false;
	private boolean inEditting;
	private int startDay = Calendar.MONDAY;
	private int endDay = Calendar.FRIDAY;
	private boolean weekView = false;
	private TermCalendar terms = null;

	private int getCurrentDay (){
		int day;
		
		day = rightNow.get(Calendar.DAY_OF_WEEK);
		
		//Always skip forward if a weekend and not showing weekends
		if (!includeWeekends && (day == Calendar.SATURDAY || day == Calendar.SUNDAY)) {
			day = Calendar.MONDAY;
		}
		
		return day;
	}

	private int getCurrentWeek (){
		int weekNo = 1;
		
		if (weekCount > 1){
			weekNo = terms.getCurrentWeek (rightNow.getTime());
		}
		
		return weekNo;
	}
	
	private String getWeekName (int weekNo){
		String weekName = "";
		
		switch (weekNo) {
		case 1 :
			weekName = getResources().getString(R.string.first_weeks_name);
			break;

		case 2 :
			weekName = getResources().getString(R.string.second_weeks_name);
			break;
			
		default :
			weekName = "Week " + weekNo;
			break;

		}
		
		return weekName;
	}

    @Override
    public boolean dispatchTouchEvent(MotionEvent me){
        // Call onTouchEvent of SimpleGestureFilter class
         this.detector.onTouchEvent(me);
       return super.dispatchTouchEvent(me);
    }
   
    @Override
     public void onSwipe(int direction) {
      
    	//Only move if the user isn't editing
      if (!inEditting){
    	  switch (direction) {
    	  	case SimpleGestureFilter.SWIPE_RIGHT : 
    	  		currentDay++;
    	  		if (currentDay > endDay) {
    	  			currentDay = startDay;
        	  		currentWeek++;
        	  		if (currentWeek > weekCount)
        	  			currentWeek = 1;
    	  		}
    	  		break;
      		
    	  	case SimpleGestureFilter.SWIPE_LEFT :
    	  		currentDay--;
    	  		if (currentDay < startDay) {
    	  			currentDay = endDay;
        	  		currentWeek--;
        	  		if (currentWeek < 1)
        	  			currentWeek = weekCount;
    	  		}
    	  		break;
    	  		
    	  	case SimpleGestureFilter.SWIPE_DOWN :
    	  		currentWeek++;
    	  		if (currentWeek > weekCount)
    	  			currentWeek = 1;
    	  		break;
    	  		
    	  	case SimpleGestureFilter.SWIPE_UP :
    	  		currentWeek--;
    	  		if (currentWeek < 1)
    	  			currentWeek = weekCount;
    	  		break;
      
      		}
    	  	
    	  	if (!weekView)
    	  		populateDay(currentDay, currentWeek);
    	  	else {
    	  		
    	  		layout.removeAllViews();
            	createWeek();
            	super.setContentView(layout);    	  		
    	  	}
      	}
     }
      
     @Override
     public void onDoubleTap() {
     }	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (terms == null) {
			terms = new TermCalendar(this);
		}

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		
		//fix bug so display works correctly when upside down
		//Change to manifest also needed (android:screenOrientation="fullSensor")
		int x = Math.abs(size.x);
		int y = Math.abs(size.y);
		
		if (x > y) {
			weekView = true;
		}
		
		
        // Detect touched area 
        detector = new SimpleGestureFilter(this,this);
        
		SharedPreferences pref;
		String prefs_key;
		 		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		prefs_key = getResources().getString(R.string.pref_weeks_key);
		
		if (pref != null) {
			weekCount = Integer.parseInt(pref.getString(prefs_key, "2"));
		}

		prefs_key = getResources().getString(R.string.pref_lessons_key);
		
		if (pref != null) {
			lessonCount = Integer.parseInt(pref.getString(prefs_key, "5"));
		}

		prefs_key = getResources().getString(R.string.pref_weekend_key);
		
		if (pref != null) {
			includeWeekends = pref.getBoolean(prefs_key, false);
			
			if (includeWeekends) {
				//Defaults to Monday and Friday
				startDay = Calendar.SUNDAY;
				endDay = Calendar.SATURDAY;
			}
		}
		
		if (savedInstanceState != null){
			currentDay = savedInstanceState.getInt("currentDay");
			currentWeek = savedInstanceState.getInt("currentWeek");
		} else {
			currentDay = getCurrentDay();
			currentWeek = getCurrentWeek();
		}

		
		timeTable = new TimeTable (getApplicationContext());

        //---param for views---
        LayoutParams lp = 
            new TableLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        
        //---create a layout---
        layout = new TableLayout(this);
        layout.setId(myGenerateViewId());
        layout.setPadding(5, 5, 5, 5);

        layout.setLayoutParams(lp);  
        layout.setStretchAllColumns(true);
        layout.setBackgroundColor(Color.parseColor("#000000"));
        
        if (!weekView) {
        //-- Populate the details for the day
        	createDay();
        
			super.setContentView(layout);

        	populateDay(currentDay, currentWeek);
			releaseDay (false);        
        } else {
        	createWeek();
        	super.setContentView(layout);
        	
        }
	}

	@Override
	public void onSaveInstanceState(Bundle outState){
		
		outState.putInt("currentDay", currentDay);
		outState.putInt("currentWeek", currentWeek);
		
		super.onSaveInstanceState(outState);
	}
	
	
	private void createWeek() {
		int dayCounter;
		int lessonCounter;
		
		Locale usersLocale = Locale.getDefault();
		DateFormatSymbols dfs = new DateFormatSymbols(usersLocale);
		String weekdays[] = dfs.getShortWeekdays();
		
		
        TableRow tableRow = new TableRow(this);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(  
                LayoutParams.WRAP_CONTENT,  
                LayoutParams.WRAP_CONTENT);  

		params.setMargins(3, 3, 2, 10);
        tableRow.setLayoutParams(params);
        tableRow.setBackgroundColor(Color.parseColor("#000000"));
        //tableRow.setPadding(15, 15, 15, 15);
	
		for (dayCounter = startDay; dayCounter <= endDay; dayCounter++) {
			
			TextView header = new TextView(this);
			header.setText(weekdays[dayCounter] + " " + getWeekName(currentWeek));
			header.setTypeface(null, Typeface.BOLD);
			header.setBackgroundColor(Color.parseColor("#99CCFF"));
			        
			//---adds the textview---
			tableRow.addView(header);
		}

		layout.addView(tableRow);
		
		for (lessonCounter =1; lessonCounter <= lessonCount; lessonCounter++) {        
			tableRow = new TableRow(this);
			params = new TableLayout.LayoutParams(  
	                LayoutParams.WRAP_CONTENT,  
	                LayoutParams.WRAP_CONTENT);
			params.setMargins(3, 3, 2, 10);
	        tableRow.setLayoutParams(params);
	        //tableRow.setBackgroundColor(Color.parseColor("#D6EBFF"));

			for (dayCounter = startDay; dayCounter <= endDay; dayCounter++) {
				TextView tv = new TextView (this);
				tv.setText(timeTable.getLesson(currentWeek, dayCounter, lessonCounter).info1);
				if (dayCounter == currentDay)
					tv.setBackgroundColor(Color.parseColor("#FF9933"));
				else
					tv.setBackgroundColor(Color.parseColor("#D6EBFF"));
				tableRow.addView(tv);
			}
			
			layout.addView(tableRow);
		}
		
	}
	
	private View createDayHeader () {
        //Put edit button at the bottom
		TableLayout subTable = new TableLayout(this);
		
        TableRow tableRow = new TableRow(this);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(  
                LayoutParams.WRAP_CONTENT,  
                LayoutParams.WRAP_CONTENT);  

		params.setMargins(3, 3, 2, 10);
        tableRow.setLayoutParams(params);
        tableRow.setBackgroundColor(Color.parseColor("#99CCFF"));
        tableRow.setPadding(15, 15, 15, 15);


        TextView header = new TextView(this);
        header.setText(" ");
        header.setId(R.id.headerTextId);
        header.setTextSize(24);
        
        //---adds the textview---
        tableRow.addView(header);

		params.setMargins(3, 3, 2, 10);
        tableRow.setLayoutParams(params);
        tableRow.setPadding(15, 15, 15, 15);
        
        ImageButton cancelButton = new ImageButton (this);
        
        cancelButton.setContentDescription(getResources().getString(R.string.cancelButton));
        cancelButton.setImageResource(R.drawable.cancelbutton);
        cancelButton.setOnClickListener(saveListener);
        cancelButton.setId(R.id.cancelButtonId);
        cancelButton.setEnabled(false);
        cancelButton.setVisibility(View.INVISIBLE);
        cancelButton.setBackgroundColor(Color.parseColor("#99CCFF"));
        
        tableRow.addView(cancelButton);
        
        ImageButton editButton = new ImageButton (this);
        
        editButton.setContentDescription(getResources().getString(R.string.editButton));
        editButton.setImageResource(R.drawable.editbutton);
        editButton.setOnClickListener(editListener);
        editButton.setId(R.id.editButtonId);
        editButton.setBackgroundColor(Color.parseColor("#99CCFF"));
        
        tableRow.addView(editButton);
        
        subTable.addView(tableRow);

        return subTable;
	}
	
	/***************************************************************************
	 *, Display the editable view of the specified week day timetable
	 */
	private void createDay () {
		int lessonCounter;
		String viewId;
		int nextId;
		ArrayAdapter <String> adapter;
		
		String info1Vals[] = timeTable.getInfo1Vals();
		String info2Vals[] = timeTable.getInfo2Vals();
		String info3Vals[] = timeTable.getInfo3Vals();

		
		TableRow tableRow = new TableRow(this);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(  
                LayoutParams.WRAP_CONTENT,  
                LayoutParams.WRAP_CONTENT);  

        layout.removeAllViews();
        viewMap.clear();
        
        layout.addView(createDayHeader());
		
		for (lessonCounter =1; lessonCounter <= lessonCount; lessonCounter++) {
			
			tableRow = new TableRow(this);
			params = new TableLayout.LayoutParams(  
	                LayoutParams.WRAP_CONTENT,  
	                LayoutParams.WRAP_CONTENT);;  
            params.setMargins(3, 3, 2, 10);
			
			tableRow.setLayoutParams(params);
			tableRow.setBackgroundColor(Color.parseColor("#D6EBFF"));
			tableRow.setPadding(5, 5, 5, 5);
			
			AutoCompleteTextView tv = new AutoCompleteTextView(this);
	        
	        nextId = myGenerateViewId();
	        viewId = "lesson" + lessonCounter + "info1";
	        tv.setId(nextId);
	        viewMap.put(viewId, nextId);
	        
	        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, info1Vals);
	        tv.setAdapter(adapter);
	        
	        tableRow.addView(tv);

	        tv = new AutoCompleteTextView(this);

	        nextId = myGenerateViewId();
	        viewId = "lesson" + lessonCounter + "info2";
	        tv.setId(nextId);
	        viewMap.put(viewId, nextId);

	        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, info2Vals);
	        tv.setAdapter(adapter);
        	        
	        tableRow.addView(tv);

	        tv = new AutoCompleteTextView(this);

	        nextId =myGenerateViewId();
	        viewId = "lesson" + lessonCounter + "info3";
	        tv.setId(nextId);
	        viewMap.put(viewId, nextId);

	        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, info3Vals);
	        tv.setAdapter(adapter);
	        	        
	        tableRow.addView(tv);
	        
	        //---adds the textview---
	        layout.addView(tableRow);			
		}
		
	}
	
private void populateDay (int currentDay, int currentWeek){
	TimeTable.Lesson lessonInfo;
	int lessonCounter;
	String viewKey;
	int viewId;
	
	Locale usersLocale = Locale.getDefault();
	DateFormatSymbols dfs = new DateFormatSymbols(usersLocale);
	String weekdays[] = dfs.getShortWeekdays();

	TextView header = (TextView)findViewById(R.id.headerTextId);
    header.setText(weekdays[currentDay] + " " + getWeekName(currentWeek));

	for (lessonCounter =1; lessonCounter <= lessonCount; lessonCounter++) {
		
		lessonInfo = timeTable.getLesson(currentWeek, currentDay, lessonCounter);

		viewKey = "lesson" + lessonCounter + "info1";		
		viewId = viewMap.get(viewKey);
		AutoCompleteTextView tv = (AutoCompleteTextView)findViewById(viewId);

        tv.setText(lessonInfo.info1);

		viewKey = "lesson" + lessonCounter + "info2";		
		viewId = viewMap.get(viewKey);
		tv = (AutoCompleteTextView)findViewById(viewId);

        tv.setText(lessonInfo.info2);

		viewKey = "lesson" + lessonCounter + "info3";		
		viewId = viewMap.get(viewKey);
		tv = (AutoCompleteTextView)findViewById(viewId);

        tv.setText(lessonInfo.info3);
	}
}
/*******************************************************************************************
 * lock or unlock the edit views	
 */
private void releaseDay (boolean allowEdit){
	int lessonCounter;
	String viewKey;
	int viewId;
	
	for (lessonCounter =1; lessonCounter <= lessonCount; lessonCounter++) {
		
		viewKey = "lesson" + lessonCounter + "info1";		
		viewId = viewMap.get(viewKey);
		AutoCompleteTextView tv = (AutoCompleteTextView)findViewById(viewId);
		
        tv.setEnabled(allowEdit);
        tv.setFocusable(allowEdit);
        tv.setFocusableInTouchMode(allowEdit);
        tv.setClickable(allowEdit);
        tv.setTextColor(Color.BLACK);
		
		viewKey = "lesson" + lessonCounter + "info2";		
		viewId = viewMap.get(viewKey);
		tv = (AutoCompleteTextView)findViewById(viewId);
		
        tv.setEnabled(allowEdit);
        tv.setFocusable(allowEdit);
        tv.setFocusableInTouchMode(allowEdit);
        tv.setClickable(allowEdit);
        tv.setTextColor(Color.BLACK);

		viewKey = "lesson" + lessonCounter + "info3";		
		viewId = viewMap.get(viewKey);
		tv = (AutoCompleteTextView)findViewById(viewId);
		
        tv.setEnabled(allowEdit);
        tv.setFocusable(allowEdit);
        tv.setFocusableInTouchMode(allowEdit);
        tv.setClickable(allowEdit);
        tv.setTextColor(Color.BLACK);
        
	}
	
	inEditting = allowEdit;
}

private void saveDay (int currentDay, int currentWeek){
	int lessonCounter;
	TimeTable.Lesson lessonInfo;
	String viewKey;
	int viewId;
			
	
	for (lessonCounter =1; lessonCounter <= lessonCount; lessonCounter++) {
		lessonInfo = timeTable.getLesson(currentWeek, currentDay, lessonCounter);
		lessonInfo.weekNo = currentWeek;
		lessonInfo.dayNo = currentDay;
		lessonInfo.lessonNo = lessonCounter;
		
		viewKey = "lesson" + lessonCounter + "info1";
		viewId = viewMap.get(viewKey);
		AutoCompleteTextView tv = (AutoCompleteTextView)findViewById(viewId);
		lessonInfo.info1 = tv.getText().toString();

		viewKey = "lesson" + lessonCounter + "info2";
		viewId = viewMap.get(viewKey);
		tv = (AutoCompleteTextView)findViewById(viewId);
		lessonInfo.info2 = tv.getText().toString();
		
		viewKey = "lesson" + lessonCounter + "info3";
		viewId = viewMap.get(viewKey);
		tv = (AutoCompleteTextView)findViewById(viewId);
		lessonInfo.info3 = tv.getText().toString();
				
		timeTable.addLesson(currentWeek, currentDay, lessonCounter, lessonInfo.info1, lessonInfo.info2, lessonInfo.info3);
	}
}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		
		switch (item.getItemId()) {
		case R.id.action_settings:
			// Starts the Settings activity on top of the current activity
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		case R.id.term_details:
			// Starts the term dates activity on top of the current activity
			intent = new Intent(this, term_details.class);
			startActivity(intent);
			break;

		default:
			break;
		}

		return true;
	}
	
	private OnClickListener editListener = new OnClickListener() {
		
		public void onClick (View v) {
			ImageButton btn = (ImageButton)v;
			ImageButton cancelButton;
			releaseDay (true);
			
			btn.setContentDescription(getResources().getString(R.string.saveButton));
			btn.setImageResource(R.drawable.savebutton);
			btn.setOnClickListener(saveListener);
			
			cancelButton = (ImageButton)findViewById(R.id.cancelButtonId);
			cancelButton.setVisibility(View.VISIBLE);
			cancelButton.setEnabled(true);
		}
	};

	private OnClickListener saveListener = new OnClickListener() {
		
		public void onClick (View v) {
			
			releaseDay (false);
			
			if (v.getId() != R.string.saveButton)
				saveDay(currentDay, currentWeek);
			
			ImageButton btn = (ImageButton)findViewById(R.id.editButtonId);
			btn.setContentDescription(getResources().getString(R.string.editButton));
			btn.setImageResource(R.drawable.editbutton);
			btn.setOnClickListener(editListener);

			ImageButton cancelButton = (ImageButton)findViewById(R.id.cancelButtonId);
			cancelButton.setVisibility(View.INVISIBLE);
			cancelButton.setEnabled(true);
		
		}
	};
	
	/**
	 * Generate a value suitable for use in {@link #setId(int)}.
	 * This value will not collide with ID values generated at build time by aapt for R.id.
	 *
	 * @return a generated ID value
	 */
	public static int myGenerateViewId() {
	    for (;;) {
	        final int result = sNextGeneratedId.get();
	        // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
	        int newValue = result + 1;
	        if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
	        if (sNextGeneratedId.compareAndSet(result, newValue)) {
	            return result;
	        }
	    }
	}
}
