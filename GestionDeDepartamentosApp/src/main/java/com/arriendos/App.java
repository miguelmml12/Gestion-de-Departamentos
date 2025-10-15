package com.arriendos;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class App extends Application {

    // Servicio en memoria (luego se puede cambiar por SQL/JPA)
    private final ArriendoService service = new ArriendoService();

    // Componentes de UI
    private ComboBox<Sucursal> comboSucursal;
    private GridPane gridDeptos;
    private Label lblTitulo;
    private VBox panelDetalle;

    // Estado actual
    private Sucursal sucursalActual;
    private Departamento dptoSeleccionado;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Gestión de Arriendos");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        // ---------- TOP ----------
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        comboSucursal = new ComboBox<>();
        comboSucursal.getItems().addAll(service.listarSucursales());
        comboSucursal.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                sucursalActual = newV;
                construirGrid();
                lblTitulo.setText("Sucursal: " + newV.getNombre());
                panelDetalle.getChildren().setAll(crearPanelPlaceholder());
                dptoSeleccionado = null;
            }
        });

        Button btnRefrescar = new Button("Refrescar");
        btnRefrescar.setOnAction(e -> construirGrid());

        lblTitulo = new Label("Gestión de Arriendos");
        lblTitulo.setFont(Font.font(18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(new Label("Sucursal:"), comboSucursal, btnRefrescar, spacer, lblTitulo);
        root.setTop(topBar);

        // ---------- CENTRO ----------
        gridDeptos = new GridPane();
        gridDeptos.setHgap(10);
        gridDeptos.setVgap(10);
        gridDeptos.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(gridDeptos);
        scroll.setFitToWidth(true);
        root.setCenter(scroll);

        // ---------- DERECHA ----------
        panelDetalle = new VBox(10);
        panelDetalle.setPadding(new Insets(10));
        panelDetalle.getChildren().add(crearPanelPlaceholder());
        panelDetalle.setPrefWidth(360);
        root.setRight(panelDetalle);

        Scene scene = new Scene(root, 1100, 650);
        stage.setScene(scene);
        stage.show();

        if (!comboSucursal.getItems().isEmpty())
            comboSucursal.getSelectionModel().selectFirst();
    }

    private VBox crearPanelPlaceholder() {
        VBox box = new VBox(6);
        box.getChildren().add(new Label("Selecciona un departamento para ver detalles."));
        return box;
    }

    private void construirGrid() {
        gridDeptos.getChildren().clear();
        if (sucursalActual == null) return;

        List<Departamento> dptos = sucursalActual.getDepartamentos();
        int cols = 4;
        for (int i = 0; i < dptos.size(); i++) {
            Departamento d = dptos.get(i);
            Button btn = new Button(String.valueOf(d.getNumero()));
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setMinSize(80, 60);

            String color = d.isOcupado() ? "-fx-background-color:#ffb3b3;" : "-fx-background-color:#b3ffb3;";
            btn.setStyle(color + "-fx-font-size:16px;-fx-font-weight:bold;");
            btn.setOnAction(e -> {
                dptoSeleccionado = d;
                renderDetalle(d);
            });

            int row = i / cols, col = i % cols;
            gridDeptos.add(btn, col, row);
            GridPane.setHgrow(btn, Priority.ALWAYS);
            GridPane.setVgrow(btn, Priority.ALWAYS);
        }
    }

    private void renderDetalle(Departamento d) {
        panelDetalle.getChildren().clear();
        Label titulo = new Label("Departamento #" + d.getNumero());
        titulo.setFont(Font.font(16));

        VBox contenido = new VBox(6);
        contenido.setPadding(new Insets(6));
        contenido.setStyle("-fx-background-color:#f5f5f5;-fx-padding:10;-fx-background-radius:8;");

        if (d.isOcupado() && d.getContrato() != null) {
            Contrato c = d.getContrato();
            Inquilino inq = c.getInquilino();

            Label l1 = new Label("Estado: OCUPADO");
            Label l2 = new Label("Arrendado desde: " + c.getFechaInicio());
            LocalDate proxCobro = service.calcularProximaFechaCobro(c);
            Label l3 = new Label("Próximo cobro: " + proxCobro + " (día corte: " + c.getDiaCorte() + ")");
            Label l4 = new Label("Mensualidad: $" + c.getMensualidad());
            Label l5 = new Label("Personas: " + c.getNumeroPersonas());
            Label l6 = new Label("Inquilino: " + inq.getNombres() + " " + inq.getApellidos());
            Label l7 = new Label("Cédula: " + inq.getCedula() + " | Tel: " + inq.getTelefono());
            Label l8 = new Label("Deuda arriendo: $" + c.getDeudaArriendo());
            Label l9 = new Label("Deuda garantía: $" + c.getDeudaGarantia());

            HBox nav = new HBox(8);
            Button prev = new Button("Anterior"), next = new Button("Siguiente");
            prev.setOnAction(e -> seleccionarAnterior());
            next.setOnAction(e -> seleccionarSiguiente());
            nav.getChildren().addAll(prev, next);

            HBox acciones = new HBox(8);
            Button desoc = new Button("Desocupar");
            Button pago = new Button("Registrar pago");
            Button editar = new Button("Editar inquilino");
            desoc.setOnAction(e -> { d.desocupar(); construirGrid(); renderDetalle(d); });
            pago.setOnAction(e -> {
                TextInputDialog dialog = new TextInputDialog("50.00");
                dialog.setHeaderText("Monto a registrar (arriendo)");
                dialog.setContentText("USD:");
                dialog.showAndWait().ifPresent(val -> {
                    try {
                        c.registrarPagoArriendo(new BigDecimal(val));
                        renderDetalle(d);
                    } catch (Exception ex) { mostrarError("Monto inválido"); }
                });
            });
            editar.setOnAction(e -> editarInquilino(c));
            acciones.getChildren().addAll(desoc, pago, editar);

            contenido.getChildren().addAll(l1,l2,l3,l4,l5,l6,l7,l8,l9,new Separator(),nav,acciones);
        } else {
            Label l1 = new Label("Estado: DESOCUPADO");
            Label l2 = new Label("Precio base: $" + d.getPrecioBase());

            HBox nav = new HBox(8);
            Button prev = new Button("Anterior"), next = new Button("Siguiente");
            prev.setOnAction(e -> seleccionarAnterior());
            next.setOnAction(e -> seleccionarSiguiente());
            nav.getChildren().addAll(prev, next);

            HBox acciones = new HBox(8);
            Button arrendar = new Button("Arrendar");
            arrendar.setOnAction(e -> arrendarDialog(d));
            acciones.getChildren().add(arrendar);

            contenido.getChildren().addAll(l1,l2,new Separator(),nav,acciones);
        }

        panelDetalle.getChildren().addAll(titulo, contenido);
    }

    private void seleccionarAnterior() {
        if (sucursalActual == null || dptoSeleccionado == null) return;
        List<Departamento> list = sucursalActual.getDepartamentos();
        int i = list.indexOf(dptoSeleccionado);
        if (i > 0) { dptoSeleccionado = list.get(i - 1); renderDetalle(dptoSeleccionado); }
    }

    private void seleccionarSiguiente() {
        if (sucursalActual == null || dptoSeleccionado == null) return;
        List<Departamento> list = sucursalActual.getDepartamentos();
        int i = list.indexOf(dptoSeleccionado);
        if (i < list.size() - 1) { dptoSeleccionado = list.get(i + 1); renderDetalle(dptoSeleccionado); }
    }

    private void arrendarDialog(Departamento d) {
        Dialog<Contrato> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Contrato");

        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(8); gp.setPadding(new Insets(10));

        TextField nom = new TextField(), ape = new TextField(),
                ced = new TextField(), tel = new TextField(),
                dia = new TextField("5"), men = new TextField(d.getPrecioBase().toPlainString()),
                dep = new TextField("0"), per = new TextField("1");
        DatePicker ini = new DatePicker(LocalDate.now());

        gp.addRow(0, new Label("Nombres:"), nom);
        gp.addRow(1, new Label("Apellidos:"), ape);
        gp.addRow(2, new Label("Cédula:"), ced);
        gp.addRow(3, new Label("Teléfono:"), tel);
        gp.addRow(4, new Label("Inicio:"), ini);
        gp.addRow(5, new Label("Día corte:"), dia);
        gp.addRow(6, new Label("Mensualidad:"), men);
        gp.addRow(7, new Label("Depósito:"), dep);
        gp.addRow(8, new Label("# Personas:"), per);

        dialog.getDialogPane().setContent(gp);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) try {
                Inquilino inq = new Inquilino(nom.getText(), ape.getText(), ced.getText(), tel.getText());
                return new Contrato(inq, ini.getValue(),
                        Integer.parseInt(dia.getText()),
                        new BigDecimal(men.getText()),
                        new BigDecimal(dep.getText()),
                        Integer.parseInt(per.getText()));
            } catch (Exception e) { mostrarError("Datos inválidos: " + e.getMessage()); }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> { d.ocupar(c); construirGrid(); renderDetalle(d); });
    }

    private void editarInquilino(Contrato c) {
        Dialog<Inquilino> dialog = new Dialog<>();
        dialog.setTitle("Editar Inquilino");
        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(8); gp.setPadding(new Insets(10));

        TextField nom = new TextField(c.getInquilino().getNombres());
        TextField ape = new TextField(c.getInquilino().getApellidos());
        TextField ced = new TextField(c.getInquilino().getCedula());
        TextField tel = new TextField(c.getInquilino().getTelefono());

        gp.addRow(0, new Label("Nombres:"), nom);
        gp.addRow(1, new Label("Apellidos:"), ape);
        gp.addRow(2, new Label("Cédula:"), ced);
        gp.addRow(3, new Label("Teléfono:"), tel);

        dialog.getDialogPane().setContent(gp);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ?
                new Inquilino(nom.getText(), ape.getText(), ced.getText(), tel.getText()) : null);
        dialog.showAndWait().ifPresent(n -> { c.setInquilino(n); renderDetalle(dptoSeleccionado); });
    }

    private void mostrarError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}
