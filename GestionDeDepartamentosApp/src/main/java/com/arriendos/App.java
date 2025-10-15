package com.arriendos;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class App extends Application {

    private final ArriendoService service = new ArriendoService();
    private ComboBox<Sucursal> comboSucursal;
    private GridPane gridDeptos;
    private VBox panelDetalle;
    private Sucursal sucursalActual;
    private Departamento dptoSeleccionado;

    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));

    @Override
    public void start(Stage stage) {
        stage.setTitle("Gestión de Arriendos");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,#F3E5F5,#EDE7F6,#D1C4E9);"
                + "-fx-font-family:'Segoe UI';");

        Label lblTitulo = new Label("Gestión de Arriendos");
        lblTitulo.setFont(Font.font("Segoe UI Semibold", 26));
        lblTitulo.setTextFill(Color.web("#4A148C"));

        comboSucursal = new ComboBox<>();
        comboSucursal.getItems().addAll(service.listarSucursales());
        comboSucursal.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-padding:6 12;" +
                "-fx-font-size:14px;" +
                "-fx-border-color:#B39DDB;" +
                "-fx-text-fill:#4A148C;"
        );
        comboSucursal.setPrefWidth(220);
        comboSucursal.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                sucursalActual = newV;
                construirGrid();
                panelDetalle.getChildren().setAll(crearPanelPlaceholder());
                dptoSeleccionado = null;
            }
        });

        HBox topBar = new HBox(15, new Label("Sucursal:"), comboSucursal);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15));
        topBar.setStyle("-fx-background-color:#F8F6FF;-fx-background-radius:15;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.1),8,0,0,2);");

        VBox top = new VBox(10, lblTitulo, topBar);
        top.setAlignment(Pos.CENTER_LEFT);
        root.setTop(top);

        gridDeptos = new GridPane();
        gridDeptos.setHgap(16);
        gridDeptos.setVgap(16);
        gridDeptos.setPadding(new Insets(20));
        ScrollPane scroll = new ScrollPane(gridDeptos);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        root.setCenter(scroll);

        panelDetalle = new VBox(15);
        panelDetalle.setPadding(new Insets(25));
        panelDetalle.setStyle("-fx-background-color:rgba(255,255,255,0.92);"
                + "-fx-background-radius:15;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.1),10,0,0,4);");
        panelDetalle.getChildren().add(crearPanelPlaceholder());
        root.setRight(panelDetalle);

        Scene scene = new Scene(root, 1200, 720);
        stage.setScene(scene);
        stage.show();

        if (!comboSucursal.getItems().isEmpty()) comboSucursal.getSelectionModel().selectFirst();
    }

    private VBox crearPanelPlaceholder() {
        Label l = new Label("Selecciona un departamento para ver detalles.");
        l.setTextFill(Color.web("#4A148C"));
        l.setFont(Font.font("Segoe UI", 15));
        VBox v = new VBox(l);
        v.setAlignment(Pos.CENTER);
        return v;
    }

    private void construirGrid() {
        gridDeptos.getChildren().clear();
        if (sucursalActual == null) return;
        List<Departamento> dptos = sucursalActual.getDepartamentos();
        int cols = 4;
        for (int i = 0; i < dptos.size(); i++) {
            Departamento d = dptos.get(i);
            Button btn = new Button("Depto " + d.getNumero());
            btn.setPrefSize(150, 100);
            btn.setWrapText(true);
            btn.setFont(Font.font("Segoe UI Semibold", 15));
            String color = d.isOcupado()
                    ? "-fx-background-color:#B39DDB;"
                    : "-fx-background-color:#D1C4E9;";
            btn.setStyle(color + "-fx-text-fill:#4A148C;-fx-background-radius:12;"
                    + "-fx-font-weight:600;-fx-cursor:hand;");
            btn.setOnAction(e -> {
                dptoSeleccionado = d;
                renderDetalle(d);
            });
            int row = i / cols, col = i % cols;
            gridDeptos.add(btn, col, row);
        }
    }

    private void renderDetalle(Departamento d) {
        panelDetalle.getChildren().clear();

        Label titulo = new Label("Departamento #" + d.getNumero());
        titulo.setFont(Font.font("Segoe UI Semibold", 22));
        titulo.setTextFill(Color.web("#4A148C"));

        VBox box = new VBox(12);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color:#F8F6FF;-fx-background-radius:15;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.1),8,0,0,2);");

        if (d.isOcupado() && d.getContrato() != null) {
            Contrato c = d.getContrato();
            Inquilino i = c.getInquilino();

            Label[] info = {
                new Label("Inquilino: " + i.getNombres() + " " + i.getApellidos()),
                new Label("Cédula: " + i.getCedula() + " | Teléfono: " + i.getTelefono()),
                new Label("Arrendado desde: " + c.getFechaInicio()),
                new Label("Día de corte: " + c.getDiaCorte()),
                new Label("Próximo cobro: " + service.calcularProximaFechaCobro(c)),
                new Label("Personas en departamento: " + c.getNumeroPersonas()),
                new Label("Mensualidad: " + formatoMoneda.format(c.getMensualidad())),
                new Label("Garantía pactada: " + formatoMoneda.format(c.getDeposito())),
                new Label("Deuda arriendo: " + formatoMoneda.format(c.getDeudaArriendo())),
                new Label("Deuda garantía: " + formatoMoneda.format(c.getDeudaGarantia()))
            };
            for (Label l : info) {
                l.setTextFill(Color.web("#4A148C"));
                l.setFont(Font.font("Segoe UI", 14));
            }

            FlowPane botones = new FlowPane(15, 15);
            botones.setAlignment(Pos.CENTER);
            botones.getChildren().addAll(
                    botonMorado("Editar precio", e -> editarPrecio(c, d)),
                    botonMorado("Editar garantía", e -> editarGarantia(c, d)),
                    botonMorado("Pago arriendo", e -> pagoArriendo(c, d)),
                    botonMorado("Pago garantía", e -> pagoGarantia(c, d)),
                    botonMorado("Desocupar", e -> { d.desocupar(); construirGrid(); panelDetalle.getChildren().setAll(crearPanelPlaceholder()); })
            );

            box.getChildren().addAll(info);
            box.getChildren().add(new Separator());
            box.getChildren().add(botones);
        } else {
            Label l1 = new Label("Estado: Vacío");
            l1.setTextFill(Color.web("#4A148C"));
            l1.setFont(Font.font("Segoe UI", 14));
            Label l2 = new Label("Precio base: " + formatoMoneda.format(d.getPrecioBase()));
            l2.setTextFill(Color.web("#4A148C"));
            Button arrendar = botonMorado("Arrendar departamento", e -> arrendarDialog(d));
            VBox vacioBox = new VBox(10, l1, l2, arrendar);
            vacioBox.setAlignment(Pos.CENTER);
            box.getChildren().add(vacioBox);
        }

        panelDetalle.getChildren().addAll(titulo, box);
    }

    private Button botonMorado(String texto, javafx.event.EventHandler<javafx.event.ActionEvent> accion) {
        Button b = new Button(texto);
        b.setStyle("-fx-background-color:#9575cd;-fx-text-fill:white;"
                + "-fx-background-radius:12;-fx-font-size:14px;-fx-cursor:hand;"
                + "-fx-padding:8 16;-fx-font-weight:600;");
        b.setOnAction(accion);
        return b;
    }

    private void editarPrecio(Contrato c, Departamento d) {
        TextInputDialog t = new TextInputDialog(c.getMensualidad().toPlainString());
        t.setHeaderText("Nuevo precio mensual");
        t.showAndWait().ifPresent(v -> {
            try { c.setMensualidad(new BigDecimal(v)); renderDetalle(d); }
            catch (Exception ex) { mostrarError("Valor inválido"); }
        });
    }

    private void editarGarantia(Contrato c, Departamento d) {
        TextInputDialog t = new TextInputDialog(c.getDeposito().toPlainString());
        t.setHeaderText("Monto de garantía (pagada o a pagar)");
        t.showAndWait().ifPresent(v -> {
            try { c.setDeposito(new BigDecimal(v)); renderDetalle(d); }
            catch (Exception ex) { mostrarError("Valor inválido"); }
        });
    }

    private void pagoArriendo(Contrato c, Departamento d) {
        TextInputDialog t = new TextInputDialog("0");
        t.setHeaderText("Registrar pago de arriendo");
        t.showAndWait().ifPresent(v -> {
            try { c.registrarPagoArriendo(new BigDecimal(v)); renderDetalle(d); }
            catch (Exception ex) { mostrarError("Monto inválido"); }
        });
    }

    private void pagoGarantia(Contrato c, Departamento d) {
        TextInputDialog t = new TextInputDialog("0");
        t.setHeaderText("Registrar pago de garantía");
        t.showAndWait().ifPresent(v -> {
            try { c.registrarPagoGarantia(new BigDecimal(v)); renderDetalle(d); }
            catch (Exception ex) { mostrarError("Monto inválido"); }
        });
    }

    private void arrendarDialog(Departamento d) {
        Dialog<Contrato> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Arriendo");
        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10); gp.setPadding(new Insets(15));

        TextField tfNom = new TextField();
        TextField tfApe = new TextField();
        TextField tfCed = new TextField();
        TextField tfTel = new TextField();
        DatePicker dpInicio = new DatePicker(LocalDate.now());
        TextField tfDiaCorte = new TextField("5");
        TextField tfMens = new TextField(d.getPrecioBase().toPlainString());
        TextField tfDep = new TextField("0");
        TextField tfPersonas = new TextField("1");

        gp.addRow(0, new Label("Nombres:"), tfNom);
        gp.addRow(1, new Label("Apellidos:"), tfApe);
        gp.addRow(2, new Label("Cédula:"), tfCed);
        gp.addRow(3, new Label("Teléfono:"), tfTel);
        gp.addRow(4, new Label("Inicio:"), dpInicio);
        gp.addRow(5, new Label("Día corte:"), tfDiaCorte);
        gp.addRow(6, new Label("Mensualidad ($):"), tfMens);
        gp.addRow(7, new Label("Garantía ($):"), tfDep);
        gp.addRow(8, new Label("# Personas:"), tfPersonas);

        dialog.getDialogPane().setContent(gp);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    Inquilino inq = new Inquilino(tfNom.getText(), tfApe.getText(), tfCed.getText(), tfTel.getText());
                    int diaCorte = Integer.parseInt(tfDiaCorte.getText());
                    BigDecimal mensualidad = new BigDecimal(tfMens.getText());
                    BigDecimal deposito = new BigDecimal(tfDep.getText());
                    int personas = Integer.parseInt(tfPersonas.getText());
                    return new Contrato(inq, dpInicio.getValue(), diaCorte, mensualidad, deposito, personas);
                } catch (Exception ex) { mostrarError("Datos inválidos"); }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> { d.ocupar(c); construirGrid(); renderDetalle(d); });
    }

    private void mostrarError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    public static void main(String[] args) { launch(); }
}
