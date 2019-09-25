package com.example.georealm.adapters;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.georealm.R;
import com.example.georealm.helper.Constants;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

import static com.example.georealm.helper.Constants.ROGUE;
import static com.example.georealm.helper.Constants.SORCERER;
import static com.example.georealm.helper.Constants.SWORDSMAN;

public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;

    public MarkerInfoWindowAdapter(Activity ctx) {

        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {

        if (marker.getTitle().equals("gem")) {

            return context.getLayoutInflater().inflate(R.layout.layout_marker_gem, null);
        }
        else if (marker.getTitle().equals("shop")) {

            return context.getLayoutInflater().inflate(R.layout.layout_marker_shop, null);
        }
        else if (marker.getTitle().equals("character")) {

            HashMap character = (HashMap) marker.getTag();
            String name = character.get("name").toString();
            int subclass = Integer.parseInt(character.get("subclass").toString());
            int level = Integer.parseInt(character.get("level").toString());
            String username = character.get("username").toString();

            View info_window = context.getLayoutInflater().inflate(R.layout.layout_marker_character, null);

            ImageView character_icon = info_window.findViewById(R.id.character_icon);
            TextView character_name = info_window.findViewById(R.id.character_name);
            TextView character_info = info_window.findViewById(R.id.character_info);
            TextView username_text = info_window.findViewById(R.id.username);

            String subcl = "";
            int image = -1;

            switch (subclass) {

                case Constants.BERSERKER:
                    subcl = "berserker";
                    image = R.drawable.ic_sword;
                    break;
                case Constants.PALADIN:
                    subcl = "paladin";
                    image = R.drawable.ic_sword;
                    break;
                case Constants.PYROMANCER:
                    subcl = "pyromancer";
                    image = R.drawable.ic_hat;
                    break;
                case Constants.ICEBOUND:
                    subcl = "icebound";
                    image = R.drawable.ic_hat;
                    break;
                case Constants.ASSASSIN:
                    subcl = "assassin";
                    image = R.drawable.ic_dagger;
                    break;
                case Constants.SHADOW:
                    subcl = "shadow";
                    image = R.drawable.ic_dagger;
                    break;
            }

            character_icon.setImageResource(image);
            character_name.setText(name);
            character_info.setText(context.getString(R.string.name_level, subcl, level));
            username_text.setText(username);

            return info_window;
        }

        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        return null;
    }
}
