package uk.me.timlittle.tt1;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import uk.me.timlittle.tt1.SimpleGestureFilter.SimpleGestureListener;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.DatePicker.OnDateChangedListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class term_details extends Activity  implements OnDateSetListener, SimpleGestureListener {
	private TermCalendar terms = null;
	private SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
	private boolean startDatePicked = true;
	private int currentTerm;
	private TermCalendar.Term term;
	private SimpleGestureFilter detector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.term_details);
		
		if (savedInstanceState != null){
			currentTerm = savedInstanceState.getInt("currentTerm");
		}	else {
			currentTerm = 1;
		}
		
		if (terms == null) {
			terms = new TermCalendar(this);
		}
				
		term = terms.getTerm(currentTerm);

        // Detect touched area 
        detector = new SimpleGestureFilter(this,this);
		
		
		Button dateButton = (Button)findViewById(R.id.pickStartButton);
		dateButton.setOnClickListener(dateListener);
		dateButton = (Button)findViewById(R.id.pickEndButton);
		dateButton.setOnClickListener(dateListener);
		
		ImageButton navBtn = (ImageButton)findViewById(R.id.earlierTerm);
		navBtn.setOnClickListener(navButtonListener);

		navBtn = (ImageButton)findViewById(R.id.nextTerm);
		navBtn.setOnClickListener(navButtonListener);
		
		
		CheckBox startWeekChk = (CheckBox)findViewById(R.id.termStartWeekChk);		
		startWeekChk.setOnClickListener ( new OnClickListener () {
		
			@Override
			public void onClick (View v) {
				CheckBox startWeekOnA = (CheckBox) v;
				term.setStartWeek (startWeekOnA.isChecked());
				term.save();
			}
		});
		
		EditText nameView = (EditText)findViewById(R.id.termName);
		
		nameView.setOnFocusChangeListener(new OnFocusChangeListener() {

		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		    /* When focus is lost check that the text field
		    * has valid values.
		    */
		      if (!hasFocus) {
		    	  if (v.getId() == R.id.termName){
		    		  term.name = ((EditText)v).getText().toString();
		    		  term.save();
		    	  }
		      }
		    }
		});
		
		nameView.addTextChangedListener( new TextWatcher () {
			
			public void afterTextChanged(Editable s) {
			   }
			 
			   public void beforeTextChanged(CharSequence s, int start, 
			     int count, int after) {
			   }
			 
			   public void onTextChanged(CharSequence s, int start, 
			     int before, int count) {
				   
				   term.name = s.toString();
				   term.save();
			  
			   }
			
		});
		
		populate();
		
	}



	private OnClickListener dateListener = new OnClickListener () {
		public void onClick (View v) {
		    DialogFragment newFragment = new DatePickerFragment();
		    TextView dateBox;
		    
		    if (v.getId() == R.id.pickStartButton) {
		    	dateBox = (TextView)findViewById(R.id.startDate);
		    	startDatePicked = true;
		    } else {
		    	dateBox = (TextView)findViewById(R.id.endDate);
		    	startDatePicked = false;		    	
		    }

		    CharSequence currentDate = dateBox.getText();
		    	
		    if (currentDate.length() > 0){
		    	((DatePickerFragment) newFragment).setDate(currentDate.toString());
		    }
		    
		    newFragment.show(getFragmentManager(), "datePicker");
		}
		
	};

	private OnClickListener navButtonListener = new OnClickListener () {
		public void onClick (View v) {
		    if (v.getId() == R.id.earlierTerm) {
		    	currentTerm--;
		    	if (currentTerm <1 )
		    		currentTerm = terms.getTermCount();
		    } else {
		    	currentTerm++;
		    	if (currentTerm > terms.getTermCount())
		    		currentTerm = 1;
		    }
		    
		    populate();
		}
		
		
	};

	
	public void onDateSet(DatePicker view, int year, int month, int day) {
		Calendar pickedDate = Calendar.getInstance();
		String pickedDateString;
		
		pickedDate.set(year, month, day);
		pickedDateString = df.format(pickedDate.getTime());
		
		TextView dateBox;
		
		if (startDatePicked) {
			dateBox = (TextView)findViewById(R.id.startDate);
			term.setStartDate (pickedDateString);
			term.save();
		}
		else {
			dateBox = (TextView)findViewById(R.id.endDate);
			term.setEndDate (pickedDateString);
			term.save();
		}
		
		dateBox.setText(pickedDateString);
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState){
		
		outState.putInt("currentTerm", currentTerm);
		
		super.onSaveInstanceState(outState);
	}
	
	private void populate () {
		term = terms.getTerm(currentTerm);
		
		TextView textView = (TextView)findViewById (R.id.termName);
		textView.setText(term.name);
		
		textView  = (TextView)findViewById (R.id.startDate);
		textView.setText(term.getStartDate());
		
		textView  = (TextView)findViewById (R.id.endDate);
		textView.setText(term.getEndDate());

		CheckBox startWeekChk = (CheckBox)findViewById(R.id.termStartWeekChk);
		startWeekChk.setChecked(term.startsA());		
	}
	
    @Override
    public boolean dispatchTouchEvent(MotionEvent me){
        // Call onTouchEvent of SimpleGestureFilter class
         this.detector.onTouchEvent(me);
       return super.dispatchTouchEvent(me);
    }
   
    @Override
     public void onSwipe(int direction) {
  	  switch (direction) {
	  	case SimpleGestureFilter.SWIPE_RIGHT :
	    	currentTerm++;
	    	if (currentTerm > terms.getTermCount() )
	    		currentTerm = 1;
	    	populate();
	  		break;
		
	  	case SimpleGestureFilter.SWIPE_LEFT :
	    	currentTerm--;
	    	if (currentTerm <1 )
	    		currentTerm = terms.getTermCount();
	    	populate();
	  		break;
	  		
	  	default :
	  		break;
  	  }
    	
    }
	
    @Override
    public void onDoubleTap() {
    }	

    
}
