package com.example.lifesaved.UI.Viewing;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifesaved.UI.OnItemClickListener;
import com.example.lifesaved.R;
import com.example.lifesaved.UI.OnLongClickListener;
import com.example.lifesaved.models.Image;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private ArrayList<Image> images;

    private OnItemClickListener listener;
    private OnLongClickListener longClickListener;

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public void setLongClickListener(OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public ImageAdapter(ArrayList<Image> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public ImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View imageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem_image, parent, false);
        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ImageViewHolder holder, int position) {
        Image currentImage = images.get(position);
        Log.e("ImageAdapter", "onBindViewHolder: " + "I AM AT THE ADAPTER NOW" + currentImage.getImgUri());
        Uri uri = currentImage.getImgUri();
        if(uri != null)
            holder.imageView.setImageURI(uri);
        else //don't think it can even happen tho idk
            holder.imageView.setImageResource(R.drawable.red_star);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.e("ImageAdapter", "onLongClick: " + "button is visible");
                holder.buttonView.setVisibility(View.VISIBLE);
                longClickListener.passImageButton(holder.buttonView, holder.getAdapterPosition());
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(holder.getAdapterPosition());
                Log.e("FolderAdapter", "onClick: " + "button is invisible");
                holder.buttonView.setVisibility(View.INVISIBLE);
            }
        });


    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        public TextView nameView;
        public ImageView imageView;
        //
        public ImageButton buttonView;


        public ImageViewHolder(@NonNull View itemView) {
            super((itemView));

            imageView = itemView.findViewById(R.id.imageView_recycler_image_image); //set default image
            buttonView = itemView.findViewById(R.id.imageButton_recycler_image_close);

        }

    }
}
