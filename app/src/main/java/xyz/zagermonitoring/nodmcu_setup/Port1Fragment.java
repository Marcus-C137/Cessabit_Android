package xyz.zagermonitoring.nodmcu_setup;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.zagermonitoring.nodmcu_setup.CustomObjects.NewTempDialogFragment;
import xyz.zagermonitoring.nodmcu_setup.Items.FirebaseTimeTemp;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Port1Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Port1Fragment extends Fragment implements NewTempDialogFragment.OnNewTemp {

    private static final String TAG = "Port1Fragment";
    private static final int portIndex = 0;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private DevicePage devicePage;
    private String deviceID;
    private BroadcastReceiver br;
    private BroadcastReceiver br1;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String UID = user.getUid();

    private LineDataSet tempDataSet;
    private LineDataSet powerDataSet;
    private LineChart tempChart;
    private LineChart powerChart;
    private MyXAxisFormatter myXAxisFormatter = new MyXAxisFormatter();
    private int numOfPoints = 360; // number of dataPoints on chart
    private int tempDatGran = 10; //seconds between data
    private int powerDatGran = 10; //seconds between data

    private ScrollView scrollView;
    private SeekBar tempSeekBar;
    private SeekBar powerSeekBar;
    private Switch  switchPortOn;
    private Button setTempBtn;
    private Button lowAlarmBtn;
    private Button highAlarmBtn;

    public Port1Fragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeviceHomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Port1Fragment newInstance(String param1, String param2) {
        Port1Fragment fragment = new Port1Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart(){
        super.onStart();
        DevicePage devicePage = (DevicePage) getActivity();
        deviceID = devicePage.getDeviceID();
        scrollView = getView().findViewById(R.id.ScrollView_P1);
        tempSeekBar = getView().findViewById(R.id.TempSeekBar_port1);
        tempChart = getView().findViewById(R.id.TempChart_Port1);
        powerSeekBar = getView().findViewById(R.id.PowerSeekBar_port1);
        powerChart = getView().findViewById(R.id.PowerChart_Port1);
        switchPortOn = getView().findViewById(R.id.switch_p1_portOn);
        setTempBtn = getView().findViewById(R.id.btn_p1_sp);
        lowAlarmBtn = getView().findViewById(R.id.btn_p1_la);
        highAlarmBtn = getView().findViewById(R.id.btn_p1_ha);
        setUpSeek();
        setUpChart();
        ArrayList<Long> times = devicePage._firebaseData.getTimes();
        ArrayList<Float> temps = devicePage._firebaseData.getTempsP1();
        ArrayList<Float> powers = devicePage._firebaseData.getPowersP1();
        ArrayList<Boolean> portsOn = devicePage._firebaseData.getPortActive();
        List<Number> setTemps = devicePage._firebaseData.getSetTemps();
        List<Number> lowAlarms = devicePage._firebaseData.getLowAlarms();
        List<Number> highAlarms = devicePage._firebaseData.getHighAlarms();
        if (setTemps.size() > 0 && lowAlarms.size() > 0 && highAlarms.size() > 0){
            String stText = setTemps.get(0).toString() + " F";
            String laText = lowAlarms.get(0).toString() + " F";
            String haText = highAlarms.get(0).toString() + " F";
            setTempBtn.setText(stText);
            lowAlarmBtn.setText(laText);
            highAlarmBtn.setText(haText);
        }
        if (portsOn.size() > 0){
            switchPortOn.setChecked(portsOn.get(0));
        }
        if (temps.size() > 0 && times.size() > 0){
            fillTempChart(temps, times);
        }
        if (temps.size() > 0 && powers.size() > 0){
            fillPowerChart(powers, times);
        }

        setUpButtons();

        switchPortOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Map<String, Object> port1OnMap = new HashMap<>();
                port1OnMap.put("port1On", b);
                db.collection("users").document(UID).collection("devices").document(deviceID).set(port1OnMap, SetOptions.merge());
                if (b) Toast.makeText(getContext(), "Port On", Toast.LENGTH_LONG).show();
                if (!b) Toast.makeText(getContext(), "Port Off", Toast.LENGTH_LONG).show();
            }
        });

        br = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if("com.zagermonitoring.FIREBASE_NEWDATA".equals(intent.getAction())){
                    FirebaseTimeTemp ftt = intent.getParcelableExtra("com.zagermonitoring.FIREBASE_NEWTEMP_P1");
                    fillTempChart(ftt.getTemps(), ftt.getTimes());
                    fillPowerChart(ftt.getTemps(), ftt.getTimes());
                }
            }
        };
        br1 = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if("com.zagermonitoring.FIREBASE_NEWSETTINGS".equals(intent.getAction())){
                    Log.d(TAG, "called");

                    List<Number> setTemps = (List<Number>) intent.getSerializableExtra("com.zagermonitoring.FIREABSE_NEW_SET_TEMPS");
                    List<Number> lowAlarms= (List<Number>) intent.getSerializableExtra("com.zagermonitoring.FIREABSE_NEW_LOW_ALARM");
                    List<Number> highAlarms=(List<Number>) intent.getSerializableExtra("com.zagermonitoring.FIREABSE_NEW_HIGH_ALARM");
                    setTempBtn.setText(setTemps.get(portIndex).toString());
                    lowAlarmBtn.setText(lowAlarms.get(portIndex).toString());
                    highAlarmBtn.setText(highAlarms.get(portIndex).toString());
                }
            }
        };
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(br , new IntentFilter("com.zagermonitoring.FIREBASE_NEWDATA"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(br1 , new IntentFilter("com.zagermonitoring.FIREBASE_NEWSETTINGS"));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        devicePage = (DevicePage) getActivity();
        deviceID = devicePage.getDeviceID();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_port1, container, false);
    }

    private void setUpButtons(){
        setTempBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewTempDialogFragment dialog = NewTempDialogFragment.newInstance(1);
                dialog.setTargetFragment(Port1Fragment.this, 1);
                dialog.show(getParentFragmentManager(), "NewTempDialogFragment");
            }
        });
        lowAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewTempDialogFragment dialog = NewTempDialogFragment.newInstance(2);
                dialog.setTargetFragment(Port1Fragment.this, 1);
                dialog.show(getParentFragmentManager(), "NewTempDialogFragment");
            }
        });
        highAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewTempDialogFragment dialog = NewTempDialogFragment.newInstance(3);
                dialog.setTargetFragment(Port1Fragment.this, 1);
                dialog.show(getParentFragmentManager(), "NewTempDialogFragment");
            }
        });
    }

    private void fillTempChart(ArrayList<Float> temps, ArrayList<Long> times){
        Log.d(TAG, "temps size: " + temps.size());
        Log.d(TAG, "times size: " + times.size());
        ArrayList<Long> timeSlice = new ArrayList<>();
        ArrayList<Float> displayTemps = new ArrayList<>();
        long startTime = (Calendar.getInstance().getTimeInMillis() / 1000) - 10; //10 sec delay offset
        //Log.d(TAG, "start time in secs " + startTime);
        for(int i=0; i<numOfPoints; i++){
            timeSlice.add(startTime - i*tempDatGran);
        }
        //Log.d(TAG, "timeSlice " + timeSlice.toString());
        //Log.d(TAG, "timeSlice " + timeSlice.toString());
        //make displayTemps find non congruent temp snap shots (device off) fill with zero

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


        //Log.d(TAG, "displayTemps size" + displayTemps.size());
        //Log.d(TAG, "timeSlice size" + timeSlice.size());
        //Log.d(TAG, "displayTemps " + displayTemps.toString());
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


    private void fillPowerChart(ArrayList<Float> powers, ArrayList<Long> times){
        Log.d(TAG, "powers size: " + powers.size());
        Log.d(TAG, "times size: " + times.size());
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

//    private void appendData(Entry value){
//        if (tempChart.getData() != null && tempChart.getData().getDataSetCount() > 0){
//            set1 = (LineDataSet) tempChart.getData().getDataSetByIndex(0);
//            set1.addEntry(value);
//            set1.notifyDataSetChanged();
//            tempChart.getData().notifyDataChanged();
//            tempChart.notifyDataSetChanged();
//        }
//    }

    private void setTempData(ArrayList<Entry> values){
        List<Integer> colors = new ArrayList<>();

        for (int i = 0; i < values.size(); i++){
            if (values.get(i).getY() == 0f){
                colors.add(Color.GRAY);
            }else if (values.get(i).getY() < -195){
                colors.add(Color.RED);
                values.get(i).setY(0f);
            }else{
                colors.add(Color.BLUE);
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
        Log.d(TAG, tempChart.getData().getDataSets().toString());
        tempChart.invalidate();

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
                colors.add(Color.BLUE);
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
        Log.d(TAG, powerChart.getData().getDataSets().toString());
        powerChart.invalidate();

    }

    private void setUpSeek(){
        tempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

                fillTempChart(devicePage._firebaseData.getTempsP1(), devicePage._firebaseData.getTimes());
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

                fillPowerChart(devicePage._firebaseData.getPowersP1(), devicePage._firebaseData.getTimes());
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
    private void setUpChart(){

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

    @Override
    public void sendNewTemp(int category, Number newTemp) {
        Map<String, Object> fieldObject = new HashMap<>();
        Map<String, List> newData = new HashMap<>();
        List<Number> setTemps = devicePage._firebaseData.getSetTemps();
        List<Number> lowAlarms = devicePage._firebaseData.getLowAlarms();
        List<Number> highAlarms = devicePage._firebaseData.getHighAlarms();
        switch (category){
            case 1:
                setTemps.set(portIndex, newTemp);
                break;
            case 2:
                lowAlarms.set(portIndex, newTemp);
                break;
            case 3:
                highAlarms.set(portIndex, newTemp);
                break;
            default:
                Log.e(TAG, "Not uno dos o tres");
        }
        newData.put("setTemperatures", setTemps);
        newData.put("lowAlarms", lowAlarms);
        newData.put("highAlarms", highAlarms);
        fieldObject.put("setAlarms", newData);
        db.collection("users").document(UID).collection("devices").document(deviceID)
                .set(fieldObject, SetOptions.merge());

    }
}
