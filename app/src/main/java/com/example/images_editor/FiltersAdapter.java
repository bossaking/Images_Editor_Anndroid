package com.example.images_editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uvstudio.him.photofilterlibrary.PhotoFilter;

import java.util.ArrayList;
import java.util.List;

public class FiltersAdapter extends RecyclerView.Adapter<FiltersAdapter.FilterHolder> {

    private Bitmap image;
    PhotoFilter photoFilter;
    Context context;

    private List<Bitmap> images;

    private FilterClickListener filterClickListener;

    public FiltersAdapter(Bitmap image, PhotoFilter photoFilter, Context context, FilterClickListener filterClickListener){
        this.image = image;
        this.photoFilter = photoFilter;
        this.context = context;
        this.filterClickListener = filterClickListener;


        images = new ArrayList<>();
        images.add(image);
        for(int i = 0; i < 5; i++){
            switch (i){
                case 0:
                    images.add(photoFilter.one(this.context, this.image));
                    break;
                case 1:
                    images.add(photoFilter.two(this.context, this.image));
                    break;
                case 2:
                    images.add(photoFilter.three(this.context, this.image));
                    break;
                case 3:
                    images.add(photoFilter.four(this.context, this.image));
                    break;
                case 4:
                    images.add(photoFilter.five(this.context, this.image));
                    break;
            }
        }
    }



    @NonNull
    @Override
    public FiltersAdapter.FilterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.image_filter_example_card_view, parent, false);
        return new FilterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FiltersAdapter.FilterHolder holder, int position) {
        holder.filterClickListener = this.filterClickListener;
        holder.setImageView(this.images.get(position));
    }

    @Override
    public int getItemCount() {
        return image == null ? 0 : 6;
    }

    public static class FilterHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final ImageView imageView;
        private FilterClickListener filterClickListener;

        public FilterHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            itemView.setOnClickListener(this);
        }

        public void setImageView(Bitmap bmp){
            imageView.setImageBitmap(bmp);
        }

        public void setFilterClickListener(FilterClickListener filterClickListener){
            this.filterClickListener = filterClickListener;
        }


        @Override
        public void onClick(View view) {
            if(filterClickListener != null) filterClickListener.onItemClick(getAdapterPosition());
        }
    }

    public interface FilterClickListener{
        void onItemClick(int position);
    }
}
