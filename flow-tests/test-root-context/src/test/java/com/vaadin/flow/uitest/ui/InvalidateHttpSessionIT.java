/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.flow.uitest.ui;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.ChromeBrowserTest;

public class InvalidateHttpSessionIT extends ChromeBrowserTest {

    @Test
    public void invalidateHttpSession_vaadinSessionIsClosed() {
        open();

        findElement(By.id("invalidate-session")).click();

        waitForElementPresent(By.id("invalidated-session-id"));

        String invalidatedSessionId = findElement(
                By.id("invalidated-session-id")).getText();

        String sessionId = findElement(By.id("current-session-id")).getText();
        Assert.assertNotEquals(sessionId, invalidatedSessionId);
    }

}