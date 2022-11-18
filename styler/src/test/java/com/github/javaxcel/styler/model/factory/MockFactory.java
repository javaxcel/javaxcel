package com.github.javaxcel.styler.model.factory;

import com.github.javaxcel.styler.model.Product;
import com.github.javaxcel.styler.model.YesOrNo;

import java.util.*;

public final class MockFactory {

    private static final Random RANDOM = new Random();

    /**
     * Products for test.
     */
    private static final List<Product> products = Arrays.asList(
            new Product(100000, "알티지 클린 Omega 3", "9b9e7d29-2a60-4973-aec0-685e672eb07a", YesOrNo.NO, 3.0, 3.765, 20.5, 580.5),
            new Product(100001, "레이델 면역쾌청", "a7f3be7b-b235-45b8-9fc5-28f2578ee8e0", YesOrNo.YES, 14.0, 140, 15, 570.50),
            new Product(100002, "그린스토어 우먼케어 건강한 질엔", "d3a6b7c4-c328-470b-b2c9-5e1b937acd0a", YesOrNo.YES, 10.75, 14.1, 15, 170.55),
            new Product(100003, "Bubbleless Vitamin-C", "8a2d7b5d-1a57-4055-a75b-98e495e58a4e", YesOrNo.NO, 18.0, 6, 20, 340.07)
    );

    private MockFactory() {
    }

    public static List<Product> generateStaticProducts() {
        return products;
    }

    public static List<Product> generateRandomProducts(long size) {
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            long serialNumber = RANDOM.nextInt(1000000) + 1000000;
            String name = RANDOM.nextDouble() <= 0.75 ? generateRandomText(RANDOM.nextInt(16)) : null;
            String apiId = UUID.randomUUID().toString();
            YesOrNo imported = RANDOM.nextDouble() >= 0.65 ? YesOrNo.YES : YesOrNo.NO;
            Double width = RANDOM.nextDouble() >= 0.5 ? RANDOM.nextDouble() * 100 : null;
            double depth = RANDOM.nextDouble() * 100;
            double height = RANDOM.nextDouble() * 100;
            Double weight = RANDOM.nextDouble() >= 0.5 ? RANDOM.nextDouble() * 1000 : null;

            Product product = new Product(serialNumber, name, apiId, imported, width, depth, height, weight);
            products.add(product);
        }

        return products;
    }

    private static String generateRandomText(int len) {
        final int leftLimit = 97; // letter 'a'
        final int rightLimit = 122; // letter 'z'

        return RANDOM.ints(leftLimit, rightLimit + 1)
                .limit(len)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}
