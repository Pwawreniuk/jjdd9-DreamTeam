package com.infoshareacademy.dreamteam.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoshareacademy.dreamteam.domain.entity.*;
import com.infoshareacademy.dreamteam.domain.pojo.*;
import com.infoshareacademy.dreamteam.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Singleton
@Startup
public class APILoadBean {
    private static final Logger logger = LoggerFactory.getLogger(APILoadBean.class.getName());
    private static final String url = "https://wolnelektury.pl/api/books/?format=json";

    @Inject
    private BookService bookService;
    @Inject
    private AuthorService authorService;
    @Inject
    private GenreService genreService;
    @Inject
    private KindService kindService;
    @Inject
    private EpochService epochService;
    @Inject
    private TranslatorService translatorService;

    @PostConstruct
    public void loadBooksFromAPI() throws IOException {
        List<BookUrl> urls = getURLList(url);


        for (BookUrl bookUrl : urls) {
            BookPlain bookPlain = (readBook(new URL(bookUrl.getHref() + "?format=json")));

            Book book = bookService.findByTitle(bookPlain.getTitle());

            if (book == null) {
                book = new Book();
                book.setTitle(bookPlain.getTitle());
                book.setIsbn(bookPlain.getIsbn());
                book.setCover(bookPlain.getCover());
                book.setAudio(!bookPlain.getAudio().isEmpty());
                FragmentData fragmentData = new FragmentData();
                book.setFragment(fragmentData.getFragment(bookPlain));
            } else {
                continue;
            }

            for (AuthorPlain authorPlain : bookPlain.getAuthors()) {
                Author author = authorService.findByName(authorPlain.getName());
                if (author == null) {
                    author = new Author();
                    author.setName(authorPlain.getName());
                    authorService.save(author);
                }
                author.getBooks().add(book);
                book.getAuthors().add(author);
            }

            for (GenrePlain genrePlain : bookPlain.getGenres()) {
                Genre genre = genreService.findByName(genrePlain.getName());
                if (genre == null) {
                    genre = new Genre();
                    genre.setName(genrePlain.getName());
                    genreService.save(genre);
                }
                genre.getBooks().add(book);
                book.getGenres().add(genre);
            }

            for (KindPlain kindPlain : bookPlain.getKinds()) {
                Kind kind = kindService.findByName(kindPlain.getName());
                if (kind == null) {
                    kind = new Kind();
                    kind.setName(kindPlain.getName());
                    kindService.save(kind);
                }
                kind.getBooks().add(book);
                book.getKinds().add(kind);
            }

            for (EpochPlain epochPlain : bookPlain.getEpochs()) {
                Epoch epoch = epochService.findByName(epochPlain.getName());
                if (epoch == null) {
                    epoch = new Epoch();
                    epoch.setName(epochPlain.getName());
                    epochService.save(epoch);
                }
                epoch.getBooks().add(book);
                book.getEpochs().add(epoch);
            }

            for (TranslatorPlain translatorPlain : bookPlain.getTranslators()) {
                Translator translator = translatorService.findByName(translatorPlain.getName());
                if (translator == null) {
                    translator = new Translator();
                    translator.setName(translatorPlain.getName());
                    translatorService.save(translator);
                }
                translator.getBooks().add(book);
                book.getTranslators().add(translator);
            }
            bookService.update(book);
        }
    }

    public List<BookUrl> getURLList(String url) {
        ObjectMapper mapper = new ObjectMapper();
        List<BookUrl> urls;
        try {
            urls = mapper.readValue(new URL(url), new TypeReference<List<BookUrl>>() {
            });
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return List.of();
        }
        return urls;
    }

    public BookPlain readBook(URL url) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BookPlain book;
        try {
            book = mapper.readValue(url, new TypeReference<BookPlain>() {
            });
            return book;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }


}