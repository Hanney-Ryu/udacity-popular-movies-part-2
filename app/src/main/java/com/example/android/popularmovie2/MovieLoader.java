package com.example.android.popularmovie2;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.android.popularmovie2.data.Movie;
import com.example.android.popularmovie2.util.QueryUtils;

import java.util.List;

public class MovieLoader extends AsyncTaskLoader<List<Movie>> {
    String mUrl;

    public MovieLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Movie> loadInBackground() {
        return QueryUtils.fetchMovieData(mUrl);
    }
}
