package com.kofikay.pqfreiburg13.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kofikay.pqfreiburg13.ImageModal;
import com.kofikay.pqfreiburg13.ImageScaled;
import com.kofikay.pqfreiburg13.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GalleryAdapter2 extends ArrayAdapter<ImageModal> {

    // constructor for our list view adapter.
    public GalleryAdapter2(@NonNull Context context, ArrayList<ImageModal> dataModalArrayList) {
        super(context, 0, dataModalArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // below line is use to inflate the
        // layout for our item of list view.
        View listitemView = convertView;
        if (listitemView == null) {
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.gallery_layout, parent, false);
        }

        ImageModal dataModal = getItem(position);

        ImageView courseIV = listitemView.findViewById(R.id.villa);

        Picasso.get().load(dataModal.getImgUrl()).into(courseIV);


        listitemView.setOnClickListener(v -> {
            Intent sendimage = new Intent(getContext(), ImageScaled.class);
            sendimage.putExtra("image", dataModal.getImgUrl());
            getContext().startActivity(sendimage);

        });
        return listitemView;
    }
}