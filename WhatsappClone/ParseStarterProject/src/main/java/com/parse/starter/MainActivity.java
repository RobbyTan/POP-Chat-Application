/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

  private EditText editUsername,editPassword;
  private Button btnSignUp;
  private TextView changeSignUpMode;
  private Boolean signUpModeActive =true;
  private RelativeLayout background;
  private ImageView imgLogo;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    editUsername = (EditText) findViewById(R.id.editUsername);
    editPassword = (EditText) findViewById(R.id.editPassword);
    btnSignUp = (Button) findViewById(R.id.btnSignUp);
    changeSignUpMode = (TextView) findViewById(R.id.changeSignUpMode);
    changeSignUpMode.setOnClickListener(this);
    editPassword.setOnKeyListener(this);
    background= (RelativeLayout) findViewById(R.id.background);
    imgLogo = (ImageView) findViewById(R.id.imgLogo);
    background.setOnClickListener(this);
    imgLogo.setOnClickListener(this);
    redirectPage();

    btnSignUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        signUp();
      }
    });

    ParseAnalytics.trackAppOpenedInBackground(getIntent());
  }
  private void signUp(){
    if(signUpModeActive){
      ParseUser user = new ParseUser();

      if(editUsername.getText().toString().trim().isEmpty()||editPassword.getText().toString().trim().isEmpty()){
        Toast.makeText(MainActivity.this,"Username and Password are required",Toast.LENGTH_LONG).show();
      }else{
        user.setUsername(editUsername.getText().toString());
        user.setPassword(editPassword.getText().toString());
        user.signUpInBackground(new SignUpCallback() {
          @Override
          public void done(ParseException e) {
            if(e == null){
              Toast.makeText(MainActivity.this,"Sign Up Successfull",Toast.LENGTH_LONG).show();
              redirectPage();
            }else{
              String message = e.getMessage();
//              untuk berjaga jaga jika terdapat error  dengan kata awal java yang panjang maka di substring
              if(message.toLowerCase().contains("java")){
                message = e.getMessage().substring(e.getMessage().indexOf(" "));
              }
              Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
            }
          }
        });
      }
    }else{
      logIn();
    }


  }

  private void logIn(){
        ParseUser.logInInBackground(editUsername.getText().toString(), editPassword.getText().toString(), new LogInCallback() {
      @Override
      public void done(ParseUser user, ParseException e) {
        if(user != null){
          Toast.makeText(MainActivity.this,"Login Successfull",Toast.LENGTH_LONG).show();
          redirectPage();
        }else{
          String message = e.getMessage();
          if(message.toLowerCase().contains("java")){
            message = e.getMessage().substring(e.getMessage().indexOf(" "));
          }
          Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
        }
      }
    });
  }
  public void redirectPage(){
    if(ParseUser.getCurrentUser() !=null){
        Intent intent = new Intent(getApplicationContext(),UserListActivity.class);
        startActivity(intent);
    }
  }
  @Override
  public void onClick(View view) {
    if(view.getId()==R.id.changeSignUpMode){
      if(signUpModeActive==true){
        signUpModeActive=false;
        btnSignUp.setText("Login");
        changeSignUpMode.setText("or Sign Up");
      }else{
        signUpModeActive=true;
        btnSignUp.setText("Sign Up");
        changeSignUpMode.setText("or Login");
      }

    }else if (view.getId() == R.id.background || view.getId() == R.id.imgLogo){
//      Untuk mendapatkan keyboard
      InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//      untuk menyembunyikan keyborad
      inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }
  }

  @Override
//  this method will be called whenever a key is pressed in editPasswordText
  public boolean onKey(View view, int i, KeyEvent keyEvent) {
//    Ketika tekan enter di keyboard akan langsung signup/login
    if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
      signUp();
    }

    return false;
  }
}
