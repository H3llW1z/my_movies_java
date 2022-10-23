package com.example.mymovies.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class NetworkUtils {

    private static final String BASE_URL = "https://api.themoviedb.org/3/discover/movie";
    private static final String BASE_URL_VIDEOS = "https://api.themoviedb.org/3/movie/%s/videos";
    private static final String BASE_URL_REVIEWS = "https://api.themoviedb.org/3/movie/%s/reviews";

    private static final String PARAMS_API_KEY = "api_key";
    private static final String PARAMS_LANGUAGE = "language";
    private static final String PARAMS_SORT_BY = "sort_by";
    private static final String PARAMS_PAGE = "page";
    private static final String PARAMS_MIN_VOTE_COUNT = "vote_count.gte";

    private static final String API_KEY = "c80fe70af1132219c63f266b9c2104e1";
    private static final String SORT_BY_POPULARITY = "popularity.desc";
    private static final String SORT_BY_TOP_RATED = "vote_average.desc";
    private static final String MIN_VOTE_COUNT_VALUE = "1000";

    public static final int POPULARITY = 0;
    public static final int TOP_RATED = 1;

    public static URL buildUrlToVideos(int id, String lang) {
        Uri uri = Uri.parse(String.format(BASE_URL_VIDEOS, id))
                .buildUpon()
                .appendQueryParameter(PARAMS_API_KEY, API_KEY)
                .appendQueryParameter(PARAMS_LANGUAGE, lang)
                .build();
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getJSONForVideos(int id, String lang) {
        JSONObject result = null;
        URL url= buildUrlToVideos(id, lang);
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static URL buildUrlToReviews(int id, String lang) {
        Uri uri = Uri.parse(String.format(BASE_URL_REVIEWS, id))
                .buildUpon()
                .appendQueryParameter(PARAMS_API_KEY, API_KEY)
                .appendQueryParameter(PARAMS_LANGUAGE, lang)
                .build();
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getJSONForReviews(int id, String lang) {
        JSONObject result = null;
        URL url= buildUrlToReviews(id, lang);
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static URL buildUrl(int sortBy, int page, String lang) {
        URL result = null;
        String sortingMethod;
        if (sortBy == POPULARITY) {
            sortingMethod = SORT_BY_POPULARITY;
        } else {
            sortingMethod = SORT_BY_TOP_RATED;
        }
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(PARAMS_API_KEY, API_KEY)
                .appendQueryParameter(PARAMS_LANGUAGE, lang)
                .appendQueryParameter(PARAMS_SORT_BY, sortingMethod)
                .appendQueryParameter(PARAMS_MIN_VOTE_COUNT, MIN_VOTE_COUNT_VALUE)
                .appendQueryParameter(PARAMS_PAGE, Integer.toString(page))
                .build();

        try {
            result = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONObject getJSONFromNetwork(int sortBy, int page, String lang) {
        JSONObject result = null;
        URL url= buildUrl(sortBy, page, lang);
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static class JSONLoader extends AsyncTaskLoader<JSONObject> {
        public interface OnStartLoadingListener {
            void onStartLoading();
        }
        private Bundle bundle;
        private OnStartLoadingListener onStartLoadingListener;

        public void setOnStartLoadingListener(OnStartLoadingListener onStartLoadingListener) {
            this.onStartLoadingListener = onStartLoadingListener;
        }

        public JSONLoader(@NonNull Context context, Bundle bundle) {
            super(context);
            this.bundle = bundle;
        }

        @Override
        protected void onStartLoading() {
            if (onStartLoadingListener != null) {
                onStartLoadingListener.onStartLoading();
            }
            super.onStartLoading();
            forceLoad();
        }

        @Nullable
        @Override
        public JSONObject loadInBackground() {
            if (bundle == null) {
                return null;
            }
            String urlAsString = bundle.getString("url");
            URL url = null;
            try {
                url = new URL(urlAsString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (url == null) {
                return null;
            }
            JSONObject result = null;
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                StringBuilder builder = new StringBuilder();
                while(line != null) {
                    builder.append(line);
                    line = reader.readLine();
                }
                result = new JSONObject(builder.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result;
        }
    }
    private static class JSONLoadTask extends AsyncTask<URL, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(URL... urls) {
            JSONObject result = null;
            if (urls == null || urls.length == 0) {
                return result;
            }
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) urls[0].openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                StringBuilder builder = new StringBuilder();
                while(line != null) {
                    builder.append(line);
                    line = reader.readLine();
                }
                result = new JSONObject(builder.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result;
        }
    }
}
