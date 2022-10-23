package com.example.mymovies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.mymovies.adapters.MovieAdapter;
import com.example.mymovies.data.FavouriteMovie;
import com.example.mymovies.data.MainViewModel;
import com.example.mymovies.data.Movie;

import java.util.ArrayList;
import java.util.List;

public class FavouriteActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFavouriteMovies;
    private MovieAdapter adapter;
    private MainViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        recyclerViewFavouriteMovies = findViewById(R.id.recyclerViewFavouriteMovies);
        recyclerViewFavouriteMovies.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new MovieAdapter();
        adapter.setClickListener(position -> {
            Movie movie = adapter.getMovies().get(position);
            Intent intent = new Intent(FavouriteActivity.this, DetailActivity.class);
            intent.putExtra("id", movie.getId());
            startActivity(intent);
        });
        recyclerViewFavouriteMovies.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        LiveData<List<FavouriteMovie>> favouriteMovies = viewModel.getFavouriteMovies();
        favouriteMovies.observe(this, favouriteMovies1 -> {
            if (favouriteMovies1 != null) {
                List<Movie> movies = new ArrayList<>(favouriteMovies1);
                adapter.setMovies(movies);
            }
        });
    }

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
}