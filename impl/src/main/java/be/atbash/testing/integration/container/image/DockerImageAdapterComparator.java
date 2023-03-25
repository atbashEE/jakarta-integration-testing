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
package be.atbash.testing.integration.container.image;

import jakarta.annotation.Priority;
import java.util.Comparator;

public class DockerImageAdapterComparator implements Comparator<DockerImageAdapter> {
    @Override
    public int compare(DockerImageAdapter adapter1, DockerImageAdapter adapter2) {
        Integer priority1 = getPriorityValue(adapter1);
        Integer priority2 = getPriorityValue(adapter2);
        return priority1.compareTo(priority2);
    }

    private int getPriorityValue(DockerImageAdapter adapter) {
        Priority priority = adapter.getClass().getAnnotation(Priority.class);
        return priority == null ? 100 : priority.value();
    }
}
