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
        // Zawsze kopiujemy w fazie debugowania, aby mieć pewność użycia najnowszego modelu z assets
        try (InputStream is = context.getAssets().open(filename);
             FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
        }
        return file.getAbsolutePath();
    }

    public float[] predict(float[] features) throws Exception {
        float[][] input = new float[][]{features};
        try (OnnxTensor tensor = OnnxTensor.createTensor(env, input);
             OrtSession.Result result = session.run(Collections.singletonMap(inputName, tensor))) {
            
            Log.d(TAG, "Liczba wyjść modelu: " + result.size());
            
            // Logowanie wszystkich wyjść dla debugowania
            for (String outputName : session.getOutputNames()) {
                Log.d(TAG, "Output name: " + outputName);
            }

            // Próbujemy znaleźć prawdopodobieństwa. 
            // W modelach scikit-learn (skl2onnx): 
            // result.get(0) to zwykle etykiety (LongTensor/StringTensor)
            // result.get(1) to zwykle prawdopodobieństwa (Sequence of Maps)
            
            Object labelsObj = result.get(0).getValue();
            Object probsObj = result.size() > 1 ? result.get(1).getValue() : null;

            Log.d(TAG, "Typ wyjścia 0 (labels): " + labelsObj.getClass().getName());
            if (probsObj != null) {
                Log.d(TAG, "Typ wyjścia 1 (probs): " + probsObj.getClass().getName());
            }

            // 1. Próba wyciągnięcia prawdopodobieństw (najlepsza opcja)
            if (probsObj != null) {
                float[] probs = processOutput(probsObj);
                if (probs != null && probs.length > 0) return probs;
            }

            // 2. Jeśli nie ma prawdopodobieństw lub są puste, spróbujmy z wyjścia 0 (może tam są?)
            float[] probsFrom0 = processOutput(labelsObj);
            if (probsFrom0 != null && probsFrom0.length > 0 && probsFrom0.length == classes.size()) {
                return probsFrom0;
            }

            // 3. Fallback: Jeśli mamy tylko etykietę (prediction), stwórzmy "ostrą" dystrybucję (one-hot)
            return createOneHotFromLabel(labelsObj);
        }
    }

    private float[] processOutput(Object out) {
        if (out instanceof float[][]) {
            return ((float[][]) out)[0];
        } else if (out instanceof List) {
            List<?> list = (List<?>) out;
            if (list.isEmpty()) return null;
            Object firstItem = list.get(0);
            if (firstItem instanceof Map) {
                return extractProbsFromMap((Map<?, ?>) firstItem);
            }
        } else if (out instanceof Map) {
            return extractProbsFromMap((Map<?, ?>) out);
        }
        return null;
    }

    private float[] createOneHotFromLabel(Object labelsObj) {
        // Jeśli etykieta to np. long[1][1] lub String[1][1]
        String predictedLabel = "";
        if (labelsObj instanceof long[][]) {
            long labelIdx = ((long[][]) labelsObj)[0][0];
            if (labelIdx >= 0 && labelIdx < classes.size()) {
                predictedLabel = classes.get((int) labelIdx);
            } else {
                // Jeśli etykieta to numer klasy (np. 0, 1, 2)
                predictedLabel = String.valueOf(labelIdx);
            }
        } else if (labelsObj instanceof String[][]) {
            predictedLabel = ((String[][]) labelsObj)[0][0];
        } else if (labelsObj instanceof long[]) {
            predictedLabel = String.valueOf(((long[]) labelsObj)[0]);
        } else if (labelsObj instanceof String[]) {
            predictedLabel = ((String[]) labelsObj)[0];
        }

        Log.d(TAG, "Przewidziana etykieta (fallback): " + predictedLabel);

        float[] probs = new float[classes.size()];
        for (int i = 0; i < classes.size(); i++) {
            if (classes.get(i).equalsIgnoreCase(predictedLabel)) {
                probs[i] = 1.0f;
                return probs;
            }
        }
        
        // Jeśli etykieta jest numerem (np. "0"), a klasy to ["sila", ...]
        try {
            int idx = Integer.parseInt(predictedLabel);
            if (idx >= 0 && idx < probs.length) {
                probs[idx] = 1.0f;
                return probs;
            }
        } catch (Exception ignored) {}

        // Ostatnia deska ratunku - jeśli nic nie pasuje, zwróćmy cokolwiek (np. pierwszą klasę)
        if (probs.length > 0) probs[0] = 1.0f;
        return probs;
    }

    private float[] extractProbsFromMap(Map<?, ?> map) {
        float[] probs = new float[classes.size()];
        boolean foundAnything = false;

        for (int i = 0; i < classes.size(); i++) {
            String className = classes.get(i);
            // Próbujemy po nazwie klasy (String)
            Object val = map.get(className);
            // Próbujemy po indeksie (Long/Integer)
            if (val == null) val = map.get((long) i);
            if (val == null) val = map.get(i);
            if (val == null) val = map.get(String.valueOf(i));
            
            if (val instanceof Float) {
                probs[i] = (Float) val;
                foundAnything = true;
            } else if (val instanceof Double) {
                probs[i] = ((Double) val).floatValue();
                foundAnything = true;
            }
        }

        if (!foundAnything) {
            // Fallback: jeśli nie znaleźliśmy po kluczu, spróbujmy wziąć wartości w kolejności
            Log.w(TAG, "Nie znaleziono kluczy w mapie, używam fallbacku wartości");
            int idx = 0;
            for (Object v : map.values()) {
                if (idx < probs.length) {
                    if (v instanceof Float) probs[idx] = (Float) v;
                    else if (v instanceof Double) probs[idx] = ((Double) v).floatValue();
                    foundAnything = true;
                }
                idx++;
            }
        }
        
        return foundAnything ? probs : new float[0];
    }

    public List<String> getClasses() {
        return classes;
    }

    public void close() throws Exception {
        if (session != null) session.close();
        if (env != null) env.close();
    }
}
