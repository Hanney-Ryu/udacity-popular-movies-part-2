package com.example.android.popularmovie2;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovie2.data.Movie;
import com.example.android.popularmovie2.data.MovieContract.MovieEntry;
import com.example.android.popularmovie2.util.QueryUtils;
import com.squareup.picasso.Picasso;

//TODO: feat: trailer
//TODO: feat: review
public class MovieDetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = MovieDetailActivity.class.getSimpleName();
    Button mAddToWatchlistButton;
    Button mRemoveFromWatchlistButton;
    Movie mCurrentMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        ImageView posterImageView = (ImageView) findViewById(R.id.movie_detail_poster_image);
        TextView titleTextView = (TextView) findViewById(R.id.movie_detail_title);
        TextView releaseDateTextView = (TextView) findViewById(R.id.movie_detail_release_date);
        TextView voteAverageTextView = (TextView) findViewById(R.id.movie_detail_vote_average);
        TextView overViewTextView = (TextView) findViewById(R.id.movie_detail_overview);

        mAddToWatchlistButton = (Button) findViewById(R.id.action_add_watchlist);
        mRemoveFromWatchlistButton = (Button) findViewById(R.id.action_remove_watchlist);

        mAddToWatchlistButton.setOnClickListener(new actionAddWatchlistListener());
        mRemoveFromWatchlistButton.setOnClickListener(new actionRemoveWatchlistListener());

        mCurrentMovie = getIntent().getExtras().getParcelable(Movie.class.getSimpleName());

        String requestUrlForPoster = QueryUtils.makeRequestUrlForPoster(mCurrentMovie.getPosterPath());

        Picasso.with(this)
                .load(requestUrlForPoster)
                .into(posterImageView);

        titleTextView.setText(mCurrentMovie.getTitle());
        releaseDateTextView.setText(mCurrentMovie.getReleaseDate());
        voteAverageTextView.setText(mCurrentMovie.getVoteAverage());
        overViewTextView.setText(mCurrentMovie.getOverview());

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

    private class MovieDetailAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            Integer countsOfCursor;
            Cursor cursor = getContentResolver().query(MovieEntry.CONTENT_URI,
                    null,
                    MovieEntry.COLUMN_MOVIE_ID + "=?",
                    new String[]{mCurrentMovie.getId()},
                    null
            );
            countsOfCursor = cursor.getCount();
            cursor.close();
            return countsOfCursor;
        }

        @Override
        protected void onPostExecute(Integer countsOfCursor) {
            if (countsOfCursor == 0) {
                makeAddButtonVisible();
            } else {
                makeRemoveButtonVisible();
            }
        }
    }

    private void makeAddButtonVisible(){
        mAddToWatchlistButton.setVisibility(View.VISIBLE);
        mRemoveFromWatchlistButton.setVisibility(View.GONE);
    }

    private void makeRemoveButtonVisible(){
        mAddToWatchlistButton.setVisibility(View.GONE);
        mRemoveFromWatchlistButton.setVisibility(View.VISIBLE);
    }
}
