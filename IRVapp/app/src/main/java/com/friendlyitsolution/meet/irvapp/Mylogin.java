package com.friendlyitsolution.meet.irvapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Mylogin extends AppCompatActivity {
    Button btn;
    MaterialEditText etphone,etname;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String mVerificationId;
    private static final String TAG = "PhoneAuthActivity";
    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mylogin);
        pd=new ProgressDialog(Mylogin.this);
        pd.setCancelable(false);
        pd.setMessage("please wait");



        btn=(Button)findViewById(R.id.btnLogin);
        etphone=(MaterialEditText)findViewById(R.id.etphone);
        etname=(MaterialEditText)findViewById(R.id.etUser);
        if(!MyApp.mynumber.equals("")) {
            if(!MyApp.userdata.containsKey("licence"))
            {

                Intent i=new Intent(getApplicationContext(),FormActivity.class);
                startActivity(i);
                finish();
            }
            else
            {
                Intent im=new Intent(getApplicationContext(),Myhome.class);
                startActivity(im);
                finish();
            }
        }


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!etname.getText().toString().equals("")&&!etphone.getText().toString().equals(""))
                {
                    pd.show();
                    startPhoneNumberVerification(etphone.getText().toString());
                }
                if(etphone.equals(""))
                {
                    etphone.setError("please enter mobile no");
                }
                if(etname.equals(""))
                {
                    etname.setError("please enter name");
                }
            }
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                pd.dismiss();
                Toast.makeText(getApplicationContext(),"Varified",Toast.LENGTH_SHORT).show();

                signup();
                // signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    pd.dismiss();
                    etphone.setError("Invalid phone number");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    pd.dismiss();
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;
                pd.setMessage("code sucessfully send");
            }
        };



    }

    void signup()
    {
        pd.setMessage("Checking Information...");
        pd.show();
       final DatabaseReference reff=MyApp.ref.child("users").child(etphone.getText().toString());
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                reff.removeEventListener(this);

                if(dataSnapshot.getValue()==null)
                {
                    pd.setMessage("Registring user...");
                    Map<String,String> data=new HashMap<>();
                    data.put("contact",etphone.getText().toString());
                    data.put("name",etname.getText().toString());
                    data.put("status","pending");
                    MyApp.ref.child("users").child(etphone.getText().toString()).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pd.dismiss();

                            SharedPreferences.Editor edit=MyApp.sharedpreferences.edit();
                            edit.putString("mynumber",etphone.getText().toString());
                            edit.putString("myname",etname.getText().toString());
                            edit.commit();

                            MyApp.myid=etphone.getText().toString();
                            MyApp.mynumber=etphone.getText().toString();
                            MyApp.myname=etname.getText().toString();
                            Intent i=new Intent(Mylogin.this,FormActivity.class);
                            startActivity(i);
                            finish();
                        }
                    });


                }
                else
                {
                    pd.dismiss();
                    MyApp.userdata=(Map<String, Object>) dataSnapshot.getValue();

                    SharedPreferences.Editor edit=MyApp.sharedpreferences.edit();
                    edit.putString("mynumber",etphone.getText().toString());
                    edit.putString("myname",etname.getText().toString());
                    edit.commit();
                    MyApp.mynumber=etphone.getText().toString();
                    MyApp.myname=etname.getText().toString();

                    if(!MyApp.userdata.containsKey("licence"))
                    {

                        Intent i=new Intent(getApplicationContext(),FormActivity.class);
                        startActivity(i);
                        finish();
                    }
                    else
                    {
                        Intent im=new Intent(getApplicationContext(),Myhome.class);
                        startActivity(im);
                        finish();
                    }


                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91"+phoneNumber,        // Phone number to verify
                120,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

}
