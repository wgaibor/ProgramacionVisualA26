package com.simposons.personajes;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import com.simposons.personajes.model.Personaje;
import com.simposons.personajes.model.SimpsonsResponse;
import com.simposons.personajes.service.SimpsonsService;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;



public class PersonajeController {

    @FXML private FlowPane charactersContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField searchField;
    @FXML private Button loadMoreButton;
    public static int currentPage = 1;

    private boolean isLoading = false;
    private boolean hasMorePage = true;

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

    @FXML
    private void loadMoreCharacters() {
        if(isLoading || !hasMorePage) {
            return;
        }
        isLoading = true;
        loadMoreButton.setDisable(true);
        showLoadingIndicator();
        Task<SimpsonsResponse> loadTask = new Task<>() {

            @Override
            protected SimpsonsResponse call() throws Exception {
                return simpsonsService.getCharacters(currentPage).get();
            }
            
        };

        loadTask.setOnSucceeded(exito -> {
            hideLoadingIndicator();
            isLoading = false;

            SimpsonsResponse response = loadTask.getValue();
            List<Personaje> newCharacters = response != null ? response.getResults() : null;

            if(newCharacters != null && !newCharacters.isEmpty()) {
                currentCharactersLists.addAll(newCharacters);
                newCharacters.forEach(this::displayCharacter);
                currentPage++;
            }

            hasMorePage = response != null && response.getNext() != null && !response.getNext().isBlank();
            loadMoreButton.setDisable(!hasMorePage);
            if(!hasMorePage) {
                loadMoreButton.setText("No hay mas personajes");
            }
        });

        loadTask.setOnFailed(error -> {
            hideLoadingIndicator();
            isLoading = false;
            loadMoreButton.setDisable(false);
            showAlert("Error", "Hubo un problema al momento de consumir el ws");
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles de "+ nonEmpty(character.getName(), "Personaje"));
        alert.setHeaderText(nonEmpty(character.getName(), "Personaje"));

        StringBuilder details = new StringBuilder();
        details.append("ID:  ").append(character.getId()).append("\n");
        details.append("Nombre: ").append(nonEmpty(character.getName(), "SIN NOMBRE")).append("\n");

        if(character.getAge() > 0) {
            details.append("Edad ").append(character.getAge()).append("\n");
        }
        if(character.getBirthdate() != null) {
            details.append("Fecha de nacimiento: ").append(character.getBirthdate()).append("\n");
        }
        if(hasText(character.getGender())) {
            details.append("Genero: ").append(character.getGender()).append("\n");
        }
        if(hasText(character.getOccupation())) {
            details.append("Ocupacion: ").append(character.getOccupation()).append("\n");
        }
        if(hasText(character.getStatus())) {
            details.append("Estado: ").append(character.getStatus()).append("\n");
        }
        if(character.getPhrases() != null && character.getPhrases().length > 0) {
            details.append("\n Frases famosas \n");
            for(String phrase: character.getPhrases()) {
                if(hasText(phrase)) {
                    details.append("- ").append(phrase).append("\n");
                }
            }
        }

        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    // MÉTODO COMPLETO - NO MODIFICAR
    private ImageView createCharacterImage(Personaje character) {
        ImageView imageView = new ImageView();
        imageView.setFitHeight(180);
        imageView.setFitWidth(180);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        
        if (character.getPortrait_path() != null && !character.getPortrait_path().isEmpty()) {
            // El portrait_path viene como "/character/X.webp" desde la API
            String portraitPathTemp = character.getPortrait_path().trim();
            
            // Asegurar que empiece con "/"
            if (!portraitPathTemp.startsWith("/")) {
                portraitPathTemp = "/" + portraitPathTemp;
            }
            
            // Crear variable final para usar en lambdas
            final String portraitPath = portraitPathTemp;
            final String imageUrl = "https://cdn.thesimpsonsapi.com/200" + portraitPath;
            
            System.out.println("📸 Cargando imagen para: " + character.getName());
            System.out.println("   URL: " + imageUrl);
            System.out.println("   Portrait path original: " + character.getPortrait_path());
            
            // PRIMERO: Intentar carga directa con JavaFX Image (más simple)
            Image directImage = new Image(imageUrl, true);
            imageView.setImage(directImage);
            
            // Monitorear si la carga directa funciona
            directImage.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                if (newProgress.doubleValue() == 1.0) {
                    if (!directImage.isError()) {
                        System.out.println("✅ Carga directa exitosa para: " + character.getName());
                    } else {
                        System.out.println("⚠️ Carga directa falló, intentando descarga manual para: " + character.getName());
                        downloadAndLoadImage(imageView, imageUrl, portraitPath, character.getName());
                    }
                }
            });
            
            directImage.errorProperty().addListener((obs, wasError, isNowError) -> {
                if (isNowError) {
                    System.out.println("⚠️ Error en carga directa, intentando descarga manual para: " + character.getName());
                    downloadAndLoadImage(imageView, imageUrl, portraitPath, character.getName());
                }
            });
        } else {
            System.out.println("⚠️ No hay portrait_path para: " + character.getName());
        }
        
        return imageView;
    }

    // MÉTODO COMPLETO - NO MODIFICAR
    private void downloadAndLoadImage(ImageView imageView, String primaryUrl, String portraitPath, String characterName) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        
        // URLs a intentar en orden
        final String[] urls = {
            primaryUrl,
            "https://cdn.thesimpsonsapi.com/200" + portraitPath,
            "https://cdn.thesimpsonsapi.com" + portraitPath
        };
        
        downloadImageBytes(client, imageView, urls, 0, characterName);
    }

