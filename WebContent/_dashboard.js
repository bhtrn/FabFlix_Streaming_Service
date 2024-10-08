let login_form = $("#employee_login");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    console.log("Parsing resultDataString from handleLoginResult:");
    console.log(resultDataString);
    let resultDataJson = JSON.parse(resultDataString);

    // If login succeeds, it will redirect the user to employee dashboard
    if (resultDataJson["status"] === "success") {
        window.location.replace("_employee_dashboard.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit employee login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/_dashboard", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult
        }
    );
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);