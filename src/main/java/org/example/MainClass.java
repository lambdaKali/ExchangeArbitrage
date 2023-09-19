package org.example;

import java.util.Map;

public class MainClass {
    public static void main(String[] args) {
        Map<String, String> binancePairsAndPrices = BinanceAPI.fetchTradingPairsAndPrices();
        Map<String, String> kucoinPairsAndPrices = KuCoinAPI.fetchTradingPairsAndPrices();

        for (String bp : binancePairsAndPrices.keySet()) {
            if (kucoinPairsAndPrices.containsKey(bp)) {
                String binancePrice = binancePairsAndPrices.get(bp);
                String kucoinPrice = kucoinPairsAndPrices.get(bp);

                if (binancePrice == null || kucoinPrice == null || !isNumeric(binancePrice) || !isNumeric(kucoinPrice)) {
                    System.out.println("Invalid price for pair " + bp);
                    continue;
                }

                double bPrice = Double.parseDouble(binancePrice);
                double kPrice = Double.parseDouble(kucoinPrice);

                if (bPrice != kPrice) {
                    System.out.println("Arbitrage opportunity found for " + bp + ": Binance price = " + bPrice + ", KuCoin price = " + kPrice);
                }
            }
        }
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
