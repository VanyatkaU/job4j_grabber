package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final int PAGE = 5;

    private static final String PAGE_LINK = String.format(
            "%s/vacancies/java_developer", SOURCE_LINK);

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private static String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        Document document = null;
        try {
            document = connection.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert document != null;
        Element descriptionElement = document.select(".style-ugc").first();
        return descriptionElement.text();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> list = new ArrayList<>();
        for (int i = 1; i <= PAGE; i++) {
            Connection connection = Jsoup.connect(link + i);
            Document document = null;
            try {
                document = connection.get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert document != null;
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row ->
                list.add(parsePost(row))
            );
        }
        return list;
    }

    private Post parsePost(Element row) {
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String vacancyName = titleElement.text();
        Element dateTime = row.select(".vacancy-card__date").first();
        Element linkTime = dateTime.child(0);
        LocalDateTime vacancyDate = dateTimeParser.parse(linkTime.attr("datetime"));
        String linkVacancy = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String description;
        description = retrieveDescription(linkVacancy);
        return new Post(vacancyName, linkVacancy, description, vacancyDate);
    }

    public static void main(String[] args) {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        habrCareerParse.list(PAGE_LINK + "?page=");
    }
}
