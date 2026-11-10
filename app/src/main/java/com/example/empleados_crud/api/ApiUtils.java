package com.example.empleados_crud.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.empleados_crud.model.Empleado;
import okhttp3.ResponseBody;
import retrofit2.Response;

/** Utilidades para interpretar respuestas y errores de la API. */
public final class ApiUtils {

    private ApiUtils() { }

    /**
     * Convierte la respuesta del listado en una lista de empleados.
     * Soporta: {"datos": {"content": [..]}}, [..], {"_embedded":{"x":[..]}} y {"content":[..]}.
     */
    public static List<Empleado> parsearLista(JsonElement json) {
        if (json == null || json.isJsonNull()) return new ArrayList<>();

        JsonArray arreglo = null;
        if (json.isJsonArray()) {
            arreglo = json.getAsJsonArray();
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("datos")) {
                JsonElement datos = obj.get("datos");
                if (datos.isJsonArray()) {
                    arreglo = datos.getAsJsonArray();
                } else if (datos.isJsonObject()) {
                    JsonObject dObj = datos.getAsJsonObject();
                    if (dObj.has("content") && dObj.get("content").isJsonArray()) {
                        arreglo = dObj.getAsJsonArray("content");
                    }
                }
            } else if (obj.has("_embedded") && obj.get("_embedded").isJsonObject()) {
                for (Map.Entry<String, JsonElement> e : obj.getAsJsonObject("_embedded").entrySet()) {
                    if (e.getValue().isJsonArray()) {
                        arreglo = e.getValue().getAsJsonArray();
                        break;
                    }
                }
            } else if (obj.has("content") && obj.get("content").isJsonArray()) {
                arreglo = obj.getAsJsonArray("content");
            }
        }
        if (arreglo == null) return new ArrayList<>();

        Type tipo = new TypeToken<List<Empleado>>() { }.getType();
        List<Empleado> lista = RetrofitClient.getGson().fromJson(arreglo, tipo);
        return lista != null ? lista : new ArrayList<>();
    }

    /**
     * Extrae un Empleado individual de una respuesta JSON que puede estar en un envoltorio {"datos": {...}}.
     */
    public static Empleado parsearEmpleado(JsonElement json) {
        if (json == null || json.isJsonNull()) return null;
        JsonObject obj = null;
        if (json.isJsonObject()) {
            JsonObject root = json.getAsJsonObject();
            if (root.has("datos") && root.get("datos").isJsonObject()) {
                obj = root.getAsJsonObject("datos");
            } else {
                obj = root;
            }
        }
        if (obj == null) return null;
        return RetrofitClient.getGson().fromJson(obj, Empleado.class);
    }

    /** Devuelve el id del empleado; si no viene en el id del objeto, intenta obtenerlo del link "self". */
    public static Long idDe(Empleado e) {
        if (e == null) return null;
        if (e.getId() != null) return e.getId();
        String href = hrefSelf(e.getLinks());
        if (href == null) return null;
        String limpio = href.replaceAll("\\{.*}", "").replaceAll("/+$", "");
        String ultimo = limpio.substring(limpio.lastIndexOf('/') + 1);
        try {
            return Long.parseLong(ultimo);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String hrefSelf(JsonElement links) {
        if (links == null || links.isJsonNull()) return null;
        if (links.isJsonObject()) {
            JsonElement self = links.getAsJsonObject().get("self");
            if (self == null) return null;
            if (self.isJsonObject()) return texto(self.getAsJsonObject(), "href");
            if (self.isJsonArray() && self.getAsJsonArray().size() > 0
                    && self.getAsJsonArray().get(0).isJsonObject()) {
                return texto(self.getAsJsonArray().get(0).getAsJsonObject(), "href");
            }
        } else if (links.isJsonArray()) {
            for (JsonElement l : links.getAsJsonArray()) {
                if (l.isJsonObject() && "self".equals(texto(l.getAsJsonObject(), "rel"))) {
                    return texto(l.getAsJsonObject(), "href");
                }
            }
        }
        return null;
    }

    /** Texto legible de los links (rel: href), uno por línea. */
    public static String textoLinks(JsonElement links) {
        if (links == null || links.isJsonNull()) return "";
        StringBuilder sb = new StringBuilder();
        if (links.isJsonObject()) {
            for (Map.Entry<String, JsonElement> e : links.getAsJsonObject().entrySet()) {
                JsonElement v = e.getValue();
                String href = v.isJsonObject() ? texto(v.getAsJsonObject(), "href") : v.toString();
                sb.append(e.getKey()).append(": ").append(href).append('\n');
            }
        } else if (links.isJsonArray()) {
            for (JsonElement l : links.getAsJsonArray()) {
                if (l.isJsonObject()) {
                    sb.append(texto(l.getAsJsonObject(), "rel")).append(": ")
                            .append(texto(l.getAsJsonObject(), "href")).append('\n');
                }
            }
        } else {
            sb.append(links.getAsString());
        }
        return sb.toString().trim();
    }

    /** Texto legible del campo registrado (texto, número, booleano o arreglo [aaaa,mm,dd]). */
    public static String textoRegistrado(JsonElement r) {
        if (r == null || r.isJsonNull()) return "";
        if (r.isJsonPrimitive()) return r.getAsString();
        if (r.isJsonArray()) {
            JsonArray a = r.getAsJsonArray();
            if (a.size() >= 3) {
                return String.format(java.util.Locale.US, "%04d-%02d-%02d",
                        a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt());
            }
        }
        return r.toString();
    }

    private static String texto(JsonObject o, String clave) {
        JsonElement v = o.get(clave);
        return (v == null || v.isJsonNull()) ? null : v.getAsString();
    }

    /** Mensaje entendible para respuestas HTTP con error. */
    public static String mensajeError(Response<?> respuesta) {
        String detalle = "";
        try (ResponseBody cuerpo = respuesta.errorBody()) {
            if (cuerpo != null) detalle = cuerpo.string().trim();
        } catch (IOException ignored) { }

        int code = respuesta.code();
        String base;
        if (code == 400) base = "Datos inválidos (400). Revisa la información capturada.";
        else if (code == 404) base = "El empleado no existe o la ruta no es válida (404).";
        else if (code == 405) base = "Método HTTP no permitido (405).";
        else if (code == 409) base = "Conflicto: el registro ya existe (409).";
        else if (code == 415) base = "Formato de datos no soportado (415).";
        else if (code >= 500) base = "Error interno del servidor (" + code + ").";
        else base = "Error HTTP " + code + ".";

        if (detalle.length() > 250) detalle = detalle.substring(0, 250) + "…";
        return detalle.isEmpty() ? base : base + "\n" + detalle;
    }

    /** Mensaje entendible para fallos de conexión o de conversión. */
    public static String mensajeFallo(Throwable t) {
        if (t instanceof SocketTimeoutException) {
            return "El servidor tardó demasiado en responder. Si es la primera petición, "
                    + "el servidor puede estar iniciando; intenta de nuevo en unos segundos.";
        }
        if (t instanceof IOException) {
            return "No hay conexión a Internet o no se pudo contactar al servidor.";
        }
        return "No se pudo procesar la respuesta del servidor: " + t.getMessage();
    }
}
