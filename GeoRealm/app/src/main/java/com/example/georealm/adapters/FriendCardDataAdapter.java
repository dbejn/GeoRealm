package com.example.georealm.adapters;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.georealm.AccountActivity;
import com.example.georealm.R;

import java.util.ArrayList;
import java.util.List;

public class FriendCardDataAdapter extends RecyclerView.Adapter<FriendCardDataAdapter.FriendCardViewHolder> {

    private List<String> friend_names;
    private List<String> friend_statuses;
    private Context context;
    private LinearLayout friend_options;
    private Button button_view;
    private Button button_remove;
    private int selected;
    private FriendCardDataAdapter.FriendCardViewHolder selected_holder;

    public FriendCardDataAdapter(Context ctx, Button view, Button remove,
                                 List<String> names, LinearLayout options) {

        context = ctx;
        button_view = view;
        button_remove = remove;
        friend_names = names;
        friend_statuses = new ArrayList<>();
        for (String name : friend_names)
            friend_statuses.add("");
        friend_options = options;
        selected = -1;
    }

    public void addStatus(String friend, String status) {

        int position = friend_names.indexOf(friend);
        friend_statuses.remove(position);
        friend_statuses.add(position, status);
    }

    public void removeFriend(String friend_name) {

        int position = friend_names.indexOf(friend_name);
        friend_statuses.remove(position);
        friend_names.remove(position);

        selected_holder = null;
        selected = -1;
    }

    private void deselectCurrentLayout() {

        if (selected_holder != null) {

            selected_holder.friend_layout.setForeground(null);
        }
    }

    public static class FriendCardViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout friend_layout;
        public TextView friend_name;
        public TextView friend_status;

        public FriendCardViewHolder(View card_view) {

            super(card_view);

            friend_layout = card_view.findViewById(R.id.friend_layout);
            friend_name = card_view.findViewById(R.id.friend_name);
            friend_status = card_view.findViewById(R.id.friend_status);
        }
    }

    @NonNull
    @Override
    public FriendCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View card_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_friend_card, parent, false);

        return new FriendCardViewHolder(card_view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendCardViewHolder holder, final int position) {

        final String name = friend_names.get(position);
        final String status = friend_statuses.get(position);

        holder.friend_name.setText(name);
        holder.friend_status.setText(status);
        holder.friend_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selected != position) {

                    selected = position;

                    deselectCurrentLayout();
                    holder.friend_layout.setForeground(context.getDrawable(R.drawable.select));
                    selected_holder = holder;

                    friend_options.setBackground(context.getDrawable(R.drawable.top_rounded_rec_trans));
                    button_view.setEnabled(true);
                    button_view.setTag(name);
                    button_remove.setEnabled(true);
                    button_remove.setTag(name);
                }
            }
        });
    }

    @Override
    public int getItemCount() {

        return friend_names.size();
    }
}
