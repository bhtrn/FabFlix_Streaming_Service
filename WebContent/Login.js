let login_form = $("#login_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    console.log("Parsing resultDataString from handleLoginResult:");
    console.log(resultDataString);

    try {
        let resultDataJson = JSON.parse(resultDataString);
        console.log("Parsed JSON:", resultDataJson);

        // If login succeeds, it will redirect the user to MovieList.html
        if (resultDataJson["status"] === "success") {
            window.location.replace("MovieList.html");
        } else {
            // If login fails, the web page will display
            // error messages on <div> with id "login_error_message"
            console.log("show error error message");
            console.log(resultDataJson["message"]);
            $("#login_error_message").text(resultDataJson["message"]);
        }
    } catch (error) {
        // Handle parsing error, display message to user, etc.
        console.error("Error parsing JSON:", error);
        $("#login_error_message").text("Error processing login response. Please try again.");
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/login", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult
        }
    );
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);