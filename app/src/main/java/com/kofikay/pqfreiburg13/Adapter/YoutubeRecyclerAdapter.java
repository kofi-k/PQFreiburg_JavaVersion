package com.kofikay.pqfreiburg13.Adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kofikay.pqfreiburg13.PQVideoModel;
import com.kofikay.pqfreiburg13.PlayVideo;
import com.kofikay.pqfreiburg13.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class YoutubeRecyclerAdapter extends RecyclerView.Adapter<YoutubeRecyclerAdapter.MyViewHolder> {

    Context context;
    ArrayList<PQVideoModel> list;

    public YoutubeRecyclerAdapter(Context context, ArrayList<PQVideoModel> videoModels) {
        this.context = context;
        this.list = videoModels;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View  itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.v_tour, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final PQVideoModel model = list.get(position);
        Picasso.get().load(model.getUrl()).into(holder.vidImageView);
        holder.vidImageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayVideo.class);
            intent.putExtra("videoId", model.getVideoId());
            intent.putExtra("title", model.getTitle()); 
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        public ImageView vidImageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            vidImageView =itemView.findViewById(R.id.vidImageView);
        }
    }
}
