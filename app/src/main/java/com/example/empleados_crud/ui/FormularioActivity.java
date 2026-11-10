package com.example.empleados_crud.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.example.empleados_crud.R;
import com.example.empleados_crud.api.ApiUtils;
import com.example.empleados_crud.api.RetrofitClient;
import com.example.empleados_crud.model.Empleado;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** CREATE y UPDATE con el mismo formulario. */
public class FormularioActivity extends AppCompatActivity {

    public static final String EXTRA_EMPLEADO_JSON = "extra_empleado_json";
    public static final String EXTRA_ID = "extra_id";

    private static final String FORMATO_FECHA = "yyyy-MM-dd";

    private boolean modoEdicion;
    private long idEdicion = -1;

    private TextInputLayout tilId, tilNombre, tilCorreo, tilTelefono, tilDireccion, tilRegistrado;
    private TextInputEditText etId, etNombre, etCorreo, etTelefono, etDireccion, etRegistrado;
    private TextView tvLinks;
    private MaterialButton btnGuardar;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);
        UiUtils.aplicarEdgeToEdge(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tilId = findViewById(R.id.tilId);
        tilNombre = findViewById(R.id.tilNombre);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilTelefono = findViewById(R.id.tilTelefono);
        tilDireccion = findViewById(R.id.tilDireccion);
        tilRegistrado = findViewById(R.id.tilRegistrado);
        etId = findViewById(R.id.etId);
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etTelefono = findViewById(R.id.etTelefono);
        etDireccion = findViewById(R.id.etDireccion);
        etRegistrado = findViewById(R.id.etRegistrado);
        tvLinks = findViewById(R.id.tvLinks);
        btnGuardar = findViewById(R.id.btnGuardar);
        progress = findViewById(R.id.progress);

        etRegistrado.setOnClickListener(v -> mostrarCalendario());
        tilRegistrado.setEndIconOnClickListener(v -> mostrarCalendario());

        String json = getIntent().getStringExtra(EXTRA_EMPLEADO_JSON);
        modoEdicion = json != null;

        if (modoEdicion) {
            Empleado original = RetrofitClient.getGson().fromJson(json, Empleado.class);
            long extraId = getIntent().getLongExtra(EXTRA_ID, -1);
            Long parsedId = ApiUtils.idDe(original);
            if (parsedId != null && parsedId > 0) {
                idEdicion = parsedId;
            } else if (extraId > 0) {
                idEdicion = extraId;
            } else {
                idEdicion = -1;
            }

            toolbar.setTitle("Editar empleado");
            llenarFormulario(original);
            tilId.setVisibility(View.VISIBLE);
            etId.setEnabled(false);
            tilId.setHelperText("ID asignado por el servidor");
        } else {
            toolbar.setTitle("Nuevo empleado");
            tilId.setVisibility(View.GONE);
            etRegistrado.setText("true");
            tvLinks.setText("Los links los genera el servidor al guardar.");
        }

        btnGuardar.setOnClickListener(v -> guardar());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void llenarFormulario(Empleado e) {
        etId.setText(idEdicion >= 0 ? String.valueOf(idEdicion) : "");
        etNombre.setText(e.getNombre());
        etCorreo.setText(e.getCorreo());
        etTelefono.setText(e.getTelefono());
        etDireccion.setText(e.getDireccion());
        etRegistrado.setText(ApiUtils.textoRegistrado(e.getRegistrado()));
        String links = ApiUtils.textoLinks(e.getLinks());
        tvLinks.setText(links.isEmpty() ? "Sin links" : links);
    }

    private void mostrarCalendario() {
        Calendar c = Calendar.getInstance();
        try {
            Date d = new SimpleDateFormat(FORMATO_FECHA, Locale.US).parse(texto(etRegistrado));
            if (d != null) c.setTime(d);
        } catch (ParseException ignored) { }

        new DatePickerDialog(this, (view, anio, mes, dia) ->
                etRegistrado.setText(String.format(Locale.US, "%04d-%02d-%02d", anio, mes + 1, dia)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private boolean validar() {
        tilId.setError(null);
        tilNombre.setError(null);
        tilCorreo.setError(null);
        tilTelefono.setError(null);
        tilDireccion.setError(null);
        tilRegistrado.setError(null);

        boolean ok = true;

        String nombre = texto(etNombre);
        if (nombre.isEmpty()) {
            tilNombre.setError("El nombre es obligatorio");
            ok = false;
        } else if (!nombre.matches("^[\\p{L} .'-]{2,100}$")) {
            tilNombre.setError("Solo letras y espacios (mínimo 2 caracteres)");
            ok = false;
        }

        String correo = texto(etCorreo);
        if (correo.isEmpty()) {
            tilCorreo.setError("El correo es obligatorio");
            ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError("Formato de correo inválido (ej. nombre@dominio.com)");
            ok = false;
        }

        String telefono = texto(etTelefono);
        if (telefono.isEmpty()) {
            tilTelefono.setError("El teléfono es obligatorio");
            ok = false;
        } else if (!telefono.matches("^[0-9+()\\-\\s]+$")) {
            tilTelefono.setError("Solo se permiten dígitos, espacios, +, - y ( )");
            ok = false;
        } else {
            int digitos = telefono.replaceAll("\\D", "").length();
            if (digitos < 7 || digitos > 15) {
                tilTelefono.setError("Debe tener entre 7 y 15 dígitos");
                ok = false;
            }
        }

        String direccion = texto(etDireccion);
        if (direccion.isEmpty()) {
            tilDireccion.setError("La dirección es obligatoria");
            ok = false;
        } else if (direccion.length() < 5) {
            tilDireccion.setError("La dirección es demasiado corta");
            ok = false;
        }

        String registrado = texto(etRegistrado);
        if (registrado.isEmpty()) {
            tilRegistrado.setError("La fecha o estado de registro es obligatorio");
            ok = false;
        }
        return ok;
    }

    private Empleado construirEmpleado() {
        Empleado e = new Empleado();
        if (modoEdicion) {
            e.setId(idEdicion);
        } else {
            e.setId(null); // El id se genera automáticamente en el servidor
        }
        e.setNombre(texto(etNombre));
        e.setCorreo(texto(etCorreo));
        e.setTelefono(texto(etTelefono));
        e.setDireccion(texto(etDireccion));

        String regTexto = texto(etRegistrado);
        if (regTexto.equalsIgnoreCase("false") || regTexto.equals("0")) {
            e.setRegistrado(new JsonPrimitive(false));
        } else {
            // La API REST espera el campo booleano registrado: true
            e.setRegistrado(new JsonPrimitive(true));
        }
        e.setLinks(null);
        return e;
    }

    private void guardar() {
        if (!validar()) {
            toast("Corrige los campos marcados");
            return;
        }
        if (modoEdicion && idEdicion < 0) {
            toast("No se encontró el id del empleado a actualizar");
            return;
        }

        setEnviando(true);
        Empleado empleado = construirEmpleado();
        Call<JsonElement> llamada = modoEdicion
                ? RetrofitClient.getApi().actualizar(idEdicion, empleado)
                : RetrofitClient.getApi().crear(empleado);

        llamada.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (isDestroyed()) return;
                setEnviando(false);
                if (response.isSuccessful()) {
                    toast(modoEdicion ? "Empleado actualizado correctamente" : "Empleado registrado correctamente");
                    finish();
                } else {
                    toast((modoEdicion ? "No se pudo actualizar.\n" : "No se pudo registrar.\n")
                            + ApiUtils.mensajeError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                if (isDestroyed()) return;
                setEnviando(false);
                toast(ApiUtils.mensajeFallo(t));
            }
        });
    }

    private void setEnviando(boolean enviando) {
        progress.setVisibility(enviando ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!enviando);
        btnGuardar.setText(enviando ? "Guardando…" : "Guardar");
    }

    private static String texto(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
    }
}
