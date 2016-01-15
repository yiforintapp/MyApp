
package com.leo.appmaster.applocker;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;

public class DayView extends TextView implements OnClickListener {

    public static String[] days = AppMasterApplication.getInstance().getResources()
            .getStringArray(R.array.days_of_week);

    private DayOfWeek dayOfWeek;

    public DayView(Context context) {
        super(context);
    }

    public DayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        dayOfWeek = new DayOfWeek();
        setTextColor(Color.GRAY);
        setBackgroundResource(R.drawable.time_unselect);
        this.setGravity(Gravity.CENTER);
        setOnClickListener(this);
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            setTextColor(Color.WHITE);
            setBackgroundResource(R.drawable.time_select);
        } else {
            setTextColor(Color.GRAY);
            setBackgroundResource(R.drawable.time_unselect);
        }
        super.setSelected(selected);
    }

    public void setDayOfWeek(int day) {
        if (day < 1 || day > 7) {
            setText("null");
        } else {
            dayOfWeek.dayOfWeek = day;
            dayOfWeek.day = days[day - 1];
            setText(dayOfWeek.day);
        }
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    @Override
    public void onClick(View v) {
        DayView dv = (DayView) v;
        dv.setSelected(!dv.isSelected());
    }

    public class DayOfWeek {
        int dayOfWeek;
        String day;
    }
    
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
            
        }
    }
}
