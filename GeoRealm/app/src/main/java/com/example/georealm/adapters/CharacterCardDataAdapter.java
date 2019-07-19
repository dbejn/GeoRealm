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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.georealm.R;
import com.example.georealm.data.CharacterCardData;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CharacterCardDataAdapter extends RecyclerView.Adapter<CharacterCardDataAdapter.CharacterCardViewHolder> {

    private List<CharacterCardData> character_data;
    private Context context;
    private int selected;
    private RelativeLayout layout_play;
    private CharacterCardDataAdapter.CharacterCardViewHolder selected_holder;

    public CharacterCardDataAdapter(Context ctx, RelativeLayout play) {

        character_data = new ArrayList<>();
        context = ctx;
        selected = -1;
        layout_play = play;
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
        CharacterCardData data = character_data.get(i);

        characterCardViewHolder.character_card_name.setText(data.getCharacter_name());
        characterCardViewHolder.character_card_level.setText(context.getString(R.string.level_x, data.getCharacter_level()));
        characterCardViewHolder.character_card_subclass.setText(data.getCharacter_subclass());
        characterCardViewHolder.character_card_class.setText(data.getCharacter_class());
        characterCardViewHolder.character_card_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(context, Integer.toString(position), Toast.LENGTH_SHORT).show();

                if (selected != position) {

                    deselectCurrentLayout();
                    characterCardViewHolder.character_card_layout.setForeground(context.getDrawable(R.drawable.select));
                    selected_holder = characterCardViewHolder;

                    if (selected == -1) {

                        layout_play.setBackgroundResource(R.drawable.left_rounded_rec_base);
                        ImageButton button_play = layout_play.findViewById(R.id.button_play);
                        button_play.setEnabled(true);
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

        if (data.getCharacter_class().equals("swordsman")) {

            characterCardViewHolder.character_card_layout.setBackgroundResource(R.drawable.choose_character_swordsman_card);
            characterCardViewHolder.character_card_image.setImageResource(R.drawable.sword);
            characterCardViewHolder.character_card_image.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorSword)));
            characterCardViewHolder.character_card_name.setTextColor(ContextCompat.getColor(context, R.color.colorSword));
            characterCardViewHolder.character_card_level.setTextColor(ContextCompat.getColor(context, R.color.colorSword));
            characterCardViewHolder.character_card_subclass.setTextColor(ContextCompat.getColor(context, R.color.colorSword));
            characterCardViewHolder.character_card_class.setTextColor(ContextCompat.getColor(context, R.color.colorSword));
        }
        else if (data.getCharacter_class().equals("sorcerer")) {

            characterCardViewHolder.character_card_layout.setBackgroundResource(R.drawable.choose_character_sorcerer_card);
            characterCardViewHolder.character_card_image.setImageResource(R.drawable.magic_hat);
            characterCardViewHolder.character_card_image.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorHat)));
            characterCardViewHolder.character_card_name.setTextColor(ContextCompat.getColor(context, R.color.colorHat));
            characterCardViewHolder.character_card_level.setTextColor(ContextCompat.getColor(context, R.color.colorHat));
            characterCardViewHolder.character_card_subclass.setTextColor(ContextCompat.getColor(context, R.color.colorHat));
            characterCardViewHolder.character_card_class.setTextColor(ContextCompat.getColor(context, R.color.colorHat));
        }
        else if (data.getCharacter_class().equals("rogue")) {

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
