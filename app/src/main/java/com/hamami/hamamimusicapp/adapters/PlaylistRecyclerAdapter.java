package com.hamami.hamamimusicapp.adapters;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hamami.hamamimusicapp.Models.Songs;
import com.hamami.hamamimusicapp.R;

import java.util.ArrayList;

public class PlaylistRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "PlaylistRecyclerAdapter";

    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private ArrayList<Songs> songsList = new ArrayList<>();
    private Context mContext;
    private IMediaSelector mIMediaSelector;
    private int mSelectedIndex;

    public PlaylistRecyclerAdapter(Context context, ArrayList<Songs> songsList, ArrayList<MediaMetadataCompat> mMediaList,IMediaSelector mediaSelector)
    {
        Log.d(TAG, "PlaylistRecyclerAdapter: called.");
        this.mMediaList = mMediaList;
        this.songsList = songsList;
        this.mContext = context;
        this.mIMediaSelector = mediaSelector;
        mSelectedIndex = -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_playlist_list_item, null);
        ViewHolder vh = new ViewHolder(view, mIMediaSelector);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

//        ((ViewHolder)viewHolder).songName.setText(songsList.get(i).getNameSong());
//         ((ViewHolder)viewHolder).songTime.setText(songsList.get(i).getSongLength());

        ((ViewHolder)viewHolder).songName.setText(mMediaList.get(i).getDescription().getTitle());
        ((ViewHolder)viewHolder).songTime.setText(songsList.get(i).getSongLength());

        if(i == mSelectedIndex){
            ((ViewHolder)viewHolder).songName.setTextColor(ContextCompat.getColor(mContext, R.color.green));
        }
        else{
            ((ViewHolder)viewHolder).songName.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public void setSelectedIndex(int index){
        mSelectedIndex = index;
        notifyDataSetChanged();
    }

    public int getSelectedIndex(){
        return mSelectedIndex;
    }

    public int getIndexOfItem(MediaMetadataCompat mediaItem){
        for(int i = 0; i<mMediaList.size(); i++ ){
            if(mMediaList.get(i).getDescription().getMediaId().equals(mediaItem.getDescription().getMediaId())){
                return i;
            }
        }
        return -1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // title = songName , artist=songTime
        private TextView songName, songTime;
//        private TextView songOptions;
        private ImageView songOptions;

        private IMediaSelector iMediaSelector;

        public ViewHolder(@NonNull View itemView, IMediaSelector iMediaSelector) {
            super(itemView);
            songName = itemView.findViewById(R.id.song_name);
            songTime = itemView.findViewById(R.id.song_time);
            songOptions = itemView.findViewById(R.id.song_option);
            this.iMediaSelector = iMediaSelector;

            itemView.setOnClickListener(this);
            songOptions.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.song_option)
            {
                iMediaSelector.onSongOptionSelected(getAdapterPosition(),view);
            }
            else
            {
                iMediaSelector.onMediaSelected(getAdapterPosition());
            }
        }
    }

    public interface IMediaSelector{
        void onMediaSelected(int position);
        void onSongOptionSelected(int position, View view);
    }

}
