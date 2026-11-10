package com.example.empleados_crud.api;

import com.google.gson.JsonElement;
import com.example.empleados_crud.model.Empleado;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Servicios REST de empleados.
 */
public interface EmpleadoApi {

    String RECURSO = "api/v1/clientes";

    /** READ (todos). Se recibe como JsonElement para soportar respuesta envuelta en datos.content. */
    @GET(RECURSO)
    Call<JsonElement> listar();

    /** READ (uno). Se recibe JsonElement para poder desempaquetar el objeto datos. */
    @GET(RECURSO + "/{id}")
    Call<JsonElement> obtener(@Path("id") long id);

    /** CREATE. */
    @POST(RECURSO)
    Call<JsonElement> crear(@Body Empleado empleado);

    /** UPDATE. */
    @PUT(RECURSO + "/{id}")
    Call<JsonElement> actualizar(@Path("id") long id, @Body Empleado empleado);

    /** DELETE. */
    @DELETE(RECURSO + "/{id}")
    Call<JsonElement> eliminar(@Path("id") long id);
}
