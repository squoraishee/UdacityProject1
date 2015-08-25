package project0.android.mobilonix.com.popularmovies;

import android.animation.AnimatorSet;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PopularMoviesMainActivity extends AppCompatActivity {

    GridView movieGridView;
    ArrayList<Bitmap> imageArray;
    private Spinner sortSpinner;
    private static final String API_KEY="REPLACE_KEY";
    private static final String MOST_POPULAR_MOVIES_API =
            "http://api.themoviedb.org/3/discover/movie?" +
                    "sort_by=popularity.desc&api_key=" + API_KEY;
    private static final String HIGHEST_RATED_MOVIES_API =
            "http://api.themoviedb.org/3/discover/movie?" +
                    "sort_by?" +
                    "certification_country=US&certification=R&sort_by=" +
                    "vote_average.desc&api_key=" +
                    API_KEY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        imageArray = new ArrayList<>();
        init();
    }

    private void init() {
        sortSpinner = (Spinner)findViewById(R.id.sort_spinner);
        movieGridView = (GridView)findViewById(R.id.movie_grid_view);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        switch (position) {
                            case 0: {
                                getHigestRatedMovies();
                                break;
                            }
                            case 1: {
                                getMostPopularMovies();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
        });
        getHigestRatedMovies();
    }

    private void getMostPopularMovies() {
        DownloadApiDataTask task = new DownloadApiDataTask(this,
                new Callback<ArrayList<MovieItem>>() {
            @Override
            public void onExecute(final ArrayList<MovieItem> result) {
                movieGridView.setAdapter(new MoviePosterAdapter(
                        PopularMoviesMainActivity.this, result));
                movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            View view, int position, long id) {
                        final Dialog dialog = new Dialog(PopularMoviesMainActivity.this);
                        dialog.setContentView(R.layout.details_layout);
                        dialog.setTitle("Movie Details");

                        ImageView posterImage = (ImageView) dialog.findViewById(R.id.poster_image);
                        posterImage.setImageBitmap(result.get(position).getImageData());
                        TextView titleText = (TextView) dialog.findViewById(R.id.title_text);
                        titleText.setText(result.get(position).getTitle());
                        TextView ratingText = (TextView) dialog.findViewById(R.id.rating_text);
                        ratingText.setText(result.get(position).getVoteAverage());
                        TextView releaseDateText = (TextView)
                                dialog.findViewById(R.id.release_date_text);
                        releaseDateText.setText(result.get(position).getReleaseDate());
                        TextView synopsisText = (TextView) dialog.findViewById(R.id.synopsis_text);
                        synopsisText.setText(result.get(position).getPlotSynopsis());
                        dialog.show();
                    }
                });
            }
        });
        task.execute(MOST_POPULAR_MOVIES_API);
    }

    private void getHigestRatedMovies() {
        DownloadApiDataTask task = new DownloadApiDataTask(this,
                new Callback<ArrayList<MovieItem>>() {
            @Override
            public void onExecute(final ArrayList<MovieItem> result) {
                movieGridView.setAdapter(new
                        MoviePosterAdapter(PopularMoviesMainActivity.this, result));
                movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            View view, int position, long id) {
                        final Dialog dialog = new Dialog(PopularMoviesMainActivity.this);
                        dialog.setContentView(R.layout.details_layout);
                        dialog.setTitle("Movie Details");

                        ImageView posterImage = (ImageView)dialog.findViewById(R.id.poster_image);
                        posterImage.setImageBitmap(result.get(position).getImageData());
                        TextView titleText = (TextView) dialog.findViewById(R.id.title_text);
                        titleText.setText(result.get(position).getTitle());
                        TextView ratingText = (TextView) dialog.findViewById(R.id.rating_text);
                        ratingText.setText(result.get(position).getVoteAverage());
                        TextView releaseDateText = (TextView)
                                dialog.findViewById(R.id.release_date_text);
                        releaseDateText.setText(result.get(position).getReleaseDate());
                        TextView synopsisText = (TextView) dialog.findViewById(R.id.synopsis_text);
                        synopsisText.setText(result.get(position).getPlotSynopsis());
                        dialog.show();
                    }
                });

            }
        });
        task.execute(HIGHEST_RATED_MOVIES_API);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_popular_movies_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A custom adapter for the gridview used for storing the movies
     */
    public static class MoviePosterAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<MovieItem> movieArray;

        // Constructor
        public MoviePosterAdapter(Context context, ArrayList<MovieItem> movieArray) {
            this.context = context;
            this.movieArray = movieArray;
        }

        public int getCount() {
            return movieArray.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(final int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            }
            else
            {
                imageView = (ImageView) convertView;
            }

            downloadImageBitmap(movieArray.get(position).getImageUrl(), position, imageView);
            return imageView;
        }

        public void downloadImageBitmap(String path, final int position, final ImageView view) {
            AsyncTask<String, Bitmap, Bitmap> task = new AsyncTask<String, Bitmap, Bitmap>() {
                @Override
                protected Bitmap doInBackground(String... params) {
                    try {
                        URL urlConnection = new URL("http://image.tmdb.org/t/p/w185/" + params[0]);
                        HttpURLConnection connection = (HttpURLConnection) urlConnection
                                .openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(input);
                        return bitmap;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    view.setImageBitmap(bitmap);
                    movieArray.get(position).setImageData(bitmap);
                }
            };
            task.execute(path);
        }

    }

    /**
     * A task used to download the responses from the dbMoviesAPI
     */
    static class DownloadApiDataTask extends AsyncTask<String, String, ArrayList<MovieItem>> {

        Context context;
        Callback<ArrayList<MovieItem>> callback;

        public DownloadApiDataTask(Context context, Callback<ArrayList<MovieItem>> callback) {
            this.context = context;
            this.callback = callback;
        }

        @Override
        public void onPreExecute() {

        }

        @Override
        protected ArrayList<MovieItem> doInBackground(String... params) {

            String result = "";
            JSONObject resultJSON = null;
            URL urlConnection = null;
            HttpURLConnection connection = null;
            ArrayList<MovieItem> movieItems = new ArrayList<>();

            /* Loop through lines of the open input stream and add them to the result */
            BufferedReader in = null;
            try {

                urlConnection = new URL(params[0]);
                connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));

                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    result += inputLine + '\n';
                }

                in.close();
                resultJSON = new JSONObject(result);
                JSONArray results = resultJSON.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject movie = results.getJSONObject(i);
                    movieItems.add(new MovieItem(
                            movie.getString("title"),
                            movie.getString("poster_path"),
                            movie.getString("release_date"),
                            movie.getString("vote_average"),
                            movie.getString("overview")));
                }

            } catch (IOException e) {
                return new ArrayList<MovieItem>();
            } catch (JSONException e) {
                return new ArrayList<MovieItem>();
            }

            return movieItems;
        }

        @Override
        public void onPostExecute(ArrayList<MovieItem> result) {
            callback.onExecute(result);
        }
    }

    /**
     * A movies details model class for storing relavent movies data
     */
    public static class MovieItem {
        private String imageUrl;
        private Bitmap imageData;
        private String title;
        private String releaseDate;
        private String voteAverage;
        private String plotSynopsis;

        public MovieItem(String title, String imageUrl,
                         String releaseDate, String voteAverage, String plotSynopsis) {
            this.title = title;
            this.imageUrl = imageUrl;
            this.releaseDate = releaseDate;
            this.voteAverage = voteAverage;
            this.plotSynopsis = plotSynopsis;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public Bitmap getImageData() {
            return imageData;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public String getVoteAverage() {
            return voteAverage;
        }

        public String getPlotSynopsis() {
            return plotSynopsis;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public void setImageData(Bitmap imageData) {
            this.imageData = imageData;
        }

        public void setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
        }

        public void setVoteAverage(String voteAverage) {
            this.voteAverage = voteAverage;
        }

        public void setPlotSynopsis(String plotSynopsis) {
            this.plotSynopsis = plotSynopsis;
        }
    }

    /**
     * A generic callback interface
     *
     * @param <T>
     */
    public interface Callback<T> {
        void onExecute(T result);
    }

    /**
     * A convenience method for displaying toasts
     *
     * @param message
     */
    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


}
