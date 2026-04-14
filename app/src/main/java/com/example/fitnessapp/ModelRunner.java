package com.example.fitnessapp;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

public class ModelRunner {
    private static final String TAG = "ModelRunner";
    private OrtEnvironment env;
    private OrtSession session;
    private String inputName;
    private List<String> classes;

    public void init(Context context) throws Exception {
        env = OrtEnvironment.getEnvironment();
        String modelPath = copyAssetToFiles(context, "rf_rekomendator.onnx");
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        session = env.createSession(modelPath, options);
        inputName = session.getInputNames().iterator().next();
        
        loadMetadata(context);
    }

    private void loadMetadata(Context context) throws Exception {
        InputStream is = context.getAssets().open("metadata.json");
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, StandardCharsets.UTF_8);
        JSONObject obj = new JSONObject(json);
        JSONArray classesArray = obj.getJSONArray("classes");
        classes = new ArrayList<>();
        for (int i = 0; i < classesArray.length(); i++) {
            classes.add(classesArray.getString(i));
        }
    }

    private String copyAssetToFiles(Context context, String filename) throws Exception {
        File file = new File(context.getFilesDir(), filename);
        if (!file.exists()) {
            try (InputStream is = context.getAssets().open(filename);
                 FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            }
        }
        return file.getAbsolutePath();
    }

    public float[] predict(float[] features) throws Exception {
        float[][] input = new float[][]{features};
        try (OnnxTensor tensor = OnnxTensor.createTensor(env, input);
             OrtSession.Result result = session.run(Collections.singletonMap(inputName, tensor))) {
            
            // Pobieramy probabilities (zwykle drugi element wyniku)
            Object out = result.get(1).getValue();
            Log.d(TAG, "Wyjście modelu: " + out.getClass().getName());

            if (out instanceof float[][]) {
                return ((float[][]) out)[0];
            } else if (out instanceof List) {
                // Kolekcja map (np. List<Map<Long, Float>>)
                List<?> list = (List<?>) out;
                if (list.isEmpty()) return new float[0];
                Object firstItem = list.get(0);
                if (firstItem instanceof Map) {
                    return extractProbsFromMap((Map<?, ?>) firstItem);
                }
            } else if (out instanceof Map) {
                // Bezpośrednio mapa (np. OnnxMap)
                return extractProbsFromMap((Map<?, ?>) out);
            }
            
            Log.e(TAG, "Unknown output type: " + out.getClass().getName());
            return new float[0];
        }
    }

    private float[] extractProbsFromMap(Map<?, ?> map) {
        float[] probs = new float[map.size()];
        for (int i = 0; i < map.size(); i++) {
            // Próbujemy różnych kluczy: Long, Integer, String
            Object val = map.get((long) i);
            if (val == null) val = map.get(i);
            if (val == null) val = map.get(String.valueOf(i));
            
            if (val instanceof Float) {
                probs[i] = (Float) val;
            } else if (val instanceof Double) {
                probs[i] = ((Double) val).floatValue();
            } else if (i < map.size()) {
                // Fallback: jeśli nie znaleźliśmy po kluczu, spróbujmy wziąć wartości w kolejności
                // (może być ryzykowne, jeśli mapa nie zachowuje kolejności, ale lepsze niż 0.0)
                int idx = 0;
                for (Object v : map.values()) {
                    if (idx == i) {
                        if (v instanceof Float) probs[i] = (Float) v;
                        else if (v instanceof Double) probs[i] = ((Double) v).floatValue();
                        break;
                    }
                    idx++;
                }
            }
        }
        return probs;
    }

    public List<String> getClasses() {
        return classes;
    }

    public void close() throws Exception {
        if (session != null) session.close();
        if (env != null) env.close();
    }
}
