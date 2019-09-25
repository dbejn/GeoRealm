package com.example.georealm.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.georealm.R;
import com.example.georealm.data.CharacterData;

public class ItemCardDataAdapter extends RecyclerView.Adapter<ItemCardDataAdapter.ItemBuyCardViewHolder> {

    private CharacterData character;
    private SeekBar amount_slider;
    private TextView amount_text;
    private Button button_make_sale;
    private Context context;
    private boolean is_buy;
    private int selected;
    private ItemCardDataAdapter.ItemBuyCardViewHolder selected_holder;

    public ItemCardDataAdapter(CharacterData chara, SeekBar bar, TextView amount, Context ctx,
                               Button make_sale, boolean buyuy) {

        character = chara;
        amount_slider = bar;
        amount_text = amount;
        button_make_sale = make_sale;
        context = ctx;
        is_buy = buyuy;
        selected = -1;
    }

    private void setUpColors(ItemBuyCardViewHolder holder, int color) {

        holder.item_name.setTextColor(ContextCompat.getColor(context, color));
        holder.item_count.setTextColor(ContextCompat.getColor(context, color));
        holder.item_description.setTextColor(ContextCompat.getColor(context, color));
        holder.item_icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, color)));
    }

    public void updateCharacter(CharacterData chara) {

        character = chara;
    }

    public void deselectCurrentLayout() {

        if (selected_holder != null) {

            selected_holder.layout_item.setForeground(null);
            selected_holder = null;
            selected = -1;
        }
    }

    public static class ItemBuyCardViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout layout_item;// foreground
        public TextView item_name;// text, colour
        public TextView item_description;// text, colour
        public ImageView item_icon;// colour
        public TextView item_count;// colour

        public ItemBuyCardViewHolder(View card_view) {

            super(card_view);

            layout_item = card_view.findViewById(R.id.layout_item);
            item_name = card_view.findViewById(R.id.item_name);
            item_description = card_view.findViewById(R.id.item_desc);
            item_icon = card_view.findViewById(R.id.item_icon);
            item_count = card_view.findViewById(R.id.item_count);
        }
    }

    @NonNull
    @Override
    public ItemCardDataAdapter.ItemBuyCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View card_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_card, parent, false);

        return new ItemBuyCardViewHolder(card_view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemCardDataAdapter.ItemBuyCardViewHolder holder, final int position) {

        int color = R.color.colorDisabled;

        if (position == 0){ // health

            holder.item_name.setText(context.getString(R.string.health_potion));
            holder.item_description.setText(context.getString(R.string.health_potion_desc));
            holder.item_icon.setImageResource(R.drawable.ic_potion);
            holder.layout_item.setTag(0);
            color = R.color.colorHealth;
        }
        else if (position == 1) { // mana

            holder.item_name.setText(context.getString(R.string.mana_potion));
            holder.item_description.setText(context.getString(R.string.mana_potion_desc));
            holder.item_icon.setImageResource(R.drawable.ic_potion);
            holder.layout_item.setTag(1);
            color = R.color.colorMana;
        }
        else if (position == 2) { // stamina

            holder.item_name.setText(context.getString(R.string.stamina_potion));
            holder.item_description.setText(context.getString(R.string.stamina_potion_desc));
            holder.item_icon.setImageResource(R.drawable.ic_potion);
            holder.layout_item.setTag(2);
            color = R.color.colorStamina;
        }
        else { // items

            holder.item_name.setText(context.getString(R.string.items));
            holder.item_description.setText(context.getString(R.string.items));
            holder.item_icon.setImageResource(R.drawable.ic_diamond);
            holder.layout_item.setTag(position);
        }

        if (position < 3 && !is_buy)
            holder.item_count.setText(context.getString(R.string.brackets_integer, character.getItems().get(position)));
        else
            holder.item_count.setText(null);
        setUpColors(holder, color);

        holder.layout_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selected != position) {

                    deselectCurrentLayout();
                    selected = position;
                    holder.layout_item.setForeground(context.getDrawable(R.drawable.select));
                    selected_holder = holder;

                    updateAmountSlider(Integer.parseInt(holder.layout_item.getTag().toString()));

                    button_make_sale.setBackgroundResource(R.drawable.top_rounded_rec_trans);
                    button_make_sale.setEnabled(true);
                    button_make_sale.setTag(selected);
                }
            }
        });
    }

    @Override
    public int getItemCount() {

        return 3;
    }

    private void updateAmountSlider(int item_tag) {

        // POTIONS
        int num_of_potions;
        int num_of_allowed;

        if (is_buy) {

            num_of_potions = character.getItems().get(item_tag);
            num_of_allowed = 3 - num_of_potions;

            button_make_sale.setText(context.getString(R.string.buy_cost, 0));
        }
        else {

            num_of_potions = character.getItems().get(item_tag);
            num_of_allowed = num_of_potions;

            button_make_sale.setText(context.getString(R.string.sell_cost, 0));
        }

        amount_text.setText(context.getString(R.string.integer, 0));
        amount_slider.setProgress(0);
        amount_slider.setMax(num_of_allowed);


        // ITEMS...
    }
}
