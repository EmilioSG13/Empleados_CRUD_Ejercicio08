package com.example.empleados_crud.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Configuración de Retrofit + OkHttp + Gson.
 * Muestra el registro de peticiones en Logcat con el tag "API".
 */
public final class RetrofitClient {

    public static final String BASE_URL = "https://fw-ejercicio-08.onrender.com/";
    public static final String TAG = "API";

    private static Retrofit retrofit;
    private static EmpleadoApi api;
    private static final Gson GSON = new GsonBuilder().create();

    private RetrofitClient() { }

    public static synchronized EmpleadoApi getApi() {
        if (api == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(mensaje -> Log.d(TAG, mensaje));
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(GSON))
                    .build();

            api = retrofit.create(EmpleadoApi.class);
        }
        return api;
    }

    public static Gson getGson() {
        return GSON;
    }
}
