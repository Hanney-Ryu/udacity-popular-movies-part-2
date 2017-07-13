package com.example.android.popularmovie2;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.android.popularmovie2.data.Movie;
import com.example.android.popularmovie2.util.QueryUtils;

import java.util.List;

public class MovieLoader extends AsyncTaskLoader<List<Movie>> {
    private static final String PATH_POPULAR = "popular";
    private static final String PATH_TOP_RATED = "top_rated";

    private List<Movie> mMovies;
    private int mLoaderId = -1;
    private String mUrl;

    public MovieLoader(Context context, int loaderId) {
        super(context);
        mLoaderId = loaderId;
    }

    @Override
    protected void onStartLoading() {
        if (mMovies == null) {
            forceLoad();
        } else {
            deliverResult(mMovies);
        }
    }

    @Override
    public List<Movie> loadInBackground() {
        switch (mLoaderId) {
            case MovieListActivity.LOADER_ID_TOP_RATED:
                mUrl = QueryUtils.makeRequestUrlForMovieList(PATH_TOP_RATED);
                mMovies = QueryUtils.fetchMovieData(mUrl);
                break;
            case MovieListActivity.LOADER_ID_POPULAR:
                mUrl = QueryUtils.makeRequestUrlForMovieList(PATH_POPULAR);
                mMovies = QueryUtils.fetchMovieData(mUrl);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Loader ID : " + mLoaderId);
        }
        return mMovies;
    }
}
