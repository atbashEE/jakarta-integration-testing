/*
 * Copyright 2022-2023 Rudy De Busscher (https://www.atbash.be)
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
package be.atbash.test.integration.example.json;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;

@ApplicationScoped
public class ProductService {

    private final Set<Product> products = new HashSet<>();

    public List<Product> getAll() {
        return new ArrayList<>(products);
    }

    public void addProduct(Product product) {
        product.setId(UUID.randomUUID().toString());
        products.add(product);
    }
}
