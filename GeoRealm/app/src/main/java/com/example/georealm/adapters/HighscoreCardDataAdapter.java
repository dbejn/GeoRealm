package com.example.georealm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.georealm.R;
import com.example.georealm.data.HighscoreStruct;

import java.util.List;

public class HighscoreCardDataAdapter extends RecyclerView.Adapter<HighscoreCardDataAdapter.HighscoreCardViewHolder> {

    private List<HighscoreStruct> highscores;
    private Context context;

    public HighscoreCardDataAdapter(Context ctx, List<HighscoreStruct> highs) {

        context = ctx;
        highscores = highs;
    }

    public static class HighscoreCardViewHolder extends RecyclerView.ViewHolder {

        public TextView position;
        public TextView character_name;
        public TextView account_name;
        public TextView score;

        public HighscoreCardViewHolder(View card_view) {

            super(card_view);

            position = card_view.findViewById(R.id.position);
            character_name = card_view.findViewById(R.id.character_name);
            account_name = card_view.findViewById(R.id.account_name);
            score = card_view.findViewById(R.id.score);
        }
    }

    @NonNull
    @Override
    public HighscoreCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View card_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_highscore_card, parent, false);

        return new HighscoreCardViewHolder(card_view);
    }

    @Override
    public void onBindViewHolder(@NonNull HighscoreCardViewHolder holder, int position) {

        holder.position.setText(context.getString(R.string.position, highscores.get(position).position));
        holder.character_name.setText(highscores.get(position).character_name);
        holder.account_name.setText(highscores.get(position).account_name);
        holder.score.setText(context.getString(R.string.integer, highscores.get(position).score));
    }

    @Override
    public int getItemCount() {

        return highscores.size();
    }
}
