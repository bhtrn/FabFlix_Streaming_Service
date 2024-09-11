/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieListResult(resultData) {
    console.log("handleMovieListResult: populating #movie_table from resultData");

    // Populate the MovieList table
    // Find the empty table body by id "movieList_table_body"
    let movieListTableBodyElement = jQuery("#movieList_table_body");

    // Iterate through resultData, result data is already limited by database queue
    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += '<th><button class="addToCartButton" data-movie-id="' + resultData[i]['movieId'] + '">&#43;</button></th>';
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movieId'] + '">'
            + resultData[i]["title"] +     // display movie title for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";

        // WARNING: Assumes len(genresIds) == len(genres)
        //genres here
        rowHTML += "<th>";
        for (let j = 0; j < resultData[i]["genresIds"].length; j++) {
            rowHTML += '<a href="MovieList.html?browseGenreId=' + resultData[i]["genresIds"][j] + '&page=1">'
            + resultData[i]["genres"][j] + '</a>';

            if (j !== resultData[i]["genres"].length - 1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";


        // WARNING: Assumes len(starsId) == len(stars)

        //stars here
        rowHTML += "<th>";
        for (let j = 0; j < resultData[i]["starsIds"].length; j++) {
            rowHTML += '<a href="single-star.html?id=' + resultData[i]["starsIds"][j] + '">'
            + resultData[i]["stars"][j] + '</a>';

            if (j !== (resultData[i]["starsIds"].length - 1))  {
                rowHTML += ", ";
            }

        }
        rowHTML += "</th>";

        // ratings
        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += "<th> $" + resultData[i]["price"] + "</th>"
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieListTableBodyElement.append(rowHTML);
    }
}

// Function to fetch genres and populate the list
function fetchAndPopulateGenres() {
    // Requests data from the java endpoint '/api/genres'
    fetch('api/genres')
        // Once we've received the data, convert it into JSON data for us to process
        .then(response => response.json())
        // Once converted into JSON data, store it into variable 'genres' and insert into HTML
        .then(genres => {
            // Select the list from the "document" (our HTML, MovieList.html)
            console.log("Successfully converted genres into json");
            const genreList = document.getElementById('genre_list');

            // For each genre returned by '/api/genres', add a <li>
            genres.forEach(genre => {
                const li = document.createElement('li');
                li.classList.add('list-inline-item'); // Add Bootstrap class to display items inline

                // Adding parameter to URL
                const a = document.createElement('a');
                a.href = "MovieList.html?browseGenreId=" + genre.id + "&page=1";

                // Adding visible text to list
                a.textContent = genre.name; // Assuming 'genre' is an object with a 'name' property

                // Making the list element a hotlink
                li.append(a);

                genreList.appendChild(li);
            });
        })
        .catch(error => console.error('Error fetching genres:', error));
}

function populateTitles() {
    console.log("populateTitles: hyperlinking #title_list");
    const titleList = document.getElementById('title_list');

    // Clear any existing content in the titleList
    titleList.innerHTML = '';

    // Define the range of alphanumeric characters from 0 to z
    const characters = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ\*'.split('');

    // Create a list item for each character and append it to the titleList
    characters.forEach(char => {
        const li = document.createElement('li');
        li.classList.add('list-inline-item');
        const a = document.createElement('a');
        a.href = "MovieList.html?browseTitle=" + char + "&page=1";

        a.textContent = char;

        li.appendChild(a);
        titleList.appendChild(li);
    });
}

// Helper function to make the AJAX call
function makeMovieListAjaxCall(url) {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: url,
        success: (resultData) => handleMovieListResult(resultData)
    });
}

// MovieList.js
// Used for the Movie entry per page selection
function handleEntriesChange() {
    var entriesPerPage = document.getElementById('entriesPerPage').value;

    jQuery.ajax({
        method: "POST",
        url: "api/SessionEntriesUpdate",
        data: {
            "entriesPerPage": entriesPerPage
        },
        success: function(response) {
            console.log("Successfully updated entries: " + JSON.stringify(response));
            // Update the URL and reload the page to show the new number of entries per page
            var currentUrl = window.location.href;
            var url = new URL(currentUrl);
            var searchParams = url.searchParams;
            var selectedOption = selectElement.options[selectElement.selectedIndex].value;

            // Store the selected value in local storage
            sessionStorage.setItem("selectedEntriesPerPage", selectedOption);
            searchParams.set('entriesPerPage', entriesPerPage);
            searchParams.set('page', 1);
            url.search = searchParams.toString();
            window.location.href = url.toString();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            // Handle any errors that occur during the request
            console.error("Error updating entries per page: " + textStatus, errorThrown);
            alert("An error occurred while updating your preference. Please try again.");
        }
    });

    return false;
}

