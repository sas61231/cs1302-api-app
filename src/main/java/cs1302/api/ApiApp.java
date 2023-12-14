package cs1302.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Integrates TMDb's API with Watchmode's API to help
 * produce program recommendations on the user's mood.
 * Will ask for type of program (movie or show), the genre
 * of the program, then the minimum user rating of the program.
 * TMDb's API is used to fetch the movie recommendations,
 * the Watchmode api is used to show the user where the program
 * is available to watch.
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

    private static final String DEF_IMG = "resources/default.png";

    private Stage stage;
    private Scene scene;
    private VBox root;

    private VBox vbox;

    private HBox statusH;
    private Button aboutMeButton;
    private Label statusLabel;
    private String uri;

    private HBox preferencesH;
    private HBox typeH;
    private Label typeLabel;
    private ComboBox<String> typeBox;

    private HBox genreH;
    private Label moodLabel;
    private ComboBox<String> genreBox;

    private HBox imdbH;
    private Label boundLabel;
    private TextField boundField;

    private TMDbResponse tmdbResponse;
    private Button getRecsButton;

    private HBox recH;
    private int currentRecIndex = 0;
    private ImageView postersImageView;
    private Image postersImage;
    private TextArea movieInfo;

    private String tmdbPosterPath;
    private String baseUrl;
    private String fullUrl;
    private Set<String> visitedTitles;

    private VBox left;
    private VBox right;

    private HBox bottom;
    private Button nextButton;

    private HBox spacer;

    Map<String, Integer> genreMapping = new HashMap<>();

    URL apiUrl;


    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        this.scene = null;
        this.root = new VBox();
        vbox = new VBox(); //
        vbox.setMinWidth(500);
        vbox.setMinHeight(550);
        vbox.setSpacing(10);
        statusH = new HBox(); //
        statusH.setSpacing(10);
        statusH.setMinWidth(500);
        statusH.setPadding(new Insets(0, 0, 0, 5));
        statusLabel = new Label("Put your preferences below");
        uri = new String();
        preferencesH = new HBox(); //
        preferencesH.setMinWidth(500);
        preferencesH.setMinHeight(50);
        typeH = new HBox(5); //
        typeLabel = new Label("Type?");
        typeBox = new ComboBox<>();
        HBox.setHgrow(typeBox, Priority.ALWAYS);
        genreH = new HBox(5); //
        genreH.setPadding(new Insets(10, 0, 0, 0));
        moodLabel = new Label("Genre?");
        genreBox = new ComboBox<>();
        HBox.setHgrow(genreBox, Priority.ALWAYS);
        imdbH = new HBox(5); //
        boundLabel = new Label("User Rating");
        boundField = new TextField();
        HBox.setHgrow(boundField, Priority.ALWAYS);
        spacer = new HBox();
        spacer.setMinHeight(7);
        spacer.setMinWidth(10);
        getRecsButton = new Button("Get Recommendations"); //
        getRecsButton.setMinWidth(80);
        getRecsButton.setMinHeight(10);
        recH = new HBox(); //
        recH.setMinWidth(500);
        recH.setMinHeight(400);
        postersImageView = new ImageView();
        postersImage = new Image("file:" + DEF_IMG);
        postersImageView.setImage(postersImage);
        movieInfo = new TextArea();
        movieInfo.setEditable(false);
        movieInfo.setMaxWidth(200);
        movieInfo.setWrapText(true);
        movieInfo.setPrefRowCount(10);
        visitedTitles = new HashSet<>();
        bottom = new HBox();
        nextButton = new Button("Next Recommendation");
        left = new VBox();
        right = new VBox();
        initializeGenreMapping(genreMapping);
    } // ApiApp

    /**{@inheritDoc}**/
    @Override
    public void init() {
        vbox.getChildren().addAll(statusH, preferencesH, recH, bottom);
        ObservableList<String> genres = FXCollections.observableArrayList(
            "Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary",
            "Drama", "Fantasy", "History", "Horror", "Music", "Mystery",
            "Romance", "Sci-Fi", "Thriller", "War", "Western");
        ObservableList<String> types = FXCollections.observableArrayList(
            "TV", "Movie");
        genreBox = new ComboBox(genres);
        typeBox = new ComboBox(types);
        genreBox.setValue("Drama");
        aboutMeButton = new Button("About Me");
        statusH.getChildren().addAll(aboutMeButton, spacer, statusLabel);
        preferencesH.getChildren().addAll(left, spacer, right);
        left.getChildren().addAll(typeH, spacer, genreH);
        right.getChildren().addAll(imdbH, spacer, getRecsButton);
        typeH.getChildren().addAll(typeLabel, typeBox);
        typeBox.setValue("Movie");
        typeBox.setMinWidth(12);
        genreH.getChildren().addAll(moodLabel, genreBox);
        genreBox.setValue("Drama");
        genreBox.setMinWidth(10);
        imdbH.getChildren().addAll(boundLabel, boundField);
        boundField.setMaxWidth(135);
        VBox.setVgrow(vbox, Priority.ALWAYS);
        recH.getChildren().addAll(postersImageView, movieInfo);
        recH.setMaxHeight(720);
        postersImageView.setImage(postersImage);
        postersImageView.setFitHeight(400);
        postersImageView.setFitWidth(300);
        bottom.getChildren().addAll(nextButton);
        typeH.setMargin(typeBox, new Insets(0, 10, 0, 0));
        HBox.setMargin(left, new Insets(0, 5, 0, 0));
        HBox.setMargin(right, new Insets(0, 0, 0, 5));
        vbox.setPadding(new Insets(10));
        nextButton.setDisable(true);
        nextButton.setStyle("-fx-opacity: 0.5;");
        getRecsButton.setOnAction(e -> getRecsPressed());
        nextButton.setOnAction(e -> {
            currentRecIndex++;
            if (currentRecIndex == tmdbResponse.results.length) {
                currentRecIndex = 0;
            } // if
            System.out.println(currentRecIndex);
            displayRecommendation(tmdbResponse, currentRecIndex);
        }); // nextButton
        aboutMeButton.setOnAction(e -> openAboutMe());
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
        this.stage.setTitle("What To Watch?");
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

    /** Represents results from the TMDb API. */
    public class TMDbResult {
        @SerializedName("genre_ids")
        String[] genreIds;
        String overview;
        @SerializedName("poster_path")
        String posterPath;
        String title;
        String name;
        @SerializedName("vote_average")
        String voteAverage;
        String id;
        private MediaSource[] mediaSources;
    } // IMDbProgram

    /** Represents results from the TMDb API. */
    public class TMDbResponse {
        int page;
        TMDbResult[] results;
    }

    /** Represents results from the Watchmode API. */
    public class MediaSource {
        @SerializedName("source_id")
        int sourceId;
        String name;
        String type;
        @SerializedName("web_url")
        String webUrl;
    }

    /**
     * Gets the recommendations based on the user's preferences,
     * creating a search query for the TMDb API.
     *
     * @param titleType String.
     * @param minRating String.
     * @param genre String.
     * @return tmdbResponse TMDbResponse
     */
    private TMDbResponse getRecs(String titleType, String minRating, String genre) {
        TMDbResponse tmdbResponse = null;

        try {
            String genreID = Integer.toString(getGenreID(genreMapping, genre));
            String uri = "https://api.themoviedb.org/3/discover/" + titleType +
                         "?vote_average.gte=" + minRating +
                         "&with_genres=" + genreID;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("accept", "application/json")
                    .header("Authorization",
                    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzMjY5NDJj" +
                    "Yjg4NDkxN2U5YjcyMThiY2JhMzAzM2I1OCIsInN1YiI6IjY1" +
                    "NzY1ZDgyODlkOTdmMDBhZWZiYmYzMiIsInNjb3BlcyI6WyJh" +
                    "cGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.iCYRCoEu1fMRXsgjGLZT-pJ5SbU7gWTV7jHSRWeQHYs")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(
                request, HttpResponse.BodyHandlers.ofString());
            String jsonString = response.body();
            tmdbResponse = GSON.fromJson(jsonString, TMDbResponse.class);
            printTMDbResponse(tmdbResponse, titleType);
        } catch (Exception e) {
            e.printStackTrace();
        } // try

        if (tmdbResponse.results.length == 0) {
            IllegalStateException ise = new IllegalStateException();
            String s = "No recommendations found :(";
            alertError(ise, s);
            statusLabel.setText("Last attempt to get recommendations failed");
        } // alertError

        return tmdbResponse;
    } // getRecs

    /**
     * Gets the streaming availability of the recommendations,
     * using the Watchmode API.
     *
     * @param titleType String.
     * @param tmdbresult TMDbResult.
     * @return mediaSources MediaSource.
     */
    private MediaSource[] getStreamingSources(String titleType, TMDbResult tmdbresult) {
        MediaSource[] mediaSources = null;

        try {
            String apiKey = "YpAN6K13oKNnWZm2n3rjkAix24WouDWcUT8z28X1";
            String regionCode = "US";
            String apiTitleID = String.format("%s-%s", titleType, tmdbresult.id);
            String apiUrl = String.format
                ("https://api.watchmode.com/v1/title/%s/sources/?apiKey=%s&regions=%s",
                    apiTitleID, apiKey, regionCode);
            URL url = new URL(apiUrl);
            System.out.println(url.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                mediaSources = GSON.fromJson(response.toString(), MediaSource[].class);
            } else {
                System.out.println("Error: " + responseCode);
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } // if

        return mediaSources;
    } // getMediaSources


    /**
     * Code that helps me visualize my output.
     * @param tmdbResponse TMDbResponse.
     * @param titleType String.
     */
    public static void printTMDbResponse(TMDbResponse tmdbResponse, String titleType) {
        for (TMDbResult result : tmdbResponse.results) {
            String contentName = getTitle(result, titleType);
            // System.out.println("genre ids:");
            // for(String genre_id : result.genre_ids)
            //     System.out.println("\t" + genre_id);
            // System.out.println("overview: \n\t" + result.overview);
            // System.out.println("poster path: \n\t" + result.poster_path);
            // System.out.println("title: \n\t" + result.title);
            // System.out.println("vote average: \n\t" + result.vote_average + "\n");
            System.out.println("content name: " + contentName + " id: " + result.id);
        }
    }

    /**
     * Code that helps me visualize my output.
     * @param tmdbResponse TMDbResponse.
     * @param titleType String.
     */
    public static void printMediaSources(String titleType, TMDbResponse tmdbResponse) {
        for (TMDbResult result : tmdbResponse.results) {
            String contentName = getTitle(result, titleType);
            if (result.mediaSources != null) {
                for (MediaSource source : result.mediaSources) {
                    System.out.println("content name: " + contentName + "\tsource: " + source.name);
                }
            } else {
                System.out.println("content name: " + contentName + "\tsource: null");
            }
        }
    } // printMediaSources

    /**
     * Displays the movie recommendation one a time for the user.
     *
     * @param tmdbResponse TMDbResponse.
     * @param currentRecIndex int.
     */
    private void displayRecommendation(TMDbResponse tmdbResponse, int currentRecIndex) {
        TMDbResult result = tmdbResponse.results[currentRecIndex];

        try {
            String basePosterPathUrl = "https://image.tmdb.org/t/p/w500";
            String fullPosterPathUrl = basePosterPathUrl + result.posterPath;
            postersImageView = new ImageView();
            postersImage = new Image(fullPosterPathUrl);
            postersImageView.setImage(postersImage);
            postersImageView.setFitWidth(300);
            postersImageView.setFitHeight(400);
            postersImageView.setPreserveRatio(true);
            System.out.println(fullPosterPathUrl);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }

        String recommendationInfo = buildRecommendationInfo(result);

        movieInfo.setText(recommendationInfo);
        recH.getChildren().clear();
        recH.getChildren().addAll(postersImageView, movieInfo);
        nextButton.setDisable(false);
        nextButton.setStyle("-fx-opacity: 1.0;");
    } // displayRecommendation

    /**
     * Gets movie title.
     *
     * @param result TMDbResult.
     * @param titleType String.
     * @return titleType String.
     */
    private static String getTitle(TMDbResult result, String titleType) {
        return titleType.equals("movie") ? result.title : result.name;
    } //getTitle

    /**
     * Builds the recommendation info.
     *
     * @param result TMDbResult.
     * @return recommendationInfo String.
     */
    private String buildRecommendationInfo(TMDbResult result) {
        String title = (result.title != null) ? result.title : "Not available";
        String overview = (result.overview != null) ? result.overview : "Not available";
        String voteAverage = (result.voteAverage != null) ? result.voteAverage : "Not available";
        String recommendationInfo = String.format(
            "TITLE: %s\nOVERVIEW: %s", title, overview);

        List<String> genres = new ArrayList<>();
        for (String genreId : result.genreIds) {
            String userFriendlyGenre = getUserFriendlyGenre(
                genreMapping, Integer.parseInt(genreId));
            genres.add(userFriendlyGenre);
        }
        recommendationInfo += "\nGENRE: " + String.join(", ", genres);
        recommendationInfo += "\nUSER RATINGS: " + voteAverage;
        Set<String> uniqueStreamingSources = new HashSet<>();
        List<String> streamingServiceNames = new ArrayList<>();
        if (result.mediaSources != null) {
            if (result.mediaSources.length > 0) {
                for (MediaSource source : result.mediaSources) {
                    String streamingServiceName = source.name;
                    if (uniqueStreamingSources.add(streamingServiceName)) {
                        streamingServiceNames.add(streamingServiceName);
                    } // if
                } // for

                recommendationInfo += "\nStreaming Services: "
                    + String.join(", ", streamingServiceNames);
            } else {
                recommendationInfo += "\nStreaming Services: Not available";
            } // if
        } else {
            recommendationInfo += "\nStreaming Services: Not available";
        } // if

        return recommendationInfo;
    } // buildRecommendationInfo

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
     * Maps the Genre IDs into readable words.
     *
     * @param genreMapping Map.
     */
    private static void initializeGenreMapping(Map<String, Integer> genreMapping) {
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
    } //initializeGenreMapping

    /**
     * Gets the Genre IDs from TMDb responses.
     * @param genreMapping Map.
     * @param userFriendlyGenre String.
     * @return genreMapping Map.
     */
    private static int getGenreID(Map<String, Integer> genreMapping, String userFriendlyGenre) {
        System.out.println(userFriendlyGenre);
        return genreMapping.get(userFriendlyGenre);
    } // getGenreID

    /**
     * Converts the Genre IDs to readable text.
     * @param genreMapping Map.
     * @param genreID int.
     * @return entry Map.
     */
    private static String getUserFriendlyGenre(Map<String, Integer> genreMapping, int genreID) {
        for (Map.Entry<String, Integer> entry : genreMapping.entrySet()) {
            if (entry.getValue() == genreID) {
                return entry.getKey();
            } // if
        } // for

        return "Unknown Genre";
    } // getUserFriendlyGenre

    /**
     * Opens the about me stage.
     */
    private void openAboutMe() {
        Stage aboutMeStage = new Stage();
        aboutMeStage.setTitle("About Me");
        TextArea aboutMeTextArea = new TextArea();
        aboutMeTextArea.setMinWidth(400.0);
        aboutMeTextArea.setMinHeight(300.0);
        aboutMeTextArea.setEditable(false);
        aboutMeTextArea.setWrapText(true);
        aboutMeTextArea.setText(" \"What To Watch\" is an app that " +
                "integrates the TMDb API and the Watchmode API! It builds a " +
                "search query with the TMDb API using an Http Request. It creates " +
                "tv or movie recommendations based on the user's preferences. " +
                "It then takes the response from TMDb and sends it to Watchmode's API " +
                "to show where the program can be watched! " +
                "To start, choose type of program, then preferred genre, then enter a " +
                "double value from 0.0 to 10.0 in the User Rating field, then click the button! " +
                "If you are unsatisfied with the recommendation, click the next recommendation " +
                "to get another movie displayed to you. \n\n Note: the Watchmode API has 1000 " +
                "uses per month. Once it runs out, a 402 message will display in the terminal, " +
                "but the rest of the program should work fine! Thanks!");
        VBox aboutMeV = new VBox(10);
        aboutMeV.getChildren().add(aboutMeTextArea);
        Scene aboutMeScene = new Scene(aboutMeV, 400, 350);
        aboutMeStage.setScene(aboutMeScene);
        aboutMeStage.show();
    } // openAboutMe

    /**
     * Event handler for getRecsButton.
     */
    private void getRecsPressed() {
        statusLabel.setText("Getting Recommendations...");

        String titleType = URLEncoder.encode(typeBox.getValue()
            .toLowerCase(), StandardCharsets.UTF_8);
        String genre = URLEncoder.encode(genreBox.getValue(),
            StandardCharsets.UTF_8);
        String minRating = URLEncoder.encode(boundField.getText(),
            StandardCharsets.UTF_8);
        double value = 0.0;
        System.out.println(titleType);

        try {
            value = Double.parseDouble(minRating);
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
            String s = uri +
                "Please enter a Double value, from 0.0 to 10.0";
            alertError(exception, s);
            statusLabel.setText("Last attempt to get recommendations failed");
            return;
        }
        if (!minRating.isEmpty()) {
            value = Double.parseDouble(minRating);
        } else {
            value = 0.0;
        }

        if (value >= 0.0 && value <= 10.0) {
            System.out.println(titleType);
            System.out.println(genre);
            System.out.println(minRating);
            try {
                tmdbResponse = getRecs(titleType, minRating, genre);
                for (TMDbResult result : tmdbResponse.results) {
                    result.mediaSources = getStreamingSources(titleType, result);
                    statusLabel.setText(uri);
                } // for
                if (tmdbResponse != null && tmdbResponse.results.length > 0) {
                    displayRecommendation(tmdbResponse, currentRecIndex);
                    statusLabel.setText(uri);
                } // if
            } catch (Exception exception) {
                exception.printStackTrace();
            } // try
        } else {
            IllegalArgumentException iae = new IllegalArgumentException();
            String s = uri +
                "Please enter a Double value, from 0.0 to 10.0";
            alertError(iae, s);
            statusLabel.setText("Last attempt to get recommendations failed");
        }
    } // getRecsButton

} // ApiApp
