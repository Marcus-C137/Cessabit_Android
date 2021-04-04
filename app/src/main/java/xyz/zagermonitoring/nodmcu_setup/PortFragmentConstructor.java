package xyz.zagermonitoring.nodmcu_setup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.view.ScrollingView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.zagermonitoring.nodmcu_setup.CustomObjects.NewTempDialogFragment;
import xyz.zagermonitoring.nodmcu_setup.Items.FirebaseTimeTemp;
import xyz.zagermonitoring.nodmcu_setup.Items.FirebaseTimeTempUpdate;

public class PortFragmentConstructor  {
    private Fragment portFragment;
    private DevicePage devicePage;
    private ScrollView scrollView;
    private String deviceID;
    private int portIndex;
    private String TAG;
    private int numOfPoints = 360; // number of dataPoints on chart
    private int tempDatGran = 10; //seconds between data
    private int powerDatGran = 10; //seconds between data
    private final int hourDataGranularity = 10; //seconds
    private final int dayDataGranularity = 240; //seconds
    private final int weekDataGranularity = 1680; //seconds
    private final int monthDataGranularity = 7200; //seconds

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String UID = mAuth.getCurrentUser().getUid();

    private MyXAxisFormatter myXAxisFormatter;
    private LineChart tempChart;
    private LineChart powerChart;
    private LineDataSet tempDataSet;
    private LineDataSet powerDataSet;

    private Button setTempBtn;
    private Button lowAlarmBtn;
    private Button highAlarmBtn;
    private Switch switchPortOn;

    public PortFragmentConstructor(int portIndex, Fragment portFragment, Activity activity){
        this.portIndex =  portIndex;
        TAG = "PORT" + String.valueOf(portIndex) + "FRAGMENT";
        this.portFragment = portFragment;
        devicePage = (DevicePage) activity;
        deviceID = devicePage.getDeviceID();
    }

    public void initializeCharts(){
        ArrayList<Long> times = devicePage._firebaseData.getTimes();
        ArrayList<Float> temps = devicePage._firebaseData.getTemps(portIndex);
        ArrayList<Float> powers = devicePage._firebaseData.getPowers(portIndex);
        ArrayList<Boolean> portsOn = devicePage._firebaseData.getPortActive();
        List<Number> setTemps = devicePage._firebaseData.getSetTemps();
        List<Number> lowAlarms = devicePage._firebaseData.getLowAlarms();
        List<Number> highAlarms = devicePage._firebaseData.getHighAlarms();
        if (setTemps != null && lowAlarms != null && highAlarms != null){
            String stText = setTemps.get(portIndex).toString() + " F";
            String laText = lowAlarms.get(portIndex).toString() + " F";
            String haText = highAlarms.get(portIndex).toString() + " F";
            setTempBtn.setText(stText);
            lowAlarmBtn.setText(laText);
            highAlarmBtn.setText(haText);
        }
        if (portsOn != null && !portsOn.isEmpty()){
            switchPortOn.setChecked(portsOn.get(portIndex));
        }
        if (temps != null && times != null){
            fillTempChart(temps, times);
        }
        if (powers != null && times !=null){
            fillPowerChart(powers, times);
        }
    }


    private void fillTempChart(ArrayList<Float> temps, ArrayList<Long> times){
        Log.d(TAG, "temps size: " + temps.size());
        Log.d(TAG, "times size: " + times.size());
        ArrayList<Long> timeSlice = new ArrayList<>();
        ArrayList<Float> displayTemps = new ArrayList<>();
        long latestTime = times.get(0);
        long currentTime = (Calendar.getInstance().getTimeInMillis() / 1000) - 10;
        long offsetTime = currentTime - latestTime;
        Log.d(TAG, "latestTime " + latestTime);
        Log.d(TAG, "currentTime " + currentTime);

        for(int i=0; i<numOfPoints; i++){
            timeSlice.add(currentTime - i*tempDatGran);
        }

        for (int i=0; i<numOfPoints; i++){
            long timeInt = timeSlice.get(i);
            long timeIntMax = timeInt + tempDatGran/2;
            long timeIntMin = timeInt - tempDatGran/2;
            for(int j=0; j< numOfPoints; j++){
                int indexJ = j * tempDatGran/10;
                long time;
                if (indexJ < times.size()){
                    time = times.get(indexJ);
                    if (timeIntMin < time && time < timeIntMax){
                        displayTemps.add(temps.get(indexJ));
                        break;
                    }
                }
                if (j == numOfPoints-1){
                    displayTemps.add(0f);
                }
            }
        }

        ArrayList<Entry> values = new ArrayList<>();
        Collections.reverse(timeSlice);
        Collections.reverse(displayTemps);
        myXAxisFormatter.setInterval(tempDatGran);
        myXAxisFormatter.setStartTime(timeSlice.get(0));
        for(int i=0; i< displayTemps.size(); i++){
            values.add(new Entry(i, displayTemps.get(i)));
        }
        setTempData(values);
    }

