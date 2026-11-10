package com.example.empleados_crud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonElement;

import java.util.List;

import com.example.empleados_crud.R;
import com.example.empleados_crud.api.ApiUtils;
import com.example.empleados_crud.api.RetrofitClient;
import com.example.empleados_crud.model.Empleado;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** READ: listado de empleados. Se recarga cada vez que la pantalla vuelve a primer plano. */
public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipe;
    private TextView tvMensaje;
    private EmpleadoAdapter adapter;
    private Call<JsonElement> llamada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UiUtils.aplicarEdgeToEdge(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipe = findViewById(R.id.swipe);
        tvMensaje = findViewById(R.id.tvMensaje);
        RecyclerView recycler = findViewById(R.id.recycler);
        FloatingActionButton fab = findViewById(R.id.fabAgregar);

        adapter = new EmpleadoAdapter(this::abrirDetalle);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        swipe.setOnRefreshListener(this::cargarEmpleados);
        fab.setOnClickListener(v -> startActivity(new Intent(this, FormularioActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarEmpleados();
    }

    @Override
    protected void onDestroy() {
        if (llamada != null) llamada.cancel();
        super.onDestroy();
    }

    private void cargarEmpleados() {
        if (llamada != null) llamada.cancel();
        swipe.setRefreshing(true);
        tvMensaje.setVisibility(View.GONE);

        llamada = RetrofitClient.getApi().listar();
        llamada.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (isDestroyed()) return;
                swipe.setRefreshing(false);

                if (response.isSuccessful()) {
                    List<Empleado> lista = ApiUtils.parsearLista(response.body());
                    adapter.setDatos(lista);
                    if (lista.isEmpty()) mostrarMensaje("No hay empleados registrados.\nToca + para agregar uno.");
                } else {
                    adapter.setDatos(null);
                    mostrarMensaje(ApiUtils.mensajeError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                if (call.isCanceled() || isDestroyed()) return;
                swipe.setRefreshing(false);
                adapter.setDatos(null);
                mostrarMensaje(ApiUtils.mensajeFallo(t) + "\n\nDesliza hacia abajo para reintentar.");
            }
        });
    }

    private void mostrarMensaje(String texto) {
        tvMensaje.setText(texto);
        tvMensaje.setVisibility(View.VISIBLE);
    }

    private void abrirDetalle(Empleado e) {
        Long id = ApiUtils.idDe(e);
        if (id == null) {
            Toast.makeText(this, "Este empleado no tiene un id válido", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, DetalleActivity.class);
        i.putExtra(DetalleActivity.EXTRA_ID, id.longValue());
        startActivity(i);
    }
}
