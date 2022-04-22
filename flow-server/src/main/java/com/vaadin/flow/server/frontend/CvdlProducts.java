package com.vaadin.flow.server.frontend;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.vaadin.pro.licensechecker.Product;

import org.apache.commons.io.FileUtils;

import elemental.json.Json;
import elemental.json.JsonObject;

public class CvdlProducts {

    private static final String CVDL_PACKAGE_KEY = "cvdlName";

    public static Product getProductIfCvdl(File nodeModules, String npmModule) {
        File packageJsonFile = new File(new File(nodeModules, npmModule),
                "package.json");

        try {
            JsonObject packageJson = Json.parse(FileUtils
                    .readFileToString(packageJsonFile, StandardCharsets.UTF_8));
            if (packageJson.hasKey(CVDL_PACKAGE_KEY)) {
                return new Product(packageJson.getString(CVDL_PACKAGE_KEY),
                        packageJson.getString("version"));
            } else if (packageJson.hasKey("license")) {
                String packageName = packageJson.getString("name");
                String license = packageJson.getString("license");
                if (packageName.startsWith("@vaadin/") && license
                        .startsWith("https://raw.githubusercontent.com")) {
                    // Free components have "Apache-2.0"
                    String cvdlName = packageName;
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
