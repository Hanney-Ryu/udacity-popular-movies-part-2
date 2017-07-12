package com.example.android.popularmovie2;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovie2.data.Movie;
import com.example.android.popularmovie2.util.QueryUtils;
import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        ImageView posterImageView = (ImageView) findViewById(R.id.movie_detail_poster_image);
        TextView titleTextView = (TextView) findViewById(R.id.movie_detail_title);
        TextView releaseDateTextView = (TextView) findViewById(R.id.movie_detail_release_date);
        TextView voteAverageTextView = (TextView) findViewById(R.id.movie_detail_vote_average);
        TextView overViewTextView = (TextView) findViewById(R.id.movie_detail_overview);

        Movie currentMovie = getIntent().getExtras().getParcelable(Movie.class.getSimpleName());
        String requestUrlForPoster = QueryUtils.makeRequestUrlForPoster(currentMovie.getPosterPath());

        Picasso.with(this)
                .load(requestUrlForPoster)
                .into(posterImageView);

        titleTextView.setText(currentMovie.getTitle());
        releaseDateTextView.setText(currentMovie.getReleaseDate());
        voteAverageTextView.setText(currentMovie.getVoteAverage());
        overViewTextView.setText(currentMovie.getOverview());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
