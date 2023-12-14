# Deadline

Modify this file to satisfy a submission requirement related to the project
deadline. Please keep this file organized using Markdown. If you click on
this file in your GitHub repository website, then you will see that the
Markdown is transformed into nice-looking HTML.

## Part 1.1: App Description

> Please provide a friendly description of your app, including
> the primary functions available to users of the app. Be sure to
> describe exactly what APIs you are using and how they are connected
> in a meaningful way.

> **Also, include the GitHub `https` URL to your repository.**

"What To Watch" is an app that integrates the TMDb API and the Watchmode API! It builds a search query with the TMDb API using an Http Request. It creates tv or movie recommendations based on the user's preferences. It then takes the response from TMDb and sends it to Watchmode's API to show where the program can be watched! To start, choose type of program, then preferred genre, then enter a double value from 0.0 to 10.0 in the User Rating field, then click the button! If you are unsatisfied with the recommendation, click the next recommendation to get another movie displayed to you.

 Note: the Watchmode API has 1000 uses per month. Once it runs out, a 402 message will display in the terminal, but the rest of the program should work fine! Thanks!

https://github.com/sas61231/cs1302-api-app?tab=readme-ov-file#submission-instructions

## Part 1.2: APIs

> For each RESTful JSON API that your app uses (at least two are required),
> include an example URL for a typical request made by your app. If you
> need to include additional notes (e.g., regarding API keys or rate
> limits), then you can do that below the URL/URI. Placeholders for this
> information are provided below. If your app uses more than two RESTful
> JSON APIs, then include them with similar formatting.

### API 1

GET https://api.themoviedb.org/3/discover/movie?api_key=YOUR_API_KEY&with_original_language=en&with_genres=28,35&vote_average.gte=7.5&query=The Matrix&include_adult=false


> TMDb API

### API 2

GET https://api.watchmode.com/v1/title/{apiTitleID}/sources/?apiKey={apiKey}&regions={regionCode}

> Watchmode

## Part 2: New

> What is something new and/or exciting that you learned from working
> on this project?

I got to see all the free APIs that are out there and it inspires me to create many more projects now :) .

## Part 3: Retrospect

> If you could start the project over from scratch, what do
> you think might do differently and why?

I would try to make the GUI prettier. I kinda gave up on it because making a gui with JavaFX is making me lose my mind.
