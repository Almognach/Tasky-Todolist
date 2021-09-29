package com.codingstuff.todolist;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codingstuff.todolist.Adapter.ToDoAdapter;
import com.codingstuff.todolist.Model.ToDoModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DisplayMemberList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseFirestore mDB;
    private ToDoAdapter mAdapter;
    private List<ToDoModel> mList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);
        mDB = FirebaseFirestore.getInstance();
        initRecyclerView();
        showUserToDoListData();
    }


    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recycerlview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(DisplayMemberList.this));
        mList = new ArrayList<>();
        mAdapter = new ToDoAdapter(this, mList, false);
        recyclerView.setAdapter(mAdapter);
    }


    private void showUserToDoListData() {
        Intent i = getIntent();
        String emailMember = i.getStringExtra("email");
        mDB.collection(emailMember).document("todolist").collection("list").get().addOnCompleteListener(task -> {
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
}
