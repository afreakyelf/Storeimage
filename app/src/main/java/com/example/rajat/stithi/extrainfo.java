package com.example.rajat.stithi;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Map;

public class extrainfo extends AppCompatActivity{

    RadioButton male,female,findbutton;
    RadioGroup rg;
    ImageButton datepick;
    Button finish;
    TextView showdate,name,goback;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference,myref3,myref2;
    Firebase firebase;
    String age,age2,userid,namea;
    int d,m,y,dd,mm,yyyy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extrainfo);
        Firebase.setAndroidContext(this);

        male = findViewById(R.id.male);
        female= findViewById(R.id.female);
        datepick = findViewById(R.id.pickdate);
        showdate = findViewById(R.id.date);
        name = findViewById(R.id.name);
        finish = findViewById(R.id.finish);
        rg = findViewById(R.id.rg);
        goback = findViewById(R.id.goback);




        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        userid = firebaseAuth.getCurrentUser().getUid();
        namea =firebaseAuth.getCurrentUser().getDisplayName();
        name.setText(namea);

        myref3 = FirebaseDatabase.getInstance().getReference("Yourself").child("Stithi").child("totalusers").child(String.valueOf(System.currentTimeMillis()));
        myref3.child(namea).child("Userid").setValue(userid);

        myref2= FirebaseDatabase.getInstance().getReference("Yourself").child("Stithi").child("Users");


        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(extrainfo.this,login.class));
                finish();
            }
        });



        final Calendar c  = Calendar.getInstance();

        datepick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d = c.get(Calendar.DATE);
                m = c.get(Calendar.MONTH);
                y = c.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(extrainfo.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int date) {
                        int finalm = month+1;
                        showdate.setText(date+"-"+finalm+"-"+year);
                        calculateage(date,finalm,year);

                    }
                },y,m,d);
                datePickerDialog.show();
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectid = rg.getCheckedRadioButtonId();
                findbutton = findViewById(selectid);
                String gender = findbutton.getText().toString();
                String dated = showdate.getText().toString();
                if(TextUtils.isEmpty(dated)){
                    Toast.makeText(extrainfo.this, "Please fill all the details first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebase = new Firebase("https://yourself-bro.firebaseio.com/").child("Yourself").child("Stithi").child("Users").child(userid);
                final Firebase name = firebase.child("Name");
                final Firebase age_ref = firebase.child("Age");
                final Firebase gender_ref = firebase.child("Gender");
                final Firebase profile_pic = firebase.child("Profile_pic");
                final Firebase points = firebase.child("points");
                points.setValue("50");
                name.setValue(namea);
                age_ref.setValue(age);
                gender_ref.setValue(gender);
                profile_pic.setValue(firebaseAuth.getCurrentUser().getPhotoUrl().toString());
                startActivity(new Intent(extrainfo.this, MainActivity.class));

            }
        });

    }

    private String calculateage(int date, int finalm, int year) {

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        dob.set(year,finalm,date);
        int agea = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if(today.get(Calendar.DAY_OF_YEAR)<dob.get(Calendar.DAY_OF_YEAR)){
            agea--;
        }
        Integer ageint = new Integer(agea);
        age = ageint.toString();
        return age;

    }
}
