function fetchAndPopulateTablesMetadata() {
    fetch('api/_tableMetadata')
        .then(res => res.json())
        .then(metadata => {
            console.log("Successfully retrieved metadata");
            console.log(metadata);

            const metadataDiv = document.getElementById('moviedb_tables');

            // Clear the div before populating it
            metadataDiv.innerHTML = '';

            // Iterate over each table's metadata
            metadata.forEach(table => {
                // Create a new table element
                const tableElement = document.createElement('table');
                tableElement.className = 'metadata-table';

                // Create a header row with the table name
                const headerRow = document.createElement('tr');
                const header = document.createElement('th');
                header.colSpan = 2; // Span across two columns (name and type)
                header.textContent = `Table: ${table.table_name}`;
                headerRow.appendChild(header);
                tableElement.appendChild(headerRow);

                // Add column metadata
                table.columns.forEach(column => {
                    const row = document.createElement('tr');
                    const nameCell = document.createElement('td');
                    const typeCell = document.createElement('td');

                    nameCell.textContent = column.column_name;
                    typeCell.textContent = column.column_type;

                    row.appendChild(nameCell);
                    row.appendChild(typeCell);

                    tableElement.appendChild(row);
                });

                // Append the table to the div
                metadataDiv.appendChild(tableElement);
            });
        })
        .catch(error => console.error('Error fetching table metadata:', error));
}

// Fetch metadata when page is loaded
document.addEventListener('DOMContentLoaded', (event) => {
    console.log("Dom Loaded, fetching metadata");
    fetchAndPopulateTablesMetadata();
});

$(document).ready(function() {
    $("#add_star_form").submit(function(event) {
        event.preventDefault(); // Prevent the default form submission

        // Use the input name attribute to select the element with jQuery
        let starName = $('input[name="starName"]').val();
        let birthYear = $('input[name="birthYear"]').val();

        console.log("input starName: " + starName);
        console.log("input birthYear: " + birthYear);

        let formData = {
            starName: starName,
            birthYear: birthYear
        };

        $.ajax({
            type: 'POST',
            url: 'api/add_star',
            data: formData,
            dataType: 'json',
            encode: true
        })
            .done(function(data) {
                console.log(data);
                if (data.status === 'success') {
                    // Star was added successfully
                    alert('Star added successfully!');
                } else {
                    // Handle errors
                    alert('Error adding star: ' + data.message);
                }
            })
            .fail(function(error) {
                console.error('Error adding star:', error);
            });
    });
});

$(document).ready(function() {
    $("#add_movie_form").submit(function(event) {
        event.preventDefault(); // Prevent the default form submission

        // Movie Info | jQuery
        let movieTitle = $('input[name="movieTitle"]').val();
        let movieYear = $('input[name="movieYear"]').val();
        let movieDirector = $('input[name="movieDirector"]').val();

        // Star Info
        let starName = $('input[name="movieStarName"]').val();
        let birthYear = $('input[name="movieStarBirthYear"]').val();

        // Genre Info
        let genre = $('input[name="genre"]').val();


        let formData = {
            movieTitle: movieTitle,
            movieYear: movieYear,
            movieDirector: movieDirector,
            starName: starName,
            birthYear: birthYear,
            genre: genre
        };

        $.ajax({
            type: 'POST',
            url: 'api/add_movie',
            data: formData,
            dataType: 'json',
            encode: true
        })
            .done(function(data) {
                console.log(data);
                if (data.status === 'success') {
                    // Star was added successfully
                    alert('Movie added successfully!');
                } else {
                    // Handle errors
                    alert('Error adding movie: ' + data.message);
                }
            })
            .fail(function(error) {
                console.error('Error adding movie:', error);
            });
    });
});