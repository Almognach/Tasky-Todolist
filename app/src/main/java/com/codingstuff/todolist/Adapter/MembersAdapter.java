package com.codingstuff.todolist.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codingstuff.todolist.DisplayMemberList;
import com.codingstuff.todolist.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberHolder> {

    private List<String> mMemberList;
    private Context mContext;
    private FirebaseFirestore mDB;
    private Activity mActivity;

    public MembersAdapter(Activity activity, Context context, List<String> members) {
        this.mMemberList = members;
        mContext = context;
        mActivity = activity;
    }

    @NonNull
    @Override
    public MemberHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.member_row, parent, false);
        mDB = FirebaseFirestore.getInstance();
        return new MemberHolder(view);
    }

    public void deleteMember(int position) {
        String memberToDelete = mMemberList.get(position);
        mDB.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document("members").update("array", FieldValue.arrayRemove(memberToDelete));
        mMemberList.remove(position);
        notifyItemRemoved(position);
    }


    @Override
    public void onBindViewHolder(@NonNull MemberHolder holder, int position) {
        final String member = mMemberList.get(position);
        holder.mEmailMember.setText(member);
        holder.mEmailMember.setOnClickListener(view -> {
            Intent intent = new Intent(mActivity, DisplayMemberList.class);
            intent.putExtra("email", member);
            mActivity.startActivity(intent);
        });
        holder.mRemoveMember.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setMessage("Are You Sure?")
                    .setTitle("Delete " + member).
                    setPositiveButton("Yes", (dialog, which) -> deleteMember(position)).setNegativeButton("No", (dialogInterface, i) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }


    @Override
    public int getItemCount() {
        return mMemberList.size();
    }

    public class MemberHolder extends RecyclerView.ViewHolder {
        TextView mEmailMember;
        ImageView mRemoveMember;

        public MemberHolder(@NonNull View itemView) {
            super(itemView);
            mEmailMember = itemView.findViewById(R.id.mEmailMember);
            mRemoveMember = itemView.findViewById(R.id.removeMemberBTN);
        }
    }

}
