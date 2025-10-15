package com.mycompany.sistemadearriendo;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Aplicación JavaFX sencilla que permite interactuar con el {@link SistemaDeArriendo}.
 */
public class SistemaDeArriendoApp extends Application {

    private final SistemaDeArriendo sistema = new SistemaDeArriendo();
    private final ObservableList<Sucursal> sucursales = FXCollections.observableArrayList();
    private final ObservableList<Departamento> departamentos = FXCollections.observableArrayList();
    private TableView<Departamento> tablaDepartamentos;
    private ComboBox<Sucursal> filtroSucursal;
    private TextArea consola;

    private TextField codigoArriendoField;
    private TextField nombreClienteField;
    private TextField cedulaClienteField;
    private TextField telefonoClienteField;
    private TextField correoClienteField;
    private TextField codigoDevolucionField;

    @Override
    public void start(Stage stage) {
        inicializarDatosDemo();
        construirInterfaz(stage);
    }

    private void construirInterfaz(Stage stage) {
        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(15));

        raiz.setTop(crearBarraSuperior());
        raiz.setCenter(crearTablaDepartamentos());
        raiz.setRight(crearPanelAcciones());
        raiz.setBottom(crearConsola());

        Scene escena = new Scene(raiz, 1050, 600);
        stage.setTitle("Sistema de Arriendo de Departamentos");
        stage.setScene(escena);
        stage.show();
    }

    private ToolBar crearBarraSuperior() {
        filtroSucursal = new ComboBox<>(sucursales);
        filtroSucursal.setPromptText("Selecciona una sucursal");
        filtroSucursal.setPrefWidth(300);
        filtroSucursal.setButtonCell(new SucursalListCell());
        filtroSucursal.setCellFactory(list -> new SucursalListCell());

        filtroSucursal.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nueva) -> refrescarDepartamentos(nueva));
        if (!sucursales.isEmpty()) {
            filtroSucursal.getSelectionModel().selectFirst();
        }

        Label titulo = new Label("Inventario de departamentos");
        titulo.getStyleClass().add("titulo-principal");

        ToolBar barra = new ToolBar(titulo, filtroSucursal);
        titulo.setPadding(new Insets(0, 20, 0, 0));
        return barra;
    }

    private TableView<Departamento> crearTablaDepartamentos() {
        tablaDepartamentos = new TableView<>(departamentos);
        tablaDepartamentos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaDepartamentos.setPlaceholder(new Label("No hay departamentos registrados"));

        TableColumn<Departamento, String> codigoCol = new TableColumn<>("Código");
        codigoCol.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<Departamento, String> direccionCol = new TableColumn<>("Dirección");
        direccionCol.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        TableColumn<Departamento, Integer> habitacionesCol = new TableColumn<>("Habitaciones");
        habitacionesCol.setCellValueFactory(new PropertyValueFactory<>("numeroHabitaciones"));

        TableColumn<Departamento, String> precioCol = new TableColumn<>("Precio mensual");
        precioCol.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$ %.2f", cell.getValue().getPrecioMensual())));

        TableColumn<Departamento, String> disponibilidadCol = new TableColumn<>("Estado");
        disponibilidadCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().estaDisponible() ? "Disponible" : "Arrendado"));

        tablaDepartamentos.getColumns().addAll(codigoCol, direccionCol, habitacionesCol, precioCol, disponibilidadCol);
        tablaDepartamentos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                if (codigoArriendoField != null) {
                    codigoArriendoField.setText(seleccionado.getCodigo());
                }
                if (codigoDevolucionField != null) {
                    codigoDevolucionField.setText(seleccionado.getCodigo());
                }
            }
        });

        return tablaDepartamentos;
    }

    private VBox crearPanelAcciones() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(350);

        Label tituloAcciones = new Label("Operaciones");
        tituloAcciones.getStyleClass().add("titulo-seccion");

        VBox panelArriendo = crearFormularioArriendo();
        VBox panelDevolucion = crearFormularioDevolucion();

        panel.getChildren().addAll(tituloAcciones, panelArriendo, panelDevolucion);
        return panel;
    }

    private VBox crearFormularioArriendo() {
        VBox contenedor = new VBox(10);

        Label subtitulo = new Label("Registrar arriendo");
        subtitulo.getStyleClass().add("subtitulo");

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(8);

        codigoArriendoField = new TextField();
        nombreClienteField = new TextField();
        cedulaClienteField = new TextField();
        telefonoClienteField = new TextField();
        correoClienteField = new TextField();

        formulario.addRow(0, new Label("Código depto:"), codigoArriendoField);
        formulario.addRow(1, new Label("Nombre cliente:"), nombreClienteField);
        formulario.addRow(2, new Label("Cédula:"), cedulaClienteField);
        formulario.addRow(3, new Label("Teléfono:"), telefonoClienteField);
        formulario.addRow(4, new Label("Correo:"), correoClienteField);

        Button botonArriendo = new Button("Arrendar");
        botonArriendo.setMaxWidth(Double.MAX_VALUE);
        botonArriendo.disableProperty().bind(Bindings.createBooleanBinding(
                () -> codigoArriendoField.getText().isBlank()
                        || nombreClienteField.getText().isBlank()
                        || cedulaClienteField.getText().isBlank()
                        || telefonoClienteField.getText().isBlank()
                        || correoClienteField.getText().isBlank(),
                codigoArriendoField.textProperty(),
                nombreClienteField.textProperty(),
                cedulaClienteField.textProperty(),
                telefonoClienteField.textProperty(),
                correoClienteField.textProperty()));

        botonArriendo.setOnAction(event -> registrarArriendo());

        contenedor.getChildren().addAll(subtitulo, formulario, botonArriendo);
        VBox.setVgrow(formulario, Priority.NEVER);
        return contenedor;
    }

    private VBox crearFormularioDevolucion() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(20, 0, 0, 0));

        Label subtitulo = new Label("Registrar devolución");
        subtitulo.getStyleClass().add("subtitulo");

        codigoDevolucionField = new TextField();
        codigoDevolucionField.setPromptText("Código del departamento");

        Button botonDevolucion = new Button("Devolver");
        botonDevolucion.setMaxWidth(Double.MAX_VALUE);
        botonDevolucion.disableProperty().bind(codigoDevolucionField.textProperty().isEmpty());
        botonDevolucion.setOnAction(event -> registrarDevolucion());

        contenedor.getChildren().addAll(subtitulo, codigoDevolucionField, botonDevolucion);
        return contenedor;
    }

    private TextArea crearConsola() {
        consola = new TextArea();
        consola.setEditable(false);
        consola.setWrapText(true);
        consola.setPromptText("Aquí aparecerán los mensajes del sistema");
        consola.setPrefRowCount(5);
        return consola;
    }

    private void registrarArriendo() {
        String codigo = codigoArriendoField.getText().trim();
        Cliente cliente = new Cliente(
                nombreClienteField.getText().trim(),
                cedulaClienteField.getText().trim(),
                telefonoClienteField.getText().trim(),
                correoClienteField.getText().trim());

        boolean realizado = sistema.arrendarDepartamento(codigo, cliente);
        if (realizado) {
            escribirMensaje("Arriendo registrado para el departamento " + codigo + " a nombre de " + cliente.getNombre());
            limpiarCamposArriendo();
            refrescarDepartamentos(filtroSucursal.getValue());
        } else {
            escribirMensaje("No fue posible arrendar el departamento con código " + codigo + ". Verifica que esté disponible.");
        }
    }

    private void registrarDevolucion() {
        String codigo = codigoDevolucionField.getText().trim();
        if (sistema.devolverDepartamento(codigo)) {
            escribirMensaje("Devolución registrada para el departamento " + codigo);
            codigoDevolucionField.clear();
            refrescarDepartamentos(filtroSucursal.getValue());
        } else {
            escribirMensaje("No se encontró un arriendo activo con código " + codigo + ".");
        }
    }

    private void limpiarCamposArriendo() {
        codigoArriendoField.clear();
        nombreClienteField.clear();
        cedulaClienteField.clear();
        telefonoClienteField.clear();
        correoClienteField.clear();
    }

    private void escribirMensaje(String mensaje) {
        consola.appendText(mensaje + "\n");
    }

    private void refrescarDepartamentos(Sucursal sucursalSeleccionada) {
        List<Departamento> listado;
        if (sucursalSeleccionada != null) {
            listado = sucursalSeleccionada.getDepartamentos().stream()
                    .sorted(Comparator.comparing(Departamento::getCodigo))
                    .collect(Collectors.toList());
        } else {
            listado = sistema.getSucursales().stream()
                    .flatMap(sucursal -> sucursal.getDepartamentos().stream())
                    .sorted(Comparator.comparing(Departamento::getCodigo))
                    .collect(Collectors.toList());
        }
        departamentos.setAll(listado);
    }

    private void inicializarDatosDemo() {
        Sucursal sucursalCentro = new Sucursal(1, "Sucursal Centro", "Av. Principal 123");
        Sucursal sucursalNorte = new Sucursal(2, "Sucursal Norte", "Av. de los Granados 250");
        sistema.agregarSucursal(sucursalCentro);
        sistema.agregarSucursal(sucursalNorte);

        Manager managerCentro = new Manager("Ana Pérez", "0102030405", "0999001122");
        Manager managerNorte = new Manager("Juan Ruiz", "1718181818", "0988776655");
        sucursalCentro.asignarManager(managerCentro);
        sucursalNorte.asignarManager(managerNorte);

        sucursalCentro.agregarDepartamento(new Departamento("DEP-101", "Amazonas y Colón", 3, 580.0));
        sucursalCentro.agregarDepartamento(new Departamento("DEP-102", "10 de Agosto 123", 2, 450.0));
        sucursalNorte.agregarDepartamento(new Departamento("DEP-201", "La Carolina 88", 1, 350.0));
        sucursalNorte.agregarDepartamento(new Departamento("DEP-202", "De los Shyris 550", 2, 420.0));

        sucursales.setAll(sistema.getSucursales());
        refrescarDepartamentos(null);
    }

    private static class SucursalListCell extends ListCell<Sucursal> {

        @Override
        protected void updateItem(Sucursal item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getNombre());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}