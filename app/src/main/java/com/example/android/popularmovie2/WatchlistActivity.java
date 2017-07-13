package com.example.android.popularmovie2;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovie2.data.Movie;
import com.example.android.popularmovie2.data.MovieContract.MovieEntry;

import java.util.ArrayList;

public class WatchlistActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = WatchlistActivity.class.getSimpleName();

    private static final int MOVIE_LIST_TOTAL_COLUMN = 2;
    private static final int WATCHLIST_LOADER_ID = 2;

    private ProgressBar mLoadingIndicator;
    private TextView mNoResultTextView;
    private RecyclerView mMovieListRecyclerView;
    private WatchlistAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        setTitle(getString(R.string.watchlist_title));

        mLoadingIndicator = (ProgressBar) findViewById(R.id.movie_list_loading_indicator);
        mNoResultTextView = (TextView) findViewById(R.id.movie_list_no_result_text_view);
        mMovieListRecyclerView = (RecyclerView) findViewById(R.id.movie_list_recycler_view);
        mMovieListRecyclerView.setLayoutManager(new GridLayoutManager(this, MOVIE_LIST_TOTAL_COLUMN));
        mMovieListRecyclerView.setHasFixedSize(true);
        mAdapter = new WatchlistAdapter(this);
        mMovieListRecyclerView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(WATCHLIST_LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(WATCHLIST_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle loaderArgs) {
        return new AsyncTaskLoader<Cursor>(this) {
            Cursor mMovies = null;

            @Override
            protected void onStartLoading() {
                if (mMovies != null) {
                    deliverResult(mMovies);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    return getContentResolver().query(MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            MovieEntry.COLUMN_TIMESTAMP);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(Cursor data) {
                mMovies = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);

        mLoadingIndicator.setVisibility(View.GONE);
        if (data.getCount() == 0) {
            mNoResultTextView.setVisibility(View.VISIBLE);
            mMovieListRecyclerView.setVisibility(View.GONE);
        } else {
            mNoResultTextView.setVisibility(View.GONE);
            mMovieListRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
