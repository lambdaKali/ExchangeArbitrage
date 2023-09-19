package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class KuCoinAPI {
    private static final int THREAD_POOL_SIZE = 12;
    private static final Semaphore SEMAPHORE = new Semaphore(10);

    public static Map<String, String> fetchTradingPairsAndPrices() {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        Map<String, String> tradingPairsAndPrices = new HashMap<>();
        String apiUrl = "https://api.kucoin.com/api/v1/symbols";

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
            JsonArray symbolsArray = jsonObject.getAsJsonArray("data");

            List<Future<Void>> futures = new ArrayList<>();
            for (int i = 0; i < symbolsArray.size(); i++) {
                final int index = i;
                Future<Void> future = executorService.submit(() -> {
                    SEMAPHORE.acquire();
                    JsonObject symbolObject = symbolsArray.get(index).getAsJsonObject();
                    String symbol = PairNormalizer.normalizePair(symbolObject.get("symbol").getAsString());
                    String price = fetchPriceForSymbol(symbol);
                    tradingPairsAndPrices.put(symbol, price);
                    SEMAPHORE.release();
                    return null;
                });
                futures.add(future);
            }

            for (Future<Void> future : futures) {
                future.get();
            }

            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tradingPairsAndPrices;
    }

    private static String fetchPriceForSymbol(String symbol) {
        String apiUrl = "https://api.kucoin.com/api/v1/market/orderbook/level1?symbol=" + symbol;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();

            // Check for null value before attempting to get the JsonObject
            if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                JsonObject dataObject = jsonObject.getAsJsonObject("data");
                if (dataObject.has("price") && !dataObject.get("price").isJsonNull()) {
                    return dataObject.get("price").getAsString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "0";
    }

}
