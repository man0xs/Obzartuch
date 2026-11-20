package com.example.obzartuch;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface OnFoodSelectedListener {
        void onFoodSelected(FoodItem food);
    }

    private final Context context;
    private final List<FoodItem> foodList;
    private final OnFoodSelectedListener listener;

    public FoodAdapter(Context context, List<FoodItem> foodList, OnFoodSelectedListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = foodList.get(position);
        holder.tvName.setText(item.getName());

        Drawable drawable = ContextCompat.getDrawable(context, item.getIconResId());
        if (drawable != null) {
            drawable = drawable.mutate();
            drawable.setFilterBitmap(false);
            holder.ivIcon.setImageDrawable(drawable);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFoodSelected(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_food_icon);
            tvName = itemView.findViewById(R.id.tv_food_name);
        }
    }
}