package com.example.teamloosers.behereandroid;

import android.app.Dialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by teamloosers on 01/05/17.
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    OnDateSelectedListener mListener;

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        mListener = (OnDateSelectedListener) getActivity();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        mListener.onDateSelected(dayOfMonth, month, year);
    }

    public interface OnDateSelectedListener {

        public void onDateSelected(int day, int month, int year);
    }
}
