package xyz.zagermonitoring.nodmcu_setup;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

    }

    public void updateUI(FirebaseUser currentUser){
        if (currentUser == null) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        }
        else if (!currentUser.isEmailVerified()){
            Intent intent = new Intent(this, VerifyEmailActivity.class);
            startActivity(intent);
        }
        else {

            Intent intent = new Intent(this, HomePage.class);
            startActivity(intent);
        }
    }

}