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
package be.atbash.testing.integration.test;

import be.atbash.testing.integration.container.AbstractIntegrationContainer;
import com.github.dockerjava.api.exception.NotFoundException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.platform.commons.support.AnnotationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import java.lang.reflect.Field;

/**
 * Exception handler that shows the container log in case the test fails (and log could provide us with more info)
 */
public class ShowLogWhenFailedExceptionHandler implements TestExecutionExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShowLogWhenFailedExceptionHandler.class);

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        if (throwable instanceof AssertionError || throwable instanceof NotFoundException || isInternalServerError(throwable)) {

            AbstractIntegrationContainer<?> mainContainer = getMainContainer(extensionContext.getRequiredTestClass());

            String logs = mainContainer.getLogs();
            System.out.println("Container log");
            System.out.println(logs);
        }
        throw throwable;  // rethrow. We just wanted to output the container log.
    }

    private boolean isInternalServerError(Throwable throwable) {
        boolean result = throwable instanceof InternalServerErrorException;
        if (!result && throwable instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) throwable;
            result = wae.getResponse().getStatus() == 500;
        }

        return result;
    }

    private AbstractIntegrationContainer<?> getMainContainer(Class<?> testClass) {
        AbstractIntegrationContainer<?> result = null;
        for (Field containerField : AnnotationSupport.findAnnotatedFields(testClass, Container.class)) {
            try {

                if (AbstractIntegrationContainer.class.isAssignableFrom(containerField.getType())) {
                    result = (AbstractIntegrationContainer<?>) containerField.get(null);

                }


            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.warn("Unable to access field " + containerField, e);
            }
        }
        return result;
    }
}
