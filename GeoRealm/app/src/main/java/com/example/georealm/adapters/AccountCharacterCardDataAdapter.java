package com.example.georealm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.georealm.R;
import com.example.georealm.data.CharacterCardData;
import com.example.georealm.helper.Constants;

import java.util.ArrayList;
import java.util.List;

public class AccountCharacterCardDataAdapter
        extends RecyclerView.Adapter<AccountCharacterCardDataAdapter.AccountCharacterCardViewHolder> {

    private List<CharacterCardData> character_data;
    private Context context;
    private boolean selectable;
    private int selected;
    private LinearLayout layout_character_options;
    private Button button_view;
    private Button button_delete;
    private AccountCharacterCardDataAdapter.AccountCharacterCardViewHolder selected_holder;

    public AccountCharacterCardDataAdapter(Context ctx, List<CharacterCardData> data,
                                           Button view, Button delete, boolean sele,
                                           LinearLayout line) {

        character_data = data;
        context = ctx;
        selectable = sele;
        selected = -1;
        layout_character_options = line;
        button_view = view;
        button_delete = delete;
    }

    private void deselectCurrentLayout() {

        if (selected_holder != null) {

            selected_holder.character_card_layout.setForeground(null);
        }
    }

    public void removeCharacterFromList(int position) {

        character_data.remove(position);
        selected = -1;
        deselectCurrentLayout();
    }

    public static class AccountCharacterCardViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout character_card_layout;// background, foreground
        public ImageView character_card_image;// src, colour
        public TextView character_card_name;// text, colour
        public TextView character_card_level;// text, colour
        public TextView character_card_subclass;// text, colour
        public TextView character_card_class;// text, colour

        public AccountCharacterCardViewHolder(View card_view) {

            super(card_view);

            character_card_layout = card_view.findViewById(R.id.character_card_layout);
            character_card_image = card_view.findViewById(R.id.character_card_image);
            character_card_name = card_view.findViewById(R.id.character_card_name);
            character_card_level = card_view.findViewById(R.id.character_card_level);
            character_card_subclass = card_view.findViewById(R.id.character_card_subclass);
            character_card_class = card_view.findViewById(R.id.character_card_class);
        }
    }

    @NonNull
    @Override
    public AccountCharacterCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View card_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_character_card, parent, false);

        return new AccountCharacterCardDataAdapter.AccountCharacterCardViewHolder(card_view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AccountCharacterCardViewHolder holder, final int position) {

        final CharacterCardData data = character_data.get(position);

        holder.character_card_name.setText(data.getCharacter_name());
        holder.character_card_level.setText(context.getString(R.string.level_x, data.getCharacter_level()));

        String character_class = "";
        String character_subclass = "";

        switch (data.getCharacter_subclass()) {

            case Constants.BERSERKER:
                character_class = "swordsman";
                character_subclass = "berserker";
                break;
            case Constants.PALADIN:
                character_class = "swordsman";
                character_subclass = "paladin";
                break;
            case Constants.PYROMANCER:
                character_class = "sorcerer";
                character_subclass = "pyromancer";
                break;
            case Constants.ICEBOUND:
                character_class = "sorcerer";
                character_subclass = "icebound";
                break;
            case Constants.ASSASSIN:
                character_class = "rogue";
                character_subclass = "assassin";
                break;
            case Constants.SHADOW:
                character_class = "rogue";
                character_subclass = "shadow";
                break;
        }

        holder.character_card_subclass.setText(character_subclass);
        holder.character_card_class.setText(character_class);
        if (selectable) {

            holder.character_card_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (selected != position) {

                        selected = position;

                        deselectCurrentLayout();
                        holder.character_card_layout.setForeground(context.getDrawable(R.drawable.select));
                        selected_holder = holder;

                        layout_character_options.setBackground(context.getDrawable(R.drawable.top_rounded_rec_trans));
                        button_view.setEnabled(true);
                        button_view.setTag(R.string.character_name, data.getCharacter_name());
                        button_view.setTag(R.string.character_subclass, data.getCharacter_subclass());
                        button_delete.setEnabled(true);
                        button_delete.setTag(R.string.character_name, data.getCharacter_name());
                        button_delete.setTag(R.string.character_subclass, position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {

        return character_data.size();
    }
}
