/* movielist.js */
// File: src/main/webapp/js/movielist.js

window.addEventListener("DOMContentLoaded", () => {
    fetchMovies();
});

function fetchMovies() {
    const sort = document.getElementById("sort").value;
    const order = document.getElementById("order").value;
    const limit = document.getElementById("limit").value;

    fetch(`api/movielist?sort=${sort}&order=${order}&limit=${limit}`)
        .then(response => response.json())
        .then(data => populateTable(data))
        .catch(error => console.error("Error fetching movie data:", error));
}

function populateTable(movies) {
    const tbody = document.getElementById("movieResults");
    tbody.innerHTML = "";

    movies.forEach(movie => {
        const row = document.createElement("tr");

        // Create star links
        const starLinks = movie.stars?.map(star => 
            `<a href="star.html?id=${encodeURIComponent(star.id)}">${star.name}</a>`
        ).join(", ") ?? "N/A";

        row.innerHTML = `
            <td><a href="movie.html?id=${encodeURIComponent(movie.id)}">${movie.title}</a></td>
            <td>${movie.year}</td>
            <td>${movie.director}</td>
            <td>${movie.rating}</td>
            <td>${starLinks}</td>
        `;

        tbody.appendChild(row);
    });
}
