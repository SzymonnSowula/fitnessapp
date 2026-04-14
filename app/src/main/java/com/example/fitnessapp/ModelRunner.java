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

            // W modelach scikit-learn wyjście 1 to zazwyczaj lista map (prawdopodobieństwa)
            // lub tablica float[][] w zależności od wersji skl2onnx i typu modelu.
            // Sprawdzamy co mamy w result.get(1)
            Object out = result.get(1).getValue();
            if (out instanceof float[][]) {
                return ((float[][]) out)[0];
            } else if (out instanceof List) {
                // Często skl2onnx zwraca List<Map<Long, Float>> dla klasyfikacji
                List<Map<Long, Float>> list = (List<Map<Long, Float>>) out;
                Map<Long, Float> map = list.get(0);
                float[] probs = new float[map.size()];
                for (int i = 0; i < map.size(); i++) {
                    probs[i] = map.get((long) i);
                }
                return probs;
            } else {
                Log.e(TAG, "Unknown output type: " + out.getClass().getName());
                return new float[0];
            }
        }
    }

    public List<String> getClasses() {
        return classes;
    }

    public void close() throws Exception {
        if (session != null) session.close();
        if (env != null) env.close();
    }
}
