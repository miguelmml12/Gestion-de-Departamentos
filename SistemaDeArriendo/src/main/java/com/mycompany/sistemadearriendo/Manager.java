
package com.mycompany.sistemadearriendo;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class Manager {
    private final String nombre;
    private final String cedula;
    private final String telefono;
    private final List<Cliente> clientes;
    private Sucursal sucursal;

    public Manager(String nombre, String cedula, String telefono) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        this.cedula = Objects.requireNonNull(cedula, "La cédula no puede ser nula");
        this.telefono = Objects.requireNonNull(telefono, "El teléfono no puede ser nulo");
        this.clientes = new ArrayList<>();
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

    public Sucursal getSucursal() {
        return sucursal;
    }

    void asignarSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public void registrarCliente(Cliente cliente) {
        Objects.requireNonNull(cliente, "El cliente no puede ser nulo");
        boolean existe = clientes.stream().anyMatch(c -> c.getCedula().equals(cliente.getCedula()));
        if (!existe) {
            clientes.add(cliente);
        }
    }

    public boolean arrendarDepartamento(String codigoDepartamento, Cliente cliente) {
        if (sucursal == null) {
            throw new IllegalStateException("El manager no tiene una sucursal asignada");
        }
        registrarCliente(cliente);
        return sucursal.arrendarDepartamento(cliente, codigoDepartamento);
    }

    public void recibirDevolucion(String codigoDepartamento) {
        if (sucursal == null) {
            throw new IllegalStateException("El manager no tiene una sucursal asignada");
        }
        Departamento departamento = sucursal.buscarDepartamentoPorCodigo(codigoDepartamento);
        if (departamento == null) {
            throw new IllegalArgumentException("No existe un departamento con código " + codigoDepartamento);
        }
        departamento.liberar();
    }

    public List<Cliente> getClientes() {
        return Collections.unmodifiableList(clientes);
    }

    @Override
    public String toString() {
        return "Manager{" + "nombre='" + nombre + '\''
                + ", cedula='" + cedula + '\''
                + ", telefono='" + telefono + '\''
                + ", clientesRegistrados=" + clientes.size()
                + '}';
    }
}