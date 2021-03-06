package com.infoshareacademy.dreamteam.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jsoup.Jsoup;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FragmentData {
    String html;

    public void setHtml(String html) {
        this.html = html;
    }

    public String getHtml() {
        return Jsoup.parse(html).text();
    }

    public String getFragment(BookDetailsPlain book) {
        return Optional.ofNullable(book)
                .map(BookDetailsPlain::getFragmentData)
                .map(FragmentData::getHtml)
                .filter(fragment -> !((String) fragment).isEmpty())
                .orElse("");
    }

}