    private void setTempData(ArrayList<Entry> values){
        List<Integer> colors = new ArrayList<>();

        for (int i = 0; i < values.size(); i++){
            if (values.get(i).getY() == 0f){
                colors.add(Color.GRAY);
            }else if (values.get(i).getY() < -195){
                colors.add(Color.RED);
                values.get(i).setY(0f);
            }else{
                colors.add(Color.parseColor("#00dadf"));
            }
        }
        tempDataSet = new LineDataSet(values, "DataSet 1");
        tempDataSet.setColors(colors);
        tempDataSet.setCircleColors(colors);
        tempDataSet.setValueTextColor(Color.WHITE);
        tempDataSet.setDrawCircles(false);
        tempDataSet.setDrawCircleHole(false);
        //tempDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(tempDataSet); // add the data sets
        LineData tempLineData = new LineData(dataSets);
        tempChart.clear();
        tempChart.setData(tempLineData);
        //Log.d(TAG, tempChart.getData().getDataSets().toString());
        tempChart.invalidate();

    }

    private void appendTempChart(Float temp, Long time){
        if (tempDatGran == hourDataGranularity) {
            long startTime = time - (numOfPoints * tempDatGran);
            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(startTime * 1000);
            myXAxisFormatter.setInterval(tempDatGran);
            myXAxisFormatter.setStartTime(startTime);
            int entryCount = tempChart.getLineData().getDataSetByIndex(0).getEntryCount();
            //tempChart.getLineData().getDataSetByIndex(0).removeEntry(entryCount-1);
            tempChart.getLineData().getDataSetByIndex(0).addEntry(new Entry(entryCount, temp));
            tempChart.getLineData().notifyDataChanged();
            tempChart.notifyDataSetChanged();
            tempChart.invalidate();
        }
    }

    private void fillPowerChart(ArrayList<Float> powers, ArrayList<Long> times){
        Log.d(TAG, "powers size: " + powers.size());
        Log.d(TAG, "times size: " + times.size());
        //Log.d(TAG, "powers " + powers);
        ArrayList<Long> timeSlice = new ArrayList<>();
        ArrayList<Float> displayPowers = new ArrayList<>();
        long startTime = (Calendar.getInstance().getTimeInMillis() / 1000) - 10; //10 sec delay offset
        //Log.d(TAG, "start time in secs " + startTime);
        for(int i=0; i<numOfPoints; i++){
            timeSlice.add(startTime - i*powerDatGran);
        }
        //Log.d(TAG, "timeSlice " + timeSlice.toString());
        //Log.d(TAG, "timeSlice " + timeSlice.toString());
        //make displayTemps find non congruent temp snap shots (device off) fill with zero

        for (int i=0; i<numOfPoints; i++){
            long timeInt = timeSlice.get(i);
            long timeIntMax = timeInt + powerDatGran/2;
            long timeIntMin = timeInt - powerDatGran/2;
            for(int j=0; j< numOfPoints; j++){
                int indexJ = j * powerDatGran/10;
                long time;
                if (indexJ < times.size()){
                    time = times.get(indexJ);
                    if (timeIntMin < time && time < timeIntMax){
                        displayPowers.add(powers.get(indexJ));
                        break;
                    }
                }
                if (j == numOfPoints-1){
                    displayPowers.add(0f);
                }
            }
        }


        //Log.d(TAG, "displayTemps size" + displayTemps.size());
        //Log.d(TAG, "timeSlice size" + timeSlice.size());
        //Log.d(TAG, "displayTemps " + displayTemps.toString());
        ArrayList<Entry> values = new ArrayList<>();
        Collections.reverse(timeSlice);
        Collections.reverse(displayPowers);
        myXAxisFormatter.setInterval(powerDatGran);
        myXAxisFormatter.setStartTime(timeSlice.get(0));
        for(int i=0; i< displayPowers.size(); i++){
            values.add(new Entry(i, displayPowers.get(i)));
        }
        setPowerData(values);
    }

