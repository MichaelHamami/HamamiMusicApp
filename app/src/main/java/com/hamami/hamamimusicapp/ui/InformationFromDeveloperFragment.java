package com.hamami.hamamimusicapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamami.hamamimusicapp.R;

import java.util.Objects;

public class InformationFromDeveloperFragment extends Fragment {

        private static final String TAG = "InfoDeveloperFragment";

        @SuppressWarnings("FieldCanBeLocal")
        private TextView mAppName;
        @SuppressWarnings({"FieldCanBeLocal", "unused"})
        private ImageView mIconApp;
        @SuppressWarnings("FieldCanBeLocal")
         private EditText mInformation;
        private String mResult;



        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }



        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            return inflater.inflate(R.layout.fragment_information_from_developer,container,false);
        }

        // called after onCreateView
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
        {
            Log.d(TAG, "onViewCreated: we get here?");
            mAppName = view.findViewById(R.id.developer_app_name);
            mIconApp = view.findViewById(R.id.developer_app_icon);
            mInformation = view.findViewById(R.id.developer_information);
            mAppName.setText("Music is Life");
            mInformation.setText("should be text from somewhere");
            retrieveInformationFromFireStore();
        }


        private void retrieveInformationFromFireStore()
        {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference reference = database.collection("Information")
                    .document("owner");

            reference.get().addOnCompleteListener(task -> {
                if(task.isSuccessful())
                {
                    DocumentSnapshot doc = task.getResult();
                    Log.d(TAG, "onComplete: "+ doc);
                    if (doc != null) {
                        mResult  = Objects.requireNonNull(doc.get("input")).toString();
                    }
                    mInformation.setText(mResult);
                }
                else
                {
                    mResult = " didn't get information";
                    mInformation.setText(mResult);
                }
            });
        }



}
