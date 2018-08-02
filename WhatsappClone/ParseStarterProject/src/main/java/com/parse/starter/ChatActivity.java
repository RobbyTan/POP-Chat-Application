package com.parse.starter;

import android.app.ActionBar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.Image;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.adapter.CustomAdapter;
import com.parse.starter.models.ChatModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String activeUser="";
    private ArrayList<String> messages = new ArrayList<>();
    List<ChatModel> list_chat = new ArrayList<>();
    private ArrayAdapter arrayAdapter;
    private CustomAdapter adapter;
    private String messageContent;
    private Handler handler = new Handler();
    private EditText chatEditText;
    private ListView chatListView;
    private Button btnSendText;
    android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatListView = (ListView) findViewById(R.id.chatListView);
        chatEditText = (EditText) findViewById(R.id.chatEditText);
        btnSendText = (Button) findViewById(R.id.btnSendText);
//        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,messages);
//
//        chatListView.setAdapter(arrayAdapter);

        adapter = new CustomAdapter(list_chat,getApplicationContext());
        chatListView.setAdapter(adapter);
        Intent intent=getIntent();
        activeUser = intent.getStringExtra("username");
        System.out.println("tapped "+activeUser);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chatProfile();
        getSupportActionBar().setTitle(activeUser);

        querryBackground();
        StartUpdateHandler();
        btnSendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendChat();
            }
        });

    }

    public Bitmap createCircleBitmap(Bitmap bitmapimg){
        Bitmap output = Bitmap.createBitmap(bitmapimg.getWidth(),
                bitmapimg.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmapimg.getWidth(),
                bitmapimg.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmapimg.getWidth() / 2,
                bitmapimg.getHeight() / 2, bitmapimg.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmapimg, rect, rect, paint);
        return output;
    }

    public void sendChat(){
        messageContent=chatEditText.getText().toString();
        final ParseObject message = new ParseObject("Message");
        message.put("sender", ParseUser.getCurrentUser().getUsername());
        message.put("recepient",activeUser);
        message.put("message",messageContent);
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm|dd-MM-yy");
                    String date = DATE_FORMAT.format(message.getCreatedAt());
                    ChatModel model = new ChatModel(messageContent,true,date); // user send message
                    list_chat.add(model);
                    updateAdapter(list_chat);
//                    messages.add(messageContent);
//                    arrayAdapter.notifyDataSetChanged();
//                  Untuk membuat selection ke bawah setiap selesai menerima pesan
//                    chatListView.setSelection(messages.size());
                    chatEditText.setText("");
                    Toast.makeText(ChatActivity.this, "Chat Send", Toast.LENGTH_SHORT).show();
                }else{
//                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void StartUpdateHandler(){
//        untuk melakukan update setiap beberapa detik
        handler.postDelayed(runQuerry,10*1000);
    }
    private Runnable runQuerry= new Runnable() {
        @Override
        public void run() {
            System.out.println("background handler");
            querryBackground();
            handler.postDelayed(this, 10*1000);
        }
    };
    private void querryBackground(){
//      Mengquery chat yang kita kirim
        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Message");
        query1.whereEqualTo("sender",ParseUser.getCurrentUser().getUsername());
        query1.whereEqualTo("recepient",activeUser);
//       Mengquery chat yang kita terima
        ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Message");
        query2.whereEqualTo("recepient",ParseUser.getCurrentUser().getUsername());
        query2.whereEqualTo("sender",activeUser);

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
                        if(objects.size()>list_chat.size() && list_chat.size()!=0){
                            generateNotifications(objects.get(objects.size()-1).getString("message"),objects.get(objects.size()-1).getString("sender"));
//                          System.out.println("Notif : "+objects.get(objects.size()-1).getString("message")+" "+objects.get(objects.size()-1).getString("sender"));
                            System.out.println("object size : "+objects.size() + " message size : "+messages.size());
                        }
                        System.out.println("Size object : "+objects.size()+" message : "+messages.size());
                        list_chat.clear();
//                        messages.clear();
                        for(ParseObject message : objects){
//                            untuk mendapat waktu message dikirim
//                            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yy:HH:mm");
                            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm|dd-MM-yy");
                            String date = DATE_FORMAT.format(message.getCreatedAt());
                            String messageContent = message.getString("message");
                            if(!message.getString("sender").equals(ParseUser.getCurrentUser().getUsername())){
                                ChatModel model = new ChatModel(messageContent,false,date); // user send message
                                list_chat.add(model);
                            }else{
                                ChatModel model = new ChatModel(messageContent,true,date);
                                list_chat.add(model);
                            }

                            updateAdapter(list_chat);
//                            messages.add(messageContent);
//                            arrayAdapter.notifyDataSetChanged();
//                            Untuk membuat selection ke bawah setiap selesai menerima pesan
//                            chatListView.setSelection(messages.size());
                            System.out.println("date : "+message.getCreatedAt());
                            System.out.println("message : "+list_chat);
                        }
                    }
                }else{
                    System.out.println("error send "+e.getMessage());
                }
            }
        });
    }
    private void generateNotifications(String text,String user){
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
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setSound(alarmSound)
//                membuat suara,vibrate dan light
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1,notification);

        }
    }

    private void updateAdapter(List<ChatModel> list_chat){
        adapter= new CustomAdapter(list_chat,getApplicationContext());
        chatListView.setAdapter(adapter);
    }
    private void chatProfile(){

        noprofile();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username",activeUser);

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
//                untuk mengambil profile dr user
                ParseFile file= (ParseFile) objects.get(0).get("profile");
                System.out.println("file "+file);
                if(file!=null){
                    file.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            if(e == null && data!=null){
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                                Bitmap b = createCircleBitmap(bitmap);
                                Drawable d = new BitmapDrawable(getResources(),
                                        Bitmap.createScaledBitmap(b, 80, 80, true));
                                toolbar.setNavigationIcon(d);
                            }
                        }
                    });
                }else{
                    noprofile();
                }
            }
        });
    }

    private void noprofile(){
        Drawable dr = getResources().getDrawable(R.drawable.nopp);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Bitmap b = createCircleBitmap(bitmap);
        Drawable d = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(b, 80, 80, true));
        toolbar.setNavigationIcon(d);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = new MenuInflater(getApplicationContext());
        getMenuInflater().inflate(R.menu.chat_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.clearChat){
            //      Mengquery chat yang kita kirim
            ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Message");
            query1.whereEqualTo("sender",ParseUser.getCurrentUser().getUsername());
            query1.whereEqualTo("recepient",activeUser);
//       Mengquery chat yang kita terima
            ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Message");
            query2.whereEqualTo("recepient",ParseUser.getCurrentUser().getUsername());
            query2.whereEqualTo("sender",activeUser);

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
                                    list_chat.clear();
                                    adapter.notifyDataSetChanged();
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(ChatActivity.this,UserListActivity.class);
        startActivity(intent);
    }
}
