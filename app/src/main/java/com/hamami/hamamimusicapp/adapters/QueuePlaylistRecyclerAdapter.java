package com.hamami.hamamimusicapp.adapters;

import android.content.Context;
import android.graphics.Color;
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
import com.hamami.hamamimusicapp.util.ItemTouchHelperViewHolder;

import java.util.ArrayList;
import java.util.Collections;

public class QueuePlaylistRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
implements ItemTouchHelperAdapter {

    private static final String TAG = "QueueRecyclerAdapter";

    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private ArrayList<Songs> mSongsList = new ArrayList<>();
    private Context mContext;
    private IMediaSelector mIMediaSelector;
    private int mSelectedIndex;

    public QueuePlaylistRecyclerAdapter(Context context, ArrayList<Songs> songsList, ArrayList<MediaMetadataCompat> mMediaList, IMediaSelector mediaSelector)
    {
        Log.d(TAG, "QueueRecyclerAdapter: called.");
        this.mMediaList = mMediaList;
        this.mSongsList = songsList;
        this.mContext = context;
        this.mIMediaSelector = mediaSelector;
        mSelectedIndex = -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_queue_list_item, null);
        ViewHolder vh = new ViewHolder(view, mIMediaSelector);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {

//        ((ViewHolder)viewHolder).songName.setText(songsList.get(i).getNameSong());
//         ((ViewHolder)viewHolder).songTime.setText(songsList.get(i).getSongLength());

        ((ViewHolder)viewHolder).songName.setText(mMediaList.get(i).getDescription().getTitle());
        ((ViewHolder)viewHolder).songTime.setText(mSongsList.get(i).getSongLength());
        ((ViewHolder)viewHolder).handleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIMediaSelector.onStartDrag(viewHolder);
            }
        });
        if(i == mSelectedIndex){
            ((ViewHolder)viewHolder).songName.setTextColor(ContextCompat.getColor(mContext, R.color.green));
        }
        else{
            ((ViewHolder)viewHolder).songName.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }
    }

    @Override
    public int getItemCount() {
        return mSongsList.size();
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

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Log.d(TAG, "onItemMove: called");
        Collections.swap(mSongsList, fromPosition, toPosition);
        Collections.swap(mMediaList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        Log.d(TAG, "onItemMove: moved?");
        return true;
    }

    @Override
    public void onItemDismiss(int position)
    {
        Log.d(TAG, "onItemDismiss: called");

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    , ItemTouchHelperViewHolder {

        // title = songName , artist=songTime
        private TextView songName, songTime;
        private ImageView songOptions;
        private IMediaSelector iMediaSelector;
        private final ImageView handleView;

        public ViewHolder(@NonNull View itemView, IMediaSelector iMediaSelector) {
            super(itemView);
            songName = itemView.findViewById(R.id.song_name);
            songTime = itemView.findViewById(R.id.song_time);
            handleView = (ImageView) itemView.findViewById(R.id.handle);
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

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            Log.d(TAG, "onItemClear: finished to move?");
            mIMediaSelector.onFinishedDrag(mSongsList,mMediaList);
            itemView.setBackgroundColor(0);
        }
    }

    public interface IMediaSelector{
        void onMediaSelected(int position);
        void onSongOptionSelected(int position, View view);
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
        void onFinishedDrag(ArrayList<Songs> songsList, ArrayList<MediaMetadataCompat> mMediaList);
    }

}
