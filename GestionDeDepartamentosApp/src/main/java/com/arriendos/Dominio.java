package com.arriendos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

class Inquilino {
    private String nombres;
    private String apellidos;
    private String cedula;
    private String telefono;

    public Inquilino(String nombres, String apellidos, String cedula, String telefono) {
        this.nombres = Objects.requireNonNullElse(nombres, "");
        this.apellidos = Objects.requireNonNullElse(apellidos, "");
        this.cedula = Objects.requireNonNullElse(cedula, "");
        this.telefono = Objects.requireNonNullElse(telefono, "");
    }

    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getCedula() { return cedula; }
    public String getTelefono() { return telefono; }

    public void setNombres(String nombres) { this.nombres = nombres; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}

class Contrato {
    private Inquilino inquilino;
    private LocalDate fechaInicio;
    private int diaCorte;
    private BigDecimal mensualidad;
    private BigDecimal deposito;
    private int numeroPersonas;

    private BigDecimal deudaArriendo = BigDecimal.ZERO;
    private BigDecimal deudaGarantia = BigDecimal.ZERO;

    public Contrato(Inquilino inquilino, LocalDate fechaInicio, int diaCorte,
                    BigDecimal mensualidad, BigDecimal deposito, int numeroPersonas) {
        this.inquilino = inquilino;
        this.fechaInicio = fechaInicio;
        this.diaCorte = diaCorte;
        this.mensualidad = mensualidad;
        this.deposito = deposito;
        this.numeroPersonas = numeroPersonas;
    }

    public Inquilino getInquilino() { return inquilino; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public int getDiaCorte() { return diaCorte; }
    public BigDecimal getMensualidad() { return mensualidad; }
    public BigDecimal getDeposito() { return deposito; }
    public int getNumeroPersonas() { return numeroPersonas; }
    public BigDecimal getDeudaArriendo() { return deudaArriendo; }
    public BigDecimal getDeudaGarantia() { return deudaGarantia; }

    public void setInquilino(Inquilino inquilino) { this.inquilino = inquilino; }
    public void setMensualidad(BigDecimal mensualidad) { this.mensualidad = mensualidad; }
    public void setDeposito(BigDecimal deposito) { this.deposito = deposito; }

    public void agregarDeudaArriendo(BigDecimal monto) {
        deudaArriendo = deudaArriendo.add(monto);
    }

    public void registrarPagoArriendo(BigDecimal monto) {
        deudaArriendo = deudaArriendo.subtract(monto);
        if (deudaArriendo.compareTo(BigDecimal.ZERO) < 0) deudaArriendo = BigDecimal.ZERO;
    }

    public void agregarDeudaGarantia(BigDecimal monto) {
        deudaGarantia = deudaGarantia.add(monto);
    }

    public void registrarPagoGarantia(BigDecimal monto) {
        deudaGarantia = deudaGarantia.subtract(monto);
        if (deudaGarantia.compareTo(BigDecimal.ZERO) < 0) deudaGarantia = BigDecimal.ZERO;
    }
}

class Departamento {
    private final int numero;
    private BigDecimal precioBase;
    private Contrato contrato;

    public Departamento(int numero, BigDecimal precioBase) {
        this.numero = numero;
        this.precioBase = precioBase;
    }

    public int getNumero() { return numero; }
    public BigDecimal getPrecioBase() { return precioBase; }
    public Contrato getContrato() { return contrato; }
    public boolean isOcupado() { return contrato != null; }

    public void ocupar(Contrato contrato) { this.contrato = contrato; }
    public void desocupar() { this.contrato = null; }
}

class Sucursal {
    private final String nombre;
    private final List<Departamento> departamentos;

    public Sucursal(String nombre, List<Departamento> departamentos) {
        this.nombre = nombre;
        this.departamentos = departamentos;
    }

    public String getNombre() { return nombre; }
    public List<Departamento> getDepartamentos() { return departamentos; }

    @Override public String toString() { return nombre; }
}

class ArriendoService {
    private final List<Sucursal> sucursales = new ArrayList<>();

    public ArriendoService() {
        sucursales.add(new Sucursal("Sucursal A", crearDepartamentos(8, new BigDecimal("220.00"))));
        sucursales.add(new Sucursal("Sucursal B", crearDepartamentos(15, new BigDecimal("250.00"))));

        Departamento d1 = sucursales.get(0).getDepartamentos().get(0);
        d1.ocupar(new Contrato(
                new Inquilino("Ana", "Pérez", "0912345678", "099111222"),
                LocalDate.now().minusMonths(2),
                5,
                new BigDecimal("220.00"),
                new BigDecimal("100.00"),
                2));
        d1.getContrato().agregarDeudaArriendo(new BigDecimal("40.00"));

        Departamento d2 = sucursales.get(1).getDepartamentos().get(3);
        d2.ocupar(new Contrato(
                new Inquilino("Luis", "García", "0922334455", "098333444"),
                LocalDate.now().minusMonths(1),
                10,
                new BigDecimal("250.00"),
                new BigDecimal("120.00"),
                3));
    }

    private List<Departamento> crearDepartamentos(int cantidad, BigDecimal precioBase) {
        List<Departamento> list = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            list.add(new Departamento(i, precioBase));
        }
        return list;
    }

    public List<Sucursal> listarSucursales() {
        return Collections.unmodifiableList(sucursales);
    }

    public LocalDate calcularProximaFechaCobro(Contrato c) {
        int dia = Math.max(1, Math.min(28, c.getDiaCorte()));
        LocalDate hoy = LocalDate.now();
        YearMonth ym = YearMonth.from(hoy);
        LocalDate corteEsteMes = ym.atDay(dia);
        if (!hoy.isAfter(corteEsteMes.minusDays(1))) {
            return corteEsteMes;
        } else {
            return ym.plusMonths(1).atDay(dia);
        }
    }
}
