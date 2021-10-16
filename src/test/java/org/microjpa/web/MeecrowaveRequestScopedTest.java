/*
 * Copyright 2021 Arne Limburg
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
package org.microjpa.web;

import java.io.IOException;
import java.net.URL;

import org.apache.meecrowave.Meecrowave;
import org.apache.meecrowave.junit5.MeecrowaveConfig;
import org.apache.meecrowave.testing.ConfigurationInject;
import org.junit.jupiter.api.BeforeEach;

@MeecrowaveConfig
public class MeecrowaveRequestScopedTest extends AbstractRequestScopedTest {

    @ConfigurationInject
    private Meecrowave.Builder config;

    @BeforeEach
    public void initialize() throws IOException {
        URL baseUrl = new URL("http://localhost:" + config.getHttpPort() + "/test-parent");
        super.initialize(baseUrl);
    }
}