    // MÉTODO COMPLETO - NO MODIFICAR
    private void downloadImageBytes(HttpClient client, ImageView imageView, String[] urls, int index, String characterName) {
        if (index >= urls.length) {
            System.err.println("❌ No se pudo cargar imagen para: " + characterName + " después de " + urls.length + " intentos");
            Platform.runLater(() -> {
                // Mostrar placeholder visual
                imageView.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 90;");
            });
            return;
        }
        
        final String url = urls[index];
        System.out.println("🔄 Intento " + (index + 1) + " para " + characterName + ": " + url);
        
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
            
            client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept(response -> {
                    System.out.println("📥 Respuesta HTTP " + response.statusCode() + " para " + characterName);
                    
                    if (response.statusCode() == 200) {
                        byte[] imageBytes = response.body();
                        System.out.println("📦 Bytes descargados: " + (imageBytes != null ? imageBytes.length : 0) + " para " + characterName);
                        
                        if (imageBytes != null && imageBytes.length > 0) {
                            // Verificar que es una imagen válida (WebP empieza con RIFF)
                            if (imageBytes.length >= 4) {
                                String header = new String(imageBytes, 0, Math.min(4, imageBytes.length));
                                System.out.println("🔍 Header de imagen: " + bytesToHex(imageBytes, 0, 4) + " para " + characterName);
                            }
                            
                            Platform.runLater(() -> {
                                try {
                                    // Leer imagen WebP usando ImageIO con soporte de TwelveMonkeys
                                    ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                                    BufferedImage bufferedImage = ImageIO.read(bais);
                                    
                                    if (bufferedImage != null) {
                                        // Convertir BufferedImage a JavaFX Image
                                        Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                                        
                                        // Establecer imagen en ImageView
                                        imageView.setImage(fxImage);
                                        System.out.println("✅ Imagen WebP cargada exitosamente para: " + characterName);
                                    } else {
                                        System.err.println("❌ No se pudo leer imagen WebP para: " + characterName);
                                        if (index + 1 < urls.length) {
                                            downloadImageBytes(client, imageView, urls, index + 1, characterName);
                                        }
                                    }
                                    
                                } catch (Exception e) {
                                    System.err.println("❌ Excepción procesando imagen WebP para " + characterName + ": " + e.getMessage());
                                    e.printStackTrace();
                                    if (index + 1 < urls.length) {
                                        downloadImageBytes(client, imageView, urls, index + 1, characterName);
                                    }
                                }
                            });
                        } else {
                            System.err.println("❌ Imagen vacía o nula desde: " + url);
                            downloadImageBytes(client, imageView, urls, index + 1, characterName);
                        }
                    } else {
                        System.err.println("❌ HTTP " + response.statusCode() + " desde: " + url);
                        downloadImageBytes(client, imageView, urls, index + 1, characterName);
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("❌ Excepción descargando " + url + " para " + characterName + ": " + throwable.getMessage());
                    throwable.printStackTrace();
                    downloadImageBytes(client, imageView, urls, index + 1, characterName);
                    return null;
                });
        } catch (Exception e) {
            System.err.println("❌ Error creando request para " + characterName + ": " + e.getMessage());
            e.printStackTrace();
            downloadImageBytes(client, imageView, urls, index + 1, characterName);
        }
    }

    private String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int inc = offset; inc < Math.min(offset + length, bytes.length); inc++){
            sb.append(String.format("%02X", bytes[inc]));
        }
        return sb.toString().trim();
    }

    @FXML
    private void searchCharacter() {
        String searchText = searchField.getText();
        if(!hasText(searchText)) {
            showAlert("Error", "Por favor ingresa el nombre de un personaje");
        }

        clearCharacterContainer();
        showLoadingIndicator();

        Personaje foundCharacter = currentCharactersLists.stream()
                                        .filter(character -> character.getName() != null &&
                                            character.getName().toLowerCase().contains(searchText.trim().toLowerCase()))
                                        .findFirst()
                                        .orElse(null);

        hideLoadingIndicator();
        clearCharacterContainer();

        if(foundCharacter != null) {
            displayCharacter(foundCharacter);
        } else {
            showAlert("Informacion", "Personaje no encontrado en la lista actual. Intenta cargar mas personajes o busca por ID");
        }
    }

    @FXML
    private void getRadomCharacter(){
        clearCharacterContainer();
        showLoadingIndicator();

        Task<Personaje> ramdonTask = new Task<>() {
            @Override
            protected Personaje call() throws Exception {
                int randomId = new Random().nextInt(1182) + 1;
                return simpsonsService.getCharacterById(randomId).get();
            }
        };

        ramdonTask.setOnSucceeded(exitoso -> {
            hideLoadingIndicator();
            clearCharacterContainer();

            Personaje character = ramdonTask.getValue();
            if(character != null) {
                currentCharactersLists.clear();
                currentCharactersLists.add(character);
                displayCharacter(character);
            } else {
                showAlert("Error", "No se pudo obtener un personaje aleatorio");
            }
        });

        ramdonTask.setOnFailed(error -> {
            hideLoadingIndicator();
            clearCharacterContainer();
            showAlert("Error", "Problemas de conexion");
        });

        Thread thread = new Thread(ramdonTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearCharacterContainer() {
        charactersContainer.getChildren().clear();
    }

}
