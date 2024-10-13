package Main;

import Controller.PixelArtController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private PixelArtController pixelArtController;
    private Stage primaryStage;
    private ListView<String> pixelArtListView;
    private Map<String, VBox> kanbanColumns = new HashMap<>();
    private Map<String, String> pixelArtStatus = new HashMap<>();  // Map para armazenar o status das tarefas

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Carregar a fonte "Press Start 2P"
        Font.loadFont(getClass().getResource("/fonts/PressStart2P-Regular.ttf").toExternalForm(), 12);

        // Inicializar o controlador e conectar ao MongoDB
        pixelArtController = new PixelArtController();
        pixelArtController.connectToMongoDB();

        // Configurar a interface gráfica
        stage.setTitle("PixelArt To-Do List");

        // Layout principal com as funcionalidades originais
        VBox layout = createMainLayout();

        Scene scene = new Scene(layout, 400, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    private VBox createMainLayout() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20, 20, 20, 20));

        // Título centralizado
        Label titleLabel = new Label("Nome da PixelArt:");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER); // Centraliza o título

        // Campo de entrada para o nome da PixelArt
        nameField = new TextField();
        nameField.setPromptText("Digite o nome da PixelArt");

        // ListView para exibir as PixelArts adicionadas
        pixelArtListView = new ListView<>();
        loadPixelArtsToListView(); // Carregar PixelArts no ListView

        // Botão para alternar para o layout Kanban expansível
        Button toggleLayoutButton = new Button("Ir para o Layout Expansível (Kanban)");
        toggleLayoutButton.setOnAction(e -> showKanbanLayout());

        // Botões de funcionalidades originais (adicionar, remover, editar)
        Button addButton = new Button("Adicionar PixelArt");
        addButton.setOnAction(e -> {
            addPixelArt();
            loadPixelArtsToListView(); // Atualizar lista ao adicionar
        });

        Button removeButton = new Button("Remover PixelArt");
        removeButton.setOnAction(e -> {
            removePixelArt();
            loadPixelArtsToListView(); // Atualizar lista ao remover
        });

        Button editButton = new Button("Editar PixelArt");
        editButton.setOnAction(e -> {
            editPixelArt();
            loadPixelArtsToListView(); // Atualizar lista ao editar
        });

        layout.getChildren().addAll(titleLabel, nameField, addButton, removeButton, editButton, toggleLayoutButton, pixelArtListView);

        // Aplicar a fonte estilo pixel art
        layout.setStyle("-fx-font-family: 'Press Start 2P', sans-serif; -fx-font-size: 12px;");

        return layout;
    }

    // Mostrar o layout Kanban expansível
    private void showKanbanLayout() {
        Pane kanbanPane = new Pane();
        kanbanPane.setPrefSize(1000, 600);

        VBox todoColumn = createKanbanColumn("To-Do");
        VBox inProgressColumn = createKanbanColumn("In Progress");
        VBox doneColumn = createKanbanColumn("Done");

        todoColumn.setLayoutX(50);
        todoColumn.setLayoutY(50);
        inProgressColumn.setLayoutX(350);
        inProgressColumn.setLayoutY(50);
        doneColumn.setLayoutX(650);
        doneColumn.setLayoutY(50);

        kanbanPane.getChildren().addAll(todoColumn, inProgressColumn, doneColumn);

        Button returnButton = new Button("Voltar ao Layout Original");
        returnButton.setLayoutX(850);
        returnButton.setLayoutY(550);
        returnButton.setOnAction(e -> {
            VBox mainLayout = createMainLayout();
            Scene scene = new Scene(mainLayout, 400, 400);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); // Reaplicar o estilo CSS
            loadPixelArtsToListView(); // Atualizar lista com status atualizado
            primaryStage.setScene(scene);
        });

        kanbanPane.getChildren().add(returnButton);

        Scene kanbanScene = new Scene(kanbanPane, 1000, 600);
        primaryStage.setScene(kanbanScene);
        primaryStage.show();

        loadPixelArtsToKanban(todoColumn, inProgressColumn, doneColumn);
    }

    private VBox createKanbanColumn(String columnName) {
        VBox column = new VBox(10);
        column.setPadding(new Insets(10));
        column.setPrefSize(250, 500);
        column.setStyle("-fx-border-color: black; -fx-border-width: 2px;");

        Label columnTitle = new Label(columnName);
        columnTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
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
                Label draggedLabel = new Label(db.getString());
                addDragAndDropHandlers(draggedLabel);
                column.getChildren().add(draggedLabel);

                // Atualizar o status da PixelArt no Map e no MongoDB
                String taskName = draggedLabel.getText();
                String columnNameLocal = ((Label) column.getChildren().get(0)).getText();
                pixelArtStatus.put(taskName, columnNameLocal);
                pixelArtController.updatePixelArtStatus(taskName, columnNameLocal); // Atualizar no banco de dados

                Node source = (Node) event.getGestureSource();
                VBox sourceColumn = (VBox) source.getParent();
                sourceColumn.getChildren().remove(source);

                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        kanbanColumns.put(columnName, column);
        return column;
    }

    // Carregar as PixelArts em suas respectivas colunas no layout Kanban
    private void loadPixelArtsToKanban(VBox todoColumn, VBox inProgressColumn, VBox doneColumn) {
        List<PixelArt> pixelArts = pixelArtController.getAllPixelArts();
        for (PixelArt art : pixelArts) {
            Label itemLabel = new Label(art.getName());
            addDragAndDropHandlers(itemLabel);

            // Verificar o status da PixelArt e colocar na coluna apropriada
            String status = art.getStatus();
            if (status.equals("In Progress")) {
                inProgressColumn.getChildren().add(itemLabel);
            } else if (status.equals("Done")) {
                doneColumn.getChildren().add(itemLabel);
            } else {
                todoColumn.getChildren().add(itemLabel);
            }
        }
    }

    private void addDragAndDropHandlers(Label label) {
        label.setOnDragDetected(event -> {
            Dragboard db = label.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(label.getText());
            db.setContent(content);
            event.consume();
        });

        label.setOnDragDone(event -> event.consume());
    }

    private void addPixelArt() {
        String name = nameField.getText();
        if (!name.isEmpty()) {
            PixelArt newArt = new PixelArt(name, false, "To-Do");
            pixelArtController.addPixelArt(newArt);
            pixelArtStatus.put(name, "To-Do"); // Adicionar novo PixelArt com status "To-Do"
            loadPixelArtsToListView(); // Atualizar o layout original
            nameField.clear();
        } else {
            showAlert("Erro", "O nome da PixelArt não pode ser vazio.");
        }
    }

    private void removePixelArt() {
        String name = nameField.getText();
        if (!name.isEmpty()) {
            pixelArtController.removePixelArt(name);
            pixelArtStatus.remove(name); // Remover o status ao remover a PixelArt
            loadPixelArtsToListView(); // Atualizar o layout original
            nameField.clear();
        } else {
            showAlert("Erro", "O nome da PixelArt não pode ser vazio.");
        }
    }

    private void editPixelArt() {
        // Verificar se o usuário selecionou uma PixelArt na lista
        String selectedArt = pixelArtListView.getSelectionModel().getSelectedItem();

        if (selectedArt != null) {
            // Exibir um campo para o usuário digitar o novo nome
            TextInputDialog dialog = new TextInputDialog(selectedArt);
            dialog.setTitle("Editar PixelArt");
            dialog.setHeaderText(null);
            dialog.setContentText("Novo nome para a PixelArt:");

            // Mostrar o diálogo e pegar o novo nome do usuário
            dialog.showAndWait().ifPresent(newName -> {
                if (!newName.isEmpty()) {
                    // Atualizar o nome da PixelArt no banco de dados
                    pixelArtController.editPixelArt(selectedArt, newName);

                    // Atualizar a ListView
                    loadPixelArtsToListView();
                } else {
                    showAlert("Erro", "O novo nome da PixelArt não pode ser vazio.");
                }
            });
        } else {
            showAlert("Erro", "Por favor, selecione uma PixelArt para editar.");
        }
    }

    private void loadPixelArtsToListView() {
        List<PixelArt> pixelArts = pixelArtController.getAllPixelArts();
        pixelArtListView.getItems().clear();
        for (PixelArt art : pixelArts) {
            pixelArtListView.getItems().add(art.getName());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
