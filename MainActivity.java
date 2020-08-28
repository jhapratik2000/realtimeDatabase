package com.example.realtimedatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.Attributes;

public class MainActivity extends AppCompatActivity {

    private EditText regUserName,regPassword,regEmailId,loginEmailId,loginPassword;
    private Button registerButton,loginButton;
    private Student student;
    private ImageView regImg,loginImg;
    private TextView name,email,password;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference ref;
    private FirebaseStorage mStorage;
    StorageReference sRef;
    Uri imagePath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imagePath=data.getData();
            regImg.setImageURI(imagePath);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();

        student= new Student();
        mAuth=FirebaseAuth.getInstance();
        mStorage=FirebaseStorage.getInstance();
        database=FirebaseDatabase.getInstance();
        sRef=mStorage.getReference();
        //ref=database.getReference().child("Student");
        //sRef=mStorage.getReference().child("Images");

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName=regUserName.getText().toString().trim();
                String userMail=regEmailId.getText().toString().trim();
                String userPwd=regPassword.getText().toString().trim();
                if(userName.isEmpty()||userMail.isEmpty()||userPwd.isEmpty())
                    Toast.makeText(MainActivity.this,"Enter all the details!",Toast.LENGTH_SHORT).show();
                else {
                    if(imagePath!=null){
                student.setName(userName);
                student.setEmailId(userMail);
                student.setPassword(userPwd);

                mAuth.createUserWithEmailAndPassword(regEmailId.getText().toString().trim(),regPassword.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            ref=database.getReference(mAuth.getUid());
                            //ref.child(mAuth.getUid()).setValue(student);
                            ref.setValue(student).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(MainActivity.this,"Details uploaded",Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(MainActivity.this,"Details upload failed!",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            StorageReference imageReference = sRef.child(mAuth.getUid()).child("Image");
                            UploadTask uploadTask=imageReference.putFile(imagePath);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this,"Image Upload failed!",Toast.LENGTH_SHORT).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(MainActivity.this,"Image Upload successful",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else{
                            Toast.makeText(MainActivity.this,"Registration failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                    }
                    else
                        Toast.makeText(MainActivity.this,"Select image!",Toast.LENGTH_SHORT).show();
                }

                //ref.child(student.getName()).setValue(student);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId=loginEmailId.getText().toString().trim();
                String userPassword=loginPassword.getText().toString().trim();
                if (userId.isEmpty()||userPassword.isEmpty())
                    Toast.makeText(MainActivity.this,"Enter details!",Toast.LENGTH_SHORT).show();
                else {
                mAuth.signInWithEmailAndPassword(userId,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            ref=database.getReference(mAuth.getUid());
                            //ref.child(mAuth.getUid());
                            ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    student=dataSnapshot.getValue(Student.class);
                                    name.setText("Name       :  "+student.getName());
                                    email.setText("Email id    :  " +student.getEmailId());
                                    password.setText("Password :  " +student.getPassword());
                                    Toast.makeText(MainActivity.this,"Login successful",Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(MainActivity.this,databaseError.getCode(),Toast.LENGTH_SHORT).show();
                                }
                            });
                            StorageReference storageReference=mStorage.getReference().child(mAuth.getUid()).child("Image");
                            try {
                                final File localFile =File.createTempFile("profile pic","jpg");
                                storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        Bitmap bitmap= BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                        loginImg.setImageBitmap(bitmap);
                                        Toast.makeText(MainActivity.this,"Image download successful",Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this,"Error occurred!",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            /*storageReference.child(mAuth.getUid()).child("Image").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                }
                            });*/

                        }
                        else{
                            Toast.makeText(MainActivity.this,"Login failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                }

            }
        });
        regImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");//application/* audio/* video/*
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,1);
            }
        });
    }
    private void setupViews(){
        regUserName=(EditText) findViewById(R.id.reg_user_name);
        regPassword=(EditText) findViewById(R.id.reg_pass_word);
        regEmailId=(EditText) findViewById(R.id.reg_user_email_id);
        loginEmailId=(EditText) findViewById(R.id.login_user_email_id);
        loginPassword=(EditText) findViewById(R.id.login_pass_word);
        loginButton=(Button) findViewById(R.id.log_in);
        registerButton=(Button) findViewById(R.id.register);
        name=(TextView) findViewById(R.id.retrieved_name);
        email=(TextView) findViewById(R.id.retrieved_email);
        password=(TextView) findViewById(R.id.retrieved_password);
        regImg=(ImageView) findViewById(R.id.registration_image);
        loginImg=(ImageView) findViewById(R.id.login_image);
    }

}
