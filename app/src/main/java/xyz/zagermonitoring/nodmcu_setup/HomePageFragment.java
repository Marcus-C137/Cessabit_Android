package xyz.zagermonitoring.nodmcu_setup;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.provider.ContactsContract;
import android.security.ConfirmationAlreadyPresentingException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.dynamic.SupportFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


public class HomePageFragment extends Fragment {

    private static final String TAG = "HomePage";
    private int LOCATION_PERMISSION_CODE=99;
    private boolean activeSubscription;
    private String UID;
    private NavController navController;
    private NavHostFragment navHostFragment;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView txtView_noDevices;
    private ListView deviceListView;
    private View view;
    private ArrayAdapter adapter;
    private ArrayList<String> deviceUIDList = new ArrayList<>();
    private ArrayList<String> nickNameList = new ArrayList<>();

    public HomePageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home_page, container, false);
        deviceListView = view.findViewById(R.id.deviceListView);
        txtView_noDevices = view.findViewById(R.id.txtView_noDevices);
        navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        txtView_noDevices.setVisibility(View.INVISIBLE);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, nickNameList);
        navHostFragment = (NavHostFragment) this.getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        deviceListView.setAdapter(adapter);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(activeSubscription){
                    Log.d(TAG, "POSITION " + position);
                    goToDevicePage(deviceUIDList.get(position), nickNameList.get(position));
                }else{
                    navController.navigate(R.id.subscriptionFragment);
                }
            }
        });

        populateDeviceList();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<InstallationTokenResult> task) {
                if (task.getResult() != null){
                    String token = task.getResult().getToken();
                    db = FirebaseFirestore.getInstance();
                    Map<String, Object> tokenMap = new HashMap<>();
                    tokenMap.put("CM_token",token);
                    db.collection("users").document(UID).set(tokenMap, SetOptions.merge());
                }
            }
        });

        // Get new Instance ID token
        DocumentReference docRef = db.collection("users").document(UID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(!document.exists()){
                        Log.d(TAG, "document does not exist");
                    }
                    if(document.exists()) {
                        Log.d(TAG, document.getData().toString());
                        if (document.contains("Subscribed")) {
                            activeSubscription = document.getBoolean("Subscribed");
                            Log.d(TAG, "Subscribed");
                        }
                    }

                }else{
                    Log.d(TAG, "get sub active failed with ", task.getException());
                }
            }
        });
    }



    private void populateDeviceList() {
        db.collection("users").document(UID).collection("devices")
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                 @Override
                 public void onEvent(@Nullable QuerySnapshot snapshots,
                                     @Nullable FirebaseFirestoreException e) {
                     if (e != null) {
                         Log.w(TAG, "device list collection listener error");
                         return;
                     }
                     nickNameList.clear();
                     for (QueryDocumentSnapshot document : snapshots) {
                         deviceUIDList.add(document.getId());
                         Log.d(TAG, "deviceUIDList" + deviceUIDList.toString());
                         String deviceName = "Error";
                         try{
                            deviceName = document.get("nickName").toString();
                         }catch (Exception ex){
                            Log.e(TAG, ex.getMessage());
                         }
                         nickNameList.add(deviceName);
                         adapter.notifyDataSetChanged();
                     }
                     if (nickNameList.size() == 0) {
                         Log.d("Setting to Visible","now");
                         txtView_noDevices.setVisibility(View.VISIBLE);

                     } else {
                         txtView_noDevices.setVisibility(View.INVISIBLE);
                     }
                     Log.d(TAG, nickNameList.toString());
                 }
             });
    }

    private void goToDevicePage(String Device, String NickName){
        Bundle bundle = new Bundle();
        bundle.putString("Device", Device);
        bundle.putString("NickName", NickName);
        Intent intent = new Intent(getActivity(),DevicePage.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
