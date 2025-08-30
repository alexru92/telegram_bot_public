package com.example.ApartmentsSearchWithTelegramReporting.provider;

import com.example.ApartmentsSearchWithTelegramReporting.model.Flat;
import com.example.ApartmentsSearchWithTelegramReporting.service.RestService;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "provider.degewo.enabled", havingValue = "true", matchIfMissing = true)
@Log4j2
public class DegewoProvider extends AbstractFlatProvider {

    private static final String URL = "https://immosuche.degewo.de/de/search";

    public DegewoProvider(RestService restService) {
        super(restService, "https://immosuche.degewo.de/");
    }

    @Override
    protected Elements loadFlats() {
        try {
            String[] pagesArr = restService.fetchAndParseElements(URL, "li.pager__page > a")
                    .text().replace(" (aktuelle Seite)", "").split(" ");
            int pages = Integer.parseInt(pagesArr[pagesArr.length - 1]);

            Elements allFlats = new Elements();
            for (int i = 1; i <= pages; i++) {
                allFlats.addAll(restService.fetchAndParseElements(
                        "https://immosuche.degewo.de/de/search?size=10&page=" + i,
                        "div.search__results.article-list > article"
                ));
            }
            return allFlats;
        } catch (Exception e) {
            log.error("Error fetching Degewo flats", e);
            return new Elements();
        }
    }

    @Override
    protected Flat buildFlat(Element flat) {
        Flat result = new Flat();
        Elements props = flat.select("ul.article__properties li.article__properties-item > span");

        result.setRooms(props.size() > 0 ? props.get(0).text() : "N/A");
        result.setSize(props.size() > 1 ? props.get(1).text() : "N/A");
        result.setPrice(flat.select("div.article__price-tag span.price").text());
        result.setAddress(flat.select("span.article__meta").text().replace(" | ", ", "));
        result.setLink("https://degewo.de" + flat.select("a").attr("href"));

        return result;
    }

    @Override
    protected Long parseId(Element flat) {
        String rawId = flat.select("div.merken").attr("data-openimmo-bookmark-item-uid");
        return rawId.isEmpty() ? safeHash(flat.text() + flat.select("a").attr("href"))
                : Long.valueOf(rawId.replace("W", "").replace("-", "").replace(".", ""));
    }
}
