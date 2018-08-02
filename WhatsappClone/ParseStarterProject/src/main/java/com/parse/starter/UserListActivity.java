package com.parse.starter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class UserListActivity extends AppCompatActivity {

    private ListView userListView;
    private ArrayList<String> users = new ArrayList<>();
    private ArrayAdapter arrayAdapter;
    private Handler handler = new Handler();
    private ArrayList<String> messages = new ArrayList<>();
    private static final int NOTIFICATION_ID = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Contacts");

//        alarm();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("username",ParseUser.getCurrentUser().getUsername());
        query.addAscendingOrder("username");
//        Supaya tidak null kalau array kosong
        if(ParseUser.getCurrentUser().get("friends")==null){
            List<String> emptyList = new ArrayList<>();
//            Membuat list kosong untuk kolom isFollowing bagi user sekarang jika tidak ada
            ParseUser.getCurrentUser().put("friends",emptyList);
        }
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null && objects.size()>0){
                    System.out.println("Loading");
                    for(ParseUser user : objects){
                        if(ParseUser.getCurrentUser().getList("friends").contains(user.getUsername())){
                            users.add(user.getUsername());
                            arrayAdapter.notifyDataSetChanged();
                        }
                    }
                    System.out.println("Finished user");
                }else{
                    String message = e.getMessage();
                    if(message.toLowerCase().contains("java")){
                        message = e.getMessage().substring(e.getMessage().indexOf(" "));
                    }
                    Toast.makeText(UserListActivity.this,message, LENGTH_LONG).show();
                }
            }
        });
        userListView = (ListView) findViewById(R.id.userListView);
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,users);
        userListView.setAdapter(arrayAdapter);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
