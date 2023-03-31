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
package be.atbash.testing.integration.database.image;

import be.atbash.testing.integration.container.exception.UnexpectedException;
import be.atbash.testing.integration.container.image.TestContext;
import be.atbash.testing.integration.database.exception.FileNotFoundException;
import be.atbash.testing.integration.database.jupiter.DatabaseContainerAdapterMetaData;
import be.atbash.testing.integration.database.jupiter.JDBCDriverArtifact;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class JDBCDriverFileUtil {

    public static String getDriverFile(TestContext testContext) {
        DatabaseContainerAdapterMetaData databaseMetaData = testContext.getInstance(DatabaseContainerAdapterMetaData.class);
        String file;
        JDBCDriverArtifact driverArtifact = databaseMetaData.getDatabaseContainerIntegrationTest().jdbcDriverArtifact();
        // 1. Look for a file defined at @JDBCDriverArtifact
        if (!driverArtifact.driverJarFile().isBlank()) {
            file = driverArtifact.driverJarFile();
        } else {
            // 2. Look for a Maven artifact name
            String mavenLocation = driverArtifact.mavenArtifact();
            if (mavenLocation.isBlank()) {
                // 3. Use the default, also a maven artifact name
                mavenLocation = databaseMetaData.getDatabase().getMavenArtifactForDriver();
            }
            // resolve the Maven artifact within your local repository.
            file = determineDriverFile(mavenLocation);
        }

        if (!Files.exists(Paths.get(file))) {
            throw new FileNotFoundException(String.format("The JDBC Driver file is not found. This was the resolved path %s", file));
        }
        return file;

    }

    private static String determineDriverFile(String mavenLocation) {
        String[] parts = mavenLocation.split(":");
        if (parts.length != 3) {
            throw new ExtensionConfigurationException("The maven coordinates must be defined as 'groupId:artifactId:version'");
        }
        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];
        // Try to resolve the local Maven repository main directory
        String repoPath = determineMavenRepoPath();
        // Point to the jar file.
        return repoPath + groupId.replaceAll("\\.", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";
    }

    private static String determineMavenRepoPath() {
        String localMavenRepoPath = null;
        // Get the active settings.xml file for Maven
        String settingsXMLFile = determineSettingsXMLFile();
        if (settingsXMLFile != null) {
            // look for the localRepository value
            localMavenRepoPath = localRepositoryFromSettings(settingsXMLFile);
        }
        if (localMavenRepoPath == null) {
            // No settings.xml found or no localRepository value defined.
            // Use the default.
            localMavenRepoPath = System.getenv("M2_HOME") + "/repository/";
        }
        return localMavenRepoPath;
    }

    private static String localRepositoryFromSettings(String settingsXmlFile) {
        // Parse the settings.xml file.
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser;
        try {
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            throw new UnexpectedException("Exception occurred during creation of SAXParser", e);
        }

        SettingsParser handler = new SettingsParser();
        try {
            parser.parse(settingsXmlFile, handler);
        } catch (SAXException | IOException e) {
            throw new UnexpectedException("Exception occurred during parsing of settings.xml", e);
        }
        String localRepositoryPath = handler.getLocalRepositoryPath();
        // Not found, we need to fallback to default.
        if (localRepositoryPath == null) {
            return null;
        }
        // Must be ending on slash for the rest of out code.
        if (!localRepositoryPath.endsWith("/")) {
            localRepositoryPath = localRepositoryPath + "/";
        }
        return localRepositoryPath;
    }

    private static String determineSettingsXMLFile() {
        // settings.xml within user profile.
        String xmlFile = System.getProperty("user.home") + "/.m2/settings.xml";
        if (!Files.exists(Paths.get(xmlFile))) {
            // settings.xml within installation.
            xmlFile = System.getenv("M2_HOME") + "/conf/settings.xml";
        }
        if (!Path.of(xmlFile).toFile().exists()) {
            xmlFile = null; // No settings.xml
        }
        return xmlFile;

    }

    private static class SettingsParser extends DefaultHandler {
        private boolean inLocalRepository = false;
        private String localRepositoryPath = null;

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // This is what we are looking for.
            if (qName.equalsIgnoreCase("localRepository")) {
                inLocalRepository = true;
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inLocalRepository) {
                // We have a tag, get its content
                localRepositoryPath = new String(ch, start, length);
                inLocalRepository = false;
            }
        }

        public String getLocalRepositoryPath() {
            return localRepositoryPath;
        }
    }
}