    private void setPowerData(ArrayList<Entry> values){
        List<Integer> colors = new ArrayList<>();

        for (int i = 0; i < values.size(); i++){
            values.get(i).setY(values.get(i).getY() * 100); // change to percentage
            if (values.get(i).getY() == 0f){
                colors.add(Color.GRAY);
            }else if (values.get(i).getY() > 90){
                colors.add(Color.RED);
            }else{
                colors.add(Color.parseColor("#00dadf"));
            }
        }
        powerDataSet = new LineDataSet(values, "DataSet 1");
        powerDataSet.setColors(colors);
        powerDataSet.setCircleColors(colors);
        powerDataSet.setValueTextColor(Color.WHITE);
        powerDataSet.setDrawCircles(false);
        powerDataSet.setDrawCircleHole(false);
        //powerDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(powerDataSet); // add the data sets
        LineData powerLineData = new LineData(dataSets);
        powerChart.clear();
        powerChart.setData(powerLineData);
        //Log.d(TAG, powerChart.getData().getDataSets().toString());
        powerChart.invalidate();

    }

    public void setAxisFormatter(MyXAxisFormatter xAxisFormatter){
        myXAxisFormatter = xAxisFormatter;
    }

    public void setDataSets(LineDataSet tempDataSet, LineDataSet powerDataSet){
        this.tempDataSet = tempDataSet;
        this.powerDataSet = powerDataSet;
    }

