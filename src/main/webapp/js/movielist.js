/* movielist.js */
// File: src/main/webapp/js/movielist.js

window.addEventListener("DOMContentLoaded", () => {
    fetch("api/movielist")
        .then(response => response.json())
        .then(data => populateTable(data))
        .catch(error => console.error("Error fetching movie data:", error));
});

function populateTable(movies) {
    const tbody = document.querySelector("#movie_table tbody");
    tbody.innerHTML = "";

    movies.forEach(movie => {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${movie.title}</td>
            <td>${movie.year}</td>
            <td>${movie.director}</td>
            <td>${movie.genres?.join(", ") ?? "N/A"}</td>
            <td>${movie.stars?.join(", ") ?? "N/A"}</td>
            <td>${movie.rating}</td>
        `;

        tbody.appendChild(row);
    });
}
