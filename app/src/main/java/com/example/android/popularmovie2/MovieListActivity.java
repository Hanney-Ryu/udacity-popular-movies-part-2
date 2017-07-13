package com.example.android.popularmovie2;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovie2.data.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final int MOVIE_LIST_TOTAL_COLUMN = 2;
    public static final int LOADER_ID_POPULAR = 0;
    public static final int LOADER_ID_TOP_RATED = 1;

    private ProgressBar mLoadingIndicator;
    private TextView mNoNetworkTextView;
    private TextView mNoResultTextView;
    private RecyclerView mMovieListRecyclerView;
    private MovieListAdapter mAdapter;
    private Loader<List<Movie>> mMovieLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.movie_list_loading_indicator);
        mNoNetworkTextView = (TextView) findViewById(R.id.movie_list_no_network_text_view);
        mNoResultTextView = (TextView) findViewById(R.id.movie_list_no_result_text_view);
        mMovieListRecyclerView = (RecyclerView) findViewById(R.id.movie_list_recycler_view);
        mMovieListRecyclerView.setLayoutManager(new GridLayoutManager(this, MOVIE_LIST_TOTAL_COLUMN));
        mMovieListRecyclerView.setHasFixedSize(true);
        mAdapter = new MovieListAdapter(this, new ArrayList<Movie>());
        mMovieListRecyclerView.setAdapter(mAdapter);

        if (isConnected()) {
            getSupportLoaderManager().initLoader(LOADER_ID_POPULAR, null, this);
        } else {
            mLoadingIndicator.setVisibility(View.GONE);
            mNoNetworkTextView.setVisibility(View.VISIBLE);
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
        mLoadingIndicator.setVisibility(View.GONE);
        if (movies.isEmpty()) {
            mNoResultTextView.setVisibility(View.VISIBLE);
            mMovieListRecyclerView.setVisibility(View.GONE);
        } else {
            mNoResultTextView.setVisibility(View.GONE);
            mMovieListRecyclerView.setVisibility(View.VISIBLE);
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
