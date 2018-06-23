package com.example.rajat.stithi;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.rajat.stithi.login.googleApiClient;

public class MainActivity extends AppCompatActivity {

    CircleImageView imageView;
    TextView name,age,points,add_points,gender;
    ListView listView;
    List<model> names_list;
    ListViewAdapter adapter;
    DatabaseReference databaseReference,myref2,imageurlref,myref3;
    String userid,sage,point,sgender;
    FirebaseAuth mAuth;
    ProgressDialog  progressDialog;
    Firebase firebase;

    long count,stime=0,total=100;

    Button add_picture;
    StaggeredGridLayoutManager linearLayoutManager;
    StorageReference storageReference;
    public static FirebaseRecyclerAdapter<model,ShowDataViewHolder> mFirebaseAdapterw;
    Uri FilePathUri;
    RecyclerView recyclerView;
    RelativeLayout innerrv;
    CardView point_cv;

    EditText inputpoints;
    Button submit ,cancel ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);

        recyclerView = findViewById(R.id.rv);
        linearLayoutManager = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        progressDialog = new ProgressDialog(MainActivity.this);

        innerrv = findViewById(R.id.innrerv);
        add_picture = findViewById(R.id.add_picture);
        add_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addpicture();
            }
        });
        imageView =  findViewById(R.id.profile);
        name =  findViewById(R.id.name);
        points = findViewById(R.id.points);
        age =  findViewById(R.id.age);
        point_cv = findViewById(R.id.point_cv);
        listView = findViewById(R.id.listView);
        listView.setVisibility(View.GONE);
        add_points = findViewById(R.id.add_points);
        gender = findViewById(R.id.gender);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        name.setText(user.getDisplayName());

        Glide.with(this).load(user.getPhotoUrl()).into(imageView);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userid = mAuth.getCurrentUser().getUid();




        myref3 = FirebaseDatabase.getInstance().getReference("Yourself").child("Stithi").child("Users");

        progressDialog.setMessage("loading profile...");
        progressDialog.show();

        myref3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
             sage = dataSnapshot.child(userid).child("Age").getValue(String.class);
             point= dataSnapshot.child(userid).child("points").getValue(String.class);
             sgender= dataSnapshot.child(userid).child("Gender").getValue(String.class);
                 gender.setText(sgender);
                 age.setText(sage);
                 points.setText(point);

                 if(sage==null){
                     display(dataSnapshot);
                 }

                 list();

                 names_list = new ArrayList<>();
                 for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                     if(dataSnapshot1!=null) {

                         model model1 = new model(dataSnapshot1.child("Name").getValue().toString());
                         //names_list.add(String.valueOf(dataSnapshot1.child("Name").getValue()));
                         names_list.add(model1);
                     }
                 }
                 //to print array list
                 Log.d("List", Arrays.toString(names_list.toArray()));

                 //pass results to listViewAdapter class
                 adapter = new ListViewAdapter(MainActivity.this, names_list);
                    adapter.areAllItemsEnabled();
                 //bind the adapter to the listview
                 listView.setAdapter(adapter);

                 progressDialog.dismiss();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



        add_points.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.input_points);
                inputpoints = dialog.findViewById(R.id.ipoints);
                submit = dialog.findViewById(R.id.isubmit);
                cancel = dialog.findViewById(R.id.icancel);

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String input = inputpoints.getText().toString();
                        if (input != null && point != null) {
                            int current_input = Integer.parseInt(input);
                            int previous = Integer.parseInt(point);

                            firebase = new Firebase("https://yourself-bro.firebaseio.com/").child("Yourself").child("Stithi").child("Users").child(userid);
                            final Firebase point_ref = firebase.child("points");

                            point_ref.setValue(String.valueOf(current_input + previous));
                            points.setText(String.valueOf(current_input + previous));
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

    private void display(DataSnapshot dataSnapshot) {

        sage = dataSnapshot.child(userid).child("Age").getValue(String.class);
        point= dataSnapshot.child(userid).child("points").getValue(String.class);
        sgender= dataSnapshot.child(userid).child("Gender").getValue(String.class);
        gender.setText(sgender);
        age.setText(sage);
        points.setText(point);
    }


    private void addpicture() {

        databaseReference = FirebaseDatabase.getInstance().getReference("Yourself").child("Stithi").child("Users").child(userid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                count =dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(count>5){
            Toast.makeText(this, "You can add maximum of 5 pictures", Toast.LENGTH_SHORT).show();
        }else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Please select image"), 7);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        progressDialog.setMessage("Uploading image...");
        progressDialog.show();


        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 7 && resultCode == RESULT_OK && data != null && data.getData()!=null){
            FilePathUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),FilePathUri);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        if(FilePathUri!=null){
            StorageReference storageReference1 = storageReference.child(userid+"/"+System.currentTimeMillis()+"."+getfileextension(FilePathUri));
            storageReference1.putFile(FilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                         progressDialog.dismiss();

                            Toast.makeText(MainActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();

                            imageurlref = FirebaseDatabase.getInstance().getReference("Yourself").child("Stithi").child("Users").child(userid);

                            imageurlref.child("Image_urls").child(String.valueOf(System.currentTimeMillis())).child("url").setValue(taskSnapshot.getDownloadUrl().toString());
                        }
                    });
        }

                list();
    }

    private String getfileextension(Uri filePathUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(filePathUri));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);


        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                listView.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                listView.setVisibility(View.VISIBLE);
                innerrv.setVisibility(View.GONE);
                if (TextUtils.isEmpty(s)){
                    adapter.filter("");
                    listView.clearTextFilter();
                }
                else {
                    adapter.filter(s);
                }
                return true;

            }


        });

        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
        }
        return super.onOptionsItemSelected(item);
    }



    private void logout() {
        googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {

                FirebaseAuth.getInstance().signOut();
                if(googleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            Toast.makeText(getApplicationContext(),"Logged out!",Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(MainActivity.this,login.class));
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.d("Bie Bie", "Google API Client Connection Suspended");
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

        private ImageView image,delete;
        private CardView cardView;


        public ShowDataViewHolder(final View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            delete = itemView.findViewById(R.id.delete);

        }

    }
    public void list() {

        FirebaseAuth mauth = FirebaseAuth.getInstance();
        String userod = mauth.getCurrentUser().getUid();

        myref2 = FirebaseDatabase.getInstance().getReference("Yourself").child("Stithi").child("Users").child(userod).child("Image_urls");


        setmFirebaseAdapter1(new FirebaseRecyclerAdapter<model, ShowDataViewHolder>(
                model.class, R.layout.rv_item, ShowDataViewHolder.class, myref2.orderByKey()) {
            public void populateViewHolder(final ShowDataViewHolder viewHolder, final model models, final int position) {

                    Glide.with(MainActivity.this).load(models.getUrl()).into(viewHolder.image);



                viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("You Sure ?").setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int selectedItems = position;

                                        if(getmFirebaseAdapter1().getItemCount() >2) {
                                            getmFirebaseAdapter1().getRef(selectedItems).removeValue();
                                            getmFirebaseAdapter1().notifyItemRemoved(selectedItems);
                                            recyclerView.invalidate();
                                            notifyItemRangeChanged(position, getItemCount());
                                        }
                                        else {
                                            getmFirebaseAdapter1().getRef(selectedItems).removeValue();
                                            getmFirebaseAdapter1().notifyItemRemoved(selectedItems);
                                            recyclerView.invalidate();
                                            list();

                                        }
                                        dialog.dismiss();

                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        notifyDataSetChanged();
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });


            }
        });


        recyclerView.setAdapter(getmFirebaseAdapter1());


        mFirebaseAdapterw.notifyDataSetChanged();
    }


}
