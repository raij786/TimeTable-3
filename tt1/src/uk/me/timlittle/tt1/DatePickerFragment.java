package uk.me.timlittle.tt1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment  {
	// Use the current date as the default date in the picker
	private final Calendar c = Calendar.getInstance();
	private int year = c.get(Calendar.YEAR);
	private int month = c.get(Calendar.MONTH);
	private int day = c.get(Calendar.DAY_OF_MONTH);
	private SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");

	public void setDate (String date) {
		Calendar newDate = Calendar.getInstance();
		
		try {
			newDate.setTime(df.parse(date));
			
			year = newDate.get(Calendar.YEAR);
			month = newDate.get(Calendar.MONTH);
			day = newDate.get(Calendar.DAY_OF_MONTH);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	
	// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(),(term_details)getActivity(), year, month, day);
	}
	
}