// Updates te page parameter and refereshes the page to the new page
function updatePageParam(newPage) {
    // Update the page parameter
    let searchParams = new URLSearchParams(window.location.search);

    // Update the page parameter
    searchParams.set('page', newPage);

    // Construct the new URL with updated search parameters
    let newUrl = `${window.location.pathname}?${searchParams.toString()}`;

    // Navigate to the new URL
    window.location.href = newUrl;
}

//Adds movie item to shopping cart
function addToCart(movieId){
    // Retrieve the existing cart items from localStorage
    console.log("Adding to Cart: " + movieId);
    let cartItems = JSON.parse(sessionStorage.getItem("cartItems")) || [];

    // Add the movieId to the cartItems array
    cartItems.push(movieId);

    // Save the updated cartItems array back to localStorage
    sessionStorage.setItem("cartItems", JSON.stringify(cartItems));
}


/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    // If len(query) <= 2, do nothing
    if (query.length <= 2){
        console.log("query too short for autocomplete")
    }
    // Check whether the query has been made before
    else if (oldSuggestions.hasOwnProperty(query)) {
        // If so, pass that data forward rather than calling the database
        handleLookupAjaxSuccess(oldSuggestions[query], query, doneCallback)
    }
    // Otherwise, make a call to the database
    else {
        console.log("sending AJAX request to backend Java Servlet")
        // sending the HTTP GET request to the Java Servlet endpoint movie-suggestion with the query data
        jQuery.ajax({
            "method": "GET",
            // generate the request url from the query. This is how we pass data back
            // escape the query string to avoid errors caused by special characters
            "url": "movie-suggestion?query=" + escape(query),
            "success": function(data) {
                // pass the data, query, and doneCallback function into the success handler
                handleLookupAjaxSuccess(data, query, doneCallback)
            },
            "error": function(errorData) {
                console.log("lookup ajax error")
                console.log(errorData)
            }
        })
    }
}

