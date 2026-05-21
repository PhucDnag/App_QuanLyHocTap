package com.example.a9_btl.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a9_btl.R;
import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private List<String> mListClass;
    private IClickClassListener mListener;

    // Interface để xử lý sự kiện bấm vào lớp
    public interface IClickClassListener {
        void onClickClass(String className);
    }

    public ClassAdapter(List<String> list, IClickClassListener listener) {
        this.mListClass = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        String className = mListClass.get(position);
        holder.tvClassName.setText("Lớp: " + className);

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClickClass(className);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListClass != null ? mListClass.size() : 0;
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
        }
    }
}