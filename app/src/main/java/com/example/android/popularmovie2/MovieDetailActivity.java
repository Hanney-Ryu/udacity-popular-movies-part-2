package com.example.android.popularmovie2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.popularmovie2.data.Movie;
import com.example.android.popularmovie2.data.MovieContract.MovieEntry;
import com.example.android.popularmovie2.databinding.ActivityMovieDetailBinding;
import com.example.android.popularmovie2.util.QueryUtils;
import com.squareup.picasso.Picasso;

//TODO: feat: review
public class MovieDetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

    Movie mCurrentMovie;

    ActivityMovieDetailBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail);

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
            if (uri != null) {
                Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_SHORT).show();
            }
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

        @Override
        protected Void doInBackground(Void... params) {
            isWatchList = isWatchList();
            String requestUrl = QueryUtils.makeRequestUrlForTrailer(mCurrentMovie.getId());
            mTrailerUri = QueryUtils.fetchTrailerUrl(requestUrl);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (isWatchList) {
                makeRemoveButtonVisible();
            } else {
                makeAddButtonVisible();
            }

            if (mTrailerUri == null) {
                mBinding.movieDetailTrailerLabel.setVisibility(View.GONE);
                mBinding.actionSeeTrailer.setVisibility(View.GONE);
            }

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
