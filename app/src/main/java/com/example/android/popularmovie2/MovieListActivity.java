package com.example.android.popularmovie2;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.android.popularmovie2.data.Movie;
import com.example.android.popularmovie2.databinding.ActivityMovieListBinding;

import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final int MOVIE_LIST_TOTAL_COLUMN = 2;
    public static final int LOADER_ID_POPULAR = 0;
    public static final int LOADER_ID_TOP_RATED = 1;

    private MovieListAdapter mAdapter;
    private Loader<List<Movie>> mMovieLoader;

    private ActivityMovieListBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_list);

        mBinding.movieListRecyclerView.setLayoutManager(new GridLayoutManager(this, MOVIE_LIST_TOTAL_COLUMN));
        mBinding.movieListRecyclerView.setHasFixedSize(true);
        mAdapter = new MovieListAdapter(this, new ArrayList<Movie>());
        mBinding.movieListRecyclerView.setAdapter(mAdapter);

        if (isConnected()) {
            getSupportLoaderManager().initLoader(LOADER_ID_POPULAR, null, this);
        } else {
            mBinding.movieListLoadingIndicator.setVisibility(View.GONE);
            mBinding.movieListNoNetworkTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_movie_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter_popular:
                getSupportLoaderManager().restartLoader(LOADER_ID_POPULAR, null, this);
                setTitle(R.string.movie_list_title_popular);
                return true;
            case R.id.action_filter_top_rated:
                getSupportLoaderManager().restartLoader(LOADER_ID_TOP_RATED, null, this);
                setTitle(R.string.movie_list_title_top_rated);
                return true;
            case R.id.action_filter_watchlist:
                Intent WatchlistIntent = new Intent(this, WatchlistActivity.class);
                startActivity(WatchlistIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int loaderId, Bundle args) {
        mMovieLoader = new MovieLoader(this, loaderId);
        return mMovieLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        mBinding.movieListLoadingIndicator.setVisibility(View.GONE);
        if (movies.isEmpty()) {
            mBinding.movieListNoResultTextView.setVisibility(View.VISIBLE);
            mBinding.movieListRecyclerView.setVisibility(View.GONE);
        } else {
            mBinding.movieListNoResultTextView.setVisibility(View.GONE);
            mBinding.movieListRecyclerView.setVisibility(View.VISIBLE);
        }
        mAdapter.updateItems(movies);
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        loader = null;
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }
}
