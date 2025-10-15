/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemadearriendo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author angel
 * Representa una sucursal de la empresa de arriendo de departamentos.
 */
public class Sucursal {

    private final int id;
    private final String nombre;
    private final String direccion;
    private final List<Departamento> departamentos;
    private Manager manager;

    public Sucursal(int id, String nombre, String direccion) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser positivo");
        }
        this.id = id;
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        this.direccion = Objects.requireNonNull(direccion, "La dirección no puede ser nula");
        this.departamentos = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public Optional<Manager> getManager() {
        return Optional.ofNullable(manager);
    }

    public void asignarManager(Manager manager) {
        this.manager = Objects.requireNonNull(manager, "El manager no puede ser nulo");
        manager.asignarSucursal(this);
    }

    public void agregarDepartamento(Departamento departamento) {
        Objects.requireNonNull(departamento, "El departamento no puede ser nulo");
        boolean existe = departamentos.stream()
                .anyMatch(d -> d.getCodigo().equals(departamento.getCodigo()));
        if (existe) {
            throw new IllegalArgumentException("Ya existe un departamento con código " + departamento.getCodigo());
        }
        departamentos.add(departamento);
    }

    public Departamento buscarDepartamentoPorCodigo(String codigo) {
        return departamentos.stream()
                .filter(d -> d.getCodigo().equals(codigo))
                .findFirst()
                .orElse(null);
    }

    public boolean arrendarDepartamento(Cliente cliente, String codigoDepartamento) {
        Departamento departamento = buscarDepartamentoPorCodigo(codigoDepartamento);
        if (departamento == null || !departamento.estaDisponible()) {
            return false;
        }
        departamento.asignarInquilino(cliente);
        return true;
    }

    public List<Departamento> getDepartamentosDisponibles() {
        return departamentos.stream()
                .filter(Departamento::estaDisponible)
                .collect(Collectors.toList());
    }

    public List<Departamento> getDepartamentos() {
        return Collections.unmodifiableList(departamentos);
    }

    @Override
    public String toString() {
        return "Sucursal{" + "id=" + id
                + ", nombre='" + nombre + '\''
                + ", direccion='" + direccion + '\''
                + ", departamentos=" + departamentos.size()
                + '}';
    }
}