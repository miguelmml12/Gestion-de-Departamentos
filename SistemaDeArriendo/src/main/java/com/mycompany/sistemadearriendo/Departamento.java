/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemadearriendo;

import java.util.Objects;

public class Departamento {
    

    private final String codigo;
    private final String direccion;
    private final int numeroHabitaciones;
    private double precioMensual;
    private boolean disponible;
    private Cliente inquilino;

    public Departamento(String codigo, String direccion, int numeroHabitaciones, double precioMensual) {
        this.codigo = Objects.requireNonNull(codigo, "El código no puede ser nulo");
        this.direccion = Objects.requireNonNull(direccion, "La dirección no puede ser nula");
        if (numeroHabitaciones <= 0) {
            throw new IllegalArgumentException("El número de habitaciones debe ser positivo");
        }
        this.numeroHabitaciones = numeroHabitaciones;
        actualizarPrecio(precioMensual);
        this.disponible = true;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDireccion() {
        return direccion;
    }

    public int getNumeroHabitaciones() {
        return numeroHabitaciones;
    }

    public double getPrecioMensual() {
        return precioMensual;
    }

    public void actualizarPrecio(double precioMensual) {
        if (precioMensual <= 0) {
            throw new IllegalArgumentException("El precio mensual debe ser positivo");
        }
        this.precioMensual = precioMensual;
    }

    public boolean estaDisponible() {
        return disponible;
    }

    public Cliente getInquilino() {
        return inquilino;
    }

    public void asignarInquilino(Cliente cliente) {
        if (!disponible) {
            throw new IllegalStateException("El departamento ya se encuentra arrendado");
        }
        this.inquilino = Objects.requireNonNull(cliente, "El cliente no puede ser nulo");
        this.disponible = false;
        cliente.registrarArriendo(this);
    }

    public void liberar() {
        if (disponible) {
            return;
        }
        disponible = true;
        if (inquilino != null) {
            inquilino.finalizarArriendo();
            inquilino = null;
        }
    }

    @Override
    public String toString() {
        return "Departamento{" + "codigo='" + codigo + '\''
                + ", direccion='" + direccion + '\''
                + ", habitaciones=" + numeroHabitaciones
                + ", precioMensual=" + precioMensual
                + ", disponible=" + disponible
                + '}';
    }
}