package com.example.empleados_crud.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.empleados_crud.R;
import com.example.empleados_crud.api.ApiUtils;
import com.example.empleados_crud.model.Empleado;

public class EmpleadoAdapter extends RecyclerView.Adapter<EmpleadoAdapter.EmpleadoViewHolder> {

    public interface OnEmpleadoClickListener {
        void onEmpleadoClick(Empleado empleado);
    }

    private final List<Empleado> datos = new ArrayList<>();
    private final OnEmpleadoClickListener listener;

    public EmpleadoAdapter(OnEmpleadoClickListener listener) {
        this.listener = listener;
    }

    public void setDatos(List<Empleado> nuevos) {
        datos.clear();
        if (nuevos != null) datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EmpleadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empleado, parent, false);
        return new EmpleadoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EmpleadoViewHolder h, int position) {
        Empleado e = datos.get(position);
        Long id = ApiUtils.idDe(e);
        h.tvId.setText("ID: " + (id != null ? id : "—"));
        h.tvNombre.setText(valor(e.getNombre()));
        h.tvCorreo.setText("✉ " + valor(e.getCorreo()));
        h.tvTelefono.setText("☎ " + valor(e.getTelefono()));
        h.itemView.setOnClickListener(v -> listener.onEmpleadoClick(e));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    private static String valor(String s) {
        return (s == null || s.trim().isEmpty()) ? "—" : s;
    }

    static class EmpleadoViewHolder extends RecyclerView.ViewHolder {
        final TextView tvId, tvNombre, tvCorreo, tvTelefono;

        EmpleadoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvCorreo = itemView.findViewById(R.id.tvCorreo);
            tvTelefono = itemView.findViewById(R.id.tvTelefono);
        }
    }
}
