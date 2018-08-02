package com.parse.starter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView username;
    private ImageView profile;
    private Button btnLogOut;
    private String user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");

        Intent intent = getIntent();
        user = intent.getStringExtra("username");
        username = (TextView) findViewById(R.id.txtUsername);
        profile = (ImageView) findViewById(R.id.profile_image);
        btnLogOut = (Button) findViewById(R.id.btnLogOut);
        username.setText(user);
        loadProfile();
        if(user.equals(ParseUser.getCurrentUser().getUsername())){
            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("clicked");
//            To Chech if the request is accepted and API 23 or more
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                            System.out.println("asking");
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                        }else{
                            getPhoto();
                        }
                    }else{
                        getPhoto();
                    }
                }
            });
        }
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
    private void getPhoto(){
        System.out.println("get photo");
//        To select from phone media
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        To expect a result from intent
        startActivityForResult(intent,1);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        Jika sudah mendapat result maka getPhoto
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                System.out.println("granted");
                getPhoto();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==1 && resultCode == RESULT_OK && data!=null){
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                profile.setImageBitmap(bitmap);
//                to convert our images into pass file supaya bisa di upload sebagai bagian dari pass object
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                byte[] byteArray = stream.toByteArray();
//                Harus di convert dalam byte agar bisa di upload
                ParseFile file = new ParseFile("image.png",byteArray);
                ParseUser.getCurrentUser().put("profile",file);
                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            Toast.makeText(ProfileActivity.this,"Image Updated",Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(ProfileActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void loadProfile(){
        if(user.equals(ParseUser.getCurrentUser())){
            ParseFile file= (ParseFile) ParseUser.getCurrentUser().get("profile");
            if(file!=null){
                file.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if(e == null && data!=null){
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                            profile.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        }else{
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username",user);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    ParseFile file= (ParseFile) objects.get(0).get("profile");
                    System.out.println("file "+file);
                    if(file!=null){
                        file.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                if(e == null && data!=null){
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                                    profile.setImageBitmap(bitmap);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}
