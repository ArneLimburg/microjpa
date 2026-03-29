/*
 * Copyright 2021 - 2026 Arne Limburg
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

import java.net.URI;

import jakarta.inject.Inject;

import org.apache.meecrowave.Meecrowave;
import org.apache.meecrowave.junit5.MeecrowaveConfig;
import org.junit.jupiter.api.BeforeEach;
import org.microjpa.tags.WebTest;

@WebTest
@MeecrowaveConfig
class OpenWebBeansWebTest extends AbstractWebTest {

    @Inject
    private Meecrowave meecrowave;

    @BeforeEach
    public void initialize() throws Exception {
        super.initialize(new URI("http://localhost:" + meecrowave.getConfiguration().getHttpPort() + "/test-parent"));
    }
}
