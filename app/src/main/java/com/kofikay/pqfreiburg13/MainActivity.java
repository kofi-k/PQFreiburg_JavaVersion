package com.kofikay.pqfreiburg13;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseUser currentUser;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    FirebaseFirestore fStore;
    GoogleSignInClient mGoogleSignInClient;
    ListView listView;
    String[] actions = {"settings", "notifications", "account",
            "logout", "user name", "change password", "change email",
            "contact", "profile photo", "dark mode"};
    ArrayAdapter<String> stringArrayAdapter;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Home");
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager viewPager = findViewById(R.id.viewPager);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();

        MobileAds.initialize(this, initializationStatus -> {});

        FloatingActionButton floatingActionButton = findViewById(R.id.FAB);

        floatingActionButton.setOnClickListener(view -> showBottomSheetDialog());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("108128146448-qbba4j4r5koc95p6vpekk93spmhiic4l.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //binding.drawerLayout.addDrawerListener(toggle);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();


        addFragmentsToViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        UpdateDrawer();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    public void  UpdateDrawer(){
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {
                userData();
            }
        });
    }
    private void addFragmentsToViewPager(ViewPager viewPager) {
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new Gallery(), "Gallery");
        adapter.addFragment(new VirtualTour(), "Virtual Tour");
        viewPager.setAdapter(adapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search for actions");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void userData() {
        View navHeader = navigationView.getHeaderView(0);
        TextView userEmail = navHeader.findViewById(R.id.userEmail);
        TextView userName = navHeader.findViewById(R.id.Name);
        TextView userContact = navHeader.findViewById(R.id.userContact);
        CircleImageView userImage = navHeader.findViewById(R.id.imageView);

        if (currentUser != null) {
        DocumentReference documentReference = fStore.collection("User Profile")
                .document(currentUser.getUid());
        //check for last name availability
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.get("Contact") != null) {
                    String contact = document.getString("Contact");
                    userContact.setText("+233" + contact);
                } else {
                    userContact.setText("Mobile number");
                }
            }
        });

            for (UserInfo userInfo : currentUser.getProviderData()) {
                if (userInfo.getProviderId().equals("google.com")) {
                    String[] name = currentUser.getDisplayName().split("\\s+");
                    userName.setText(name[0]);
                } else {
                    userName.setText(currentUser.getDisplayName());
                }
            }
        }
        userEmail.setText(currentUser.getEmail());
        if (currentUser.getPhotoUrl() != null) {
            Glide.with(this).load(currentUser.getPhotoUrl()).into(userImage);
        } else {
            setImage(userImage);
        }
    }


    private void showBottomSheetDialog() {
        LinearLayout linearLayout = findViewById(R.id.moreBtmsheet);

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(MainActivity.this).inflate(R.layout.floating_arrow, linearLayout);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        TextView bookViewLayout = bottomSheetDialog.findViewById(R.id.bookViewing);
        TextView liveChatLayout = bottomSheetDialog.findViewById(R.id.startLiveChat);
        TextView customerServiceLayout = bottomSheetDialog.findViewById(R.id.call);

        bookViewLayout.setOnClickListener(view -> bottomSheetDialog.dismiss());
        liveChatLayout.setOnClickListener(view -> {
                    Intent chat = new Intent(getApplicationContext(), ChatActivity.class);
                    startActivity(chat);
                    bottomSheetDialog.dismiss();
                }
        );

        customerServiceLayout.setOnClickListener(view -> {
                    CallSupport();
                    bottomSheetDialog.dismiss();
                }
        );
        bottomSheetDialog.setOnDismissListener(dialog -> {
            // Instructions on bottomSheetDialog Dismiss
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.show();
    }

    public void CallSupport() {
        String callNo = "0595246127";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Call Action").
                setMessage("You're about to call support center " + callNo)
                .setPositiveButton("Proceed", (dialog, which) -> {
                    dialog.dismiss();
                    Intent callSupport = new Intent(Intent.ACTION_DIAL);
                    callSupport.setData(Uri.parse("tel:" + callNo));
                    startActivity(callSupport);
                }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    public void setImage(CircleImageView userImage) {
        Drawable color = new ColorDrawable(getResources().getColor(R.color.grey));
        Drawable image = getResources().getDrawable(R.drawable.user);
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{color, image});
        Glide.with(this).load(layerDrawable).into(userImage);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutButton:
                logout();
                return true;
            case R.id.setting:
                Intent settings_activity = new Intent(getApplicationContext(), settings_activity.class);
                startActivity(settings_activity);
                return true;
            case R.id.notificationMenu:
                Intent NotificationFragment = new Intent(getApplicationContext(), NotificationFragment.class);
                startActivity(NotificationFragment);
                return true;
            default:
                return true;
        }

    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
                .addOnSuccessListener(unused -> {
                }).addOnFailureListener(e -> showMessage(e.getMessage()));
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_left);
    }

    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        }else {super.onBackPressed();
        }

        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();*/
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int id = item.getItemId();
        switch (id) {
            case R.id.account:
                Intent profile_activity = new Intent(getApplicationContext(), profile_activity.class);
                startActivity(profile_activity);
                break;
            case R.id.D_privacy:
                Intent privacyIntent = new Intent(getApplicationContext(), PrivacyAndPolicy_activity.class);
                startActivity(privacyIntent);
                return true;
            case R.id.help:
                Intent intent = new Intent(getApplicationContext(), help_center_activity.class);
                startActivity(intent);
                return true;
            case R.id.logout:
                logout();
                return true;
            default:
                return true;
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        }
        return true;

    }

}
