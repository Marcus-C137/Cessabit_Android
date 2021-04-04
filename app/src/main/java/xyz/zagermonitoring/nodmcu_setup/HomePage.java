package xyz.zagermonitoring.nodmcu_setup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomePage extends AppCompatActivity implements PurchasesUpdatedListener, SubscriptionFragment.OnFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "HomePageActivity";
    static final String SKU_MONTHLY_SUBSCRIPTION="subscription_test";
    private FragmentManager supportFragmentManager;
    private NavHostFragment navHostFragment;
    public DrawerLayout drawerLayout;
    public NavigationView navigationView;
    public NavController navController;
    public ImageButton navOpen;
    private FirebaseAuth mAuth;
    private String UID;
    private Boolean activeSubscription = false;
    private SubscriptionFragment subscriptionFragment;
    int billingResponseCode;
    BillingClient billingClient;
    SkuDetailsParams.Builder params;
    List<SkuDetails>SkuDetailsList;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String base64EncodedPublicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn7z1SCLXRKTgCn9U" +
            "d99wE0N8D73LACJjUl+/2z58bfwvrqveMvdmfO7k5IOp2BghlywMAE8zDpGdbISreVyI8CRcuE/Eyn7ojz" +
            "6YRlUVzgINP+aAwtYdeRGcxbT0oeTQzQuF6gFO6DuQM2TG14/Eg6NyqQWB+WcV+y4Gw9K+FIQtmYQTTBz7" +
            "gtFM+2JJb6aU+FCvoQVWmU8qv68mnrSiI2CHNDUePvBlTxHUCE/VG6S6uatMVyB6CRChBjYGjI4C2ZyYds" +
            "Ljd0hDqoUL1WN0KPAK4Q8cnq6Y0uMNhOfvax6A/t+2wsy1ov/RK5OSwqu/nbRjpHq5EP5MqFxHGXgxBwIDAQAB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        subscriptionFragment = new SubscriptionFragment();
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navOpen = findViewById(R.id.navOpen);
        mAuth=FirebaseAuth.getInstance();
        UID = mAuth.getCurrentUser().getUid();
        navHostFragment = (NavHostFragment) this.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

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
                        Log.d(TAG, "Subscribed found");
                        if (!activeSubscription) {
                            Log.d(TAG, "Setting up Setup Client");
                            setupBillingClient();
                        }
                    }
                }

            }else{
                Log.d(TAG, "get sub active failed with ", task.getException());
            }
            }

        });
    }

    public int getResponseCode(){
        return billingResponseCode;
    }

    public void setupBillingClient(){
        billingClient = BillingClient.newBuilder(this)
                    .enablePendingPurchases()
                    .setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                Log.d("Billing Setup Finished", "!");
                Log.d("Billing Response Code", " " + billingResult.getResponseCode());
                billingResponseCode = billingResult.getResponseCode();
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d("Billing respsonse ok", "mkay");
                    List<String> skuList=new ArrayList<>();
                    skuList.add(SKU_MONTHLY_SUBSCRIPTION);
                    params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList)
                            .setType(BillingClient.SkuType.SUBS);
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                                    SkuDetailsList = skuDetailsList;
                                    Log.d("!!!!!!!!!11", "+ " + SkuDetailsList);
                                }
                            });
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawer_menu,menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(Navigation.findNavController(this,R.id.nav_host_fragment), drawerLayout);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_Home:{
                navController.navigate(R.id.homePageFragment);
                break;
            }

            case R.id.nav_Subscriptions:{
                if (!activeSubscription){
                    navController.navigate(R.id.subscriptionFragment);

                }else{
                    navController.navigate(R.id.subscribedFragment);
                }
                break;
            }

            case R.id.nav_Alerts:{
                navController.navigate(R.id.alertFragment);
                break;
            }
            case R.id.nav_log_out:{
                mAuth.signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
        }
        menuItem.setChecked(true);
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void buyButtonClicked() {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(SkuDetailsList.get(0))
                .build();
        BillingResult responseCode = billingClient.launchBillingFlow(HomePage.this, flowParams);
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {

            Date currentTime = Calendar.getInstance().getTime();
            Calendar c = Calendar.getInstance();
            c.setTime(currentTime);
            c.add(Calendar.YEAR,1);
            Date expirationTime = c.getTime();
            String Time = expirationTime.toString();

            Map<String, Object> paidInfo = new HashMap<>();
            paidInfo.put("Subscribed", true);
            paidInfo.put("Expiration_Time", Time);
            db.collection("users").document(UID).set(paidInfo, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Paid DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
            navController.navigate(R.id.subscribedFragment);

        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                Toast.makeText(this, "Subscription not added", Toast.LENGTH_LONG).show();
        } else {
                Toast.makeText(this, "Subscription not added", Toast.LENGTH_LONG).show();
        }
    }
}
