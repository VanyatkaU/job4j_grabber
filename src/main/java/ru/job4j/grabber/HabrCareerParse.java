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

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final int PAGE = 5;

    private static final String PAGE_LINK = String.format(
            "%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        for (int i = 0; i <= PAGE; i++) {
            Connection connection = Jsoup.connect(String.format("%s%s%d", PAGE_LINK, "?page=", i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                Element dateTime = row.select(".vacancy-card__date").first();
                Element linkTime = dateTime.child(0);
                LocalDateTime vacancyDate = dateTimeParser.parse(linkTime.attr("datetime"));
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s%n %s - ", vacancyName, link, vacancyDate);
            });
        }
    }
}
