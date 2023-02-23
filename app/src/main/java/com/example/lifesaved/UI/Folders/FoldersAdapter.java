package com.example.lifesaved.UI.Folders;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifesaved.R;
import com.example.lifesaved.UI.OnItemClickListener;

import java.util.ArrayList;

public class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.FolderViewHolder> {

    private ArrayList<Folder> folders;

    private OnItemClickListener listener;

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public FoldersAdapter(ArrayList<Folder> folders) {
        this.folders = folders;
    }

    @NonNull
    @Override
    public FoldersAdapter.FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View folderView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem_folder, parent, false);
        return new FolderViewHolder(folderView);
    }

    @Override
    public void onBindViewHolder(@NonNull FoldersAdapter.FolderViewHolder holder, int position) {
        Folder currentFolder = folders.get(position);
        holder.nameView.setText(currentFolder.getName());
        // come back when knowing how to let the user set a custom image

        Uri uri = currentFolder.getIcon();

        Log.e("FolderAdapter", "onBindViewHolder: " + "I AM AT THE ADAPTER NOW" + uri);
        if(uri != null)
            holder.imageView.setImageURI(uri);
        else
            holder.imageView.setImageResource(R.drawable.folder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(holder.getAdapterPosition());

            }
        });

    }

    @Override
    public int getItemCount() {
        return folders.size();
    }


    public static class FolderViewHolder extends RecyclerView.ViewHolder {

        public TextView nameView;
        public ImageView imageView;

        public FolderViewHolder(@NonNull View itemView) {
            super((itemView));
            nameView = itemView.findViewById(R.id.TextView_recycler_folder_name);
            imageView = itemView.findViewById(R.id.imageView_recycler_folder_image);

            //preset images of the folders
        }

    }
}