/*
 * This function is used to handle the ajax success callback function.
 * Currently, it simply logs the data pulled
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("Suggestion Lookup successful")
    var jsonData = null;

    // parse the string into JSON if new data
    if (!oldSuggestions.hasOwnProperty(query)) {
        jsonData = JSON.parse(data);

        // cache the result into a global variable
        oldSuggestions[query] = jsonData
    }
    else {
        // Otherwise, pull existing data
        console.log("Pulled existing query data");
        jsonData = oldSuggestions[query];
    }
    console.log(jsonData)

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}

/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion
    console.log(suggestion);
    console.log(suggestion["data"]);
    // Extract the movie ID from the suggestion data
    var movieId = suggestion["data"]["movieId"];

    // Construct the URL with the movie ID parameter
    var url = "single-movie.html?id=" + encodeURIComponent(movieId);

    // Redirect the browser to the new URL
    window.location.href = url;

    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieId"])
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Get id from URL
let params = new URLSearchParams(window.location.search);
let genreId = params.get('browseGenreId');
let titleChar = params.get('browseTitle');
let movieTitle = params.get('movieTitle');
let yearReleased = params.get('yearReleased');
let movieDirector = params.get('director');
let movieStar = params.get('movieStar');
let page = params.get('page') || 1;
let entriesPerPage = params.get('entriesPerPage');
let sortby = params.get('sortby') || "rating";
let sortorder = params.get('sortorder') || "desc";

// Base URL for the MovieList API
let apiUrl = "api/MovieList";

var selectElement = document.getElementById("entriesPerPage");
// Get the selected value from local storage, if available
var selectedValue = sessionStorage.getItem("selectedEntriesPerPage");


// Set the selected value in the select element, if available
if (selectedValue !== null) {
    selectElement.value = selectedValue;
}

if (genreId) {
    makeMovieListAjaxCall(`${apiUrl}?browseGenreId=${genreId}&page=${page}&sortby=${sortby}&sortorder=${sortorder}`);
}
else if (titleChar) {
    makeMovieListAjaxCall(`${apiUrl}?browseTitle=${titleChar}&page=${page}&sortby=${sortby}&sortorder=${sortorder}`);
}
else {
    // Create a searchParams object from the existing params
    let searchParams = new URLSearchParams();
    if (movieTitle) searchParams.append('movieTitle', movieTitle.trim());
    if (yearReleased) searchParams.append('yearReleased', yearReleased.trim());
    if (movieDirector) searchParams.append('director', movieDirector.trim());
    if (movieStar) searchParams.append('movieStar', movieStar.trim());

    // Add constant parameter
    searchParams.append('page', page);
    searchParams.append('sortby', sortby);
    searchParams.append('sortorder', sortorder);

    // Make the AJAX call with the constructed search parameters
    makeMovieListAjaxCall(`${apiUrl}?${searchParams.toString()}`);
}

// AutoComplete Related Events:
/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
let oldSuggestions = {};

$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
});


// DomContentLoaded Events: When the DOM is fully loaded

// Populate Title's Browsing Options
document.addEventListener('DOMContentLoaded', function() {
    populateTitles();
});

// Populate Genre Browsing Options
document.addEventListener('DOMContentLoaded', (event) => {
    fetchAndPopulateGenres();
});

// Sets a page number placeholder
document.addEventListener('DOMContentLoaded', function() {
    // Create a URLSearchParams object from the current URL's query string
    let searchParams = new URLSearchParams(window.location.search);

    // Get the current "page" parameter value
    let currentPage = searchParams.get('page') || 1; // Default to 1 if the parameter is not present

    // Set the placeholder of the input box to the current page number
    document.querySelectorAll('.page_number_input').forEach(input => {
        // Set the placeholder of each input box to the current page number
        input.placeholder = `Page ${currentPage}`;
    });
});

// Initializes the Sort By / Sort Order buttons
document.addEventListener('DOMContentLoaded', function() {
    // Function to check the URL parameters and update the radio buttons
    function setDefaultSortOptions() {
        var currentUrl = new URL(window.location);
        var sortBy = currentUrl.searchParams.get('sortby') || 'rating'; // Default to 'rating' if not present
        var sortOrder = currentUrl.searchParams.get('sortorder') || 'desc'; // Default to 'desc' if not present

        // Set the radio buttons to match the URL parameters or default
        document.querySelector(`input[name="sortby"][value="${sortBy}"]`).checked = true;
        document.querySelector(`input[name="sortorder"][value="${sortOrder}"]`).checked = true;
    }

    // Call the function to set the default sort options
    setDefaultSortOptions();

});

// Listens for changes on Sort By / Sort Order Buttons
document.addEventListener('DOMContentLoaded', function() {
    // Function to update the URL with the selected sort options and refresh the page
    function updateSortParametersAndRefreshPage() {
        // Get the selected values for sorting
        var sortBy = document.querySelector('input[name="sortby"]:checked').value;
        var sortOrder = document.querySelector('input[name="sortorder"]:checked').value;

        // Construct the new URL with sort parameters
        var currentUrl = new URL(window.location);

        // Set page parameter back to 1 so new results make sense in context of new sorting rules
        currentUrl.searchParams.set('page', 1);

        // Set new sorting rules
        currentUrl.searchParams.set('sortby', sortBy);
        currentUrl.searchParams.set('sortorder', sortOrder);

        // Reload the page with the new URL
        window.location.href = currentUrl.toString();
    }

    // Attach event listeners to the radio buttons for the 'change' event
    var sortOptions = document.querySelectorAll('input[name="sortby"], input[name="sortorder"]');
    sortOptions.forEach(function(option) {
        option.addEventListener('change', updateSortParametersAndRefreshPage);
    });
});

// Attach event listeners to all "Next" buttons
document.querySelectorAll('.next-button').forEach(button => {
    button.addEventListener('click', () => {
        // Create a URLSearchParams object from the current window's URL
        let searchParams = new URLSearchParams(window.location.search);

        // Attempt to parse the 'page' parameter or default to 1 if not present or invalid
        let currentPage = parseInt(searchParams.get('page')) || 1;
        updatePageParam(currentPage + 1); // Increment page number
    });
});

// Attach event listeners to all "Previous" buttons
document.querySelectorAll('.prev-button').forEach(button => {
    button.addEventListener('click', () => {
        // Create a URLSearchParams object from the current window's URL
        let searchParams = new URLSearchParams(window.location.search);

        // Attempt to parse the 'page' parameter or default to 1 if not present or invalid
        let currentPage = parseInt(searchParams.get('page')) || 1;
        if (currentPage > 1) {
            updatePageParam(currentPage - 1); // Decrement page number
        }
    });
});

// Listens for page jumping
document.addEventListener('keypress', function(event) {
    // Check if the event target has the class 'page_number_input'
    if (event.target.classList.contains('page_number_input') && event.key === 'Enter') {
        // Get the entered page number from the event target (the input element)
        let enteredPageNumber = parseInt(event.target.value);

        // Validate the entered page number
        if (!isNaN(enteredPageNumber) && enteredPageNumber > 0) {
            // Call the function to update the page parameter and navigate to that page
            updatePageParam(enteredPageNumber);
        } else {
            // Optionally, alert the user if the input is not valid
            alert("Please enter a valid non-zero positive integer for the page number.");
        }
    }
});

// Adds movies to cart when addToCartButton is pressed
document.addEventListener('DOMContentLoaded', function() {
    let movieListTableBody = document.getElementById("movieList_table_body");

    movieListTableBody.addEventListener('click', function(event) {
        // Check if the clicked element is a button with the class 'addToCartButton'
        if (event.target && event.target.classList.contains('addToCartButton')) {
            // Retrieve the movie ID from the data-movie-id attribute of the button
            let movieId = event.target.getAttribute('data-movie-id');
            // Call the addToCart function with the movie ID
            addToCart(movieId);
        }
    });
});