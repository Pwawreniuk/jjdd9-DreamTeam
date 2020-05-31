package com.infoshareacademy.dreamteam.servlets;

import com.infoshareacademy.dreamteam.freemarker.TemplatePrinter;
import com.infoshareacademy.dreamteam.service.BookService;
import com.infoshareacademy.dreamteam.initializer.ModelInitializer;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/browse")
public class BrowseServlet extends HttpServlet {

    @Inject
    private TemplatePrinter templatePrinter;

    @Inject
    private BookService bookService;

    @Inject
    private ModelInitializer modelInitializer;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("text/html; charset=UTF-8");

        Map<String, Object> model = modelInitializer.initModel(req);
        templatePrinter.printTemplate(resp, model, getServletContext(),
                "browse.ftlh");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("text/html; charset=UTF-8");

        int startPage = Integer.parseInt(req.getParameter("pageNum")) - 1;
        int pageSize = Integer.parseInt(req.getParameter("pageSize"));
        String audio = req.getParameter("audio");
        String genre = req.getParameter("genre");
        Map<String, Object> tableData = new HashMap<>();

        int rows;
        if ((audio == null || audio.equals("blank")) && (genre == null || genre.equals("blank"))  ) {
            tableData.put("books", bookService.findBooks(startPage * pageSize, pageSize));
            rows = bookService.countBooks();
        } else {
            tableData.put("books", bookService.findBooks(startPage * pageSize, pageSize, audio, genre));
            rows = bookService.countBooks(audio, genre);
        }

        tableData.put("pageNum", startPage + 1);

        int numberOfPages = rows / pageSize;
        if (numberOfPages % pageSize > 0) {
            numberOfPages++;
        }

        tableData.put("genres", bookService.getGenres());
        tableData.put("numberOfPages", numberOfPages);
        templatePrinter.printTemplate(resp, tableData, getServletContext(), "browse-table.ftlh");
    }
}