//                mempassing nama username yang di click
                intent.putExtra("username",users.get(i));
                startActivity(intent);
                finish();
            }
        });
        userListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                deleteFriend(users,i);
                return false;
            }
        });
        StartUpdateHandler();
    }

    private void getNotification(){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Message");
        if(ParseUser.getCurrentUser() == null){
            return;
        }else{
            query.whereEqualTo("recepient",ParseUser.getCurrentUser().getUsername());
//        Untuk mengurutkan message berdasarkan waktu
            query.orderByAscending("createdAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null) {
//                  System.out.println("FindInBackground "+objects.size());
                        if(objects.size()>0){
                            if(objects.size()>messages.size() && messages.size()!=0){
                                for(int i =0 ;i<=objects.size()-1;i++){
                                    System.out.println("object "+i+" "+objects.get(i).getString("message"));
                                }

                                generateNotifications(objects.get(objects.size()-1).getString("message"),objects.get(objects.size()-1).getString("sender"));
//                          System.out.println("Notif : "+objects.get(objects.size()-1).getString("message")+" "+objects.get(objects.size()-1).getString("sender"));
                                System.out.println("object size : "+objects.size() + " message size : "+messages.size());
                            }
                            messages.clear();
                            for(ParseObject object: objects){
                                messages.add(object.getString("message"));
                                System.out.println(messages);
                            }
                        }
                    }
                }
            });
        }
    }
    private void StartUpdateHandler(){
//        untuk melakukan update setiap beberapa detik
        handler.postDelayed(runQuerry,30*1000);
    }
    private Runnable runQuerry= new Runnable() {
        @Override
        public void run() {
//            System.out.println("background handler");
            getNotification();
            handler.postDelayed(this, 30*1000);
        }
    };
    private void generateNotifications(String text,String user){
        System.out.println("NOTIFF");
        //        To make default notification sound
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        ketika di click masuk ke main activity

        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra("username",user);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),1,intent,0);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(text)
                    .setContentText(user)
                    //                Apa yg dilakukan ketika notification di click
                    .setContentIntent(pendingIntent)
                    .addAction(android.R.drawable.sym_action_chat, "Chat",pendingIntent)
                    .setSmallIcon(R.drawable.logo)
                    .setSound(alarmSound)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1,notification);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = new MenuInflater(getApplicationContext());
        getMenuInflater().inflate(R.menu.friend_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.addFriend){
            dialogAddFriend();
        }else if(item.getItemId() == R.id.profile){
            Intent intent = new Intent(UserListActivity.this,ProfileActivity.class);
            intent.putExtra("username",ParseUser.getCurrentUser().getUsername());
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    private void dialogAddFriend(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_friend, null);
        builder.setView(dialogView);

        final EditText friendUsernametEditText = (EditText) dialogView.findViewById(R.id.add);

        builder.setIcon(R.drawable.logo);
        builder.setTitle("Add Friend ");
//            membuat alert dialog dari edit text dan positive and negative button
//        builder.setView(friendUsernametEditText);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String friend =friendUsernametEditText.getText().toString();
                ParseQuery<ParseUser> query = ParseUser.getQuery();
//                Mengecek apakah terdapat user tersebut
                query.whereEqualTo("username",friend);
                query.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        if(e==null && objects.size()>0){
//                            Jika sudah ada dalam friend maka tidak akan disimpan
                            if(ParseUser.getCurrentUser().getList("friends").contains(friend)){
                                Toast.makeText(UserListActivity.this,friend+" is already on your contact",LENGTH_LONG).show();
                            }else{
                                if(ParseUser.getCurrentUser().get("friends")==null){
                                    List<String> emptyList = new ArrayList<>();
//                                  Membuat list kosong untuk kolom isFollowing bagi user sekarang jika tidak ada
                                    ParseUser.getCurrentUser().put("friends",emptyList);
                                }
//                              kalau hanya add biasa maka ketika di logout tidak akan tersave dan hanya disimpan sementara
                                ParseUser.getCurrentUser().getList("friends").add(friend);
//                              untuk save ke server
                                ParseUser.getCurrentUser().put("friends",ParseUser.getCurrentUser().getList("friends"));
                                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e==null){
                                            Toast.makeText(UserListActivity.this,friend+" is now your friend",LENGTH_LONG).show();
                                            users.add(0,friend);
                                            arrayAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                            }
                            System.out.println("friends "+ParseUser.getCurrentUser().getList("friends"));
                        }else{
                            Toast.makeText(UserListActivity.this,"This User Doesn't Exist",LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }
    private void deleteFriend(final ArrayList users, final int i){
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(UserListActivity.this);

// add a list
        String[] actions = {"Chat","View Profile","Block Friend"};
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        chatPage(i);
                        break;
                    case 1 :
                        viewProfile(i);
                        break;
                    case 2:
                        deleteChat(users,i);
                        ParseUser.getCurrentUser().getList("friends").remove(users.get(i));
                        ParseUser.getCurrentUser().put("friends",ParseUser.getCurrentUser().getList("friends"));
                        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e==null){
                                    Toast.makeText(UserListActivity.this,users.get(i)+" has been removed",LENGTH_LONG).show();
                                    users.remove(i);
                                    arrayAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                        break;
                }
            }
        });

// create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void deleteChat(final ArrayList users, final int i){
        //      Mengquery chat yang kita kirim
        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Message");
        query1.whereEqualTo("sender",ParseUser.getCurrentUser().getUsername());
        query1.whereEqualTo("recepient",users.get(i));
//       Mengquery chat yang kita terima
        ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Message");
        query2.whereEqualTo("recepient",ParseUser.getCurrentUser().getUsername());
        query2.whereEqualTo("sender",users.get(i));

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

//        Untuk mencari kondisi yang memenuhi keduanya
        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    if(objects.size()>0){
                        for(ParseObject message : objects) {
                            try {
                                message.delete();
                                message.saveInBackground();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                        }

                    }
                }else{
                    System.out.println("error send "+e.getMessage());
                }
            }
        });
    }
    private void viewProfile(int i){
        Intent intent = new Intent(UserListActivity.this,ProfileActivity.class);
        intent.putExtra("username",users.get(i));
        startActivity(intent);
    }
    private void chatPage(int i){
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
//                mempassing nama username yang di click
        intent.putExtra("username",users.get(i));
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
//        untuk launch ke home
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        finish();
    }
}
