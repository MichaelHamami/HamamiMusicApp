package com.hamami.hamamimusicapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hamami.hamamimusicapp.R;

import java.util.ArrayList;

public class SelectPlaylistRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SelectPlaylistAdapter";

    private ArrayList<String> mFragmentsTitles = new ArrayList<>();
    private Context mContext;
    private IPlaylistSelector mIPlaylistSelector;

    public SelectPlaylistRecyclerAdapter(Context context,ArrayList<String> fragmentsTitle, IPlaylistSelector playlistSelector)
    {
        Log.d(TAG, "SelectPlaylistAdapter: called.");
        this.mFragmentsTitles = fragmentsTitle;
        this.mContext = context;
        this.mIPlaylistSelector = playlistSelector;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.selectplaylist_item, null);
        ViewHolder vh = new ViewHolder(view, mIPlaylistSelector);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        ((ViewHolder)viewHolder).playlistName.setText(mFragmentsTitles.get(i));

    }

    @Override
    public int getItemCount() {
        return mFragmentsTitles.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView playlistName;
        private IPlaylistSelector iPlaylistSelector;

        public ViewHolder(@NonNull View itemView, IPlaylistSelector iPlaylistSelector) {
            super(itemView);
            playlistName = itemView.findViewById(R.id.playlistName);
            this.iPlaylistSelector = iPlaylistSelector;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            iPlaylistSelector.onPlaylistSelected(getAdapterPosition());
        }

    }

    public interface IPlaylistSelector {
        void onPlaylistSelected(int position);
    }

}
