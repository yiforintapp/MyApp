
package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.DayView.DayOfWeek;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.view.View.OnClickListener;

public class DayOfWeekSelectedView extends LinearLayout implements OnClickListener {
    private DayView[] dayViews = new DayView[7];

    public DayOfWeekSelectedView(Context context) {
        super(context);
    }

    public DayOfWeekSelectedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public List<DayOfWeek> getSelectedDay() {
        ArrayList<DayOfWeek> list = new ArrayList<DayOfWeek>();

        for (DayView dv : dayViews) {
            if (dv.isSelected())
                list.add(dv.getDayOfWeek());
        }

        return list;
    }

    public void selectDay(byte day) {
        if (day < 1 || day > 7) {
            return;
        }
        dayViews[day - 1].setSelected(true);
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.day_of_week, this, true);
        dayViews[0] = (DayView) findViewById(R.id.day1);
        dayViews[0].setDayOfWeek(1);
        dayViews[1] = (DayView) findViewById(R.id.day2);
        dayViews[1].setDayOfWeek(2);
        dayViews[2] = (DayView) findViewById(R.id.day3);
        dayViews[2].setDayOfWeek(3);
        dayViews[3] = (DayView) findViewById(R.id.day4);
        dayViews[3].setDayOfWeek(4);
        dayViews[4] = (DayView) findViewById(R.id.day5);
        dayViews[4].setDayOfWeek(5);
        dayViews[5] = (DayView) findViewById(R.id.day6);
        dayViews[5].setDayOfWeek(6);
        dayViews[6] = (DayView) findViewById(R.id.day7);
        dayViews[6].setDayOfWeek(7);

        super.onFinishInflate();
    }
    
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
            
        }
    }

    @Override
    public void onClick(View v) {

    }

}
