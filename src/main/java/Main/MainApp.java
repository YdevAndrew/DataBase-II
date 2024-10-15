package Main;

import Controller.PixelArtController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainApp extends Application {

    private TextField nameField;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Image snorlaxGif;
    private Image saitamaGif;
    private PixelArtController pixelArtController;
    private Stage primaryStage;
    private ListView<String> pixelArtListView;
    private Map<String, VBox> kanbanColumns = new HashMap<>();
    private Map<String, String> pixelArtStatus = new HashMap<>();
    private ImageCursor customCursor;
    private Font pressStartFont;


    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        snorlaxGif = new Image(getClass().getResourceAsStream("/images/snorlax-sleeping.gif"));
        saitamaGif = new Image(getClass().getResourceAsStream("/images/saitama.gif"));

        pressStartFont = Font.loadFont(
                getClass().getResource("/fonts/PressStart2P-Regular.ttf").toExternalForm(), 12
        );

        pixelArtController = new PixelArtController();
        pixelArtController.connectToMongoDB();

        stage.setTitle("PixelArt To-Do List");

        VBox layout = createMainLayout();

        Image marioCursorImage = new Image(getClass().getResourceAsStream("/images/Mario cursor.png"));
        customCursor = new ImageCursor(marioCursorImage);

        Scene scene = new Scene(layout, 600, 600); // Ajuste para acomodar os DatePicker
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.setCursor(customCursor);

        stage.setScene(scene);
        stage.show();
    }

    private VBox createMainLayout() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);



        Label titleLabel = new Label("Nome da PixelArt:");
        titleLabel.setFont(pressStartFont);
        titleLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 14px;");

        nameField = new TextField();
        nameField.setFont(pressStartFont);
        nameField.setPromptText("Digite o nome da PixelArt");
        nameField.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 12px;");


        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Data de Início");
        startDatePicker.setPrefWidth(140);
        startDatePicker.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 10px;");



        endDatePicker = new DatePicker();
        endDatePicker.setPrefWidth(140);
        endDatePicker.setPromptText("Data de Fim");
        endDatePicker.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 10px;");


        HBox datePickersBox = new HBox(10); // Adicionando espaçamento entre eles
        datePickersBox.setAlignment(Pos.CENTER);
        datePickersBox.getChildren().addAll(startDatePicker, endDatePicker);


        Button addButton = createStyledButton("Adicionar PixelArt");
        addButton.setOnAction(e -> {
            addPixelArt();
            loadPixelArtsToListView();
        });

        Button removeButton = createStyledButton("Remover PixelArt");
        removeButton.setOnAction(e -> {
            removePixelArt();
            loadPixelArtsToListView();
        });

        Button editButton = createStyledButton("Editar PixelArt");
        editButton.setOnAction(e -> {
            editPixelArt();
            loadPixelArtsToListView();
        });

        Button toggleLayoutButton = createStyledButton("Ir para o Layout Expansível (Kanban)");
        toggleLayoutButton.setOnAction(e -> showKanbanLayout());

        pixelArtListView = new ListView<>();
        pixelArtListView.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 12px;");
        loadPixelArtsToListView();

        layout.getChildren().addAll(
                titleLabel, nameField, startDatePicker, endDatePicker,
                addButton, removeButton, editButton, toggleLayoutButton, pixelArtListView
        );

        return layout;
    }


    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setFont(pressStartFont);
        button.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 12px;");
        return button;
    }

    private void addPixelArt() {
        String name = nameField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (!name.isEmpty() && startDate != null && endDate != null) {
            PixelArt pixelArt = new PixelArt(name, "To-Do", startDate, endDate);
            pixelArtController.addPixelArt(pixelArt);
            nameField.clear();
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
        }
    }

    private void removePixelArt() {
        String selected = pixelArtListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String name = extractNameFromItem(selected);
            pixelArtController.removePixelArt(name);
        }
    }

    private void editPixelArt() {
        String selected = pixelArtListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String originalName = selected.split(" \\(")[0];

            TextInputDialog dialog = new TextInputDialog(originalName);
            dialog.setTitle("Editar PixelArt");
            dialog.setHeaderText("Editar nome da PixelArt");
            dialog.setContentText("Novo nome:");

            dialog.showAndWait().ifPresent(newName -> {
                if (!newName.isEmpty()) {
                    pixelArtController.editPixelArt(originalName, newName);
                    loadPixelArtsToListView();
                }
            });
        }
    }

    private void loadPixelArtsToListView() {
        pixelArtListView.getItems().clear();
        List<PixelArt> pixelArts = pixelArtController.getAllPixelArts();

        pixelArts.forEach(art -> {
            String itemText = art.getName() + " (" + art.getStatus() + ") - " +
                    "Início: " + art.getStartDate() + ", Fim: " + art.getEndDate();
            pixelArtListView.getItems().add(itemText);
        });
    }

    private void showKanbanLayout() {
        Pane kanbanPane = new Pane();
        kanbanPane.setPrefSize(1000, 600);

        Image backgroundImage = new Image(getClass().getResourceAsStream("/images/background  kanban.gif"));
        BackgroundSize backgroundSize = new BackgroundSize(
                BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true
        );
        BackgroundImage background = new BackgroundImage(
                backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize
        );
        kanbanPane.setBackground(new Background(background));

        VBox todoColumn = createKanbanColumn("To-Do");
        VBox inProgressColumn = createKanbanColumn("In Progress");
        VBox doneColumn = createKanbanColumn("Done");

        todoColumn.setLayoutX(50);
        inProgressColumn.setLayoutX(350);
        doneColumn.setLayoutX(650);

        kanbanPane.getChildren().addAll(todoColumn, inProgressColumn, doneColumn);

        Button returnButton = new Button("Voltar ao Layout Original");
        returnButton.setFont(pressStartFont);
        returnButton.setLayoutX(850);
        returnButton.setLayoutY(550);
        returnButton.setOnAction(e -> {
            VBox mainLayout = createMainLayout();
            Scene scene = new Scene(mainLayout, 400, 400);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            scene.setCursor(customCursor);
            loadPixelArtsToListView();
            primaryStage.setScene(scene);
        });

        kanbanPane.getChildren().add(returnButton);

        Scene kanbanScene = new Scene(kanbanPane, 1000, 600);
        kanbanScene.setCursor(customCursor);
        primaryStage.setScene(kanbanScene);

        loadPixelArtsToKanban(todoColumn, inProgressColumn, doneColumn);
    }

    private VBox createKanbanColumn(String columnName) {
        VBox column = new VBox(10);
        column.setPadding(new Insets(10));
        column.setPrefSize(250, 500);

        Label columnTitle = new Label(columnName);
        columnTitle.setFont(pressStartFont);

        switch (columnName) {
            case "To-Do" -> columnTitle.setStyle("-fx-text-fill: #5DADE2;");
            case "In Progress" -> columnTitle.setStyle("-fx-text-fill: #3498DB;");
            case "Done" -> columnTitle.setStyle("-fx-text-fill: #2E86C1;");
        }

        column.getChildren().add(columnTitle);

        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                Node source = (Node) event.getGestureSource();
                if (source.getParent() instanceof VBox sourceColumn) {
                    sourceColumn.getChildren().remove(source);
                }

                Label draggedLabel = new Label(db.getString());
                draggedLabel.setFont(pressStartFont);
                addDragAndDropHandlers(draggedLabel);
                column.getChildren().add(draggedLabel);

                String taskName = draggedLabel.getText();
                String newStatus = columnTitle.getText();
                pixelArtStatus.put(taskName, newStatus);

                pixelArtController.updatePixelArtStatus(taskName, newStatus);

                updateKanbanView();

                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        kanbanColumns.put(columnName, column);
        return column;
    }

    private void updateKanbanView() {
        kanbanColumns.forEach((key, column) -> {
            Label columnTitle = new Label(key);
            columnTitle.setFont(pressStartFont);
            switch (key) {
                case "To-Do" -> columnTitle.setStyle("-fx-text-fill: #5DADE2;");
                case "In Progress" -> columnTitle.setStyle("-fx-text-fill: #3498DB;");
                case "Done" -> columnTitle.setStyle("-fx-text-fill: #2E86C1;");
            }

            column.getChildren().clear();
            column.getChildren().add(columnTitle);
        });

        List<PixelArt> pixelArts = pixelArtController.getAllPixelArts();
        for (PixelArt art : pixelArts) {
            Label itemLabel = new Label(art.getName());
            itemLabel.setFont(pressStartFont);
            itemLabel.setStyle("-fx-text-fill: red;");
            addDragAndDropHandlers(itemLabel);

            ImageView taskGif;
            if ("In Progress".equals(art.getStatus())) {
                taskGif = new ImageView(saitamaGif);
            } else {
                taskGif = new ImageView(snorlaxGif);
            }
            taskGif.setFitHeight(30);
            taskGif.setFitWidth(30);

            HBox taskWithGif = new HBox(10);
            taskWithGif.getChildren().addAll(itemLabel, taskGif);

            switch (art.getStatus()) {
                case "In Progress" -> kanbanColumns.get("In Progress").getChildren().add(taskWithGif);
                case "Done" -> kanbanColumns.get("Done").getChildren().add(taskWithGif);
                default -> kanbanColumns.get("To-Do").getChildren().add(taskWithGif);
            }
        }
    }

    private void loadPixelArtsToKanban(VBox todoColumn, VBox inProgressColumn, VBox doneColumn) {
        List<PixelArt> pixelArts = pixelArtController.getAllPixelArts();
        for (PixelArt art : pixelArts) {
            Label itemLabel = new Label(art.getName());
            itemLabel.setFont(pressStartFont);
            itemLabel.setStyle("-fx-text-fill: red;");
            addDragAndDropHandlers(itemLabel);

            ImageView taskGif;
            if ("In Progress".equals(art.getStatus())) {
                taskGif = new ImageView(saitamaGif);
            } else {
                taskGif = new ImageView(snorlaxGif);
            }
            taskGif.setFitHeight(30);
            taskGif.setFitWidth(30);

            HBox taskWithGif = new HBox(10);
            taskWithGif.getChildren().addAll(itemLabel, taskGif);

            switch (art.getStatus()) {
                case "In Progress" -> inProgressColumn.getChildren().add(taskWithGif);
                case "Done" -> doneColumn.getChildren().add(taskWithGif);
                default -> todoColumn.getChildren().add(taskWithGif);
            }
        }
    }

    private void addDragAndDropHandlers(Label itemLabel) {
        itemLabel.setStyle("-fx-text-fill: red;");

        itemLabel.setOnDragDetected(event -> {
            Dragboard db = itemLabel.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(itemLabel.getText());
            db.setContent(content);
            event.consume();
        });
    }

    private String extractNameFromItem(String item) {
        return item.split(" \\(")[0];
    }

    public static void main(String[] args) {
        launch(args);
    }
}
