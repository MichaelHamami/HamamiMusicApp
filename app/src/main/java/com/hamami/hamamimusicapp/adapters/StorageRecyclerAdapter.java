package com.hamami.hamamimusicapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hamami.hamamimusicapp.Models.Item;
import com.hamami.hamamimusicapp.R;

import java.io.File;
import java.util.List;


public class StorageRecyclerAdapter extends RecyclerView.Adapter<StorageRecyclerAdapter.ViewHolder> {

    private static final String TAG = "StorageRecyclerAdapter";

    private List<Item> itemList;
    private Context mContext;
    private IStorageSelector mStorageSelector;

    public StorageRecyclerAdapter(Context context,List<Item> stringList,IStorageSelector iStorageSelector )
    {
        Log.d(TAG, "StorageRecyclerAdapter: called.");
        this.itemList = stringList;
        this.mContext = context;
        this.mStorageSelector = iStorageSelector;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_storage_list_item, null);
        ViewHolder vh = new ViewHolder(view,mStorageSelector);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG,"onBindViewHolder: called.");
        File file = new File(itemList.get(position).getUri().getPath());
        if(file.isDirectory())
        {
            holder.imageView.setImageResource(R.drawable.folder);
            holder.storageOptions.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.imageView.setImageResource(R.drawable.file1);
        }
        holder.textViewStringName.setText(itemList.get(position).getName());
    }



    @Override
    public int getItemCount() {
        return itemList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imageView;
        TextView textViewStringName;
        ImageView storageOptions;
        private IStorageSelector storageSelector;


        public ViewHolder(@NonNull View itemView , IStorageSelector iStorageSelector) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgView);
            textViewStringName = itemView.findViewById(R.id.string);
            storageOptions = itemView.findViewById(R.id.storage_option);
            this.storageSelector = iStorageSelector;

            itemView.setOnClickListener(this);
            imageView.setOnClickListener(this);
            storageOptions.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: called");
            if(view.getId() == R.id.imgView)
            {
                Log.d(TAG, "onClick: get here");
                File file = new File(itemList.get(getAdapterPosition()).getUri().getPath());
                if(file.isDirectory())
                {
                    Log.d(TAG, "onClick: clear items and try to get them again");
                    itemList.clear();
                    itemList.add(new Item(file.getName(),Uri.fromFile(file),file));
                    File[] files = file.listFiles();
                    for(File singleFile : files)
                    {
                        if(!singleFile.isHidden())
                        {
                            itemList.add(new Item(singleFile.getName(), Uri.fromFile(singleFile),singleFile));
                        }
                    }
                    notifyDataSetChanged();
                }
                else
                {
                    Toast.makeText(view.getContext(),"You clicked on File ",Toast.LENGTH_SHORT).show();
                }

            }
            else if(view.getId() == R.id.storage_option)
            {
                mStorageSelector.onFolderOptionSelected(itemList.get(getAdapterPosition()),view);
            }
        }
    }

    public interface IStorageSelector{
        void onFolderOptionSelected(Item item, View view);
    }

}
