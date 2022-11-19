package com.kofikay.pqfreiburg13;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.kofikay.pqfreiburg13.Adapter.MessageRVAdapter;

import org.json.JSONException;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity  {

    private EditText userMsgEdt;
    private final String BOT_KEY = "bot";

    // creating a variable for array list and adapter class.
    private ArrayList<MessageModal> messageModalArrayList;
    private MessageRVAdapter messageRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Chat wih Tee!");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // on below line we are initializing all our views.
        // creating variables for our
        // widgets in xml file.
        RecyclerView chatsRV = findViewById(R.id.idRVChats);
        ImageButton sendMsgIB = findViewById(R.id.idIBSend);
        userMsgEdt = findViewById(R.id.idEdtMessage);

        // below line is to initialize our request queue.
        // creating a variable for
        // our volley request queue.
        RequestQueue mRequestQueue = Volley.newRequestQueue(ChatActivity.this);
        mRequestQueue.getCache().clear();

        // creating a new array list
        messageModalArrayList = new ArrayList<>();

        // adding on click listener for send message button.
        sendMsgIB.setOnClickListener(v -> {
            // checking if the message entered
            // by user is empty or not.
            if (userMsgEdt.getText().toString().isEmpty()) {
                // if the edit text is empty display a toast message.
                userMsgEdt.requestFocus();
            }else{

                // calling a method to send message
                // to our bot to get response.
                sendMessage(userMsgEdt.getText().toString());

                // below line we are setting text in our edit text as empty
                userMsgEdt.setText("");
            }


        });

        // on below line we are initialing our adapter class and passing our array list to it.
        messageRVAdapter = new MessageRVAdapter(messageModalArrayList, this);

        // below line we are creating a variable for our linear layout manager.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this, RecyclerView.VERTICAL, false);

        // below line is to set layout
        // manager to our recycler view.
        chatsRV.setLayoutManager(linearLayoutManager);

        // below line we are setting
        // adapter to our recycler view.
        chatsRV.setAdapter(messageRVAdapter);
    }

    private void sendMessage(String userMsg) {
        // below line is to pass message to our
        // array list which is entered by the user.
        String USER_KEY = "user";
        messageModalArrayList.add(new MessageModal(userMsg, USER_KEY));
        messageRVAdapter.notifyDataSetChanged();

        // url for our brain
        // make sure to add mshape for uid.
        // make sure to add your url.
        String url = "http://api.brainshop.ai/get?bid=166999&key=tnvHBJ6DrGlXYuGA&uid=[uid]&msg=[msg]" + userMsg;

        // creating a variable for our request queue.
        RequestQueue queue = Volley.newRequestQueue(ChatActivity.this);

        // on below line we are making a json object request for a get request and passing our url .
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                // in on response method we are extracting data
                // from json response and adding this response to our array list.
                String botResponse = response.getString("cnt");
                messageModalArrayList.add(new MessageModal(botResponse, BOT_KEY));

                // notifying our adapter as data changed.
                messageRVAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();

                // handling error response from bot.
                messageModalArrayList.add(new MessageModal("No response", BOT_KEY));
                messageRVAdapter.notifyDataSetChanged();
            }
        }, error -> {
            // error handling.
            messageModalArrayList.add(new MessageModal("Sorry no response found", BOT_KEY));
            Toast.makeText(ChatActivity.this, "No response from the bot..", Toast.LENGTH_SHORT).show();
        });

        // at last adding json object
        // request to our queue.
        queue.add(jsonObjectRequest);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}