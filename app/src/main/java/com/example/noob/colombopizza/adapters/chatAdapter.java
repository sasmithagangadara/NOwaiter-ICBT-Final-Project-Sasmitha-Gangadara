package com.example.noob.colombopizza.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.noob.colombopizza.R;

import java.util.ArrayList;

public class chatAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> botList = new ArrayList<>();
    ArrayList<String> userList = new ArrayList<>();

    TextView botchat;
    TextView userchat;

    public chatAdapter(Context context, ArrayList<String> botList, ArrayList<String> userList) {
        this.context = context;
        this.botList = botList;
        this.userList = userList;
    }

    @Override
    public int getCount() {
        return botList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.layout_bot_chat, parent, false);

        findViews(itemView);

        botchat.setText(botList.get(position));
        userchat.setText(userList.get(position));

        return itemView;
    }

    private void findViews(View itemView) {

        botchat = (TextView) itemView.findViewById(R.id.tv_bot);
        userchat = (TextView) itemView.findViewById(R.id.tv_user);


    }
}
