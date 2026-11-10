package com.example.empleados_crud.ui;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.empleados_crud.R;

/** Ajustes de interfaz comunes a todas las pantallas. */
public final class UiUtils {

    private UiUtils() { }

    public static void aplicarEdgeToEdge(AppCompatActivity activity) {
        EdgeToEdge.enable(activity);
        ViewCompat.setOnApplyWindowInsetsListener(activity.findViewById(R.id.main), (v, insets) -> {
            Insets b = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(b.left, b.top, b.right, b.bottom);
            return insets;
        });
    }
}
