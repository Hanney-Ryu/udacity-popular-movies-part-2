package com.example.android.popularmovie2.util;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.popularmovie2.BuildConfig;
import com.example.android.popularmovie2.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private static final int CONN_READ_TIME = 10000;
    private static final int CONN_CONNECT_TIME = 15000;

    // the movie db
    private static final String THE_MOVIE_DB_REQUEST_BASE_URL = "https://api.themoviedb.org/3/movie";
    private static final String THE_MOVIE_DB_IMAGE_REQUEST_BASE_URL = "https://image.tmdb.org/t/p/w185";
    private static final String THE_MOVIE_DB_TRAILER_REQUEST_BASE_URL = "https://api.themoviedb.org/3/movie";

    private static final String THE_MOVIE_DB_PARAM_API_KEY = "api_key";
    private static final String THE_MOVIE_DB_API_KEY = BuildConfig.THE_MOVIE_DB_API_KEY;
    private static final String PATH_VIDEOS = "videos";

    // json key & value : movie list
    private static final String JSON_ARRAY_RESULTS = "results";
    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_TITLE = "title";
    private static final String JSON_KEY_VOTE_AVERAGE = "vote_average";
    private static final String JSON_KEY_POSTER_PATH = "poster_path";
    private static final String JSON_KEY_OVERVIEW = "overview";
    private static final String JSON_KEY_RELEASE_DATE = "release_date";

    // json key & value : movie video
    private static final String JSON_KEY_TYPE = "type";
    private static final String JSON_VALUE_TRAILER = "Trailer";
    private static final String JSON_KEY_SITE = "site";
    private static final String JSON_VALUE_YOUTUBE = "YouTube";
    private static final String JSON_KEY_KEY = "key";

    // youtube
    private static final String YOUTUBE_WATCH_VIDEO_BASE_URL = "https://www.youtube.com/watch";
    private static final String YOUTUBE_PARAM_VIDEO_KEY = "v";

    public static List<Movie> fetchMovieData(String requestUrl) {
        URL url = createUrl(requestUrl);
        String jsonResponse = makeHttpRequest(url);

        return extractFeatureFromJson(jsonResponse);
    }

    private static List<Movie> extractFeatureFromJson(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) return null;

        List<Movie> movies = new ArrayList<>();
        String id = "";
        String title = "";
        String voteAverage = "";
        String posterPath = "";
        String overview = "";
        String releaseDate = "";

        try {
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);
            if (baseJsonResponse.has(JSON_ARRAY_RESULTS)) {
                JSONArray results = baseJsonResponse.getJSONArray(JSON_ARRAY_RESULTS);
                for (int i = 0; i < results.length()
                        && isEnoughForMovie(results.getJSONObject(i)); i++) {
                    JSONObject result = results.getJSONObject(i);
                    id = result.getString(JSON_KEY_ID);
                    title = result.getString(JSON_KEY_TITLE);
                    voteAverage = result.getString(JSON_KEY_VOTE_AVERAGE);
                    posterPath = result.getString(JSON_KEY_POSTER_PATH).substring(1); //remove '/'
                    overview = result.getString(JSON_KEY_OVERVIEW);
                    releaseDate = result.getString(JSON_KEY_RELEASE_DATE);
                    movies.add(new Movie(id, title, voteAverage, posterPath, overview, releaseDate));
                }
            } else {
                Log.i(LOG_TAG, "Not find JSON Object");
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSON Exception", e);
            e.printStackTrace();
        }
        return movies;
    }

    public static String fetchTrailerUrl(String requestUrl) {
        URL url = createUrl(requestUrl);
        String jsonResponse = makeHttpRequest(url);
        String trailerId = extractTrailerIdFromJson(jsonResponse);
        return makeYoutubeUrlForTrailer(trailerId);
    }

    private static String extractTrailerIdFromJson(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) return null;

        try {
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);
            JSONArray results = baseJsonResponse.getJSONArray(JSON_ARRAY_RESULTS);
            if (results.length() == 0) {
                return null;
            }
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                if (!isEnoughForTrailer(result)) break;
                if (result.getString(JSON_KEY_TYPE).equals(JSON_VALUE_TRAILER)
                        && result.getString(JSON_KEY_SITE).equals(JSON_VALUE_YOUTUBE)) {
                    return result.getString(JSON_KEY_KEY);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSON Exception", e);
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isEnoughForMovie(JSONObject result) {
        return result.has(JSON_KEY_ID) && result.has(JSON_KEY_TITLE)
                && result.has(JSON_KEY_VOTE_AVERAGE) && result.has(JSON_KEY_POSTER_PATH)
                && result.has(JSON_KEY_OVERVIEW) && result.has(JSON_KEY_RELEASE_DATE);
    }

    private static boolean isEnoughForTrailer(JSONObject result) {
        return result.has(JSON_KEY_TYPE) && result.has(JSON_KEY_SITE)
                && result.has(JSON_KEY_SITE);
    }

    public static String makeRequestUrlForTrailer(String movieId) {
        Uri.Builder uriBuilder = Uri.parse(THE_MOVIE_DB_TRAILER_REQUEST_BASE_URL)
                .buildUpon()
                .appendPath(movieId)
                .appendPath(PATH_VIDEOS)
                .appendQueryParameter(THE_MOVIE_DB_PARAM_API_KEY, THE_MOVIE_DB_API_KEY);
        return uriBuilder.toString();
    }

    private static String makeYoutubeUrlForTrailer(String trailerId) {
        if (trailerId == null) return null;
        Uri.Builder uriBuilder = Uri.parse(YOUTUBE_WATCH_VIDEO_BASE_URL)
                .buildUpon()
                .appendQueryParameter(YOUTUBE_PARAM_VIDEO_KEY, trailerId);
        return uriBuilder.toString();
    }

    public static String makeRequestUrlForMovieList(String pathForFilter) {
        Uri.Builder uriBuilder = Uri.parse(THE_MOVIE_DB_REQUEST_BASE_URL)
                .buildUpon()
                .appendPath(pathForFilter)
                .appendQueryParameter(THE_MOVIE_DB_PARAM_API_KEY, THE_MOVIE_DB_API_KEY);
        return uriBuilder.toString();
    }

    public static String makeRequestUrlForPoster(String posterPath) {
        Uri.Builder uriBuilder = Uri.parse(THE_MOVIE_DB_IMAGE_REQUEST_BASE_URL)
                .buildUpon()
                .appendPath(posterPath);
        return uriBuilder.toString();
    }

    private static URL createUrl(String requestUrl) {
        URL url = null;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;

        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(CONN_READ_TIME);
            httpURLConnection.setConnectTimeout(CONN_CONNECT_TIME);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                inputStream = httpURLConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code : " + httpURLConnection.getResponseCode());
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving The Movie JSON results", e);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";
            try {
                line = bufferedReader.readLine();
                while (line != null) {
                    output.append(line);
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem reading The JSON Response", e);
            }
        }
        return output.toString();
    }
}
