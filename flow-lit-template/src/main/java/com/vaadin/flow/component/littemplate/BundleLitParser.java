/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.littemplate;

import static com.vaadin.flow.server.frontend.FrontendUtils.DEAULT_FLOW_RESOURCES_FOLDER;
import static com.vaadin.flow.server.frontend.FrontendUtils.FLOW_NPM_PACKAGE_NAME;
import static elemental.json.JsonType.ARRAY;
import static elemental.json.JsonType.OBJECT;
import static elemental.json.JsonType.STRING;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.internal.StringUtil;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonType;

/**
 * Parse statistics data provided by webpack.
 *
 * @author Vaadin Ltd
 * @since
 *
 * @see LitTemplateParser
 */
public final class BundleLitParser {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BundleLitParser.class);

    private static final String SOURCE = "source";
    private static final String NAME = "name";
    private static final String CHUNKS = "chunks";
    private static final String MODULES = "modules";

    /**
     * Lit template pattern matches the template getter
     *
     * <pre>
     *     render() {
     *       return html`
     *         &lt;style&gt;
     *           .response { margin-top: 10px`; }
     *         &lt;/style&gt;
     *         &lt;paper-checkbox checked=&quot;${liked}&quot;&gt;I like web components.&lt;/paper-checkbox&gt;
     *
     *         &lt;div id=&quot;test&quot; ?hidden=&quot;${liked}&quot; class=&quot;response&quot;&gt;Web components like you, too.&lt;/div&gt;
     *       `;
     *     }
     * </pre>
     *
     * <p>
     * <code>render\(\)[\s]*\{</code> finds the template getter method
     * <p>
     * <code>[\s]*return[\s]*html[\s]*(\`)</code> finds the return statement
     * <p>
     * </p>
     * <code>([\s\S]*)</code> captures all text until we encounter the end
     * character with <code>\1;}</code> e.g. <code>';}</code>
     */
    private static final Pattern LIT_TEMPLATE_PATTERN = Pattern.compile(
            "render\\(\\)[\\s]*\\{[\\s]*return[\\s]*html[\\s]*(\\`)([\\s\\S]*?)\\1;[\\s]*\\}");

    private static final Pattern HASH_PATTERN = Pattern
            .compile("\"hash\"\\s*:\\s*\"([^\"]+)\"\\s*,");

    private static final String TEMPLATE_TAG_NAME = "template";

    private BundleLitParser() {
    }

    /**
     * Gets the hash from the string content of a webpack stats file. It uses
     * regex to avoid parsing the entire string into a json object.
     *
     * @param fileContents
     *            the content of the stats file
     * @return the hash
     */
    public static String getHashFromStatistics(String fileContents) {
        Matcher matcher = HASH_PATTERN.matcher(fileContents);
        return matcher.find() ? matcher.group(1) : "" + fileContents.length();
    }

    /**
     * Parses the content of the stats file to return a json object.
     *
     * @param fileContents
     *            the content of the stats file
     * @return a JsonObject with the stats
     */
    public static JsonObject parseJsonStatistics(String fileContents) {
        return Json.parse(fileContents);
    }

    /**
     * Get a module source from the statistics Json file generated by webpack.
     *
     * @param fileName
     *            name of the file to get from the json
     * @param statistics
     *            statistics json as a JsonObject
     * @return JsonObject for the file statistic
     */
    public static String getSourceFromStatistics(String fileName,
            JsonObject statistics) {
        return getSourceFromObject(statistics, fileName);
    }

    /**
     * Get the Lit template element for the given polymer template source.
     *
     * @param fileName
     *            name of the handled file
     * @param source
     *            source js to get template element from
     * @return template element or {code null} if not found
     */
    public static Element parseLitTemplateElement(String fileName,
            String source) {
        Document templateDocument = null;
        String content = StringUtil.removeComments(source);
        Matcher templateMatcher = LIT_TEMPLATE_PATTERN.matcher(content);

        // GroupCount should be 2 as the first group contains ` and the second
        // is the template contents.
        if (templateMatcher.find() && templateMatcher.groupCount() == 2) {
            String group = templateMatcher.group(2);
            LOGGER.trace("Found regular Lit template content was {}", group);

            templateDocument = Jsoup.parse(group);
            LOGGER.trace("The parsed template document was {}",
                    templateDocument);
            Element template = templateDocument
                    .createElement(TEMPLATE_TAG_NAME);
            Element body = templateDocument.body();
            templateDocument.body().children().stream()
                    .filter(node -> !node.equals(body))
                    .forEach(template::appendChild);

            return template;
        }
        LOGGER.warn("No lit template data found in {} sources.", fileName);
        return null;
    }

    // find the first module whose name matches the file name
    private static String getSourceFromObject(JsonObject module,
            String fileName) {
        String source = null;
        if (validKey(module, MODULES, ARRAY)) {
            source = getSourceFromArray(module.getArray(MODULES), fileName);
        }
        if (source == null && validKey(module, CHUNKS, ARRAY)) {
            source = getSourceFromArray(module.getArray(CHUNKS), fileName);
        }
        if (source == null && validKey(module, NAME, STRING)
                && validKey(module, SOURCE, STRING)) {
            String name = module.getString(NAME);

            // append `.js` extension if not yet as webpack does
            fileName = fileName.replaceFirst("(\\.js|)$", ".js");

            String alternativeFileName = fileName
                    // Replace frontend part since webpack entry-point is
                    // already in the frontend folder
                    .replaceFirst("^(\\./)frontend/", "$1")
                    // Replace the flow frontend protocol
                    .replaceFirst("^frontend://", ".");

            // For templates inside add-ons we will not find the sources
            // using ./ as the actual path contains
            // "node_modules/@vaadin/flow-frontend/" instead of "./"
            // "target/flow-frontend/" instead of "./"
            if (name.contains(FLOW_NPM_PACKAGE_NAME)
                    || name.contains(DEAULT_FLOW_RESOURCES_FOLDER)) {
                alternativeFileName = alternativeFileName.replaceFirst("\\./",
                        "");
            }

            // Remove query-string used by webpack modules like babel (e.g
            // ?babel-target=es6)
            name = name.replaceFirst("\\?.+$", "");

            // Do check on the original fileName and the alternative one
            if (name.endsWith(fileName) || name.endsWith(alternativeFileName)) {
                source = module.getString(SOURCE);
            }
        }
        return source;
    }

    // Visits all elements of a JsonArray and returns the first element with a
    // valid source module
    private static String getSourceFromArray(JsonArray objects,
            String fileName) {
        String source = null;
        for (int i = 0; source == null && i < objects.length(); i++) {
            if (objects.get(i).getType().equals(OBJECT)) {
                source = getSourceFromObject(objects.get(i), fileName);
            }
        }
        return source;
    }

    private static boolean validKey(JsonObject o, String k, JsonType t) {
        boolean validKey = o != null && o.hasKey(k)
                && o.get(k).getType().equals(t);
        return validKey && (!t.equals(STRING) || !o.getString(k).isEmpty());
    }
}
