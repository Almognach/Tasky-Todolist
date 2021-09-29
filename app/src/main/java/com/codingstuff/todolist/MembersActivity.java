package com.codingstuff.todolist;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codingstuff.todolist.Adapter.MembersAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MembersActivity extends AppCompatActivity implements OnDialogCloseListner {

    private MembersAdapter mMembersAdapter;
    private FirebaseFirestore mDB;
    private RecyclerView recyclerView;
    private FloatingActionButton mFab;
    private final ArrayList<String> mMembersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        mDB = FirebaseFirestore.getInstance();
        initUIViews();
        initRecyclerView();
    }

    private void initUIViews() {
        recyclerView = findViewById(R.id.recycerlview);
        mFab = findViewById(R.id.floatingActionButton);
        mFab.setOnClickListener(view -> new AddNewMember().show(getSupportFragmentManager(), AddNewTask.TAG));
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recycerlview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mMembersAdapter = new MembersAdapter(MembersActivity.this, getApplicationContext(), mMembersList);
        recyclerView.setAdapter(mMembersAdapter);
        showMembers();
    }

    private void showMembers() {
        mDB.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document("members").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mMembersList.clear();
                ArrayList<String> result = (ArrayList<String>) task.getResult().get("array");
                if (result != null) {
                    mMembersList.addAll(result);
                    mMembersAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        showMembers();
    }
}