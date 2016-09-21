package ru.puffds.keygen;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.*;
import javafx.stage.Stage;
import javafx.concurrent.Worker.State;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;

/**
 * Created by R5 on 07.06.2016.
 */
public class App extends Application {
    public final static int WIDTH = 800;
    public final static int HEIGHT = 600;

    private Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle(new String("Проверка ключей на криптостойкость".getBytes(), System.getProperty("file.encoding")/*"Cp1251"*/));
        ClassLoader cl = getClass().getClassLoader();
        stage.getIcons().add(new Image(cl.getResourceAsStream("icons/256.png")));
        stage.getIcons().add(new Image(cl.getResourceAsStream("icons/128.png")));
        stage.getIcons().add(new Image(cl.getResourceAsStream("icons/64.png")));
        stage.getIcons().add(new Image(cl.getResourceAsStream("icons/32.png")));
        scene = new Scene(new Browser(), WIDTH, HEIGHT, Color.web("#666970"));
        stage.setScene(scene);
        stage.setMinHeight(400);
        stage.setMinWidth(640);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class Browser extends Region {
    private final int WIDTH = App.WIDTH;
    private final int HEIGHT = App.HEIGHT;

    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();

    public void printErrJs(String err) {
        String errMsg = "\"" + StringEscapeUtils.escapeJavaScript(err) + "\"";
        webEngine.executeScript(
                "document.getElementById('error-msg').innerHTML = " + errMsg
        );
    }

    public Browser() {
        getStyleClass().add("browser");
        browser.setContextMenuEnabled(false);
        webEngine.load(
                getClass().getClassLoader().getResource("interface.html").toString()
        );

        webEngine.setOnError(new EventHandler<WebErrorEvent>() {
            public void handle(WebErrorEvent event) {
                printErrJs(event.getMessage());
            }
        });

        webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
            public void handle(WebEvent<String> event) {
                System.out.println(event.getData());
            }
        });

        getChildren().add(browser);

        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<State>() {
                    public void changed(ObservableValue<? extends State> ov,
                                        State oldState, State newState) {
                        if (newState == State.SUCCEEDED) {
                            JSObject win =
                                    (JSObject) webEngine.executeScript("window");
                            try {
                                win.setMember("RGStats", new RGStats());
                            } catch (Exception e) {
                                printErrJs(ExceptionUtils.getStackTrace(e));
                                e.printStackTrace();
                            }
                            webEngine.executeScript("afterJavaLoading()");
                        }
                    }
                }
        );
    }

    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height) {
        return WIDTH;
    }

    @Override
    protected double computePrefHeight(double width) {
        return HEIGHT;
    }
}
