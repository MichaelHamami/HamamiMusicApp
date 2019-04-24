package com.hamami.hamamimusicapp.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

import com.hamami.hamamimusicapp.R;
import com.hamami.hamamimusicapp.async.SendMailAsyncTask;

public class DialogReportToDeveloper extends AppCompatDialogFragment {

    private static final String TAG = "DialogReportToDeveloper";


    //UI
    private EditText mInputBody;
    private EditText mInputSubject;
    private TextView mActionOk;
    private TextView mActionCancel;
    private TextView textViewHeading;
    private TextView textViewSubject;
    private TextView textViewBody;





    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.FullScreenDialogStyle);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.dialog_report_to_developer,container,false);

        textViewBody = view.findViewById(R.id.report_body);
        textViewSubject = view.findViewById(R.id.report_subject);
        textViewHeading = view.findViewById(R.id.report_heading);

        mActionCancel = view.findViewById(R.id.report_action_cancel);
        mActionOk = view.findViewById(R.id.report_action_send);

        mInputBody = view.findViewById(R.id.report_body_input);
        mInputSubject = view.findViewById(R.id.report_subject_input);


        mActionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Cancel clicked Closing dialog");
                getDialog().dismiss();
            }
        });

        mActionOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick:  send Clicked : capturing input");
                String inputSubject = mInputSubject.getText().toString();
                String inputBody = mInputBody.getText().toString();
                    checkInput(inputSubject,inputBody);
            }
        });
        return view;
    }

    private void checkInput(String inputSubject,String inputBody)
    {
        if(inputSubject.equals(""))
        {
            Log.d(TAG, "Empty subject? input: " +inputSubject);
            Toast.makeText(getContext(), "Please Enter Subject", Toast.LENGTH_SHORT).show();
        }
        else if(inputSubject.isEmpty())
        {
            Log.d(TAG, "The subject is null? input: " +inputSubject);
            Toast.makeText(getContext(), "Please Enter Subject", Toast.LENGTH_SHORT).show();
        }
        else if(inputBody.equals(""))
        {
            Log.d(TAG, "Empty String? body input: " +inputBody);
            Toast.makeText(getContext(), "Please Enter Message to the Body of the mail", Toast.LENGTH_SHORT).show();
        }
        else if(inputBody.isEmpty())
        {
            Log.d(TAG, "The String is null? body input: " +inputBody);
            Toast.makeText(getContext(), "Please Enter Message to the Body of the mail", Toast.LENGTH_SHORT).show();
        }
        // it is good Mail
        else
        {
            Log.d(TAG, "checkInput: input is good , we send it now and dismiss dialog");
            new SendMailAsyncTask(inputBody,inputSubject).execute();
            getDialog().dismiss();

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

}
