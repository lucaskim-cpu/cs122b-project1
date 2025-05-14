package org.example;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class MovieDomParser {
    List<ParsedMovie> movies = new ArrayList<>();
    Map<String, Integer> genreCache = new HashMap<>();
    Map<String, String> starNameToId = new HashMap<>();
    List<StarInMovie> starsInMovies = new ArrayList<>();
    List<String> unmatchedActors = new ArrayList<>();

    Document domMovies;
    Document domCasts;
    Connection conn;

    public void runParser() throws Exception {
        parseXmlFiles();
        parseMovies();
        parseCasts();

        printParsedMovies();
        printParsedStars();
        printParsedStarInMovies();
        logUnmatchedActors();

        connectToDatabase();
        loadExistingGenres();
        insertMoviesGenresStars();
    }

    private void parseXmlFiles() throws Exception {
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        // Use absolute paths to the XML files
        String projectDir = new File(".").getAbsolutePath();
        if (projectDir.endsWith(".")) {
            projectDir = projectDir.substring(0, projectDir.length() - 1);
        }
        
        // If running from target/classes, go up two levels
        if (projectDir.endsWith("target\\classes")) {
            projectDir = projectDir.substring(0, projectDir.indexOf("target\\classes"));
        }
        
        System.out.println("Using project directory: " + projectDir);
        
        String mainsPath = projectDir + "src\\main\\mains243.xml";
        String castsPath = projectDir + "src\\main\\casts124.xml";
        
        System.out.println("Loading movies XML from: " + mainsPath);
        System.out.println("Loading casts XML from: " + castsPath);
        
        domMovies = dBuilder.parse(new File(mainsPath));
        domCasts = dBuilder.parse(new File(castsPath));
        domMovies.getDocumentElement().normalize();
        domCasts.getDocumentElement().normalize();
    }

    private void parseMovies() {
        NodeList directorList = domMovies.getElementsByTagName("directorfilms");

        for (int i = 0; i < directorList.getLength(); i++) {
            Element df = (Element) directorList.item(i);
            String director = getTextValue(df, "dirname");

            NodeList filmList = df.getElementsByTagName("film");
            for (int j = 0; j < filmList.getLength(); j++) {
                Element film = (Element) filmList.item(j);
                String fid = getTextValue(film, "fid");
                String title = getTextValue(film, "t");
                int year = parseIntSafe(getTextValue(film, "year"));

                List<String> genres = new ArrayList<>();
                NodeList genreNodes = film.getElementsByTagName("cat");
                for (int k = 0; k < genreNodes.getLength(); k++) {
                    genres.add(genreNodes.item(k).getTextContent().trim());
                }
                movies.add(new ParsedMovie(fid, title, year, director, genres));
            }
        }
    }

    private void parseCasts() {
        NodeList directorList = domCasts.getElementsByTagName("dirfilms");
        Set<String> uniqueStars = new HashSet<>();

        for (int i = 0; i < directorList.getLength(); i++) {
            Element df = (Element) directorList.item(i);
            NodeList mList = df.getElementsByTagName("m");
            for (int j = 0; j < mList.getLength(); j++) {
                Element m = (Element) mList.item(j);
                String fid = getTextValue(m, "f");
                String actor = getTextValue(m, "a");

                if (!uniqueStars.contains(actor)) {
                    String id = "star" + (starNameToId.size() + 1);
                    starNameToId.put(actor, id);
                    uniqueStars.add(actor);
                }

                String sid = starNameToId.get(actor);
                if (sid != null) {
                    starsInMovies.add(new StarInMovie(sid, fid));
                } else {
                    unmatchedActors.add(actor);
                }
            }
        }
    }

    private void connectToDatabase() throws SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");
        conn.setAutoCommit(false);
    }

    private void loadExistingGenres() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM genres");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            genreCache.put(rs.getString("name").toLowerCase(), rs.getInt("id"));
        }
        rs.close();
        stmt.close();
    }

    private void insertMoviesGenresStars() throws SQLException, IOException {
        PreparedStatement insertMovie = conn.prepareStatement("INSERT IGNORE INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)");
        PreparedStatement insertGenre = conn.prepareStatement("INSERT INTO genres (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        PreparedStatement insertGIM = conn.prepareStatement("INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)");
        PreparedStatement insertStar = conn.prepareStatement("INSERT IGNORE INTO stars (id, name) VALUES (?, ?)");
        PreparedStatement insertSIM = conn.prepareStatement("INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)");

        Set<String> gimSet = new HashSet<>();
        Set<String> validMovieIds = new HashSet<>();
        for (ParsedMovie m : movies) {
            insertMovie.setString(1, m.fid);
            insertMovie.setString(2, m.title);
            insertMovie.setInt(3, m.year);
            insertMovie.setString(4, m.director);
            insertMovie.addBatch();
            validMovieIds.add(m.fid);

            for (String genre : m.genres) {
                int genreId;
                String lowerGenre = genre.toLowerCase();
                if (!genreCache.containsKey(lowerGenre)) {
                    insertGenre.setString(1, genre);
                    insertGenre.executeUpdate();
                    ResultSet keys = insertGenre.getGeneratedKeys();
                    if (keys.next()) {
                        genreId = keys.getInt(1);
                        genreCache.put(lowerGenre, genreId);
                    }
                    keys.close();
                }
                genreId = genreCache.get(lowerGenre);
                String key = genreId + "-" + m.fid;
                if (gimSet.add(key)) {
                    insertGIM.setInt(1, genreId);
                    insertGIM.setString(2, m.fid);
                    insertGIM.addBatch();
                }
            }
        }

        for (Map.Entry<String, String> entry : starNameToId.entrySet()) {
            insertStar.setString(1, entry.getValue());
            insertStar.setString(2, entry.getKey());
            insertStar.addBatch();
        }

        Set<String> simSet = new HashSet<>();
        try (PrintWriter invalidSIMLog = new PrintWriter("invalid_star_in_movies.log")) {
            for (StarInMovie sim : starsInMovies) {
                if (validMovieIds.contains(sim.movieId)) {
                    String key = sim.starId + "-" + sim.movieId;
                    if (simSet.add(key)) {
                        insertSIM.setString(1, sim.starId);
                        insertSIM.setString(2, sim.movieId);
                        insertSIM.addBatch();
                    }
                } else {
                    invalidSIMLog.println("Skipped StarInMovie: " + sim.starId + ", " + sim.movieId);
                }
            }
        }

        insertMovie.executeBatch();
        insertGIM.executeBatch();
        insertStar.executeBatch();
        insertSIM.executeBatch();
        conn.commit();

        insertMovie.close();
        insertGenre.close();
        insertGIM.close();
        insertStar.close();
        insertSIM.close();
        conn.close();
    }

    private void printParsedMovies() {
        System.out.println("=== Parsed Movies ===");
        for (ParsedMovie m : movies) {
            System.out.println(m);
        }
    }

    private void printParsedStars() {
        System.out.println("=== Parsed Stars ===");
        for (Map.Entry<String, String> entry : starNameToId.entrySet()) {
            System.out.println("ID=" + entry.getValue() + ", Name=" + entry.getKey());
        }
    }

    private void printParsedStarInMovies() {
        System.out.println("=== Parsed Stars in Movies ===");
        for (StarInMovie sim : starsInMovies) {
            System.out.println(sim);
        }
    }

    private void logUnmatchedActors() throws IOException {
        try (PrintWriter writer = new PrintWriter("unmatched_actors.log")) {
            for (String actor : unmatchedActors) {
                writer.println("Unmatched actor: " + actor);
            }
        }
    }

    private String getTextValue(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            Node node = list.item(0);
            if (node != null && node.getFirstChild() != null) {
                String value = node.getFirstChild().getNodeValue();
                if (value != null) {
                    return value.trim();
                }
            }
        }
        return "";
    }

    private int parseIntSafe(String input) {
        try {
            return Integer.parseInt(input.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public static void main(String[] args) throws Exception {
        new MovieDomParser().runParser();
    }
}
