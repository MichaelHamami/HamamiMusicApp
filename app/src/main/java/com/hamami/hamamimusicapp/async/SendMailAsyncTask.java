package com.hamami.hamamimusicapp.async;

import android.os.AsyncTask;
import android.util.Log;

import com.hamami.hamamimusicapp.GMailSender;


public class SendMailAsyncTask extends AsyncTask<Void,Void,Void> {

    private static final String TAG = "SendMailAsyncTask";

    private String mBody;
    private String mSubject;

    public SendMailAsyncTask(String body, String subject) {
        mBody = body;
        mSubject = subject;
    }
    

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground: SendMail Task called");
                
        GMailSender sender = new GMailSender("hamami2010@gmail.com", "z8joe9r2ef");
        try {
            sender.sendMail(mSubject,
                    mBody,
                    "hamami2010@gmail.co.il",
                    "hamami2010@gmail.com");
        } catch (Exception e) {
            Log.d(TAG, "doInBackground: error: "+e.getMessage());
                    
        }
        return null;
    }
}
