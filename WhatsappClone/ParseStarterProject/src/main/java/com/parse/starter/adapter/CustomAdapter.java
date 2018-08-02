package com.parse.starter.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.library.bubbleview.BubbleTextView;
import com.parse.starter.R;
import com.parse.starter.models.ChatModel;

import java.util.List;

/**
 * Created by USER on 7/8/2018.
 */

public class CustomAdapter extends BaseAdapter {
    private List<ChatModel> list_chat_models;
    private Context context;
    private LayoutInflater layoutInflater;
    private ImageView profile;

    public CustomAdapter(List<ChatModel> list_chat_models, Context context) {
        this.list_chat_models = list_chat_models;
        this.context = context;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.list_item_message_recv,null);
        profile = (ImageView) view.findViewById(R.id.profile_image);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list_chat_models.size();
    }

    @Override
    public Object getItem(int position) {
        return list_chat_models.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

            if(list_chat_models.get(position).isSend){
                view = layoutInflater.inflate(R.layout.list_item_message_send,null);
            }
            else{
                view = layoutInflater.inflate(R.layout.list_item_message_recv,null);
            }


            BubbleTextView text_message = (BubbleTextView)view.findViewById(R.id.text_message);
            TextView time = (TextView)view.findViewById(R.id.txtTime);
            text_message.setText(list_chat_models.get(position).message);
            time.setText(list_chat_models.get(position).time);


        return view;
    }
    public void setProfile(Bitmap bitmap){
        System.out.println("profile"+profile);
        System.out.println("bitmap"+bitmap);
        profile.setImageBitmap(bitmap);
    }
}
