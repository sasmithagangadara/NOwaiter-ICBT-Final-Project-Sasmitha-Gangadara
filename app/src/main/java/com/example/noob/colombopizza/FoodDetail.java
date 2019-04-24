package com.example.noob.colombopizza;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.noob.colombopizza.Database.Database;
import com.example.noob.colombopizza.Model.Food;
import com.example.noob.colombopizza.Model.Order;
import com.example.noob.colombopizza.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.ThreeBounce;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;

public class FoodDetail extends AppCompatActivity {
    TextView food_name, food_price, food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart;
    ElegantNumberButton numberButton;
    FirebaseDatabase database;
    DatabaseReference foods;
    Food currentFood;
    public Sprite ThreeBounce;
    public ProgressBar progressBar;

    String foodId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");

        //Init View
        numberButton = (ElegantNumberButton) findViewById(R.id.number_button);
        btnCart = (FloatingActionButton) findViewById(R.id.btnCart);

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new MaterialStyledDialog.Builder(FoodDetail.this)
                        .withDialogAnimation(true)
                        .setTitle("Add to cart !")
                        .setDescription("You Ordered "+numberButton.getNumber()+" "+currentFood.getName()+". \n you want to add this items to cart ?")
                        .withDarkerOverlay(false)
                        .setCancelable(false)
                        .withIconAnimation(true)
                        .setHeaderDrawable(R.drawable.logo)
                        .setPositive("Ok", new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {

                                new Database(getBaseContext()).addToCart(new Order(
                                        foodId,
                                        currentFood.getName(),
                                        numberButton.getNumber(),
                                        currentFood.getPrice(),
                                        currentFood.getDiscount()
                                ));


                                Intent i = new Intent(FoodDetail.this, Cart.class);
                                startActivity(i);
                            }
                        })
                        .setNegative("Cancel", new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                finish();
                            }
                        })
                        .show();

//                new Database(getBaseContext()).addToCart(new Order(
//                        foodId,
//                        currentFood.getName(),
//                        numberButton.getNumber(),
//                        currentFood.getPrice(),
//                        currentFood.getDiscount()
//                ));


            }
        });

        food_description = (TextView) findViewById(R.id.food_description);
        food_name = (TextView) findViewById(R.id.food_name);
        food_price = (TextView) findViewById(R.id.food_price);
        food_image = (ImageView) findViewById(R.id.img_food);
        progressBar = (ProgressBar)findViewById(R.id.spin_kit);
        ThreeBounce = new ThreeBounce();
        progressBar.setIndeterminateDrawable(ThreeBounce);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //Get food ID from Intent
        if(getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
        if(!foodId.isEmpty()){
            getDetailFood(foodId);
        }

    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                ThreeBounce.start();
                currentFood = dataSnapshot.getValue(Food.class);

                //Set Image
                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_image, new Callback() {
                    @Override
                    public void onSuccess() {
                        ThreeBounce.stop();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {

                    }
                });

                collapsingToolbarLayout.setTitle(currentFood.getName());

                food_price.setText(currentFood.getPrice());

                food_name.setText(currentFood.getName());

                food_description.setText(currentFood.getDescription());

            }

            @Override
            public void onCancelled(DatabaseError databaseError){

            }
        });

    }
}
