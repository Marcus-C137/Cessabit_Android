package xyz.zagermonitoring.nodmcu_setup;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
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

    public Port1Fragment() {
        // Required empty public constructor
    }

    public static Port3Fragment newInstance(String param1, String param2) {
        Port3Fragment fragment = new Port3Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart(){
        super.onStart();
        pfc = new PortFragmentConstructor(portIndex, this, getActivity());
        scrollView = getView().findViewById(R.id.ScrollView_p1);
        tempSeekBar = getView().findViewById(R.id.sb_temp_p1);
        tempChart = getView().findViewById(R.id.bc_currentTemps_p1);
        powerSeekBar = getView().findViewById(R.id.sb_power_p1);
        powerChart = getView().findViewById(R.id.PowerChart_p1);
        switchPortOn = getView().findViewById(R.id.sw_portOn_p1);
        setTempBtn = getView().findViewById(R.id.btn_sp_p1);
        lowAlarmBtn = getView().findViewById(R.id.btn_la_p1);
        highAlarmBtn = getView().findViewById(R.id.btn_ha_p1);
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        return inflater.inflate(R.layout.fragment_port1, container, false);
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
