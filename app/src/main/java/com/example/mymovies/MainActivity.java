package com.example.mymovies;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mymovies.adapters.MovieAdapter;
import com.example.mymovies.data.MainViewModel;
import com.example.mymovies.data.Movie;
import com.example.mymovies.utils.JsonUtils;
import com.example.mymovies.utils.NetworkUtils;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {
    private RecyclerView recyclerViewPosters;
    private MovieAdapter movieAdapter;
    private SwitchMaterial switchSort;
    private TextView textViewPopularity;
    private TextView textViewTopRated;
    private ProgressBar progressBarLoading;

    private MainViewModel viewModel;

    private static final int LOADER_ID = 124;
    private LoaderManager loaderManager;

    private static int page = 1;
    private static int methodOfSort;
    private static boolean isLoading = false;

    private static String language;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_main) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if (id == R.id.item_favourite) {
            Intent intentToFav = new Intent(this, FavouriteActivity.class);
            startActivity(intentToFav);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        language = Locale.getDefault().getLanguage();
        loaderManager = LoaderManager.getInstance(this);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        recyclerViewPosters = findViewById(R.id.recyclerViewPosters);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        switchSort = findViewById(R.id.switchSort);
        recyclerViewPosters.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        movieAdapter = new MovieAdapter();
        JSONObject jsonObject = NetworkUtils.getJSONFromNetwork(NetworkUtils.POPULARITY, 1, language);
        ArrayList<Movie> movies = JsonUtils.getMoviesFromJSON(jsonObject);
        for (Movie movie: movies) {
            Log.i("MyResult", movie.getTitle());
        }
        movieAdapter.setClickListener(position -> {
            Movie movie = movieAdapter.getMovies().get(position);
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("id", movie.getId());
            startActivity(intent);
        });
        movieAdapter.setOnReachEndListener(() -> {
            if (!isLoading) {
                downloadData(methodOfSort, page);
            }

        });
        recyclerViewPosters.setAdapter(movieAdapter);
        switchSort.setChecked(true);
        textViewPopularity = findViewById(R.id.textViewPopularity);
        textViewTopRated = findViewById(R.id.textViewTopRated);
        switchSort.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            page = 1;
            setMethodOfSort(isChecked);
        });
        switchSort.setChecked(false);
        textViewPopularity.setOnClickListener((view) -> {
            setMethodOfSort(false);
            switchSort.setChecked(false);
        });
        textViewTopRated.setOnClickListener((view) -> {
            setMethodOfSort(true);
            switchSort.setChecked(true);
        });

        LiveData<List<Movie>> moviesFromLiveData = MainViewModel.getMovies();
        moviesFromLiveData.observe(this, movies1 -> {
            if (page == 1) {
                movieAdapter.setMovies(movies1);
            }
        });
    }

    private int getColumnCount() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return Math.max(width / 185, 2);
    }
    private void setMethodOfSort(boolean isTopRated) {
        if (isTopRated) {
            textViewTopRated.setTextColor(getResources().getColor(R.color.teal_200));
            textViewPopularity.setTextColor(getResources().getColor(R.color.white));
            methodOfSort = NetworkUtils.TOP_RATED;
        } else {
            textViewTopRated.setTextColor(getResources().getColor(R.color.white));
            textViewPopularity.setTextColor(getResources().getColor(R.color.teal_200));
            methodOfSort = NetworkUtils.POPULARITY;
        }
        downloadData(methodOfSort, page);
    }

    private void downloadData(int methodOfSort, int page) {
        URL url = NetworkUtils.buildUrl(methodOfSort, page, language);
        Bundle bundle = new Bundle();
        bundle.putString("url", url.toString());
        loaderManager.restartLoader(LOADER_ID, bundle, this);
    }

    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
        NetworkUtils.JSONLoader jsonLoader = new NetworkUtils.JSONLoader(this, args);
        jsonLoader.setOnStartLoadingListener(() -> {
            progressBarLoading.setVisibility(View.VISIBLE);
            isLoading = true;
        });
        return jsonLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {
        ArrayList<Movie> movies = JsonUtils.getMoviesFromJSON(data);
        if (!movies.isEmpty()) {
            if (page == 1) {
                viewModel.deleteAllMovies();
                movieAdapter.clear();
            }
            for (Movie movie: movies) {
                viewModel.insertMovie(movie);
            }
            movieAdapter.addMovies(movies);
            page++;
        }
        isLoading = false;
        progressBarLoading.setVisibility(View.INVISIBLE);
        loaderManager.destroyLoader(LOADER_ID);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }
}