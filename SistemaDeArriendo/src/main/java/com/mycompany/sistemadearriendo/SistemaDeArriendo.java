/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.sistemadearriendo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SistemaDeArriendo {

    private final List<Sucursal> sucursales;

    public SistemaDeArriendo() {
        this.sucursales = new ArrayList<>();
    }

    public void agregarSucursal(Sucursal sucursal) {
        boolean existe = sucursales.stream().anyMatch(s -> s.getId() == sucursal.getId());
        if (existe) {
            throw new IllegalArgumentException("Ya existe una sucursal con id " + sucursal.getId());
        }
        sucursales.add(sucursal);
    }

    public Sucursal buscarSucursal(int id) {
        return sucursales.stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public boolean arrendarDepartamento(String codigoDepartamento, Cliente cliente) {
        for (Sucursal sucursal : sucursales) {
            if (sucursal.arrendarDepartamento(cliente, codigoDepartamento)) {
                sucursal.getManager().ifPresent(manager -> manager.registrarCliente(cliente));
                return true;
            }
        }
        return false;
    }

    public boolean devolverDepartamento(String codigoDepartamento) {
        for (Sucursal sucursal : sucursales) {
            Departamento departamento = sucursal.buscarDepartamentoPorCodigo(codigoDepartamento);
            if (departamento != null && !departamento.estaDisponible()) {
                departamento.liberar();
                return true;
            }
        }
        return false;
    }

    public List<Departamento> departamentosDisponibles() {
        List<Departamento> disponibles = new ArrayList<>();
        for (Sucursal sucursal : sucursales) {
            disponibles.addAll(sucursal.getDepartamentosDisponibles());
        }
        return Collections.unmodifiableList(disponibles);
    }

    public List<Sucursal> getSucursales() {
        return Collections.unmodifiableList(sucursales);
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        SistemaDeArriendo sistema = new SistemaDeArriendo();

        // Se crean dos sucursales con sus respectivos managers
        Sucursal sucursalCentro = new Sucursal(1, "Sucursal Centro", "Av. Principal 123");
        Sucursal sucursalNorte = new Sucursal(2, "Sucursal Norte", "Av. de los Granados 250");
        sistema.agregarSucursal(sucursalCentro);
        sistema.agregarSucursal(sucursalNorte);

        Manager managerCentro = new Manager("Ana Pérez", "0102030405", "0999001122");
        Manager managerNorte = new Manager("Juan Ruiz", "1718181818", "0988776655");
        sucursalCentro.asignarManager(managerCentro);
        sucursalNorte.asignarManager(managerNorte);

        // Se agregan departamentos a cada sucursal
        sucursalCentro.agregarDepartamento(new Departamento("DEP-101", "Amazonas y Colón", 3, 580.0));
        sucursalCentro.agregarDepartamento(new Departamento("DEP-102", "10 de Agosto 123", 2, 450.0));
        sucursalNorte.agregarDepartamento(new Departamento("DEP-201", "La Carolina 88", 1, 350.0));
        sucursalNorte.agregarDepartamento(new Departamento("DEP-202", "De los Shyris 550", 2, 420.0));

        // Se registran clientes y se realizan arriendos
        Cliente clienteLuis = new Cliente("Luis Gómez", "1717171717", "0991234567", "luis@mail.com");
        Cliente clienteMaria = new Cliente("María Torres", "1809090909", "0987654321", "maria@mail.com");

        boolean arriendoLuis = sistema.arrendarDepartamento("DEP-101", clienteLuis);
        boolean arriendoMaria = sistema.arrendarDepartamento("DEP-201", clienteMaria);

        System.out.println("Arriendo para Luis: " + (arriendoLuis ? "exitoso" : "fallido"));
        System.out.println("Arriendo para María: " + (arriendoMaria ? "exitoso" : "fallido"));

        // Se muestra el estado de los departamentos disponibles
        System.out.println("\nDepartamentos disponibles actualmente:");
        for (Departamento departamento : sistema.departamentosDisponibles()) {
            System.out.println(" - " + departamento);
        }

        // Devolución de un departamento
        sistema.devolverDepartamento("DEP-101");
        System.out.println("\nLuego de la devolución del DEP-101:");
        for (Departamento departamento : sistema.departamentosDisponibles()) {
            System.out.println(" - " + departamento);
        }

        // Mostrar el resumen de cada sucursal y su manager
        System.out.println("\nResumen de sucursales:");
        for (Sucursal sucursal : sistema.getSucursales()) {
            System.out.println(sucursal);
            sucursal.getManager().ifPresent(manager -> {
                System.out.println("  Manager: " + manager);
                System.out.println("  Clientes atendidos: " + manager.getClientes());
            });
        }
    }
}