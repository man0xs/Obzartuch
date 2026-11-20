package com.example.obzartuch;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FoodRepository {

    public static List<FoodItem> loadFoods(Context context) {
        List<FoodItem> foodList = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("foods.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.getString("name");
                String iconName = obj.getString("icon");
                int protein = obj.getInt("protein");
                int kcal = obj.getInt("kcal");
                int sugar = obj.getInt("sugar");
                boolean healthy = obj.optBoolean("healthy", true);

                int iconResId = context.getResources().getIdentifier(
                        iconName,
                        "drawable",
                        context.getPackageName()
                );

                if (iconResId != 0) {
                    foodList.add(new FoodItem(name, iconResId, protein, kcal, sugar, healthy));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return foodList;
    }
}