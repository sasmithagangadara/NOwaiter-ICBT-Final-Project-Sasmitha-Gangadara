package com.example.noob.colombopizza;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.noob.colombopizza.Common.Common;
import com.example.noob.colombopizza.Database.Database;
import com.example.noob.colombopizza.Model.Food;
import com.example.noob.colombopizza.Model.Order;
import com.example.noob.colombopizza.adapters.chatAdapter;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapzen.speakerbox.Speakerbox;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BotActivity extends AppCompatActivity implements AIListener {

    private static final String TAG ="BotActivity";
    private Button btnRecord;
//    private TextView tvShow;
    private ListView chatListView;

    private static final int RECORD_REQUEST_CODE = 101;
    private static final int INTERNET_REQUEST_CODE = 101;

    String speechofbot = "";

    FirebaseDatabase database;
    DatabaseReference foods;
    Food currentFood;

    String foodId = "";
    String food_type = "";
    String count = "";
    String topping = "";
    String action = "";

    ArrayList<String> botList = new ArrayList<>();
    ArrayList<String> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");

        btnRecord = findViewById(R.id.btn_record);
//        tvShow = findViewById(R.id.tv_botAnswer);
        chatListView = findViewById(R.id.botList);

        System.out.println("beverages -------->   "+foods.child("beverage1"));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(BotActivity.this,Cart.class);
                startActivity(cartIntent);

            }
        });

        int permission = ContextCompat.checkSelfPermission(BotActivity.this, Manifest.permission.RECORD_AUDIO);
        int permission2 = ContextCompat.checkSelfPermission(BotActivity.this, Manifest.permission.INTERNET);

        final AIConfiguration config = new AIConfiguration(Common.API_CLIENT,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        final AIService aiService = AIService.getService(BotActivity.this, config);
        aiService.setListener(BotActivity.this);

        if (permission != PackageManager.PERMISSION_GRANTED ) {
            Toast.makeText(this, "Permission to record denied", Toast.LENGTH_SHORT).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission to access the microphone is required for this app to record audio.")
                        .setTitle("Permission required");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
//                        Log.i(TAG, "Clicked");
                        makeRequest();

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                makeRequest();
            }
        }else {
            promptSpeechInput();
        }



        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });
    }

    private void promptSpeechInput() {

        try{

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Something" );

            startActivityForResult(intent, RECORD_REQUEST_CODE);
        }catch (Exception e){
            Toast.makeText(this, "error-->"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case RECORD_REQUEST_CODE:{
                if (resultCode == RESULT_OK && null != data ){

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String userQ = result.get(0);

                    new SendText(userQ).execute();

                }
            }
            break;
        }
    }

    @Override
    public void onResult(AIResponse result) {

//        tvShow.setText("user said  -- > "+result.getResult().getResolvedQuery() +"  \n   " +
//                "result --> " +result.getResult().getFulfillment().getSpeech());

    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO },
                RECORD_REQUEST_CODE);

    }

    class SendText extends AsyncTask<Void, String, Void> {
        //ReturnDataObjectServerCallManager returnDataObjectServerCallManager = new ReturnDataObjectServerCallManager();
        String url;
        String responseData;
        int statusCode;
        JSONObject returnData;

        private final String quary;
        HashMap<String, String> map;
        SharedPreferences preferences;
        String resolvedQuery;
        String speech;

        SendText(String quary) {
            this.quary = quary;
        }

        @Override
        protected void onPreExecute() {

//            url = Common.BASE_URL+"query?v=20150910";
            url = "https://api.api.ai/v1/query?query="+quary+"&lang=en&sessionId=12345&timezone=Asia/Colombo";
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                OkHttpClient client = new OkHttpClient();
                FormBody formBody = new FormBody.Builder()
                        .add("query", quary)
                        .add("lang", "en")
                        .add("sessionId", "12345")
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .header("Authorization", "Bearer "+Common.API_CLIENT)
                        .header("Content-Type", "application/json")
                        .get()
                        .build();


                Response response = client.newCall(request).execute();
                statusCode = response.code();
                responseData = response.body().string();
                publishProgress(responseData);

//                Log.d(TAG, " SendText: response body --> " + responseData);
                Log.d(TAG, " SendText: response code --> " + statusCode);

            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (statusCode == 200) {
                try {
                    returnData = new JSONObject(responseData);
                    JSONObject jsonObject = returnData.getJSONObject("result");
                    Log.d(TAG, " SendText: response body --> " + jsonObject.toString());

                    resolvedQuery = jsonObject.getString("resolvedQuery");
                    speech = jsonObject.getString("speech");

                    if (jsonObject.getString("action").equals("orderpizza")){
                        action = jsonObject.getString("action");
                        if (!jsonObject.isNull("parameters")){
                            JSONObject paramObj = jsonObject.getJSONObject("parameters");
                            if (paramObj.length() != 0){
//                                food_type = paramObj.getString("category");
                                count = paramObj.getString("count");
                                topping = paramObj.getString("topping");
                            }
                        }
                    }else if(jsonObject.getString("action").equals("orderbeverage")){
                        action = jsonObject.getString("action");
                        if (!jsonObject.isNull("parameters")){
                            JSONObject paramObj = jsonObject.getJSONObject("parameters");
                            if (paramObj.length() != 0){
                                count = paramObj.getString("count");
                                topping = paramObj.getString("drinks");
                            }
                        }
                    }else if(jsonObject.getString("action").equals("orderappetizer")){
                        action = jsonObject.getString("action");
                        if (!jsonObject.isNull("parameters")){
                            JSONObject paramObj = jsonObject.getJSONObject("parameters");
                            if (paramObj.length() != 0){
                                count = paramObj.getString("count");
                                topping = paramObj.getString("appetizer-type");
                            }
                        }
                    }else if(jsonObject.getString("action").equals("showmenu")){
                        JSONObject paramObj = jsonObject.getJSONObject("parameters");
                        if (paramObj.length() != 0){
                            food_type = paramObj.getString("category");
                            showmenu();
                        }

                    }else if(jsonObject.getString("action").equals("showcart")){
                        showcart();
                    }else if(jsonObject.getString("action").equals("logout")){
                        Intent signIn = new Intent(BotActivity.this, MainActivity.class);
                        signIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(signIn);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(BotActivity.this, "API -> "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(BotActivity.this, "API Failed !!", Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        protected void onPostExecute(Void result) {

            Speakerbox speakerbox = new Speakerbox(getApplication());
            speakerbox.play(speech);

            speechofbot = speechofbot+"User --> "+resolvedQuery+" \n bot --> "+speech+"   \n";
//            tvShow.setText(speechofbot);
            botList.add(speech);
            userList.add(resolvedQuery);

            chatAdapter adapter = new chatAdapter(BotActivity.this, botList, userList);
            chatListView.setAdapter(adapter);

            if(action.equals("orderpizza")){
                if ( !count.equals("") && !topping.equals("")){
//                foodId = "pizza2";
                    food_type = "pizza";
                    foodId = topping;
                    getDetailFood(foodId);

                }
            }else if(action.equals("orderbeverage")){
                if (!count.equals("") && !topping.equals("")){
//                foodId = "pizza2";
                    foodId = topping;
                    getDetailFood(foodId);

                }

            }else if(action.equals("orderappetizer")){
                if (!count.equals("") && !topping.equals("")){
//                foodId = "pizza2";
                    foodId = topping;
                    getDetailFood(foodId);

                }

            }


        }
    }

    private void showcart() {

        Intent cartIntent = new Intent(BotActivity.this, Cart.class);
        startActivity(cartIntent);

    }

    private void showmenu() {

        if (food_type.equals("pizza")){
            //Get CategoryId and send to new Activity
            Intent foodList = new Intent(BotActivity.this, FoodList.class);
            //Because CategoryId is key, so we just get the key of this item
            foodList.putExtra("CategoryId", "pizza");
            startActivity(foodList);
        }else if(food_type.equals("appetizers")) {
            //Get CategoryId and send to new Activity
            Intent foodList = new Intent(BotActivity.this, FoodList.class);
            //Because CategoryId is key, so we just get the key of this item
            foodList.putExtra("CategoryId", "appetizers");
            startActivity(foodList);
        }else if(food_type.equals("beverages")) {
            //Get CategoryId and send to new Activity
            Intent foodList = new Intent(BotActivity.this, FoodList.class);
            //Because CategoryId is key, so we just get the key of this item
            foodList.putExtra("CategoryId", "beverages");
            startActivity(foodList);
        }
//        appertizers
//        beverages

    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                currentFood = dataSnapshot.getValue(Food.class);
                //Set Image
//                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_image);
//                collapsingToolbarLayout.setTitle(currentFood.getName());
//                food_price.setText(currentFood.getPrice());
//                food_name.setText(currentFood.getName());
//                food_description.setText(currentFood.getDescription());
                addToCart();
            }
            @Override
            public void onCancelled(DatabaseError databaseError){
            }
        });
    }

    private void addToCart(){

        new MaterialStyledDialog.Builder(BotActivity.this)
                .withDialogAnimation(true)
                .setTitle("Add to cart !")
                .setDescription("You Ordered "+count+" "+topping+" "+food_type+". \n you want to add this items to cart ?")
                .withDarkerOverlay(false)
                .setCancelable(false)
                .withIconAnimation(true)
                .setHeaderDrawable(R.drawable.logo)
                .setPositive("Add More", new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {

                        new Database(getBaseContext()).addToCart(new Order(
                                foodId,
                                currentFood.getName(),
                                count,
                                currentFood.getPrice(),
                                currentFood.getDiscount()
                        ));

                        count = "";
                        topping = "";
                        food_type = "";


                        dialog.dismiss();


//                        Intent i = new Intent(BotActivity.this, Cart.class);
//                        startActivity(i);
                    }
                })
                .setNegative("Finish Order", new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        new Database(getBaseContext()).addToCart(new Order(
                                foodId,
                                currentFood.getName(),
                                count,
                                currentFood.getPrice(),
                                currentFood.getDiscount()
                        ));

                        count = "";
                        topping = "";
                        food_type = "";

                        Intent i = new Intent(BotActivity.this, Cart.class);
                        startActivity(i);
                    }
                })
                .show();

    }
}
