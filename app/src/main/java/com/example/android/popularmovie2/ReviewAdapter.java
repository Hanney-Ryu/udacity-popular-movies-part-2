package com.example.android.popularmovie2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovie2.data.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewHolder> {

    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<Review> mReviews;

    public ReviewAdapter(Context context, List<Review> reviews) {
        mContext = context;
        mReviews = reviews;
    }

    @Override
    public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mLayoutInflater = LayoutInflater.from(mContext);
        View view = mLayoutInflater.inflate(R.layout.review_list_item, parent, false);
        return new ReviewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewHolder holder, int position) {
        holder.authorTextView.setText(mReviews.get(position).getAuthor());
        holder.contentTextView.setText(mReviews.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return mReviews.size();
    }

    public void updateItems(List<Review> reviews) {
        mReviews.clear();
        mReviews.addAll(reviews);
        notifyDataSetChanged();
    }

    public class ReviewHolder extends RecyclerView.ViewHolder {
        private TextView authorTextView;
        private TextView contentTextView;

        public ReviewHolder(View itemView) {
            super(itemView);
            authorTextView = (TextView) itemView.findViewById(R.id.movie_detail_review_author);
            contentTextView = (TextView) itemView.findViewById(R.id.movie_detail_review_content);
        }
    }
}
