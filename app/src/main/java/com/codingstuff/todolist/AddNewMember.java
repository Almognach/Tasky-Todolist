package com.codingstuff.todolist;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddNewMember extends BottomSheetDialogFragment {

    private EditText mMemberName;
    private Button mAddButton;
    private FirebaseFirestore mDB;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_new_memeber, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMemberName = view.findViewById(R.id.member_edit_text);
        mAddButton = view.findViewById(R.id.add_btn);
        mDB = FirebaseFirestore.getInstance();
        mAddButton.setOnClickListener(v -> {
            final String member = mMemberName.getText().toString();
            if (TextUtils.isEmpty(member) || TextUtils.equals(member, FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                showErrorMsg("Please fill a valid email member");
                dismiss();
            } else {
                mDB.collection("emails").document("emails").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        ArrayList<String> result = (ArrayList<String>) task.getResult().get("emails");
                        boolean isFound = false;
                        if (result != null) {
                            for (String str : result) {
                                if (TextUtils.equals(member, str)) {
                                    isFound = true;
                                    break;
                                }
                            }
                        }
                        if (result == null || isFound) {
                            final Map<String, Object> hashMap = new HashMap<>();
                            hashMap.put("array", FieldValue.arrayUnion(member));
                            mDB.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document("members").set(hashMap).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    showErrorMsg("MemberAdded");
                                } else {
                                    showErrorMsg("Failed - please try again");
                                }
                                dismiss();
                            });
                        } else {
                            showErrorMsg("Email not found - please try again");
                            dismiss();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListner) {
            ((OnDialogCloseListner) activity).onDialogClose(dialog);
        }
    }

    private void showErrorMsg(String msg) {
        Snackbar.make(getActivity().findViewById(R.id.mainRel), msg, Snackbar.LENGTH_SHORT).show();
    }
}
