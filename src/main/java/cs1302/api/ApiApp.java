package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.geometry.Pos;
import javafx.geometry.Insets;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {

    /** HTTP Client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2) // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

        /** Google {@code Gson} object for parsing JSON-formattedstrings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    private Stage stage;
    private Scene scene;
    private VBox root;

    private VBox vbox;

    private HBox statusH;
    private Button aboutMe;
    private Label statusLabel;
    private String uri;

    private VBox preferencesV;

    private HBox typeH;
    private Label typeLabel;
    private ComboBox<String> typeBox;

    private HBox genreH;
    private Label moodLabel;
    private ComboBox<String> genreBox;

    private HBox imdbH;
    private Label boundLabel;
    private ComboBox<String> boundBox;
    private TextField boundField;

    private Button getRecs;

    private VBox recV;
    private ImageView[] posters;
    private Label movieInfo;
    private Button next;

    URL apiUrl;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        this.scene = null;
        this.root = new VBox();
        vbox = new VBox(); //
        vbox.setMaxWidth(600);
        vbox.setMaxHeight(605);
        statusH = new HBox(10); //
        statusH.setPadding(new Insets(10, 10, 10, 10));
        aboutMe = new Button("About Me");
        statusLabel = new Label("Put your preferences below");
        uri = new String();
        preferencesV = new VBox(5); //
        typeH = new HBox(5); //
        typeLabel = new Label("Show or Movie?");
        typeBox = new ComboBox<>();
        genreH = new HBox(5); //
        // genreH.setPadding(new Insets(10, 10,));
        moodLabel = new Label("What are you in the mood for?");
        genreBox = new ComboBox<>();
        imdbH = new HBox(5); //
        boundLabel = new Label("Enter your desired IMDb rating");
        boundBox = new ComboBox<>();
        boundField = new TextField();
        getRecs = new Button("Get Recommendations"); //
        getRecs.setMinWidth(80);
        getRecs.setMinHeight(10);
        recV = new VBox(15); //
        posters = new ImageView[25];
        movieInfo = new Label();
        next = new Button("Next Movie");

    } // ApiApp

    /**{@inheritDoc}**/
    @Override
    public void init() {
        vbox.getChildren().addAll(statusH, preferencesV, recV);
        ObservableList<String> genres = FXCollections.observableArrayList(
            "Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary",
            "Drama", "Fantasy", "History", "Horror", "Music", "Mystery",
            "Romance", "Sci-Fi", "Thriller", "War", "Western");
        ObservableList<String> types = FXCollections.observableArrayList(
            "Show", "Movie");
        ObservableList<String> bounds = FXCollections.observableArrayList(
            "Min", "Max");
        final ComboBox<String> genreBox = new ComboBox(genres);
        final ComboBox<String> typeBox = new ComboBox(types);
        final ComboBox<String> boundBox = new ComboBox(bounds);
        genreBox.setValue("Drama");
        statusH.getChildren().addAll(aboutMe, statusLabel);
        preferencesV.getChildren().addAll(typeH, genreH, imdbH, getRecs);
        typeH.getChildren().addAll(typeLabel, typeBox);
        typeBox.setValue("Movie");
        genreH.getChildren().addAll(moodLabel, genreBox);
        genreBox.setValue("Drama");
        imdbH.getChildren().addAll(boundLabel, boundBox, boundField);
        boundBox.setValue("Min");
        VBox.setVgrow(vbox, Priority.ALWAYS);

        getRecs("tv", "8.0", "Romance");

        System.out.println("init() called");
    } // init


    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;
        this.root = new VBox();
        this.scene = new Scene(this.root);
        this.stage.setMaxWidth(1280);
        this.stage.setMaxHeight(720);

        this.stage.setTitle("Movie Picker!");

        vbox.setAlignment(Pos.CENTER);
        this.root.getChildren().add(vbox);

        this.stage.setScene(scene);
        this.stage.setOnCloseRequest(event -> Platform.exit());
        this.stage.sizeToScene();
        this.stage.show();
        Platform.runLater(() -> this.stage.setResizable(false));
    } // start

    /**{@inheritDoc}**/
    @Override
    public void stop() {
        System.out.println("stop() called");
    } // stop

    public class TMDbResult {
        String[] genre_ids;
        String overview;
        String poster_path;
        String title;
        String vote_average;
    } // TMDbResult

    private class TMDbResponse {
        int page;
        TMDbResult[] results;
    } // TMDbResponse

    private TMDbResponse getRecs(String titleType, String minRating, String genre) {
        TMDbResponse tmdbResponse = null;
        try {
            String genreID = Integer.toString(getGenreID(genre));
            String uri = "https://api.themoviedb.org/3/discover/" + titleType +
                "?vote_average.gte=" + minRating +
                "&with_genres=" + genreID;

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("accept", "application/json")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5Yz" +
                "Y4NmFmNzA0NGEyM2FkMmI4MmFlYWMwZGI5YmZjMiIsInN1YiI6IjY1NzY1ZDhmZWM4Y" +
                "TQzMDBmZDdkNTJlMyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ." +
                "Hpo3_aBfHQgG-4egTQ6FFszGMLtKQpPUmpeXJn3xOXg")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());

            /**
            if (response.statusCode() != 200) {
                IOException ioe = new IOException();
                String s = uri + "\n404";
                alertError(ioe, s);
                uri = "Failed to get recommendations.";
            } // if
            */

            // print out json string
            String jsonString = response.body();
            System.out.println("********** RAW JSON STRING: **********");
            System.out.println(jsonString.trim());
            tmdbResponse = GSON
                .fromJson(jsonString, TMDbResponse.class);
            printTMDbResponse(tmdbResponse);

        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        } // try

        return tmdbResponse;
    } // getRecs

    /**
     * Error alert stolen from project 4.
     *
     * @param cause a {@link java.lang.Throwable Throwable} that caused the alert
     * @param s String.
     */
    public static void alertError(Throwable cause, String s) {
        String errorMessage = cause.toString() + "\n" + s;
        TextArea text = new TextArea(errorMessage);
        text.setEditable(false);
        Alert alert = new Alert(AlertType.ERROR);
        alert.getDialogPane().setContent(text);
        alert.setResizable(true);
        alert.showAndWait();

        System.err.println(errorMessage);
        cause.printStackTrace(System.err);
    } // alertError

    /**
     * Just prints out the urls and junk and allat to help visualize what I'm working with.
     * Also stolen from code from the last project.
     * @param itunesResponse ItunesResponse.
     */
    private static void printTMDbResponse(TMDbResponse tmdbResponse) {
        System.out.println();
        System.out.println("********** PRETTY JSON STRING: **********");
        System.out.println(GSON.toJson(tmdbResponse));
        System.out.println();
        System.out.println("********** PARSED RESULTS: **********");
        System.out.printf("page = %s\n", tmdbResponse.page);
        for (int i = 0; i < tmdbResponse.results.length; i++) {
            System.out.printf("tmdbResponse.results[%d]:\n", i);
            TMDbResult result = tmdbResponse.results[i];
            for (String genre_id : result.genre_ids) {
                System.out.printf(" - genre_id = %s\n", genre_id);
            }
            System.out.printf(" - overview = %s\n", result.overview);
            System.out.printf(" - poster_path = %s\n", result.poster_path);
            System.out.printf(" - title = %s\n", result.title);
            System.out.printf(" - vote_average = %s\n", result.vote_average);
        } // for
    } // printItunesResponse


    private static void runNow() {
    } // runNow

    private int getGenreID(String userFriendlyGenre) {
        Map<String, Integer> genreMapping = new HashMap<>();
        genreMapping.put("Action", 28);
        genreMapping.put("Adventure", 12);
        genreMapping.put("Action & Adventure", 10759);
        genreMapping.put("Animation", 16);
        genreMapping.put("Comedy", 35);
        genreMapping.put("Documentary", 99);
        genreMapping.put("Drama", 18);
        genreMapping.put("Family", 10751);
        genreMapping.put("Fantasy", 14);
        genreMapping.put("History", 14);
        genreMapping.put("Horror", 36);
        genreMapping.put("Music", 28);
        genreMapping.put("Mystery", 28);
        genreMapping.put("Romance", 28);
        genreMapping.put("Science Fiction", 28);
        genreMapping.put("TV Movie", 28);
        genreMapping.put("Thriller", 28);
        genreMapping.put("War", 28);
        genreMapping.put("Western", 28);
        genreMapping.put("Adventure",    12);
        genreMapping.put("Action", 28);
        genreMapping.put("Animation", 16);
        genreMapping.put("Comedy", 35);
        genreMapping.put("Crime", 80);
        genreMapping.put("Documentary", 99);
        genreMapping.put("Drama", 18);
        genreMapping.put("Family", 10751);
        genreMapping.put("Fantasy", 14);
        genreMapping.put("History", 36);
        genreMapping.put("Horror", 27);
        genreMapping.put("Music", 10402);
        genreMapping.put("Mystery", 9648);
        genreMapping.put("Romance", 10749);
        genreMapping.put("Science Fiction", 878);
        genreMapping.put("Sci-Fi & Fantasy", 10765);
        genreMapping.put("Soap", 10766);
        genreMapping.put("Talk", 10767);
        genreMapping.put("TV Movie", 10770);
        genreMapping.put("Thriller", 53);
        genreMapping.put("War", 10752);
        genreMapping.put("War & Politics", 10768);
        genreMapping.put("Western", 37);
        return genreMapping.get(userFriendlyGenre);
    }


} // ApiApp
