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
package be.rubus.courses.payara.micro.jpa.converter;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.awt.*;
import java.lang.reflect.Type;

public class ColorDeserializer implements JsonbDeserializer<Color> {

    private final ColorConverter converter = new ColorConverter();

    @Override
    public Color deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        String colorString = parser.getString();
        return converter.convertFromString(colorString);
    }
}
