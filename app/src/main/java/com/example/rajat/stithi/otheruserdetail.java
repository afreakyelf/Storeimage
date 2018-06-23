package com.example.rajat.stithi;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.channels.ReadableByteChannel;

public class otheruserdetail extends AppCompatActivity {

    ImageView profile;
    TextView name,age,gender;
    RecyclerView rv;
    Button addfriend,sendpoints;
    private String userid;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    public static FirebaseRecyclerAdapter<model,ShowDataViewHolder> mFirebaseAdapterw;
    DatabaseReference databaseReference;
    String hint,otherusername,sgender;
    EditText inputpoints;
    Button submit,cancel;
    int previous_points;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otheruserdetail);
        Firebase.setAndroidContext(this);
        progressDialog = new ProgressDialog(otheruserdetail.this);


     //   addfriend = findViewById(R.id.addfriend);
        sendpoints = findViewById(R.id.sendpoints);

        Intent intent = getIntent();
        userid= intent.getStringExtra("userid");
       // Toast.makeText(otheruserdetail.this, userid, Toast.LENGTH_SHORT).show();

        profile = findViewById(R.id.profile1);
        name = findViewById(R.id.name1);
        age = findViewById(R.id.age1);
        rv = findViewById(R.id.rv1);
        gender= findViewById(R.id.gender1);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL);
        rv.setLayoutManager(staggeredGridLayoutManager);
        rv.setNestedScrollingEnabled(false);


        progressDialog.setMessage("loading profile...");
        progressDialog.show();

        databaseReference = FirebaseDatabase.getInstance().getReference("Yourself").child("Stithi").child("Users").child(userid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
     Glide.with(otheruserdetail.this).load(dataSnapshot.child("Profile_pic").getValue(String.class).toString()).into(profile);
                    name.setText(dataSnapshot.child("Name").getValue(String.class));
                    age.setText(dataSnapshot.child("Age").getValue(String.class));
                    otherusername = dataSnapshot.child("Name").getValue(String.class);
                    gender.setText(dataSnapshot.child("Gender").getValue(String.class));
                    progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final String useridd = FirebaseAuth.getInstance().getUid();
        final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Yourself").child("Stithi").child("Users");
     /*   addfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference1.child(useridd).child("friends").child(userid).child("Useridd").setValue(userid);
            }
        });
*/
        list();

        sendpoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(otheruserdetail.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.input_points);
                inputpoints = dialog.findViewById(R.id.ipoints);
                submit = dialog.findViewById(R.id.isubmit);
                cancel = dialog.findViewById(R.id.icancel);



                databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String p = dataSnapshot.child(userid).child("points").getValue(String.class);
                         previous_points = Integer.parseInt(p);
                        hint= dataSnapshot.child(useridd).child("points").getValue(String.class);
                        inputpoints.setHint("You have "+hint+" points");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String points = inputpoints.getText().toString();
                        int b = Integer.parseInt(points);
                        if(b<Integer.parseInt(hint)){
                        databaseReference1.child(userid).child("points").setValue(String.valueOf(previous_points+b));
                            int remainigpoint = Integer.parseInt(hint)-b;
                            databaseReference1.child(useridd).child("points").setValue(String.valueOf(String.valueOf(remainigpoint)));

                            Toast.makeText(otheruserdetail.this, "You transferred points to "+otherusername, Toast.LENGTH_SHORT).show();

                        }
                        else {
                            Toast.makeText(otheruserdetail.this, "You dont have enough point. PLease add first", Toast.LENGTH_SHORT).show();
                        }

                    dialog.dismiss();
                    }


                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


                dialog.show();
            }
        });

    }


    public static FirebaseRecyclerAdapter<model,ShowDataViewHolder> getmFirebaseAdapter1() {
        return mFirebaseAdapterw;
    }

    public void setmFirebaseAdapter1(FirebaseRecyclerAdapter<model, ShowDataViewHolder> mFirebaseAdapter) {
        this.mFirebaseAdapterw = mFirebaseAdapter;
    }

    public static class ShowDataViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;


        public ShowDataViewHolder(final View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);


        }

    }
    public void list() {

        databaseReference = FirebaseDatabase.getInstance().getReference("Yourself").child("Stithi").child("Users").child(userid).child("Image_urls");


        setmFirebaseAdapter1(new FirebaseRecyclerAdapter<model,ShowDataViewHolder>(
                model.class, R.layout.rv_itemforotheruser, ShowDataViewHolder.class, databaseReference.orderByValue()) {
            public void populateViewHolder(final ShowDataViewHolder viewHolder, final model models, final int position) {

                Glide.with(otheruserdetail.this).load(models.getUrl()).into(viewHolder.image);

            }
        });


        rv.setAdapter(getmFirebaseAdapter1());


        mFirebaseAdapterw.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this,MainActivity.class));
    }
}
