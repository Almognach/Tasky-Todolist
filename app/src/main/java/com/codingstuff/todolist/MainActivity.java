package com.codingstuff.todolist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.codingstuff.todolist.Adapter.ToDoAdapter;
import com.codingstuff.todolist.Model.ToDoModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements OnDialogCloseListner {

    private RecyclerView recyclerView;
    private FloatingActionButton mFab;
    private FirebaseFirestore mDB;
    private ToDoAdapter mAdapter;
    private List<ToDoModel> mList;
    private CircleImageView mProfileImageView;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDB = FirebaseFirestore.getInstance();
        initUIViews();
        initRecyclerView();

        initClickListeners();
        showUserToDoListData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.members_button: {
                Intent intent = new Intent(MainActivity.this, MembersActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.logout: {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initClickListeners() {
        mFab.setOnClickListener(v -> AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG));
    }

    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mList = new ArrayList<>();
        mAdapter = new ToDoAdapter(MainActivity.this, mList, true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(mAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(mAdapter);
    }

    private void initUIViews() {
        recyclerView = findViewById(R.id.recycerlview);
        mFab = findViewById(R.id.floatingActionButton);
        mProfileImageView = findViewById(R.id.profile_image);
        mProfileImageView.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            mGalleryChooser.launch(intent);
        });
        setProvileImageView();
        TextView header = findViewById(R.id.header_title);
        header.setText("Hello " + FirebaseAuth.getInstance().getCurrentUser().getEmail());

    }

    private void setProvileImageView() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profilePicRef = storageRef.child("images/" + FirebaseAuth.getInstance().getUid() + "/profile.jpg");
        final long ONE_MEGABYTE = 1024 * 1024;
        profilePicRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG, "onSuccess: ");
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                mProfileImageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "onFailure: " + exception.getLocalizedMessage());
                // Handle any errors
            }
        });

    }

    private final ActivityResultLauncher<Intent> mGalleryChooser = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    final Intent intent = result.getData();
                    if (intent != null && intent.getData() != null) {
                        Uri uri = intent.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            mProfileImageView.setImageBitmap(bitmap);
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference profilePicRef = storageRef.child("images/" + FirebaseAuth.getInstance().getUid() + "/profile.jpg");
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();
                            UploadTask uploadTask = profilePicRef.putBytes(data);
                            uploadTask.addOnCompleteListener(task -> Log.d(TAG, "onComplete: " + task.isSuccessful()));
                        } catch (Exception e) {
                            Log.d(TAG, e.getLocalizedMessage());
                        }
                    }
                }
            });

    private void showUserToDoListData() {
        mDB.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document("todolist").collection("list").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mList.clear();
                for (QueryDocumentSnapshot query : task.getResult()) {
                    String id = query.getId();
                    ToDoModel toDoModel = query.toObject(ToDoModel.class).withId(id);
                    mList.add(toDoModel);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        showUserToDoListData();
    }

    private static String encodeToBase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }

    private static Bitmap decodeToBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}