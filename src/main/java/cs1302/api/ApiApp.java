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
            "Action", "Adventure", "Animation", "Comedy", "Crime", "Drama", "Fantasy",
            "Horror", "Mystery", "Romance", "Sci-Fi", "Thriller");
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

        ApiApp recService = new ApiApp();
        List<String> recommendations = recService.getRecs("movie", 7.0, 10.0, "Action");

        System.out.println("Recommendations:");
        for (String recommendation : recommendations) {
            System.out.println(recommendation);
        } // for

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

    public class IMDbProgram {
        String type;
        double imdbRating;
        String genreTwo;
        String title;

        // Getter for 'type'
        public String getType() {
            return type;
        }

        // Getter for 'imdbRating'
        public double getIMDbRating() {
            return imdbRating;
        }

        // Getter for 'genreTwo'
        public String getGenre() {
            return genre;
        }

        // Getter for 'title'
        public String getTitle() {
            return title;
        }

    } // IMDbProgram

    private List<String> getRecs(String titleType, double minRating,
    double maxRating, String genre) {
        List<String> recommendations = new ArrayList<>();

        try {
            apiUrl = new URL("https://imdb-api.com/API/AdvancedSearch/k_21eqc28g");
            URL url = new URL(apiURL);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());

            Gson gson = new Gson();
            IMDbProgram[] programs = gson.fromJson(reader, IMDbProgram[].class);

            for (IMDbProgram program : programs) {
                if (program.getType().equalsIgnoreCase(type)
                && program.getIMDbRating() >= minRating && program.getIMDbRating() <= maxRating
                && program.getGenre().equalsIgnoreCase(genre)) {
                    recommendations.add(program.getTitle());
                } // if
            } // for

            /**
            if (response.code() != 200) {
                IOException ioe = new IOException();
                String s = uri + "\n404";
                alertError(ioe, s);
                uri = "Failed to get recommendations.";
            } // if
            */
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        } // try

        return recommendations;
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
     *
    private static void printIMDbResponse(IMDbResponse imdbResponse) {
        System.out.println();
        System.out.println("********** PRETTY JSON STRING: **********");
        System.out.println(GSON.toJson(imdbResponse));
        System.out.println();
        System.out.println("********** PARSED RESULTS: **********");
        System.out.printf("resultCount = %s\n", imdbResponse.resultCount);
        for (int i = 0; i < imdbResponse.results.length; i++) {
            IMDbResult result = imdbResponse.totalResults;
            System.out.printf(" - title = %s\n", result.wrapperType);
            System.out.printf(" - kind = %s\n", result.kind);
            System.out.printf(" - artworkUrl100 = %s\n", result.artworkUrl100);
        } // for
    } // printItunesResponse
        */

    private static void runNow() {
    } // runNow


    /**
    private something getStreamSites(String imdbId) {
        //
        }
    */

} // ApiApp
