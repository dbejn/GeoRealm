package com.example.georealm.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.georealm.R;
import com.example.georealm.data.CharacterData;
import com.example.georealm.helper.Constants;

public class SkillCardDataAdapter extends RecyclerView.Adapter<SkillCardDataAdapter.SkillCardViewHolder> {

    private String[] skill_names;
    private String[] skill_descriptions;
    private String[] skill_stats;
    private CharacterData character;
    private Context context;
    private int selected;
    private Button button_level_up_skill;
    private SkillCardDataAdapter.SkillCardViewHolder selected_holder;

    public SkillCardDataAdapter(Context ctx, Button level_up,
                                String[] names, String[] descs, String[] stats, CharacterData chara) {

        context = ctx;
        selected = -1;
        button_level_up_skill = level_up;
        skill_names = names;
        skill_descriptions = descs;
        skill_stats = stats;
        character = chara;
    }

    private void deselectCurrentLayout() {

        if (selected_holder != null) {

            selected_holder.layout_skill_card.setForeground(null);
        }
    }

    private void setUpColors(SkillCardViewHolder holder, int color) {

        holder.skill_name.setTextColor(ContextCompat.getColor(context, color));
        holder.skill_status.setTextColor(ContextCompat.getColor(context, color));
        holder.skill_description.setTextColor(ContextCompat.getColor(context, color));
        holder.skill_stats.setTextColor(ContextCompat.getColor(context, color));
    }

    public static class SkillCardViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout layout_skill_card;// background, foreground
        public RelativeLayout layout_skill_description;
        public TextView skill_name;// colour
        public TextView skill_status;// colour
        public TextView skill_description;// text, colour
        public TextView skill_stats;// text, colour

        public SkillCardViewHolder(View card_view) {

            super(card_view);

            layout_skill_card = card_view.findViewById(R.id.layout_skill_card);
            layout_skill_description = card_view.findViewById(R.id.layout_skill_description);
            skill_name = card_view.findViewById(R.id.skill_name);
            skill_status = card_view.findViewById(R.id.skill_status);
            skill_description = card_view.findViewById(R.id.skill_description);
            skill_stats = card_view.findViewById(R.id.skill_stats);
        }
    }

    @NonNull
    @Override
    public SkillCardDataAdapter.SkillCardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View card_view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_skill_card, viewGroup, false);

        return new SkillCardViewHolder(card_view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SkillCardDataAdapter.SkillCardViewHolder skillCardViewHolder, int i) {

        final int position = i;

        skillCardViewHolder.skill_name.setText(skill_names[i]);
        skillCardViewHolder.skill_description.setText(skill_descriptions[i]);
        skillCardViewHolder.skill_stats.setText(skill_stats[i]);

        switch (character.getCharacter_class()) {

            case Constants.SWORDSMAN:
                setUpColors(skillCardViewHolder, R.color.colorSword);
                skillCardViewHolder.layout_skill_card.setBackgroundResource(R.drawable.choose_character_swordsman_card);
                break;
            case Constants.SORCERER:
                setUpColors(skillCardViewHolder, R.color.colorHat);
                skillCardViewHolder.layout_skill_card.setBackgroundResource(R.drawable.choose_character_sorcerer_card);
                break;
            case Constants.ROGUE:
                setUpColors(skillCardViewHolder, R.color.colorDagger);
                skillCardViewHolder.layout_skill_card.setBackgroundResource(R.drawable.choose_character_rogue_card);
                break;
        }

        if (i <= 2 * character.getCharacter_level()) {

            if (i == 0) {

                skillCardViewHolder.skill_status.setText(context.getString(R.string.learned));
            }
            else {

                if (character.getSkills().contains(i)) {

                    skillCardViewHolder.skill_status.setText(context.getString(R.string.learned));
                }
                else if ((i % 2 == 1 && character.getSkills().contains(i + 1)) || (i % 2 == 0 && character.getSkills().contains(i - 1))) {

                    skillCardViewHolder.skill_status.setText(context.getString(R.string.unavailable));
                    skillCardViewHolder.layout_skill_card.setBackgroundResource(R.drawable.all_rounded_rec_disabled);
                }
                else {

                    skillCardViewHolder.skill_status.setText(context.getString(R.string.available));
                    skillCardViewHolder.layout_skill_card.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (selected != position) {

                                deselectCurrentLayout();
                                skillCardViewHolder.layout_skill_card.setForeground(context.getDrawable(R.drawable.select));
                                selected_holder = skillCardViewHolder;

                                if (selected == -1) {

                                    button_level_up_skill.setBackgroundResource(R.drawable.top_rounded_rec_trans);
                                    button_level_up_skill.setEnabled(true);
                                    button_level_up_skill.setTag(position);
                                }

                                selected = position;
                            }
                        }
                    });
                }
            }
        }
        else {

            skillCardViewHolder.skill_status.setText(context.getString(R.string.level_needed, Math.round(i / 2.0f) + (i - 1) / 2));
            skillCardViewHolder.layout_skill_card.setBackgroundResource(R.drawable.all_rounded_rec_disabled);
        }
    }

    @Override
    public int getItemCount() {

        return skill_names.length;
    }
}
