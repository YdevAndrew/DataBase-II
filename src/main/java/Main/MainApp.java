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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainApp extends Application {

    private TextField nameField;
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

        // Carregamento dos GIFs
        snorlaxGif = new Image(getClass().getResourceAsStream("/images/snorlax-sleeping.gif"));
        saitamaGif = new Image(getClass().getResourceAsStream("/images/saitama.gif"));


        // Carregar a fonte "Press Start 2P"
        pressStartFont = Font.loadFont(
                getClass().getResource("/fonts/PressStart2P-Regular.ttf").toExternalForm(), 12
        );

        // Inicializar o controlador e conectar ao MongoDB
        pixelArtController = new PixelArtController();
        pixelArtController.connectToMongoDB();

        // Configurar a interface gráfica
        stage.setTitle("PixelArt To-Do List");

        // Layout principal
        VBox layout = createMainLayout();

        // Cursor personalizado
        Image marioCursorImage = new Image(getClass().getResourceAsStream("/images/Mario cursor.png"));
        customCursor = new ImageCursor(marioCursorImage);

        // Definir a cena principal
        Scene scene = new Scene(layout, 400, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.setCursor(customCursor);

        stage.setScene(scene);
        stage.show();
    }

    private VBox createMainLayout() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        // Configurar o título com a fonte personalizada
        Label titleLabel = new Label("Nome da PixelArt:");
        titleLabel.setFont(pressStartFont); // Aplicando a fonte aqui
        titleLabel.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 14px;");

        // Configurar o TextField com a fonte personalizada
        nameField = new TextField();
        nameField.setFont(pressStartFont);
        nameField.setPromptText("Digite o nome da PixelArt");
        nameField.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 12px;");

        // Configuração dos botões
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

        // ListView configurado com a fonte personalizada
        pixelArtListView = new ListView<>();
        pixelArtListView.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 12px;");
        loadPixelArtsToListView();

        // Adicionar todos os elementos ao layout principal
        layout.getChildren().addAll(
                titleLabel, nameField, addButton, removeButton, editButton, toggleLayoutButton, pixelArtListView
        );

        return layout;
    }

    // Método para criar botões estilizados com a fonte
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setFont(pressStartFont);
        button.setStyle("-fx-font-family: 'Press Start 2P'; -fx-font-size: 12px;");
        return button;
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

        // Definindo a cor do texto para cada coluna
        switch (columnName) {
            case "To-Do" -> columnTitle.setStyle("-fx-text-fill: #5DADE2;");
            case "In Progress" -> columnTitle.setStyle("-fx-text-fill: #3498DB;");
            case "Done" -> columnTitle.setStyle("-fx-text-fill: #2E86C1;");
        }

        column.getChildren().add(columnTitle);

        // Configuração de Drag and Drop
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
                // Remover a tarefa da coluna anterior
                Node source = (Node) event.getGestureSource();
                if (source.getParent() instanceof VBox sourceColumn) {
                    sourceColumn.getChildren().remove(source);  // Remover o item da coluna anterior
                }

                // Adicionar a tarefa à nova coluna
                Label draggedLabel = new Label(db.getString());
                draggedLabel.setFont(pressStartFont);
                addDragAndDropHandlers(draggedLabel);
                column.getChildren().add(draggedLabel);

                // Atualizar o status da tarefa
                String taskName = draggedLabel.getText();
                String newStatus = columnTitle.getText();
                pixelArtStatus.put(taskName, newStatus);

                // Atualizar o status no banco de dados
                pixelArtController.updatePixelArtStatus(taskName, newStatus);

                // Atualizar visualmente o Kanban imediatamente
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
        // Limpar as colunas do Kanban
        kanbanColumns.forEach((key, column) -> {
            // Armazenar o título da coluna
            Label columnTitle = new Label(key);
            columnTitle.setFont(pressStartFont);
            switch (key) {
                case "To-Do" -> columnTitle.setStyle("-fx-text-fill: #5DADE2;");
                case "In Progress" -> columnTitle.setStyle("-fx-text-fill: #3498DB;");
                case "Done" -> columnTitle.setStyle("-fx-text-fill: #2E86C1;");
            }

            // Limpar a coluna mantendo o título
            column.getChildren().clear();
            column.getChildren().add(columnTitle);  // Adiciona o título novamente após limpar
        });

        // Recarregar as tarefas do banco de dados e atualizar as colunas
        List<PixelArt> pixelArts = pixelArtController.getAllPixelArts();
        for (PixelArt art : pixelArts) {
            // Criar um Label com o nome da tarefa
            Label itemLabel = new Label(art.getName());
            itemLabel.setFont(pressStartFont);
            itemLabel.setStyle("-fx-text-fill: red;"); // A cor do texto da tarefa é vermelho
            addDragAndDropHandlers(itemLabel);

            // Carregar o GIF (pode usar gifs diferentes para cada status)
            ImageView taskGif;
            if ("In Progress".equals(art.getStatus())) {
                taskGif = new ImageView(saitamaGif);  // GIF para tarefas "In Progress"
            } else {
                taskGif = new ImageView(snorlaxGif);  // GIF para tarefas "To-Do" e "Done"
            }
            taskGif.setFitHeight(30); // Ajustar o tamanho do GIF
            taskGif.setFitWidth(30);

            // Criar um HBox para alinhar o nome da tarefa e o GIF
            HBox taskWithGif = new HBox(10); // Espaçamento de 10px entre o Label e o GIF
            taskWithGif.getChildren().addAll(itemLabel, taskGif);

            // Adicionar a tarefa na coluna correta
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
            // Criar um Label com o nome da tarefa
            Label itemLabel = new Label(art.getName());
            itemLabel.setFont(pressStartFont);
            itemLabel.setStyle("-fx-text-fill: red;"); // A cor do texto da tarefa é vermelho
            addDragAndDropHandlers(itemLabel);

            // Carregar o GIF (pode usar gifs diferentes para cada status)
            ImageView taskGif;
            if ("In Progress".equals(art.getStatus())) {
                taskGif = new ImageView(saitamaGif);  // GIF para tarefas "In Progress"
            } else {
                taskGif = new ImageView(snorlaxGif);  // GIF para tarefas "To-Do" e "Done"
            }
            taskGif.setFitHeight(30); // Ajustar o tamanho do GIF
            taskGif.setFitWidth(30);

            // Criar um HBox para alinhar o nome da tarefa e o GIF
            HBox taskWithGif = new HBox(10); // Espaçamento de 10px entre o Label e o GIF
            taskWithGif.getChildren().addAll(itemLabel, taskGif);

            // Adicionar a tarefa na coluna correta
            switch (art.getStatus()) {
                case "In Progress" -> inProgressColumn.getChildren().add(taskWithGif);
                case "Done" -> doneColumn.getChildren().add(taskWithGif);
                default -> todoColumn.getChildren().add(taskWithGif);
            }
        }
    }
    private void addDragAndDropHandlers(Label itemLabel) {
        // Definindo a cor do texto para vermelho
        itemLabel.setStyle("-fx-text-fill: red;");

        itemLabel.setOnDragDetected(event -> {
            Dragboard db = itemLabel.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(itemLabel.getText());
            db.setContent(content);
            event.consume();
        });
    }

    private void loadPixelArtsToListView() {
        pixelArtListView.getItems().clear();
        List<PixelArt> pixelArts = pixelArtController.getAllPixelArts();

        // Para cada PixelArt, mostrar nome + status
        pixelArts.forEach(art -> {
            String itemText = art.getName() + " (" + art.getStatus() + ")";
            pixelArtListView.getItems().add(itemText);
        });
    }

    private void addPixelArt() {
        String name = nameField.getText();
        if (!name.isEmpty()) {
            PixelArt pixelArt = new PixelArt(name, "To-Do");
            pixelArtController.addPixelArt(pixelArt);
            nameField.clear();
        }
    }

    private void removePixelArt() {
        String selected = pixelArtListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            pixelArtController.removePixelArt(selected);
        }
    }

    private void editPixelArt() {
        String selected = pixelArtListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String newName = nameField.getText();
            if (!newName.isEmpty()) {
                pixelArtController.editPixelArt(selected, newName);
                nameField.clear();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