    public void setUpBroadcast(BroadcastReceiver br, BroadcastReceiver br1, BroadcastReceiver br2, Context context){
        br = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if("com.zagermonitoring.FIREBASE_NEWDATA".equals(intent.getAction())){
                    Log.d(TAG, "UPDATED FIREBASE_NEWDATA");
                    String port = "P" + String.valueOf(portIndex +1);
                    FirebaseTimeTemp ftt = intent.getParcelableExtra("com.zagermonitoring.FIREBASE_NEWTEMP_" + port);
                    fillTempChart(ftt.getTemps(), ftt.getTimes());
                    fillPowerChart(ftt.getPowers(), ftt.getTimes());
                }
            }
        };
        br1 = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if("com.zagermonitoring.FIREBASE_NEWSETTINGS".equals(intent.getAction())){
                    Log.d(TAG, "called");

                    List<Number> setTemps = (List<Number>) intent.getSerializableExtra("com.zagermonitoring.FIREBASE_NEW_SET_TEMPS");
                    List<Number> lowAlarms= (List<Number>) intent.getSerializableExtra("com.zagermonitoring.FIREBASE_NEW_LOW_ALARM");
                    List<Number> highAlarms=(List<Number>) intent.getSerializableExtra("com.zagermonitoring.FIREBASE_NEW_HIGH_ALARM");
                    setTempBtn.setText(setTemps.get(portIndex).toString());
                    lowAlarmBtn.setText(lowAlarms.get(portIndex).toString());
                    highAlarmBtn.setText(highAlarms.get(portIndex).toString());
                }
            }
        };
        br2 = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if("com.zagermonitoring.FIREBASE_NEWDATA_POINT".equals(intent.getAction())){
                    String portName = "P" + String.valueOf(portIndex+1);
                    FirebaseTimeTempUpdate fttu = intent.getParcelableExtra("com.zagermonitoring.FIREBASE_NEWTEMP_POINT_" + portName);
                    appendTempChart(fttu.getTemp(), fttu.getTime());
                    //appendChart(fttu.getPower(), fttu.getTime());
                }
            }
        };
        LocalBroadcastManager.getInstance(context).registerReceiver(br , new IntentFilter("com.zagermonitoring.FIREBASE_NEWDATA"));
        LocalBroadcastManager.getInstance(context).registerReceiver(br1 , new IntentFilter("com.zagermonitoring.FIREBASE_NEWSETTINGS"));
        LocalBroadcastManager.getInstance(context).registerReceiver(br2 , new IntentFilter("com.zagermonitoring.FIREBASE_NEWDATA_POINT"));
    }


    public void setUpButtons(Button portSetTempBtn, Button portLowAlarmBtn, Button portHighAlarmBtn, final Fragment PortFragment){
        this.setTempBtn = portSetTempBtn;
        this.lowAlarmBtn = portLowAlarmBtn;
        this.highAlarmBtn = portHighAlarmBtn;
        setTempBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewTempDialogFragment dialog = NewTempDialogFragment.newInstance(1);
                dialog.setTargetFragment(PortFragment, 1);
                dialog.show(PortFragment.getParentFragmentManager(), "NewTempDialogFragment");
            }
        });
        lowAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewTempDialogFragment dialog = NewTempDialogFragment.newInstance(2);
                dialog.setTargetFragment(PortFragment, 1);
                dialog.show(PortFragment.getParentFragmentManager(), "NewTempDialogFragment");
            }
        });
        highAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewTempDialogFragment dialog = NewTempDialogFragment.newInstance(3);
                dialog.setTargetFragment(PortFragment, 1);
                dialog.show(PortFragment.getParentFragmentManager(), "NewTempDialogFragment");
            }
        });
    }

    public void setUpSwitch(Switch portSwitch, final Context context){
        this.switchPortOn = portSwitch;
        switchPortOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Map<String, Object> portOnMap = new HashMap<>();
                portOnMap.put("port4On", b);
                db.collection("users").document(UID).collection("devices").document(deviceID).set(portOnMap, SetOptions.merge());
                if (b) Toast.makeText(context, "Port On", Toast.LENGTH_LONG).show();
                if (!b) Toast.makeText(context, "Port Off", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setScrollView(ScrollView scrollView){
        this.scrollView = scrollView;
    }

    public void setUpSeek(SeekBar tempSeekBar, SeekBar powerSeekBar){
        tempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Write code to perform some action when progress is changed.

                Log.d(TAG, "progress " + progress);
                switch(progress){
                    case 0:
                        tempDatGran = hourDataGranularity;
                        break;
                    case 1:
                        tempDatGran = dayDataGranularity;
                        break;
                    case 2:
                        tempDatGran = weekDataGranularity;
                        break;
                    case 3:
                        tempDatGran = monthDataGranularity;
                        break;
                }

                fillTempChart(devicePage._firebaseData.getTemps(portIndex), devicePage._firebaseData.getTimes());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Write code to perform some action when touch is started.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Write code to perform some action when touch is stopped.
            }

        });
        powerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Write code to perform some action when progress is changed.
                int hourDataGranularity = 10; //seconds
                int dayDataGranularity = 240; //seconds
                int weekDataGranularity = 1680; //seconds
                int monthDataGranularity = 7200; //seconds
                Log.d(TAG, "progress " + progress);
                switch(progress){
                    case 0:
                        powerDatGran = hourDataGranularity;
                        break;
                    case 1:
                        powerDatGran = dayDataGranularity;
                        break;
                    case 2:
                        powerDatGran = weekDataGranularity;
                        break;
                    case 3:
                        powerDatGran = monthDataGranularity;
                        break;
                }

                fillPowerChart(devicePage._firebaseData.getPowers(portIndex), devicePage._firebaseData.getTimes());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Write code to perform some action when touch is started.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Write code to perform some action when touch is stopped.
            }

        });
    }


    @SuppressLint("ClickableViewAccessibility")
    public void setUpChart(LineChart portTempChart, LineChart portPowerChart){

        this.tempChart = portTempChart;
        this.powerChart = portPowerChart;
        tempChart.getDescription().setEnabled(false);
        tempChart.setDrawGridBackground(false);
        tempChart.setHighlightPerDragEnabled(false);
        tempChart.setHighlightPerTapEnabled(false);
        tempChart.setDragEnabled(true);
        tempChart.setScaleEnabled(true);
        tempChart.setPinchZoom(true);
        tempChart.getLegend().setEnabled(false);
        tempChart.setMaxVisibleValueCount(20);

        // // X-Axis Style // //
        tempChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        tempChart.getXAxis().setDrawGridLines(false);
        tempChart.getXAxis().setTextColor(Color.WHITE);
        tempChart.getXAxis().setLabelCount(4, true);
        tempChart.getXAxis().setValueFormatter( myXAxisFormatter );

        // // Y-Axis Style // //
        tempChart.getAxisLeft().setDrawGridLines(false);
        tempChart.getAxisLeft().setTextColor(Color.WHITE);
        tempChart.getAxisLeft().setAxisMinimum(-1F);
        tempChart.getAxisLeft().setAxisMaximum(120F);

        // disable dual axis (only use LEFT axis)
        tempChart.getAxisRight().setEnabled(false);
        tempChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
            @Override
            public void onChartLongPressed(MotionEvent me) {}
            @Override
            public void onChartDoubleTapped(MotionEvent me) {}
            @Override
            public void onChartSingleTapped(MotionEvent me) {}
            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}
            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                //Log.d(TAG, "scaleX: "+ scaleX + " scaleY: " + scaleY);
                float range = tempChart.getHighestVisibleX() - tempChart.getLowestVisibleX();
                if (range < 40) {
                    tempDataSet.setDrawCircles(true);
                    tempDataSet.setDrawCircleHole(true);
                }else{
                    tempDataSet.setDrawCircles(false);
                    tempDataSet.setDrawCircleHole(false);
                }
            }
            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {}
        });
        tempChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        break;
                    }
                }

                return false;
            }
        });

        ////////////POWER CHART /////////////

        powerChart.getDescription().setEnabled(false);
        powerChart.setDrawGridBackground(false);
        powerChart.setHighlightPerDragEnabled(false);
        powerChart.setHighlightPerTapEnabled(false);
        powerChart.setDragEnabled(true);
        powerChart.setScaleEnabled(true);
        powerChart.setPinchZoom(true);
        powerChart.getLegend().setEnabled(false);
        powerChart.setMaxVisibleValueCount(20);

        // // X-Axis Style // //
        powerChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        powerChart.getXAxis().setDrawGridLines(false);
        powerChart.getXAxis().setTextColor(Color.WHITE);
        powerChart.getXAxis().setLabelCount(4, true);
        powerChart.getXAxis().setValueFormatter( myXAxisFormatter );

        // // Y-Axis Style // //
        powerChart.getAxisLeft().setDrawGridLines(false);
        powerChart.getAxisLeft().setTextColor(Color.WHITE);
        powerChart.getAxisLeft().setAxisMinimum(-1F);
        powerChart.getAxisLeft().setAxisMaximum(100F);

        // disable dual axis (only use LEFT axis)
        powerChart.getAxisRight().setEnabled(false);
        powerChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
            @Override
            public void onChartLongPressed(MotionEvent me) {}
            @Override
            public void onChartDoubleTapped(MotionEvent me) {}
            @Override
            public void onChartSingleTapped(MotionEvent me) {}
            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}
            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                //Log.d(TAG, "scaleX: "+ scaleX + " scaleY: " + scaleY);
                float range = powerChart.getHighestVisibleX() - powerChart.getLowestVisibleX();
                if (range < 40) {
                    powerDataSet.setDrawCircles(true);
                    powerDataSet.setDrawCircleHole(true);
                }else{
                    powerDataSet.setDrawCircles(false);
                    powerDataSet.setDrawCircleHole(false);
                }
            }
            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {}
        });
        powerChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        break;
                    }
                }

                return false;
            }
        });

    }

}
