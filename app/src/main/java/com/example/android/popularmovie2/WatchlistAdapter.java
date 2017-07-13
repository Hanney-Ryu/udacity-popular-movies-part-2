package com.example.android.popularmovie2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovie2.data.Movie;
import com.example.android.popularmovie2.data.MovieContract.MovieEntry;
import com.example.android.popularmovie2.util.QueryUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder> {

    private Cursor mCursor;
    private List<Movie> mMovies;
    private Context mContext;

    public WatchlistAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public WatchlistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.movie_list_item, parent, false);
        return new WatchlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final WatchlistViewHolder holder, final int position) {
        final int movieIdIndex = mCursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID);
        final int titleIndex = mCursor.getColumnIndex(MovieEntry.COLUMN_TITLE);
        final int voteAverageIndex = mCursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE);
        final int posterPathIndex = mCursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH);
        final int overviewIndex = mCursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW);
        final int releaseDateIndex = mCursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE);

        mCursor.moveToPosition(position);

        Picasso.with(mContext)
                .load(QueryUtils.makeRequestUrlForPoster(mCursor.getString(posterPathIndex)))
                .into(holder.moviePosterImageView);

        holder.moviePosterImageView.setTag(position);

        Movie movie = new Movie(mCursor.getString(movieIdIndex),
                mCursor.getString(titleIndex),
                mCursor.getString(voteAverageIndex),
                mCursor.getString(posterPathIndex),
                mCursor.getString(overviewIndex),
                mCursor.getString(releaseDateIndex));
        mMovies.add(movie);

        holder.moviePosterImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MovieDetailActivity.class);
                intent.putExtra(Movie.class.getSimpleName(), mMovies.get(position));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor c) {
        mMovies = new ArrayList<>();

        if (mCursor == c) {
            return null;
        }
        Cursor temp = mCursor;
        this.mCursor = c;

        if (c != null) {
            this.notifyDataSetChanged();
        }

        return temp;
    }

    class WatchlistViewHolder extends RecyclerView.ViewHolder {
        private ImageView moviePosterImageView;

        public WatchlistViewHolder(View itemView) {
            super(itemView);
            moviePosterImageView = (ImageView) itemView.findViewById(R.id.movie_list_poster_image);
        }
    }
}
