package org.example;

import java.util.List;

public class ParsedMovie {
    public String fid;
    public String title;
    public int year;
    public String director;
    public List<String> genres;

    public ParsedMovie(String fid, String title, int year, String director, List<String> genres) {
        this.fid = fid;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = genres;
    }

    @Override
    public String toString() {
        return "FID=" + fid + ", Title=" + title + ", Year=" + year +
                ", Director=" + director + ", Genres=" + String.join(", ", genres);
    }
}