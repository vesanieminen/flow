package com.vaadin.flow.server.frontend;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.vaadin.pro.licensechecker.Product;

import org.apache.commons.io.FileUtils;

import elemental.json.Json;
import elemental.json.JsonObject;

public class CdvlProducts {

    public static Product getProductIfCDVL(File nodeModules, String npmModule) {
        File packageJsonFile = new File(new File(nodeModules, npmModule),
                "package.json");

        try {
            JsonObject packageJson = Json.parse(FileUtils
                    .readFileToString(packageJsonFile, StandardCharsets.UTF_8));
            if (packageJson.hasKey("cvdlName")) {
                return new Product(packageJson.getString("cvdlName"),
                        packageJson.getString("version"));
            } else if (packageJson.hasKey("license")) {
                String license = packageJson.getString("license");
                if (license.startsWith("https://raw.githubusercontent.com")) {
                    // Free components have "Apache-2.0"
                    String cvdlName = packageJson.getString("name");
                    cvdlName = cvdlName.replace("@", "");
                    cvdlName = cvdlName.replace("/", "-");
                    cvdlName = cvdlName.replace("charts", "chart");
                    return new Product(cvdlName,
                            packageJson.getString("version"));
                }
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unable to read package.json file " + packageJsonFile, e);
        }
    }
}
