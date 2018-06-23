package com.example.rajat.stithi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends Activity {

    SignInButton singin;
    GoogleSignInOptions googleSignInOptions;
    FirebaseAuth mAuth;
    public static GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        singin = findViewById(R.id.signin);
        progressDialog = new ProgressDialog(login.this);

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions)
                .build();
        Log.d("Api client","Api client loaded in onStart");

        googleApiClient.connect();
        mAuth = FirebaseAuth.getInstance();

        singin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singin();
            }
        });
    }

    private void singin() {
        progressDialog.setMessage("Signing In...");
        progressDialog.show();
        startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient),REQ_CODE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()!=null){
            finish();
            startActivity(new Intent(this,MainActivity.class));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE){
            progressDialog.show();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleresult(result);
        }
    }

    private void handleresult(GoogleSignInResult result) {
        if(result.isSuccess()){
            try{
                final GoogleSignInAccount account = result.getSignInAccount();
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    final FirebaseUser user = mAuth.getCurrentUser();
                                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Yourself").child("Stithi").child("Users");
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                           // for(DataSnapshot dsp: dataSnapshot.getChildren()){
                                                if(dataSnapshot.hasChild(user.getUid())){
                                                    startActivity(new Intent(login.this,MainActivity.class));
                                                }
                                                else {
                                                    startActivity(new Intent(login.this,extrainfo.class));
                                                }
                                         //   }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    }
                                progressDialog.dismiss();
                            }
                        });
            }catch (Exception e){
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }else{
            progressDialog.dismiss();
            Toast.makeText(login.this, "No Internet Connection! Please Check your Network.", Toast.LENGTH_SHORT).show();

        }
    }
}