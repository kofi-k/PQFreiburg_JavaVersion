package com.kofikay.pqfreiburg13;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kofikay.pqfreiburg13.Adapter.GalleryAdapter;
import com.kofikay.pqfreiburg13.Adapter.GalleryAdapter2;

import java.util.ArrayList;
import java.util.List;

public class Gallery extends Fragment {

    private ArrayList<String> itemsList;
    GalleryAdapter galleryAdapter;
    RecyclerView recyclerView;
    StorageReference listRef;
    ArrayList<ImageModal> dataModalArrayList;
    FirebaseFirestore db;
    SwipeRefreshLayout swipeRefreshLayout;
    GridView coursesGV;

    public Gallery() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.gallery_recycler, container, false);
        //View view = inflater.inflate(R.layout.gallery2, container, false);
        itemsList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.galleryList);
        //coursesGV = view.findViewById(R.id.idGVCourses);
        galleryAdapter = new GalleryAdapter(itemsList, this.getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(null));
        listRef = FirebaseStorage.getInstance().getReference().child("PQ");

        dataModalArrayList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        LoadImages();
        //LoadDataInGridView();
        swipeRefreshLayout = view.findViewById(R.id.refreshLayout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            listRef.listAll().addOnSuccessListener(listResult -> {
                itemsList.clear();
                for (StorageReference file : listResult.getItems()) {
                    file.getDownloadUrl().addOnSuccessListener(uri -> {
                        //galleryAdapter = new GalleryAdapter(this.getContext(), itemsList);
                        itemsList.add(uri.toString());
                    }).addOnSuccessListener(uri -> {
                        // galleryAdapter = new GalleryAdapter(getContext(), itemsList);
                        recyclerView.setAdapter(galleryAdapter);
                    });
                }
            });
            swipeRefreshLayout.setRefreshing(false);
        });


        return view;
    }

    private void LoadImages() {
        listRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference file : listResult.getItems()) {
                file.getDownloadUrl().addOnSuccessListener(uri -> {
                    //galleryAdapter = new GalleryAdapter(this.getContext(), itemsList);
                    itemsList.add(uri.toString());
                }).addOnSuccessListener(uri -> {
                    // galleryAdapter = new GalleryAdapter(getContext(), itemsList);
                    recyclerView.setAdapter(galleryAdapter);
                });
            }
        });
    }
    private void LoadDataInGridView() {
        db.collection("PQHomesCollection").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!queryDocumentSnapshots.isEmpty()) {

                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {
                            ImageModal dataModal = d.toObject(ImageModal.class);
                            dataModalArrayList.add(dataModal);
                        }
                        // after that we are passing our array list to our adapter class.
                        GalleryAdapter2 adapter = new GalleryAdapter2(getContext(), dataModalArrayList);
                        coursesGV.setAdapter(adapter);
                    } else {
                        // if the snapshot is empty we are displaying a toast message.
                        Toast.makeText(getContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}