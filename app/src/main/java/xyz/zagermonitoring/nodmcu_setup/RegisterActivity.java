package xyz.zagermonitoring.nodmcu_setup;


import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;



public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "RegisterActivity";

    EditText emailField;
    EditText passwordField;
    EditText confirmPasswordField;
    String phone;
    Button registerBtn;
    TextView loginHere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        registerBtn= findViewById(R.id.registerBtn);
        loginHere= findViewById(R.id.loginHeretxt);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);

        mAuth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();
                String confirmPassword = passwordField.getText().toString();
                boolean passVerified = verifyPassword(password,confirmPassword);
                if (passVerified){
                    createAccount(email,password);
                }

            }
        });



        loginHere.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToLogin();
            }
        });

    }
    public boolean verifyPassword(String pass, String conf){
        if (pass.length() < 6){
            Toast.makeText(getApplicationContext(), "Password must be 6 characters", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!pass.equals(conf))
        {
            Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void createAccount(String email, final String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = mAuth.getCurrentUser();

                            String uid = user.getUid();
                            String email = user.getEmail();
                            Map<String, Object> info = new HashMap<>();
                            info.put("email", email);
                            info.put("phone", phone);

                            db.collection("users").document(user.getUid())
                                    .set(info, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {

                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Log.w(TAG, "createUserWithEmail: success");
                                            sendVerifyEmail(user);

                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void sendVerifyEmail(FirebaseUser user){

        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FirebaseAuth.getInstance().signOut();
                    goToVerifyDisplayPage();
                } else {
                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private boolean validateForm() {
        boolean valid = true;

        String email = emailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Required.");
            valid = false;
        } else {
            emailField.setError(null);
        }

        String password = passwordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Required.");
            valid = false;
        } else {
            passwordField.setError(null);
        }

        return valid;
    }



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
    }

    public void goToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void goToVerifyDisplayPage() {
        Intent intent = new Intent(this, VerifyEmailActivity.class);
        startActivity(intent);
    }

    public void loginClick(View v){
        goToLogin();
    }


}
