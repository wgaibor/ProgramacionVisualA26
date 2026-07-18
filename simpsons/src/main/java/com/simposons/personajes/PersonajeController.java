package com.simposons.personajes;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.simposons.personajes.model.Personaje;
import com.simposons.personajes.model.SimpsonsResponse;
import com.simposons.personajes.service.SimpsonsService;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PersonajeController {

    @FXML private FlowPane charactersContainer;
    @FXML private ScrollPane scrollPane;
    public static int currentPage = 1;

    SimpsonsService simpsonsService;
    List<Personaje> currentCharactersLists;

    public void initialize() {
        simpsonsService = new SimpsonsService();
        currentCharactersLists = new ArrayList<>();
        setupFlowPaneLayout();
        loadInitialCharacters();
    }

    private void setupFlowPaneLayout() {
        // Configurar FlowPane para que se ajuste automaticamente
        charactersContainer.prefWrapLengthProperty().bind(
            scrollPane.widthProperty().subtract(60)
        );

        // Listener para ajustar el ancho de la tarjetas cuando cambie el tama;o de la pantalla
        scrollPane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            //updateCardWidths();
        });


    }

    private void loadInitialCharacters() {
        loadMoreCharacters();
    }

    private void loadMoreCharacters() {
        showLoadingIndicator();
        Task<SimpsonsResponse> loadTask = new Task<>() {

            @Override
            protected SimpsonsResponse call() throws Exception {
                return simpsonsService.getCharacters(currentPage).get();
            }
            
        };

        loadTask.setOnSucceeded(exito -> {
            hideLoadingIndicator();
            SimpsonsResponse response = loadTask.getValue();
            List<Personaje> newCharacters = response != null ? response.getResults() : null;

            if(newCharacters != null && !newCharacters.isEmpty()) {
                currentCharactersLists.addAll(newCharacters);
                newCharacters.forEach(this::displayCharacter);
            }
        });

        loadTask.setOnFailed(error -> {
            System.out.println("Fallo el consumo de webservices");
        });

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void showLoadingIndicator() {
        Label loadingLabel = new Label("Cargando personajes...");
        loadingLabel.getStyleClass().add("loading-indicator");
        loadingLabel.setAlignment(Pos.CENTER);
        charactersContainer.getChildren().add(loadingLabel);
    }

    private void hideLoadingIndicator() {
        charactersContainer.getChildren().removeIf(node ->
            node instanceof Label && ((Label) node).getText().contains("Cargando")
        );
    }

    private void displayCharacter(Personaje character) {
        Platform.runLater(() -> {
            VBox characterCard = createCharacterCard(character);
            charactersContainer.getChildren().add(characterCard);
            // Ajustar ancho despues de agregar
            updateCardWidths();
        });
    }

    private void updateCardWidths() {
        double containerWidth = scrollPane.getWidth();
        if(containerWidth > 0) {
            // Ancho disponible = ancho del scrollPane - padding (30px a cada lado)
            double availableWidth = containerWidth - 60;
            // Ancho de cada tarjeta = (ancho diponible - 3 gaps de 20px) / 4 columnas
            double cardWidth = (availableWidth - 60) / 4;

            // Asegurar un ancho minimo y maximo razonable
            cardWidth = Math.max(250, Math.min(cardWidth, 320));

            // Actualizar ancho de todas las tarjetas existentes
            for (var node : charactersContainer.getChildren()) {
                if( node instanceof VBox ) {
                    VBox card = (VBox) node;
                    card.setPrefWidth(cardWidth);
                    card.setMaxWidth(cardWidth);
                    card.setMinWidth(cardWidth);
                }
            }
        }
    }

    private VBox createCharacterCard(Personaje character) {
        VBox card = new VBox();
        card.getStyleClass().add("character-card");

        //Calcular ancho dinamico para mostrar las columnas
        double containerWidth = scrollPane.getWidth() > 0 ? scrollPane.getWidth() : 1200;
        double availableWidth = containerWidth - 60; // padding izquierdo + derecho
        double cardWidth = (availableWidth - 60) / 4;

        //Asegurar un ancho minimo y maximo razonable
        cardWidth = Math.max(250, Math.min(cardWidth, 320));

        card.setPrefWidth(cardWidth);
        card.setMaxWidth(cardWidth);
        card.setMinWidth(cardWidth);

        // Contenedor para mostrar la imagen con un fondo degradado
        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("character-image-container");
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPrefHeight(220);

        ImageView imageView = createCharacterImage(character);
        imageContainer.getChildren().add(imageView);

        VBox contentContainer = new VBox(12);
        contentContainer.getStyleClass().add("character-content");


        // Contenedor de nombre del personaje
        Label nameLabel = new Label(nonEmpty(character.getName(), "Sin nombre"));
        nameLabel.getStyleClass().add("character-name");
        nameLabel.setWrapText(true);

        // Contenedor de la profesion del personaje
        Label occupationLabel = new Label("Ocupacion:  "+ nonEmpty(character.getOccupation(), "Sin ocupacion"));
        occupationLabel.getStyleClass().add("character-occupation");
        occupationLabel.setWrapText(true);

        // Contenedor para ordenar elementos de manera horizontal
        HBox infoRow = new HBox(10);
        infoRow.getStyleClass().add("character-info-row");


        if(character.getAge() > 0) {
            Label ageLabel = new Label(character.getAge() + " anios");
            ageLabel.getStyleClass().add("character-age");
            infoRow.getChildren().add(ageLabel);
        }

        if(character.getStatus() != null && !character.getStatus().isEmpty()) {
            Label statusLabel = new Label(character.getStatus());
            if("Alive".equalsIgnoreCase(character.getStatus())) {
                statusLabel.getStyleClass().add("character-status-alive");
            } else if ("Deceased".equalsIgnoreCase(character.getStatus())) {
                statusLabel.getStyleClass().add("character-status-deceased");
            } else {
                statusLabel.getStyleClass().add("character-age");
            }
            infoRow.getChildren().add(statusLabel);
        }

        // Contenedor para visualizar la frase celebre del personaje
        Label phraseLabel = new Label("");
        if(character.getPhrases() != null && character.getPhrases().length > 0) {
            String phrase = character.getPhrases()[0];
            phraseLabel.setText(phrase);
            phraseLabel.getStyleClass().add("character-phrase");
            phraseLabel.setWrapText(true);
        }
        
        // Contenedor para visualizar boton
        Button buttonDetails = new Button("Ver detalles");
        buttonDetails.getStyleClass().add("character-details-button");
        buttonDetails.setMaxWidth(Double.MAX_VALUE);
        buttonDetails.setOnAction(evento -> showCharacterDetails(character));

        // --------------------------------------------
        contentContainer.getChildren().addAll(nameLabel, occupationLabel, infoRow, phraseLabel, buttonDetails);
        card.getChildren().addAll(imageContainer, contentContainer);    
        return card;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String nonEmpty(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private void showCharacterDetails(Personaje character) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private ImageView createCharacterImage(Personaje character) {
        ImageView imageView = new ImageView();
        imageView.setFitHeight(180);
        imageView.setFitWidth(180);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        if(character.getPortrait_path() != null && !character.getPortrait_path().isEmpty()) {
            // El portrait_path viene como "/character/X.webp" desde la api
            String portraitPathTemp = character.getPortrait_path().trim();

            // Asegurar que empiece con "/"
            if(!portraitPathTemp.startsWith("/")) {
                portraitPathTemp = "/" + portraitPathTemp;
            }

            // Crear constantes para usar en lambdas
            final String BASE_URL_IMAGE = "https://cdn.thesimpsonsapi.com/200";
            String urlImageDownload = BASE_URL_IMAGE + portraitPathTemp;

            // PRIMERO: Intentar carga directa con JavaFX Image (más simple)
            Image directImage = new Image(urlImageDownload, true);
            imageView.setImage(directImage);

            // Monitorear si la carga directa funciona
            directImage.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                if(newProgress.doubleValue() == 1.0) {
                    if(directImage.isError()) {
                        System.out.println("⚠️ Carga directa falló, intentando descarga manual para: " + character.getName());
                        downLoadAndLoadImage(imageView, urlImageDownload, portraitPathTemp, character);
                    }
                }
            });

        }
        return imageView;
    }

    private void downLoadAndLoadImage(ImageView imageView, String urlImageDownload, String portraitPathTemp, Personaje character) {
        HttpClient client = HttpClient.newBuilder()
                                .connectTimeout(Duration.ofSeconds(15))
                                .build();
        final String[] urls = {
            urlImageDownload,
            "https://cdn.thesimpsonsapi.com/200" + portraitPathTemp
        };

        downLoadImageBytes(client, imageView, urls, 0, character);
    }

    private void downLoadImageBytes(HttpClient client, ImageView imageView, String[] urls, int index, Personaje character) {
        if(index >= urls.length) {
            System.err.println("❌ No se pudo cargar imagen para: " + character.getName() + " después de " + urls.length + " intentos");
            Platform.runLater(() -> {
                imageView.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 90;");
            });
            return;
        }
        final String url = urls[index];
        try {
            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(URI.create(url))
                                    .GET()
                                    .timeout(Duration.ofSeconds(15))
                                    .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                    .header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                                    .header("Accept-Language", "en-US,en;q=0.9")
                                    .header("Referer", "https://thesimpsonsapi.com/")
                                    .header("Origin", "https://thesimpsonsapi.com")
                                    .header("Cache-Control", "no-cache")
                                    .build();
        } catch (Exception e) {
        }
    }

}
