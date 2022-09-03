package ru.job4j.grabber;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            cnn = DriverManager.getConnection(
                    cfg.getProperty("jdbc.url"),
                    cfg.getProperty("jdbc.username"),
                    cfg.getProperty("jdbc.password"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps = cnn.prepareStatement(
                "insert into post (name, text, link, created)" +
                "values (?, ?, ?, ?) on conflict (link) do nothing",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getDescription());
            ps.setString(3, post.getLink());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.execute();
            try (ResultSet rsk = ps.getGeneratedKeys()) {
                if (rsk.next()) {
                    post.setId(rsk.getInt(1));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> postList = new ArrayList<>();
        try (PreparedStatement ps = cnn.prepareStatement(
                "select * from post")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    postList.add(findResultSet(rs));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return postList;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement ps = cnn.prepareStatement(
                "select * from post where id = ?;")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    post = findResultSet(rs);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post findResultSet(ResultSet rs) throws SQLException {
        return new Post(rs.getInt("id"),
                rs.getString("name"),
                rs.getString("link"),
                rs.getString("text"),
                rs.getTimestamp("created").toLocalDateTime());
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        try (InputStream is = PsqlStore.class.getClassLoader().
                getResourceAsStream("jdbc.properties")) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (PsqlStore psqlStore = new PsqlStore(properties)) {
            psqlStore.save(new Post("java_developer",
                    "https://career.habr.com/vacancies/java_developer",
                    "Vacancy", LocalDateTime.now()));
            psqlStore.save(new Post("java_SQL",
                    "https://career.habr.com/vacancies/java_SQL",
                    "Vacancy1", LocalDateTime.now()));
            psqlStore.save(new Post("java_backend",
                    "https://career.habr.com/vacancies/java_backend",
                    "Vacancy2", LocalDateTime.now()));
            System.out.println(psqlStore.getAll());
            System.out.println(psqlStore.findById(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
