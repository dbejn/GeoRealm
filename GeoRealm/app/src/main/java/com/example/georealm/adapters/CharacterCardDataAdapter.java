package com.example.georealm.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.georealm.R;
import com.example.georealm.data.CharacterCardData;
import com.example.georealm.helper.Constants;

import java.util.ArrayList;
import java.util.List;

import static com.example.georealm.helper.Constants.ROGUE;
import static com.example.georealm.helper.Constants.SORCERER;
import static com.example.georealm.helper.Constants.SWORDSMAN;

public class CharacterCardDataAdapter extends RecyclerView.Adapter<CharacterCardDataAdapter.CharacterCardViewHolder> {

    private List<CharacterCardData> character_data;
    private Context context;
    private int selected;
    private Button button_play;
    private CharacterCardDataAdapter.CharacterCardViewHolder selected_holder;

    public CharacterCardDataAdapter(Context ctx, Button play) {

        character_data = new ArrayList<>();
        context = ctx;
        selected = -1;
        button_play = play;
    }

    public void addCharacterToList(int position, CharacterCardData data) {

        character_data.add(position, data);
    }

    private void deselectCurrentLayout() {

        if (selected_holder != null) {

            selected_holder.character_card_layout.setForeground(null);
        }
    }

    public static class CharacterCardViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout character_card_layout;// background, foreground
        public ImageView character_card_image;// src, colour
        public TextView character_card_name;// text, colour
        public TextView character_card_level;// text, colour
        public TextView character_card_subclass;// text, colour
        public TextView character_card_class;// text, colour

        public CharacterCardViewHolder(View card_view) {

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
    public CharacterCardDataAdapter.CharacterCardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View card_view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_character_card, viewGroup, false);

        return new CharacterCardViewHolder(card_view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CharacterCardDataAdapter.CharacterCardViewHolder characterCardViewHolder, int i) {

        final int position = i;
        final CharacterCardData data = character_data.get(i);

        characterCardViewHolder.character_card_name.setText(data.getCharacter_name());
        characterCardViewHolder.character_card_level.setText(context.getString(R.string.level_x, data.getCharacter_level()));

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

        characterCardViewHolder.character_card_subclass.setText(character_subclass);
        characterCardViewHolder.character_card_class.setText(character_class);
        characterCardViewHolder.character_card_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selected != position) {

                    deselectCurrentLayout();
                    characterCardViewHolder.character_card_layout.setForeground(context.getDrawable(R.drawable.select));
                    selected_holder = characterCardViewHolder;

                    if (selected == -1) {

                        button_play.setBackgroundResource(R.drawable.left_rounded_rec_base);
                        button_play.setEnabled(true);
                        button_play.setTag(R.string.character_name, characterCardViewHolder.character_card_name.getText().toString());// characterCardViewHolder.character_card_class.getText().toString()
                        button_play.setTag(R.string.character_class, data.getCharacter_class());
                        button_play.setTag(R.string.character_subclass, data.getCharacter_subclass());
                        // String level = characterCardViewHolder.character_card_level.getText().toString();
                        // level = level.substring(6, level.length() - 1);
                        button_play.setTag(R.string.character_level, data.getCharacter_level());
                    }

                    selected = position;
                }
            }
        });

        /*final String character_name = characterCardViewHolder.character_card_name.getText().toString();

        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(context);
        dialog_builder.setTitle("Delete character " + character_name);
        dialog_builder.setMessage("Are you sure?");
        dialog_builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                database.collection("users")
                        .document(user_id_token).collection("characters")
                        .document(character_name).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(context,
                                "Character " + character_name + " deleted successfully",
                                Toast.LENGTH_SHORT).show();

                        character_data.remove(position);
                        notifyItemRemoved(position);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context, "Failed to delete character", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog_builder.setNegativeButton("No", null);
        dialog_builder.show();*/

        if (data.getCharacter_class() == SWORDSMAN) {

            characterCardViewHolder.character_card_layout.setBackgroundResource(R.drawable.choose_character_swordsman_card);
            characterCardViewHolder.character_card_image.setImageResource(R.drawable.sword);
            characterCardViewHolder.character_card_image.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorSword)));
            characterCardViewHolder.character_card_name.setTextColor(ContextCompat.getColor(context, R.color.colorSword));
            characterCardViewHolder.character_card_level.setTextColor(ContextCompat.getColor(context, R.color.colorSword));
            characterCardViewHolder.character_card_subclass.setTextColor(ContextCompat.getColor(context, R.color.colorSword));
            characterCardViewHolder.character_card_class.setTextColor(ContextCompat.getColor(context, R.color.colorSword));
        }
        else if (data.getCharacter_class() == SORCERER) {

            characterCardViewHolder.character_card_layout.setBackgroundResource(R.drawable.choose_character_sorcerer_card);
            characterCardViewHolder.character_card_image.setImageResource(R.drawable.magic_hat);
            characterCardViewHolder.character_card_image.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorHat)));
            characterCardViewHolder.character_card_name.setTextColor(ContextCompat.getColor(context, R.color.colorHat));
            characterCardViewHolder.character_card_level.setTextColor(ContextCompat.getColor(context, R.color.colorHat));
            characterCardViewHolder.character_card_subclass.setTextColor(ContextCompat.getColor(context, R.color.colorHat));
            characterCardViewHolder.character_card_class.setTextColor(ContextCompat.getColor(context, R.color.colorHat));
        }
        else if (data.getCharacter_class() == ROGUE) {

            characterCardViewHolder.character_card_layout.setBackgroundResource(R.drawable.choose_character_rogue_card);
            characterCardViewHolder.character_card_image.setImageResource(R.drawable.dagger);
            characterCardViewHolder.character_card_image.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorDagger)));
            characterCardViewHolder.character_card_name.setTextColor(ContextCompat.getColor(context, R.color.colorDagger));
            characterCardViewHolder.character_card_level.setTextColor(ContextCompat.getColor(context, R.color.colorDagger));
            characterCardViewHolder.character_card_subclass.setTextColor(ContextCompat.getColor(context, R.color.colorDagger));
            characterCardViewHolder.character_card_class.setTextColor(ContextCompat.getColor(context, R.color.colorDagger));
        }
    }

    @Override
    public int getItemCount() {

        return character_data.size();
    }
}
