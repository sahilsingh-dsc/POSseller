package com.datsea.posseller;

import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    DatabaseReference posRef;
    SharedPreferences master;
    TextView txtName, txtUpi;
    LinearLayout lvAmount, lvTapCard, lvRegisterUser;
    EditText txtCName, txtCUpi;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtName = findViewById(R.id.txtName);
        txtUpi = findViewById(R.id.txtUpi);
        lvAmount = findViewById(R.id.lvAmount);
        lvTapCard = findViewById(R.id.lvTapCard);
        lvRegisterUser = findViewById(R.id.lvRegisterUser);
        txtCName = findViewById(R.id.txtCName);
        txtCUpi = findViewById(R.id.txtCUpi);
        btnRegister = findViewById(R.id.btnRegister);
        master = this.getSharedPreferences("master", 0);

        lvTapCard.setVisibility(View.VISIBLE);

        removeUserId();

        posRef = FirebaseDatabase.getInstance().getReference();
        posRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("user-id")){
                    final String user_id = Objects.requireNonNull(dataSnapshot.child("user-id").getValue()).toString();
                    posRef = FirebaseDatabase.getInstance().getReference("user_Data");
                    posRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(user_id)){
                                getUserData(user_id);
                            }else {
                                lvTapCard.setVisibility(View.GONE);
                                lvAmount.setVisibility(View.GONE);
                                lvRegisterUser.setVisibility(View.VISIBLE);
                                btnRegister.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String name = txtCName.getText().toString();
                                        String upi = txtCUpi.getText().toString();
                                        if (TextUtils.isEmpty(name)){
                                            Toast.makeText(MainActivity.this, "Please enter name...", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        if (TextUtils.isEmpty(upi)){
                                            Toast.makeText(MainActivity.this, "Please enter upi id...", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        registerUser(user_id, name, upi);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserData(String user_id){
        posRef = FirebaseDatabase.getInstance().getReference("user_Data");
        posRef.child(user_id).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SharedPreferences.Editor editor = master.edit();
                editor.putString("name", Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString());
                editor.putString("upi", Objects.requireNonNull(dataSnapshot.child("upi").getValue()).toString());
                editor.apply();
                setUserData();
                removeUserId();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeUserId(){
        posRef = FirebaseDatabase.getInstance().getReference();
        posRef.child("user-id").removeValue();
    }

    private void setUserData(){
        lvTapCard.setVisibility(View.GONE);
        lvAmount.setVisibility(View.VISIBLE);
        txtName.setText(master.getString("name", "--"));
        txtUpi.setText(master.getString("upi", "--"));
    }

    private void registerUser(String user_id, String name, String upi){
        posRef = FirebaseDatabase.getInstance().getReference("user_Data");
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("name", name);
        hashMap.put("upi", upi);
        posRef.child(user_id).setValue(hashMap);
        Toast.makeText(this, "User Added!", Toast.LENGTH_SHORT).show();
        lvAmount.setVisibility(View.GONE);
        lvRegisterUser.setVisibility(View.GONE);
        lvTapCard.setVisibility(View.VISIBLE);

    }

}
