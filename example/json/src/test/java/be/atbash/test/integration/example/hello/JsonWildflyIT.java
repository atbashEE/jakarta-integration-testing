/*
 * Copyright 2022 Rudy De Busscher (https://www.atbash.be)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.atbash.test.integration.example.hello;

import be.atbash.test.integration.example.json.Product;
import be.atbash.testing.integration.jupiter.ContainerIntegrationTest;
import be.atbash.testing.integration.test.WildflyContainerIntegrationTest;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.List;


@ContainerIntegrationTest
public class JsonWildflyIT extends WildflyContainerIntegrationTest {
    // Should be public and not the JUnit 5 preferred scope package.

    @RestClient
    public JsonService jsonService;

    @Test
    void testEndpoints() {
        List<Product> products = jsonService.getProducts();
        Assertions.assertThat(products).isEmpty();

        Product product = createProduct("Atbash Runtime", 5);
        jsonService.addProduct(product);

        products = jsonService.getProducts();
        Assertions.assertThat(products).hasSize(1);
        Product productFromApp = products.get(0);
        Assertions.assertThat(productFromApp.getId()).isNotEmpty();
        Assertions.assertThat(productFromApp.getName()).isEqualTo(product.getName());
        Assertions.assertThat(productFromApp.getRating()).isEqualTo(product.getRating());
    }

    private Product createProduct(String name, int rating) {
        Product result = new Product();
        result.setName(name);
        result.setRating(rating);
        return result;
    }
}