package com.kofikay.pqfreiburg13;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.kofikay.pqfreiburg13.Adapter.YoutubeRecyclerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VirtualTour extends Fragment {
    RecyclerView recyclerView;
    YoutubeRecyclerAdapter adapter;
    ArrayList<PQVideoModel> list = new ArrayList<>();

    public VirtualTour() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.virtual_tour_recycler, container, false);
        recyclerView =view.findViewById(R.id.tourRecycler);
        recyclerView.setHasFixedSize(true);
        adapter = new YoutubeRecyclerAdapter(this.getContext(), list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(adapter);
        fetchData();
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.refreshVids);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            list.clear();
            fetchData();
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }
    private void fetchData(){
        //RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                "https://www.googleapis.com/youtube/v3/search?part=snippet&channelId=UCUNxXNJHTsedeJzb98e1YbQ&maxResults=30&key=AIzaSyAQvN4bro6y2AVnSMYSgDAF0hwJGwPuAZI",
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("items");
                        for (int i = 0; i<jsonArray.length();i++){
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            JSONObject jsonVideoID = jsonObject1.getJSONObject("id");
                            JSONObject jsonSnippet = jsonObject1.getJSONObject("snippet");
                            JSONObject jsonThumbnail = jsonSnippet.getJSONObject("thumbnails").getJSONObject("medium");

                            PQVideoModel pvm = new PQVideoModel();
                            if ( i!=1 && i!=4 && i!=6 && i!=8 && i!=9 && i!=10 ){
                                pvm.setVideoId(jsonVideoID.getString("videoId"));
                                pvm.setUrl(jsonThumbnail.getString("url"));
                                list.add(pvm);
                            }
                            if(list.size()>0){
                                adapter.notifyDataSetChanged();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(this.getContext(), error.getMessage(), Toast.LENGTH_SHORT).show());
       //requestQueue.add(stringRequest);
        MySingleton.getInstance(this.getContext()).addToRequestQueue(stringRequest);
    }
}