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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

public abstract class AbstractRequestScopedTest {

    private URL url;

    public void initialize(URL baseUrl) throws IOException {
        assertEquals(Status.OK.getStatusCode(), get(baseUrl).getResponseCode());
        url = new URL(baseUrl, "test-parent/managed");
    }

    @Test
    public void entityManagerIsClosedAfterPostConstruct() throws IOException {
        assertEquals(Status.NO_CONTENT.getStatusCode(), get(url).getResponseCode());
    }

    @Test
    public void entityManagerIsClosedAfterRequest() throws IOException {
        assertEquals(Status.OK.getStatusCode(), post(url).getResponseCode());

        assertEquals(Status.NO_CONTENT.getStatusCode(), get(url).getResponseCode());
    }

    private HttpURLConnection get(URL url) throws IOException {
        return (HttpURLConnection)url.openConnection();
    }

    private HttpURLConnection post(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.connect();
        return connection;
    }
}
