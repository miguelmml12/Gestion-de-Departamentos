/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemadearriendo;

import java.util.Objects;

/**
 *
 * @author angel
 * Representa a un cliente que arrienda departamentos dentro del sistema.
 */
public class Cliente {
    

    private final String nombre;
    private final String cedula;
    private String telefono;
    private String correoElectronico;
    private Departamento departamentoActual;

    public Cliente(String nombre, String cedula, String telefono, String correoElectronico) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        this.cedula = Objects.requireNonNull(cedula, "La cédula no puede ser nula");
        this.telefono = Objects.requireNonNull(telefono, "El teléfono no puede ser nulo");
        this.correoElectronico = Objects.requireNonNull(correoElectronico, "El correo no puede ser nulo");
    }

    public String getNombre() {
        return nombre;
    }

    public String getCedula() {
        return cedula;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = Objects.requireNonNull(telefono, "El teléfono no puede ser nulo");
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = Objects.requireNonNull(correoElectronico, "El correo no puede ser nulo");
    }

    public Departamento getDepartamentoActual() {
        return departamentoActual;
    }

    public void registrarArriendo(Departamento departamento) {
        this.departamentoActual = departamento;
    }

    public void finalizarArriendo() {
        this.departamentoActual = null;
    }

    @Override
    public String toString() {
        String codigoDepartamento = departamentoActual != null ? departamentoActual.getCodigo() : "Sin arriendo";
        return "Cliente{"
                + "nombre='" + nombre + '\''
                + ", cedula='" + cedula + '\''
                + ", telefono='" + telefono + '\''
                + ", correoElectronico='" + correoElectronico + '\''
                + ", departamentoActual='" + codigoDepartamento + '\''
                + '}';
    }
}