/*
 * Copyright 2000-2018 Vaadin Ltd.
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
package com.vaadin.flow.component;

/**
 * Interface with the methods implemented by components that can gain and lose
 * focus.
 *
 * @param <T>
 *            the type of the component which implements the interface
 * @see BlurNotifier
 * @see FocusNotifier
 */
public interface Focusable<T extends Component> extends HasElement,
        BlurNotifier<T>, FocusNotifier<T>, HasEnabled {

    /**
     * Sets the <code>tabindex</code> attribute in the component. The tabIndex
     * indicates if its element can be focused, and if/where it participates in
     * sequential keyboard navigation:
     * <ul>
     * <li>A negative value (usually <code>tabindex = -1</code> means that the
     * component should be focusable, but should not be reachable via sequential
     * keyboard navigation.</li>
     *
     * <li><code>tabindex = 0</code> means that the component should be
     * focusable in sequential keyboard navigation, but its order is defined by
     * the document's source order.</li>
     *
     * <li>A positive value means the component should be focusable in
     * sequential keyboard navigation, with its order defined by the value of
     * the number. That is, <code>tabindex = 4</code> would be focused before
     * <code>tabindex = 5</code>, but after <code>tabindex = 3</code>. If
     * multiple components share the same positive tabindex value, their order
     * relative to each other follows their position in the document
     * source.</li>
     * </ul>
     *
     * @param tabIndex
     *            the tabindex attribute
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/HTML/Global_attributes/tabindex">tabindex
     *      at MDN</a>
     */
    default void setTabIndex(int tabIndex) {
        getElement().setAttribute("tabindex", String.valueOf(tabIndex));
    }

    /**
     * Gets the <code>tabindex</code> in the component. The tabIndex indicates
     * if its element can be focused, and if/where it participates in sequential
     * keyboard navigation.
     * <p>
     * If there's no such attribute set, it returns the default setting for the
     * element, which depends on the element and on the browser. If the
     * attribute cannot be parsed to <code>int</code>, then an
     * {@link IllegalStateException} is thrown.
     *
     * @return the tabindex attribute, or 0 if none
     * @throws IllegalStateException
     *             if the returned tabindex from the element is empty or can not
     *             be parsed to int
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/HTML/Global_attributes/tabindex">tabindex
     *      at MDN</a>
     */
    default int getTabIndex() {
        String attribute = getElement().getAttribute("tabindex");
        if (attribute == null || attribute.isEmpty()) {
            throw new IllegalStateException(
                    "tabindex attribute is empty on element "
                            + getElement().getTag());
        }
        try {
            return Integer.parseInt(attribute);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "tabindex attribute could not be parsed on element "
                            + getElement().getTag() + ": " + attribute);
        }
    }

    /**
     * Calls the <code>focus</code> function at the client, making the component
     * keyboard focused.
     *
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/focus">focus
     *      at MDN</a>
     */
    default void focus() {
        getElement().callFunction("focus");
    }

    /**
     * Calls the <code>blur</code> function at the client, making the component
     * lose keyboard focus.
     *
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/blur">blur
     *      at MDN</a>
     */
    default void blur() {
        getElement().callFunction("blur");
    }

}
