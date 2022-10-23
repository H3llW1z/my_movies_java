package com.example.mymovies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymovies.adapters.ReviewAdapter;
import com.example.mymovies.adapters.TrailerAdapter;
import com.example.mymovies.data.FavouriteMovie;
import com.example.mymovies.data.MainViewModel;
import com.example.mymovies.data.Movie;
import com.example.mymovies.data.Review;
import com.example.mymovies.data.Trailer;
import com.example.mymovies.utils.JsonUtils;
import com.example.mymovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private ImageView imageViewBigPoster;
    private TextView textViewTitle;
    private TextView textViewOriginalTitle;
    private TextView textViewRating;
    private TextView textViewReleaseDate;
    private TextView textViewOverview;
    private ImageView imageViewAddToFavourite;

    private int id;
    private Movie movie;
    private FavouriteMovie favouriteMovie;

    private MainViewModel viewModel;

    private RecyclerView recyclerViewTrailers;
    private RecyclerView recyclerViewReviews;
    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;

    private static String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        language = Locale.getDefault().getLanguage();
        imageViewBigPoster = findViewById(R.id.imageViewBigPoster);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewOriginalTitle = findViewById(R.id.textViewOriginalTitle);
        textViewRating = findViewById(R.id.textViewRating);
        textViewReleaseDate = findViewById(R.id.textViewReleaseDate);
        textViewOverview = findViewById(R.id.textViewOverview);
        imageViewAddToFavourite = findViewById(R.id.imageViewAddToFavourite);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            id = intent.getIntExtra("id", -1);
        } else {
            finish();
        }
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        movie = viewModel.getMovieById(id);
        Picasso.get().load(movie.getBigPosterPath()).placeholder(R.drawable.placeholder_large).into(imageViewBigPoster);
        textViewTitle.setText(movie.getTitle());
        textViewOriginalTitle.setText(movie.getOriginalTitle());
        textViewRating.setText(String.format("%s", movie.getVoteAverage()));
        textViewReleaseDate.setText(movie.getReleaseDate());
        textViewOverview.setText(movie.getOverview());
        setFavourite();
        imageViewAddToFavourite.setOnClickListener(view -> {
            if (favouriteMovie == null) {
                viewModel.insertFavouriteMovie(new FavouriteMovie(movie));
                Toast.makeText(this, R.string.add_to_fav, Toast.LENGTH_SHORT).show();
            } else {
                viewModel.deleteFavouriteMovie(favouriteMovie);
                Toast.makeText(this, R.string.remove_from_fav, Toast.LENGTH_SHORT).show();
            }
            setFavourite();
        });
        recyclerViewTrailers = findViewById(R.id.recyclerViewTrailers);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTrailers.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter();
        trailerAdapter = new TrailerAdapter();
        trailerAdapter.setOnTrailerClickListener(url -> {
            Intent intentToTrailer = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intentToTrailer);
        });
        recyclerViewReviews.setAdapter(reviewAdapter);
        recyclerViewTrailers.setAdapter(trailerAdapter);
        JSONObject jsonObjectTrailers = NetworkUtils.getJSONForVideos(movie.getId(), language);
        JSONObject jsonObjectVideos = NetworkUtils.getJSONForReviews(movie.getId(), language);
        ArrayList<Trailer> trailers = JsonUtils.getTrailersFromJSON(jsonObjectTrailers);
        ArrayList<Review> reviews = JsonUtils.getReviewsFromJSON(jsonObjectVideos);
        reviewAdapter.setReviews(reviews);
        trailerAdapter.setTrailers(trailers);
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

    private void setFavourite() {
        favouriteMovie = viewModel.getFavouriteMovieById(id);
        if (favouriteMovie == null) {
            imageViewAddToFavourite.setImageResource(R.drawable.star_outline);
        } else {
            imageViewAddToFavourite.setImageResource(R.drawable.star_filled);
        }
    }
}