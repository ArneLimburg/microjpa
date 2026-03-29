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

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;

import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

abstract class AbstractWebTest {

    private URI parentUri;
    private URI uri;

    public void initialize(URI baseUri) throws IOException {
        assertEquals(Status.OK.getStatusCode(), get(baseUri).getResponseCode());
        parentUri = baseUri.resolve("test-parent");
        uri = parentUri.resolve("test-parent/managed");
    }

    @Test
    public void entityManagerIsClosedAfterPostConstruct() throws IOException {
        assertEquals(Status.NO_CONTENT.getStatusCode(), get(uri).getResponseCode());
    }

    @Test
    public void entityManagerIsClosedAfterRequest() throws IOException {
        assertEquals(Status.OK.getStatusCode(), post(uri).getResponseCode());

        assertEquals(Status.NO_CONTENT.getStatusCode(), get(uri).getResponseCode());
    }

    @Test
    public void eTagIsNotSetBeforeSuccess() throws IOException {
        HttpURLConnection creationResponse = post(parentUri, """
                {
                    "name": "Test"
                }
            """);
        String eTag = creationResponse.getHeaderField("ETag");
        String location = creationResponse.getHeaderField("Location");
        String id = location.substring(location.lastIndexOf('/') + 1);
        assertEquals(Status.CREATED.getStatusCode(), creationResponse.getResponseCode());
        assertNotNull(eTag);

        HttpURLConnection response = put(parentUri.resolve("test-parent/" + id + "?beforeCompletion=true"), """
                {
                    "name": "Changed"
                }
            """, "ETag", eTag);
        String updatedETag = response.getHeaderField("ETag");
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getResponseCode());
        assertTrue(updatedETag.compareTo(eTag) == 0);
    }

    @Test
    public void eTagIsNotSetBeforeRollback() throws IOException {
        HttpURLConnection creationResponse = post(parentUri, """
                {
                    "name": "Test"
                }
            """);
        String eTag = creationResponse.getHeaderField("ETag");
        String location = creationResponse.getHeaderField("Location");
        String id = location.substring(location.lastIndexOf('/') + 1);
        assertEquals(Status.CREATED.getStatusCode(), creationResponse.getResponseCode());
        assertNotNull(eTag);

        HttpURLConnection response = put(parentUri.resolve("test-parent/" + id + "?beforeCompletion=true&rollback=true"), """
                {
                    "name": "Changed"
                }
            """, "ETag", eTag);
        String updatedETag = response.getHeaderField("ETag");
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getResponseCode());
        assertNotNull(updatedETag);
        assertTrue(updatedETag.compareTo(eTag) == 0);
    }


    @Test
    public void eTagIsSetAfterSuccess() throws IOException {
        HttpURLConnection creationResponse = post(parentUri, """
                {
                    "name": "Test"
                }
            """);
        String eTag = creationResponse.getHeaderField("ETag");
        String location = creationResponse.getHeaderField("Location");
        String id = location.substring(location.lastIndexOf('/') + 1);
        assertEquals(Status.CREATED.getStatusCode(), creationResponse.getResponseCode());
        assertNotNull(eTag);

        HttpURLConnection response = put(parentUri.resolve("test-parent/" + id), """
                {
                    "name": "Changed"
                }
            """, "ETag", eTag);
        String updatedETag = response.getHeaderField("ETag");
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getResponseCode());
        assertTrue(updatedETag.compareTo(eTag) > 0);
    }

    @Test
    public void eTagIsNotSetAfterRollback() throws IOException {
        HttpURLConnection creationResponse = post(parentUri, """
                {
                    "name": "Test"
                }
            """);
        String eTag = creationResponse.getHeaderField("ETag");
        String location = creationResponse.getHeaderField("Location");
        String id = location.substring(location.lastIndexOf('/') + 1);
        assertEquals(Status.CREATED.getStatusCode(), creationResponse.getResponseCode());
        assertNotNull(eTag);

        HttpURLConnection response = put(parentUri.resolve("test-parent/" + id + "?rollback=true"), """
                {
                    "name": "Changed"
                }
            """, "ETag", eTag);
        String updatedETag = response.getHeaderField("ETag");
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getResponseCode());
        assertNotNull(updatedETag);
        assertTrue(updatedETag.compareTo(eTag) == 0);
    }

    private HttpURLConnection get(URI uri) throws IOException {
        return (HttpURLConnection)uri.toURL().openConnection();
    }

    private HttpURLConnection post(URI uri, String body, String... headers) throws IOException {
        return write("POST", uri, body, headers);
    }

    private HttpURLConnection put(URI uri, String body, String... headers) throws IOException {
        return write("PUT", uri, body, headers);
    }

    private HttpURLConnection post(URI uri) throws IOException {
        return write("POST", uri);
    }

    private HttpURLConnection write(String httpMethod, URI uri, String body, String... headers) throws IOException {
        HttpURLConnection connection = write(httpMethod, uri);
        try (Writer writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.append(body);
        }
        return connection;
    }

    private HttpURLConnection write(String httpMethod, URI uri, String... headers) throws MalformedURLException, IOException {
        HttpURLConnection connection = (HttpURLConnection)uri.toURL().openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod(httpMethod);
        connection.setRequestProperty("Content-Type", APPLICATION_JSON);
        for (int i = 0; i < headers.length; i += 2) {
            connection.setRequestProperty(headers[i], headers[i + 1]);
        }
        connection.connect();
        return connection;
    }
}
