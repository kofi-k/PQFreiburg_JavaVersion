package com.kofikay.pqfreiburg13.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kofikay.pqfreiburg13.R;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private final ArrayList<String> imageList;


    // constructor for our list view adapter.
    public GalleryAdapter(ArrayList<String> imageList, Context context) {
        this.imageList = imageList;
        this.context = context;
    }

    private final Context context;

    @NonNull
    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating the Layout(Instantiates list_item.xml layout file into View object)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryAdapter.ViewHolder holder, int position) {
        //holder.setPostImage(images.get(position));
        Glide.with(holder.itemView.getContext()).load(imageList.get(position)).into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {
            // Intent intent = new Intent(context, ImageScaled.class);
            //context.startActivity(intent);
        });

    }


    @Override
    public int getItemCount() {
        // Returns number of items currently available in Adapter
        return imageList.size();
    }

    // Initializing the Views
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.villa);
        }
    }

    public class Cell {

        private Integer img;

        public Integer getImg() {
            return img;
        }

        public void setImg(Integer img) {
            this.img = img;
        }
    }

}
