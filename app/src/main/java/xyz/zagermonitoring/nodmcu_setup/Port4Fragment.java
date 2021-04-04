package xyz.zagermonitoring.nodmcu_setup;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Port4Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Port4Fragment extends Fragment implements NewTempDialogFragment.OnNewTemp{
    private static final String TAG = "Port4Fragment";
    private static final int portIndex = 3;


    private DevicePage devicePage;
    private String deviceID;
    private BroadcastReceiver br;
    private BroadcastReceiver br1;
    private BroadcastReceiver br2;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String UID = user.getUid();

    private LineDataSet tempDataSet;
    private LineDataSet powerDataSet;
    private LineChart tempChart;
    private LineChart powerChart;
    private MyXAxisFormatter myXAxisFormatter = new MyXAxisFormatter();


    private ScrollView scrollView;
    private SeekBar tempSeekBar;
    private SeekBar powerSeekBar;
    private Switch switchPortOn;
    private Button setTempBtn;
    private Button lowAlarmBtn;
    private Button highAlarmBtn;

    private PortFragmentConstructor pfc;

    public Port4Fragment() {
        // Required empty public constructor

    }

    public static Port4Fragment newInstance(String param1, String param2) {
        Port4Fragment fragment = new Port4Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "View Created");
        pfc = new PortFragmentConstructor(portIndex, this, getActivity());
        scrollView = getView().findViewById(R.id.ScrollView_p4);
        tempSeekBar = getView().findViewById(R.id.sb_temp_p4);
        tempChart = getView().findViewById(R.id.bc_currentTemps_p4);
        powerSeekBar = getView().findViewById(R.id.sb_power_p4);
        powerChart = getView().findViewById(R.id.PowerChart_p4);
        switchPortOn = getView().findViewById(R.id.sw_portOn_p4);
        setTempBtn = getView().findViewById(R.id.btn_sp_p4);
        lowAlarmBtn = getView().findViewById(R.id.btn_la_p4);
        highAlarmBtn = getView().findViewById(R.id.btn_ha_p4);
        pfc.setScrollView(scrollView);
        pfc.setAxisFormatter(myXAxisFormatter);
        pfc.setDataSets(tempDataSet, powerDataSet);
        pfc.setUpSeek(tempSeekBar, powerSeekBar);
        pfc.setUpChart(tempChart, powerChart);
        pfc.setUpButtons(setTempBtn, lowAlarmBtn, highAlarmBtn, this);
        pfc.setUpSwitch(switchPortOn, getContext());
        pfc.setUpBroadcast(br, br1, br2, getContext());
        pfc.initializeCharts();
    }

    @Override
    public void onStart(){
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
        Log.d(TAG, "Created");
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getActivity(),HomePage.class);
                startActivity(intent);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        devicePage = (DevicePage) getActivity();
        deviceID = devicePage.getDeviceID();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_port4, container, false);
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
