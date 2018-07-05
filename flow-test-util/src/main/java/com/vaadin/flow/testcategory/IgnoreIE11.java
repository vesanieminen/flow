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
package com.vaadin.flow.testcategory;

/**
 * Tests that should not be run with IE11 should be annotated with @
 * {@code Category(IgnoreIE11.class)} so they can be optionally excluded from
 * the build when running with IE11.
 * 
 * @author Vaadin Ltd
 * @since 1.0
 *
 */
public interface IgnoreIE11 extends TestCategory {

}
