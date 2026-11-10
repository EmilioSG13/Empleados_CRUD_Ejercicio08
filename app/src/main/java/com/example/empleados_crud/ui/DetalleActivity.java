package com.example.empleados_crud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonElement;

import com.example.empleados_crud.R;
import com.example.empleados_crud.api.ApiUtils;
import com.example.empleados_crud.api.RetrofitClient;
import com.example.empleados_crud.model.Empleado;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** READ (detalle) + accesos a UPDATE y DELETE. */
public class DetalleActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";

    private long id;
    private Empleado actual;

    private ProgressBar progress;
    private View contenido;
    private TextView tvId, tvNombre, tvCorreo, tvTelefono, tvDireccion, tvRegistrado, tvLinks;
    private MaterialButton btnEditar, btnEliminar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);
        UiUtils.aplicarEdgeToEdge(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id < 0) {
            Toast.makeText(this, "Empleado no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progress = findViewById(R.id.progress);
        contenido = findViewById(R.id.contenido);
        tvId = findViewById(R.id.tvId);
        tvNombre = findViewById(R.id.tvNombre);
        tvCorreo = findViewById(R.id.tvCorreo);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvDireccion = findViewById(R.id.tvDireccion);
        tvRegistrado = findViewById(R.id.tvRegistrado);
        tvLinks = findViewById(R.id.tvLinks);
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);

        btnEditar.setOnClickListener(v -> editar());
        btnEliminar.setOnClickListener(v -> confirmarEliminacion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setCargando(boolean cargando) {
        progress.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnEditar.setEnabled(!cargando && actual != null);
        btnEliminar.setEnabled(!cargando && actual != null);
    }

    private void cargar() {
        setCargando(true);
        RetrofitClient.getApi().obtener(id).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    actual = ApiUtils.parsearEmpleado(response.body());
                    if (actual != null) {
                        mostrar(actual);
                    } else {
                        toast("El servidor respondió sin datos válidos del empleado.");
                    }
                } else {
                    toast(ApiUtils.mensajeError(response));
                    if (response.code() == 404) finish();
                }
                setCargando(false);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                if (isDestroyed()) return;
                setCargando(false);
                toast(ApiUtils.mensajeFallo(t));
            }
        });
    }

    private void mostrar(Empleado e) {
        contenido.setVisibility(View.VISIBLE);
        Long idReal = ApiUtils.idDe(e);
        tvId.setText(idReal != null ? String.valueOf(idReal) : String.valueOf(id));
        tvNombre.setText(valor(e.getNombre()));
        tvCorreo.setText(valor(e.getCorreo()));
        tvTelefono.setText(valor(e.getTelefono()));
        tvDireccion.setText(valor(e.getDireccion()));
        tvRegistrado.setText(valor(ApiUtils.textoRegistrado(e.getRegistrado())));
        tvLinks.setText(valor(ApiUtils.textoLinks(e.getLinks())));
    }

    private void editar() {
        if (actual == null) return;
        Intent i = new Intent(this, FormularioActivity.class);
        i.putExtra(FormularioActivity.EXTRA_EMPLEADO_JSON, RetrofitClient.getGson().toJson(actual));
        i.putExtra(FormularioActivity.EXTRA_ID, id);
        startActivity(i);
    }

    private void confirmarEliminacion() {
        if (actual == null) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar empleado")
                .setMessage("¿Seguro que deseas eliminar a " + valor(actual.getNombre())
                        + "?\nEsta acción no se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (d, w) -> eliminar())
                .show();
    }

    private void eliminar() {
        setCargando(true);
        RetrofitClient.getApi().eliminar(id).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (isDestroyed()) return;
                setCargando(false);
                if (response.isSuccessful()) {
                    toast("Empleado eliminado correctamente");
                    finish();
                } else {
                    toast("No se pudo eliminar.\n" + ApiUtils.mensajeError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                if (isDestroyed()) return;
                setCargando(false);
                toast(ApiUtils.mensajeFallo(t));
            }
        });
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
    }

    private static String valor(String s) {
        return (s == null || s.trim().isEmpty()) ? "—" : s;
    }
}
