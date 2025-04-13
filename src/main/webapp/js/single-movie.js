window.addEventListener("DOMContentLoaded", () => {
    const urlParams = new URLSearchParams(window.location.search);
    const movieId = urlParams.get('id');
    
    if (!movieId) {
        document.body.innerHTML = "<h1>Error: Movie ID not provided</h1>";
        return;
    }

    fetch(`api/movie?id=${encodeURIComponent(movieId)}`)
        .then(response => response.json())
        .then(movie => {
            document.getElementById("movie-title").textContent = movie.title;
            document.getElementById("movie-year").textContent = movie.year;
            document.getElementById("movie-director").textContent = movie.director;
            document.getElementById("movie-genres").textContent = movie.genres?.join(", ") ?? "N/A";
            
            // Create star links
            const starLinks = movie.stars?.map(star => 
                `<a href="single-star.html?name=${encodeURIComponent(star)}">${star}</a>`
            ).join(", ") ?? "N/A";
            document.getElementById("movie-stars").innerHTML = starLinks;

            document.getElementById("movie-rating").textContent = (movie.rating !== undefined)
                ? Number(movie.rating).toFixed(1)
                : "N/A";
        })
        .catch(error => {
            console.error("Error fetching movie details:", error);
            document.body.innerHTML = "<h1>Error loading movie details</h1>";
        });
}); 