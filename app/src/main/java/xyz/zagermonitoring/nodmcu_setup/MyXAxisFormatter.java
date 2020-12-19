package xyz.zagermonitoring.nodmcu_setup;

import android.icu.text.SimpleDateFormat;

import androidx.appcompat.app.WindowDecorActionBar;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.type.Date;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MyXAxisFormatter extends ValueFormatter {

    private Calendar cal = new GregorianCalendar();
    private SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss a");
    private int _interval;
    private long _startTime;

    public void setInterval(int interval){
            _interval = interval;
    }

    public void setStartTime(long startTime){
        _startTime = startTime;
    }

    @Override
    public String getAxisLabel(float index, AxisBase axisBase){
        int indexI = (int) index;
        long time = _startTime + indexI*_interval; //time in seconds
        cal.setTimeInMillis(time * 1000);
        return sdf.format(cal.getTime());
    }
}
