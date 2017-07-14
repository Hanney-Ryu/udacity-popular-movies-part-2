package com.example.android.popularmovie2;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;

import com.example.android.popularmovie2.data.MovieContract.MovieEntry;
import com.example.android.popularmovie2.databinding.ActivityMovieListBinding;

public class WatchlistActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = WatchlistActivity.class.getSimpleName();

    private static final int MOVIE_LIST_TOTAL_COLUMN = 2;
    private static final int WATCHLIST_LOADER_ID = 2;

    private WatchlistAdapter mAdapter;

    private ActivityMovieListBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_list);
        setTitle(getString(R.string.watchlist_title));

        mBinding.movieListRecyclerView.setLayoutManager(new GridLayoutManager(this, MOVIE_LIST_TOTAL_COLUMN));
        mBinding.movieListRecyclerView.setHasFixedSize(true);
        mAdapter = new WatchlistAdapter(this);
        mBinding.movieListRecyclerView.setAdapter(mAdapter);

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
        if (data.getCount() == 0) {
            mBinding.movieListNoResultTextView.setVisibility(View.VISIBLE);
            mBinding.movieListRecyclerView.setVisibility(View.GONE);
        } else {
            mBinding.movieListNoResultTextView.setVisibility(View.GONE);
            mBinding.movieListRecyclerView.setVisibility(View.VISIBLE);
        }
        mBinding.movieListLoadingIndicatorFrame.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
