package com.example.rajat.stithi;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListViewAdapter extends BaseAdapter{

    //variables
    Context mContext;
    LayoutInflater inflater;
    List<model> modellist;
    ArrayList<model> arrayList;
    DatabaseReference myref2;
    FirebaseAuth firebaseAuth;
    String userid;

    //constructor
    public ListViewAdapter(Context context, List<model> modellist) {
        mContext = context;
        this.modellist = modellist;
        inflater = LayoutInflater.from(mContext);
        this.arrayList = new ArrayList<model>();
        this.arrayList.addAll(modellist);
    }

    public class ViewHolder{
        TextView name;
    }

    @Override
    public int getCount() {
        return modellist.size();
    }

    @Override
    public Object getItem(int i) {
        return modellist.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int postition, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view==null){
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.row, null);

            //locate the views in row.xml
            holder.name = view.findViewById(R.id.name);

            view.setTag(holder);

        }
        else {
            holder = (ViewHolder)view.getTag();
        }
        //set the results into textviews
        holder.name.setText(modellist.get(postition).getName());


        firebaseAuth  =FirebaseAuth.getInstance();
        userid = firebaseAuth.getCurrentUser().getUid();

        myref2 = FirebaseDatabase.getInstance().getReference("Yourself").child("Stithi").child("totalusers");

        //listview item clicks
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               final String item = modellist.get(postition).getName().toString();

                myref2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot dsp:dataSnapshot.getChildren()){
                            for(DataSnapshot dsp2:dsp.getChildren()){
                                if(item.equals(dsp2.getKey())) {

                                    String useridfinal =  dsp.child(item).child("Userid").getValue().toString();
                                    Intent intent = new Intent(mContext, otheruserdetail.class);
                                    intent.putExtra("userid", useridfinal);
                                    mContext.startActivity(intent);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }
        });


        return view;
    }

    //filter
    public void filter(String charText){
        charText = charText.toLowerCase(Locale.getDefault());
        modellist.clear();
        if (charText.length()==0){
            modellist.addAll(arrayList);
        }
        else {
            for (model model : arrayList){
                if (model.getName().toLowerCase(Locale.getDefault())
                        .contains(charText)){
                    modellist.add(model);
                }
            }
        }
        notifyDataSetChanged();
    }



}
