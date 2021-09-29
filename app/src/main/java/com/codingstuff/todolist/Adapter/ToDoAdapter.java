package com.codingstuff.todolist.Adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codingstuff.todolist.AddNewTask;
import com.codingstuff.todolist.MainActivity;
import com.codingstuff.todolist.Model.ToDoModel;
import com.codingstuff.todolist.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {

    private List<ToDoModel> todoList;
    private Activity activity;
    private FirebaseFirestore mDB;
    private boolean shouldEnable;

    public ToDoAdapter(Activity mainActivity, List<ToDoModel> todoList, boolean enable) {
        this.todoList = todoList;
        activity = mainActivity;
        shouldEnable = enable;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.each_task, parent, false);
        mDB = FirebaseFirestore.getInstance();
        return new MyViewHolder(view);
    }

    public void deleteTask(int position) {
        ToDoModel toDoModel = todoList.get(position);
        mDB.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document("todolist").collection("list").document(toDoModel.TaskId).delete();
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public Context getContext() {
        return activity;
    }

    public void editTask(int position) {
        ToDoModel toDoModel = todoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putString("task", toDoModel.getTask());
        bundle.putString("due", toDoModel.getDue());
        bundle.putString("id", toDoModel.TaskId);
        AddNewTask addNewTask = new AddNewTask();
        addNewTask.setArguments(bundle);
        addNewTask.show(((MainActivity) activity).getSupportFragmentManager(), addNewTask.getTag());
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final ToDoModel toDoModel = todoList.get(position);
        holder.mCheckBox.setText(toDoModel.getTask());
        holder.mDueDateTv.setText("Due On " + toDoModel.getDue());
        holder.mCheckBox.setChecked(toBoolean(toDoModel.getStatus()));
        holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> mDB.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document("todolist").collection("list").document(toDoModel.TaskId).update("status", isChecked ? 1 : 0));
        if (!shouldEnable) {
            holder.mCheckBox.setEnabled(false);
            holder.mDueDateTv.setEnabled(false);
        }
    }


    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mDueDateTv;
        CheckBox mCheckBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mDueDateTv = itemView.findViewById(R.id.due_date_tv);
            mCheckBox = itemView.findViewById(R.id.mcheckbox);
        }
    }

    private boolean toBoolean(int status) {
        return status != 0;
    }
}
