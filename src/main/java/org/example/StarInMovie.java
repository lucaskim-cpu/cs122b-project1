package org.example;

public class StarInMovie {
    public String starId;
    public String movieId;

    public StarInMovie(String starId, String movieId) {
        this.starId = starId;
        this.movieId = movieId;
    }

    @Override
    public String toString() {
        return "StarID=" + starId + ", MovieID=" + movieId;
    }
}
