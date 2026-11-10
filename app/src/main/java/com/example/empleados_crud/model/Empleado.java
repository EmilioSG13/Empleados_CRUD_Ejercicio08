package com.example.empleados_crud.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * Representa la estructura JSON de un empleado de la API.
 */
public class Empleado {

    @SerializedName("id")
    private Long id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("correo")
    private String correo;

    @SerializedName("telefono")
    private String telefono;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("registrado")
    private JsonElement registrado;

    @SerializedName(value = "links", alternate = {"_links"})
    private JsonElement links;

    public Empleado() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public JsonElement getRegistrado() { return registrado; }
    public void setRegistrado(JsonElement registrado) { this.registrado = registrado; }

    public JsonElement getLinks() { return links; }
    public void setLinks(JsonElement links) { this.links = links; }
}
