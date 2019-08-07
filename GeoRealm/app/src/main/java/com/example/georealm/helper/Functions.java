package com.example.georealm.helper;

import java.util.ArrayList;

public class Functions {

    public static boolean arrayContains(ArrayList<Long> array, int value) {

        if (array != null) {

            for (int i = 0; i < array.size(); i++) {

                if (array.get(i) == Long.parseLong(Integer.toString(value))) {

                    return true;
                }
            }

            return false;
        }
        else {

            return false;
        }
    }
}
