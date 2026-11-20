package com.example.obzartuch;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private static final int MAX_FOODS_PER_ROUND = 5;
    private static final int MAX_SUGAR_LIMIT = 25;

    private TextView tvRoundInfo;
    private TextView tvProtein;
    private TextView tvKcal;
    private TextView tvSugar;
    private FrameLayout gameArea;
    private ImageView ivObzartuch;
    private ImageView ivSelectedFood;
    private Drawable idleDrawable;
    private Drawable biteDrawable;

    private List<FoodItem> availableFoods;
    private FoodItem currentFood;

    private int roundNumber = 1;
    private int targetProtein;
    private int targetKcal;
    private int eatenProtein;
    private int eatenKcal;
    private int eatenSugar;
    private int foodsEatenInRound;
    private int unhealthyItemsEaten;
    private final List<String> eatenFoodNames = new ArrayList<>();
    private final Random random = new Random();

    private float dX;
    private float dY;

    private final Handler chewHandler = new Handler(Looper.getMainLooper());
    private int chewStep = 0;
    private Runnable chewRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        CardView btnMenu = findViewById(R.id.btn_menu);
        CardView btnFoodList = findViewById(R.id.btn_food_list);
        CardView btnNext = findViewById(R.id.btn_next);

        tvRoundInfo = findViewById(R.id.tv_round_info);
        tvProtein = findViewById(R.id.tv_protein);
        tvKcal = findViewById(R.id.tv_kcal);
        tvSugar = findViewById(R.id.tv_sugar);
        gameArea = findViewById(R.id.game_area);
        ivObzartuch = findViewById(R.id.iv_obzartuch);
        ivSelectedFood = findViewById(R.id.iv_selected_food);

        idleDrawable = prepareSharpDrawable(R.drawable.obzartuch);
        biteDrawable = prepareSharpDrawable(R.drawable.obzartuch_bite);
        setIdleState();

        availableFoods = FoodRepository.loadFoods(this);
        setupFoodDragListener();
        startNewRound();

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnFoodList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (foodsEatenInRound >= MAX_FOODS_PER_ROUND) {
                    Toast.makeText(
                            GameActivity.this,
                            "Osiągnięto limit 5 potraw! Kliknij Dalej.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
                showFoodListDialog();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRoundSummaryDialog();
            }
        });
    }

    private void startNewRound() {
        eatenProtein = 0;
        eatenKcal = 0;
        eatenSugar = 0;
        foodsEatenInRound = 0;
        unhealthyItemsEaten = 0;
        eatenFoodNames.clear();

        ivSelectedFood.setVisibility(View.GONE);
        ivSelectedFood.setTranslationX(0f);
        ivSelectedFood.setTranslationY(0f);
        currentFood = null;

        generateAchievableRequirements();
        updateRequirementsUI();
    }

    private void generateAchievableRequirements() {
        if (availableFoods == null || availableFoods.isEmpty()) {
            targetProtein = 40;
            targetKcal = 600;
            return;
        }

        List<FoodItem> pool = new ArrayList<>(availableFoods);
        Collections.shuffle(pool, random);

        int sampleSize = 3 + random.nextInt(3);
        int selectedCount = 0;
        int sumProtein = 0;
        int sumKcal = 0;
        int sumSugar = 0;

        for (FoodItem item : pool) {
            if (selectedCount >= sampleSize) {
                break;
            }
            if (sumSugar + item.getSugar() <= MAX_SUGAR_LIMIT) {
                sumProtein += item.getProtein();
                sumKcal += item.getKcal();
                sumSugar += item.getSugar();
                selectedCount++;
            }
        }

        if (selectedCount == 0) {
            targetProtein = 30;
            targetKcal = 450;
        } else {
            targetProtein = Math.max(15, sumProtein);
            targetKcal = Math.max(250, sumKcal);
        }
    }

    private void showFoodListDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_food_list);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        RecyclerView rvFoods = dialog.findViewById(R.id.rv_foods);
        CardView btnClose = dialog.findViewById(R.id.btn_close_dialog);

        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        FoodAdapter adapter = new FoodAdapter(this, availableFoods, new FoodAdapter.OnFoodSelectedListener() {
            @Override
            public void onFoodSelected(FoodItem food) {
                spawnSelectedFood(food);
                dialog.dismiss();
            }
        });
        rvFoods.setAdapter(adapter);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void spawnSelectedFood(FoodItem food) {
        if (foodsEatenInRound >= MAX_FOODS_PER_ROUND) {
            return;
        }
        currentFood = food;
        Drawable foodDrawable = prepareSharpDrawable(food.getIconResId());
        ivSelectedFood.setImageDrawable(foodDrawable);
        ivSelectedFood.setTranslationX(0f);
        ivSelectedFood.setTranslationY(0f);
        ivSelectedFood.setVisibility(View.VISIBLE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupFoodDragListener() {
        ivSelectedFood.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (currentFood == null) {
                    return false;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        float maxX = gameArea.getWidth() - view.getWidth();
                        float maxY = gameArea.getHeight() - view.getHeight();

                        newX = Math.max(0f, Math.min(newX, maxX));
                        newY = Math.max(0f, Math.min(newY, maxY));

                        view.setX(newX);
                        view.setY(newY);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (isFoodOverMonster()) {
                            feedMonster();
                        } else {
                            view.animate()
                                    .translationX(0f)
                                    .translationY(0f)
                                    .setDuration(200)
                                    .start();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    private boolean isFoodOverMonster() {
        float foodCenterX = ivSelectedFood.getX() + ivSelectedFood.getWidth() / 2f;
        float foodCenterY = ivSelectedFood.getY() + ivSelectedFood.getHeight() / 2f;

        float monsterCenterX = ivObzartuch.getX() + ivObzartuch.getWidth() / 2f;
        float monsterCenterY = ivObzartuch.getY() + ivObzartuch.getHeight() / 2f;

        float dx = foodCenterX - monsterCenterX;
        float dy = foodCenterY - monsterCenterY;
        double distance = Math.hypot(dx, dy);

        float eatRadius = ivObzartuch.getWidth() * 0.35f;
        return distance <= eatRadius;
    }

    private void feedMonster() {
        if (currentFood == null || foodsEatenInRound >= MAX_FOODS_PER_ROUND) {
            return;
        }

        eatenProtein += currentFood.getProtein();
        eatenKcal += currentFood.getKcal();
        eatenSugar += currentFood.getSugar();
        foodsEatenInRound++;
        eatenFoodNames.add(currentFood.getName());

        if (!currentFood.isHealthy()) {
            unhealthyItemsEaten++;
        }

        updateRequirementsUI();

        ivSelectedFood.setVisibility(View.GONE);
        ivSelectedFood.setTranslationX(0f);
        ivSelectedFood.setTranslationY(0f);
        currentFood = null;

        startChewingAnimation();
    }

    @SuppressLint("SetTextI18n")
    private void showRoundSummaryDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_round_summary);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView tvTitle = dialog.findViewById(R.id.tv_summary_title);
        TextView tvBody = dialog.findViewById(R.id.tv_summary_body);
        CardView btnConfirm = dialog.findViewById(R.id.btn_next_round_confirm);

        tvTitle.setText("Podsumowanie rundy " + roundNumber);

        boolean proteinMet = eatenProtein >= targetProtein;
        boolean kcalMet = eatenKcal >= targetKcal;
        boolean sugarExceeded = eatenSugar > MAX_SUGAR_LIMIT;

        StringBuilder sb = new StringBuilder();
        sb.append("Podane potrawy (").append(foodsEatenInRound).append("/").append(MAX_FOODS_PER_ROUND).append("): ");
        if (eatenFoodNames.isEmpty()) {
            sb.append("brak\n\n");
        } else {
            sb.append(String.join(", ", eatenFoodNames)).append("\n\n");
        }

        sb.append("• Białko: ").append(eatenProtein).append("g / ").append(targetProtein).append("g ")
                .append(proteinMet ? "(OK)\n" : "(Za mało)\n");
        sb.append("• Energia: ").append(eatenKcal).append(" / ").append(targetKcal).append(" kcal ")
                .append(kcalMet ? "(OK)\n" : "(Za mało)\n");
        sb.append("• Cukier: ").append(eatenSugar).append("g / ").append(MAX_SUGAR_LIMIT).append("g ")
                .append(sugarExceeded ? "(Przekroczono!)\n\n" : "(OK)\n\n");

        if (sugarExceeded) {
            sb.append("Uwaga! Obżartuch zjadł za dużo cukru (powyżej 25g). Nadmiar cukru szkodzi zdrowiu i powoduje tycie!");
        } else if (unhealthyItemsEaten >= 2) {
            sb.append("Podano sporo niezdrowych przekąsek. Staraj się częściej wybierać warzywa, owoce i pełnowartościowe posiłki.");
        } else if (proteinMet && kcalMet) {
            sb.append("Świetny wybór! Zapotrzebowanie zostało spełnione, a cukier pozostał w bezpiecznej normie.");
        } else {
            sb.append("Obżartuch nie otrzymał wszystkich potrzebnych składników. W kolejnej rundzie dobierz bardziej odżywcze produkty.");
        }

        tvBody.setText(sb.toString());

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                roundNumber++;
                startNewRound();
            }
        });

        dialog.show();
    }

    private void startChewingAnimation() {
        if (chewRunnable != null) {
            chewHandler.removeCallbacks(chewRunnable);
        }

        chewStep = 0;
        chewRunnable = new Runnable() {
            @Override
            public void run() {
                if (chewStep < 6) {
                    if (chewStep % 2 == 0) {
                        setBiteState();
                    } else {
                        setIdleState();
                    }
                    chewStep++;
                    chewHandler.postDelayed(this, 160);
                } else {
                    setIdleState();
                }
            }
        };

        chewHandler.post(chewRunnable);
    }

    private void updateRequirementsUI() {
        int remainingProtein = Math.max(0, targetProtein - eatenProtein);
        int remainingKcal = Math.max(0, targetKcal - eatenKcal);

        tvRoundInfo.setText("Runda " + roundNumber + "  |  Potrawy: " + foodsEatenInRound + "/" + MAX_FOODS_PER_ROUND);
        tvProtein.setText("Białko: " + remainingProtein + "g");
        tvKcal.setText("Kcal: " + remainingKcal);
        tvSugar.setText("Cukier: " + eatenSugar + "/" + MAX_SUGAR_LIMIT + "g");

        if (eatenSugar > MAX_SUGAR_LIMIT) {
            tvSugar.setTextColor(Color.parseColor("#C0392B"));
        } else {
            tvSugar.setTextColor(Color.parseColor("#B8860B"));
        }
    }

    private Drawable prepareSharpDrawable(int drawableResId) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableResId);
        if (drawable != null) {
            drawable = drawable.mutate();
            drawable.setFilterBitmap(false);
        }
        return drawable;
    }

    public void setIdleState() {
        if (idleDrawable != null) {
            ivObzartuch.setImageDrawable(idleDrawable);
        }
    }

    public void setBiteState() {
        if (biteDrawable != null) {
            ivObzartuch.setImageDrawable(biteDrawable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chewRunnable != null) {
            chewHandler.removeCallbacks(chewRunnable);
        }
    }
}