package com.example.android.popularmovie2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;

import com.example.android.popularmovie2.data.Movie;
import com.example.android.popularmovie2.data.MovieContract.MovieEntry;
import com.example.android.popularmovie2.data.Review;
import com.example.android.popularmovie2.databinding.ActivityMovieDetailBinding;
import com.example.android.popularmovie2.util.QueryUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {

    Movie mCurrentMovie;
    ReviewAdapter mAdapter;

    ActivityMovieDetailBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail);
        mAdapter = new ReviewAdapter(this, new ArrayList<Review>());
        mBinding.movieDetailReviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.movieDetailReviewRecyclerView.setAdapter(mAdapter);

        mBinding.actionAddWatchlist.setOnClickListener(new actionAddWatchlistListener());
        mBinding.actionRemoveWatchlist.setOnClickListener(new actionRemoveWatchlistListener());

        mCurrentMovie = getIntent().getExtras().getParcelable(Movie.class.getSimpleName());

        String requestUrlForPoster = QueryUtils.makeRequestUrlForPoster(mCurrentMovie.getPosterPath());

        Picasso.with(this)
                .load(requestUrlForPoster)
                .into(mBinding.movieDetailPosterImage);

        mBinding.movieDetailTitle.setText(mCurrentMovie.getTitle());
        mBinding.movieDetailReleaseDate.setText(mCurrentMovie.getReleaseDate());
        mBinding.movieDetailVoteAverage.setText(mCurrentMovie.getVoteAverage());
        mBinding.movieDetailOverview.setText(mCurrentMovie.getOverview());

        new MovieDetailAsyncTask().execute();

        setTitle(mCurrentMovie.getTitle());
    }

    private final class actionAddWatchlistListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MovieEntry.COLUMN_MOVIE_ID, mCurrentMovie.getId());
            contentValues.put(MovieEntry.COLUMN_TITLE, mCurrentMovie.getTitle());
            contentValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, mCurrentMovie.getVoteAverage());
            contentValues.put(MovieEntry.COLUMN_POSTER_PATH, mCurrentMovie.getPosterPath());
            contentValues.put(MovieEntry.COLUMN_OVERVIEW, mCurrentMovie.getOverview());
            contentValues.put(MovieEntry.COLUMN_RELEASE_DATE, mCurrentMovie.getReleaseDate());

            Uri uri = getContentResolver().insert(MovieEntry.CONTENT_URI, contentValues);
            makeRemoveButtonVisible();
        }
    }

    private final class actionRemoveWatchlistListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Uri uri = MovieEntry.CONTENT_URI.buildUpon().appendPath(mCurrentMovie.getId()).build();
            getContentResolver().delete(uri, null, null);
            makeAddButtonVisible();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class MovieDetailAsyncTask extends AsyncTask<Void, Void, Void> {
        private boolean isWatchList;
        private String mTrailerUri;
        private List<Review> mReviews;

        @Override
        protected Void doInBackground(Void... params) {
            isWatchList = isWatchList();

            String requestUrlForReviews = QueryUtils.makeRequestUrlForReviews(mCurrentMovie.getId());
            mReviews = QueryUtils.fetchReviewData(requestUrlForReviews);

            String requestUrlForTrailer = QueryUtils.makeRequestUrlForTrailer(mCurrentMovie.getId());
            mTrailerUri = QueryUtils.fetchTrailerUrl(requestUrlForTrailer);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (isWatchList) {
                makeRemoveButtonVisible();
            } else {
                makeAddButtonVisible();
            }

            if (mReviews == null || mReviews.isEmpty()) {
                mBinding.movieDetailReviewLabel.setVisibility(View.GONE);
            } else {
                mBinding.movieDetailReviewLabel.setVisibility(View.VISIBLE);
                mAdapter.updateItems(mReviews);
            }

            if (mTrailerUri == null) {
                mBinding.movieDetailTrailerLabel.setVisibility(View.GONE);
                mBinding.actionSeeTrailer.setVisibility(View.GONE);
            }

            mBinding.movieDetailLoadingIndicatorFrame.setVisibility(View.GONE);

            mBinding.actionSeeTrailer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTrailerUri != null) {
                        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(mTrailerUri));
                        startActivity(youtubeIntent);
                    }
                }
            });
        }
    }

    private boolean isWatchList() {
        Integer countsOfCursor;
        Cursor cursor = getContentResolver().query(MovieEntry.CONTENT_URI,
                null,
                MovieEntry.COLUMN_MOVIE_ID + "=?",
                new String[]{mCurrentMovie.getId()},
                null
        );
        countsOfCursor = cursor.getCount();
        cursor.close();
        return countsOfCursor > 0;
    }

    private void makeAddButtonVisible() {
        mBinding.actionAddWatchlist.setVisibility(View.VISIBLE);
        mBinding.actionRemoveWatchlist.setVisibility(View.GONE);
    }

    private void makeRemoveButtonVisible() {
        mBinding.actionAddWatchlist.setVisibility(View.GONE);
        mBinding.actionRemoveWatchlist.setVisibility(View.VISIBLE);
    }
}
