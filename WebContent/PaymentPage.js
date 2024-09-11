let payment_form = $('#payment_form');

let totalPrice = sessionStorage.getItem('totalPrice');
// Select the h2 element by its ID
let cartPriceElement = document.getElementById('cart_price');
// Update the text content of the h2 element
if (totalPrice !== null) {
    cartPriceElement.textContent = 'Total Price: $' + totalPrice;
}
else{
    cartPriceElement.textContent = 'Total Price: $0.00';
}

function handlePaymentResult(resultDataJson) {
    console.log("Received response from server regarding payment");

    if (resultDataJson["success"]) {
        // Payment was successful
        console.log('Payment processed successfully');

        // Clear cart and price
        sessionStorage.setItem("cartItems", JSON.stringify([]));
        sessionStorage.setItem("totalPrice", "0.00");

        // Redirect to a success page or update the UI accordingly
        window.location.replace("Confirmation.html");
    } else {
        // Payment failed
        console.error('Payment processing failed:', resultDataJson["message"]);
        // Display the error message to the user
        alert("Payment processed failed, please verify your information and try again.");
    }
}

function submitPaymentForm(formSubmitEvent) {
    console.log("submit payment form");

    formSubmitEvent.preventDefault();

    // Ensure that a cart will have items
    var cartItems = sessionStorage.getItem("cartItems");
    if (cartItems == null || cartItems.length < 1) {
        alert("No items in cart");
    } else {
        // Serialize the form data
        var paymentFormData = payment_form.serialize();

        $.ajax("api/payment", {
            method: "POST",
            dataType: "json", // Expecting JSON response from the server
            success: handlePaymentResult,
            data: {
                cartItems: cartItems,
                paymentData: paymentFormData
            },
            error: function(jqXHR, textStatus, errorThrown) {
                // Handle any errors that occur during the request
                console.error('Error during the AJAX request:', textStatus, errorThrown);
            }
        });
    }
}

payment_form.submit(submitPaymentForm);