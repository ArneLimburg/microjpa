/*
 * Copyright 2020 - 2024 Arne Limburg
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
package org.microjpa;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microjpa.event.EventConsumer;
import org.microjpa.event.EventProducer;
import org.microjpa.event.SomeEvent;

class TransactionalEventingTest {

    private SeContainer cdiContainer;
    private EventProducer producer;
    private EventConsumer consumer;

    @BeforeEach
    public void startCdi() {
        cdiContainer = SeContainerInitializer.newInstance().initialize();

        producer = cdiContainer.select(EventProducer.class).get();
        consumer = cdiContainer.select(EventConsumer.class).get();
    }

    @AfterEach
    public void shutDownCdi() {
        cdiContainer.close();
    }

    @Test
    public void transactionalEvent() {
        producer
            .assertThatInProgress(() -> {
                assertTrue(consumer.hasConsumedAnyEventInProgress());
                assertTrue(consumer.hasConsumedDefaultEventInProgress());
                assertFalse(consumer.hasConsumedQualifiedEventInProgress());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventInProgress());
                assertFalse(consumer.hasConsumedAnyEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDefaultEventBeforeCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
                assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterFailure());
                assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
                assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
                assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
                assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
            })
            .assertThatBeforeCompletion(() -> {
                assertTrue(consumer.hasConsumedAnyEventBeforeCompletion());
                assertTrue(consumer.hasConsumedDefaultEventBeforeCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
                assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterFailure());
                assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
                assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
                assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
                assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
            });
        producer.fireInTransaction(new SomeEvent());
        assertTrue(consumer.hasConsumedAnyEventBeforeCompletion());
        assertTrue(consumer.hasConsumedDefaultEventBeforeCompletion());
        assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
        assertTrue(consumer.hasConsumedAnyEventAfterCompletion());
        assertTrue(consumer.hasConsumedDefaultEventAfterCompletion());
        assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
        assertFalse(consumer.hasConsumedAnyEventAfterFailure());
        assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
        assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
        assertTrue(consumer.hasConsumedAnyEventAfterSuccess());
        assertTrue(consumer.hasConsumedDefaultEventAfterSuccess());
        assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
    }

    @Test
    public void nonTransactionalEvent() {
        producer
            .assertThatInProgress(() -> {
                assertTrue(consumer.hasConsumedAnyEventInProgress());
                assertTrue(consumer.hasConsumedDefaultEventInProgress());
                assertFalse(consumer.hasConsumedQualifiedEventInProgress());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventInProgress());
                assertFalse(consumer.hasConsumedAnyEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDefaultEventBeforeCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
                assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterFailure());
                assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
                assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
                assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
                assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
            })
            .assertThatBeforeCompletion(() -> {
                assertFalse(consumer.hasConsumedAnyEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDefaultEventBeforeCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
                assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterFailure());
                assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
                assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
                assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
                assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
            });
        producer.fireWithoutTransaction(new SomeEvent());
        assertFalse(consumer.hasConsumedAnyEventBeforeCompletion());
        assertFalse(consumer.hasConsumedDefaultEventBeforeCompletion());
        assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
        assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
        assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
        assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
        assertFalse(consumer.hasConsumedAnyEventAfterFailure());
        assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
        assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
        assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
        assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
        assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
    }

    @Test
    public void transactionalQualifiedEvent() {
        producer
            .assertThatInProgress(() -> {
                assertTrue(consumer.hasConsumedAnyEventInProgress());
                assertFalse(consumer.hasConsumedDefaultEventInProgress());
                assertTrue(consumer.hasConsumedQualifiedEventInProgress());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventInProgress());
                assertFalse(consumer.hasConsumedAnyEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDefaultEventBeforeCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
                assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterFailure());
                assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
                assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
                assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
                assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
            })
            .assertThatBeforeCompletion(() -> {
                assertTrue(consumer.hasConsumedAnyEventBeforeCompletion());
                assertTrue(consumer.hasConsumedDefaultEventBeforeCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
                assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterFailure());
                assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
                assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
                assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
                assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
            });
        producer.fireQualifiedInTransaction(new SomeEvent());
        assertTrue(consumer.hasConsumedAnyEventBeforeCompletion());
        assertFalse(consumer.hasConsumedDefaultEventBeforeCompletion());
        assertTrue(consumer.hasConsumedQualifiedEventBeforeCompletion());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
        assertTrue(consumer.hasConsumedAnyEventAfterCompletion());
        assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
        assertTrue(consumer.hasConsumedQualifiedEventAfterCompletion());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
        assertFalse(consumer.hasConsumedAnyEventAfterFailure());
        assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
        assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
        assertTrue(consumer.hasConsumedAnyEventAfterSuccess());
        assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
        assertTrue(consumer.hasConsumedQualifiedEventAfterSuccess());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
    }

    @Test
    public void transactionalDoubleQualifiedEvent() {
        producer
            .assertThatInProgress(() -> {
                assertTrue(consumer.hasConsumedAnyEventInProgress());
                assertFalse(consumer.hasConsumedDefaultEventInProgress());
                assertTrue(consumer.hasConsumedQualifiedEventInProgress());
                assertTrue(consumer.hasConsumedDoubleQualifiedEventInProgress());
                assertFalse(consumer.hasConsumedAnyEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDefaultEventBeforeCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
                assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterFailure());
                assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
                assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
                assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
                assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
            })
            .assertThatBeforeCompletion(() -> {
                assertTrue(consumer.hasConsumedAnyEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDefaultEventBeforeCompletion());
                assertTrue(consumer.hasConsumedQualifiedEventBeforeCompletion());
                assertTrue(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
                assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterFailure());
                assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
                assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
                assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
                assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
            });
        producer.fireDoubleQualifiedInTransaction(new SomeEvent());
        assertTrue(consumer.hasConsumedAnyEventBeforeCompletion());
        assertFalse(consumer.hasConsumedDefaultEventBeforeCompletion());
        assertTrue(consumer.hasConsumedQualifiedEventBeforeCompletion());
        assertTrue(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
        assertTrue(consumer.hasConsumedAnyEventAfterCompletion());
        assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
        assertTrue(consumer.hasConsumedQualifiedEventAfterCompletion());
        assertTrue(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
        assertFalse(consumer.hasConsumedAnyEventAfterFailure());
        assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
        assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
        assertTrue(consumer.hasConsumedAnyEventAfterSuccess());
        assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
        assertTrue(consumer.hasConsumedQualifiedEventAfterSuccess());
        assertTrue(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
    }

    @Test
    public void transactionalEventWithoutEntitymanagerWithException() {
        producer
            .assertThatInProgress(() -> {
                assertTrue(consumer.hasConsumedAnyEventInProgress());
                assertTrue(consumer.hasConsumedDefaultEventInProgress());
                assertFalse(consumer.hasConsumedQualifiedEventInProgress());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventInProgress());
                assertFalse(consumer.hasConsumedAnyEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDefaultEventBeforeCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
                assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterFailure());
                assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
                assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
                assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
                assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
            })
            .assertThatBeforeCompletion(() -> {
                assertTrue(consumer.hasConsumedAnyEventBeforeCompletion());
                assertTrue(consumer.hasConsumedDefaultEventBeforeCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
                assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterFailure());
                assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
                assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
                assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
                assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
            });
        assertThrows(RuntimeException.class, () -> producer.fireInTransactionWithException(new SomeEvent()));
        assertTrue(consumer.hasConsumedAnyEventInProgress());
        assertTrue(consumer.hasConsumedDefaultEventInProgress());
        assertFalse(consumer.hasConsumedQualifiedEventInProgress());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventInProgress());
        assertTrue(consumer.hasConsumedAnyEventBeforeCompletion());
        assertTrue(consumer.hasConsumedDefaultEventBeforeCompletion());
        assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
        assertTrue(consumer.hasConsumedAnyEventAfterCompletion());
        assertTrue(consumer.hasConsumedDefaultEventAfterCompletion());
        assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
        assertTrue(consumer.hasConsumedAnyEventAfterFailure());
        assertTrue(consumer.hasConsumedDefaultEventAfterFailure());
        assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
        assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
        assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
        assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
    }

    @Test
    public void transactionalEventWithEntitymanagerWithException() {
        producer
            .assertThatInProgress(() -> {
                assertTrue(consumer.hasConsumedAnyEventInProgress());
                assertTrue(consumer.hasConsumedDefaultEventInProgress());
                assertFalse(consumer.hasConsumedQualifiedEventInProgress());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventInProgress());
                assertFalse(consumer.hasConsumedAnyEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDefaultEventBeforeCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
                assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterFailure());
                assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
                assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
                assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
                assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
            })
            .assertThatBeforeCompletion(() -> {
                assertTrue(consumer.hasConsumedAnyEventBeforeCompletion());
                assertTrue(consumer.hasConsumedDefaultEventBeforeCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
                assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
                assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
                assertFalse(consumer.hasConsumedAnyEventAfterFailure());
                assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
                assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
                assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
                assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
                assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
                assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
            });
        assertThrows(RuntimeException.class, () -> producer.fireWithEntitymanagerInTransactionWithException(new SomeEvent()));
        assertTrue(consumer.hasConsumedAnyEventInProgress());
        assertTrue(consumer.hasConsumedDefaultEventInProgress());
        assertFalse(consumer.hasConsumedQualifiedEventInProgress());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventInProgress());
        assertTrue(consumer.hasConsumedAnyEventBeforeCompletion());
        assertTrue(consumer.hasConsumedDefaultEventBeforeCompletion());
        assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
        assertTrue(consumer.hasConsumedAnyEventAfterCompletion());
        assertTrue(consumer.hasConsumedDefaultEventAfterCompletion());
        assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
        assertTrue(consumer.hasConsumedAnyEventAfterFailure());
        assertTrue(consumer.hasConsumedDefaultEventAfterFailure());
        assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
        assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
        assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
        assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
        assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
    }

    @Test
    public void asyncEvent() throws InterruptedException {
        consumer.expect(2);
        producer.assertThatInProgress(() -> {
            assertFalse(consumer.hasConsumedAnyEventInProgress());
            assertFalse(consumer.hasConsumedDefaultEventInProgress());
            assertFalse(consumer.hasConsumedQualifiedEventInProgress());
            assertFalse(consumer.hasConsumedDoubleQualifiedEventInProgress());
            assertFalse(consumer.hasConsumedAnyEventBeforeCompletion());
            assertFalse(consumer.hasConsumedDefaultEventBeforeCompletion());
            assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
            assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
            assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
            assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
            assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
            assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
            assertFalse(consumer.hasConsumedAnyEventAfterFailure());
            assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
            assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
            assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
            assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
            assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
            assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
            assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
        }).fireAsync(new SomeEvent());
        consumer.waitForAsync();
        assertTrue(consumer.hasConsumedAnyEventAsync());
        assertTrue(consumer.hasConsumedDefaultEventAsync());
        assertFalse(consumer.hasConsumedQualifiedEventAsync());
    }

    @Test
    public void qualifiedAsyncEvent() throws InterruptedException {
        consumer.expect(2);
        producer.assertThatInProgress(() -> {
            assertFalse(consumer.hasConsumedAnyEventInProgress());
            assertFalse(consumer.hasConsumedDefaultEventInProgress());
            assertFalse(consumer.hasConsumedQualifiedEventInProgress());
            assertFalse(consumer.hasConsumedDoubleQualifiedEventInProgress());
            assertFalse(consumer.hasConsumedAnyEventBeforeCompletion());
            assertFalse(consumer.hasConsumedDefaultEventBeforeCompletion());
            assertFalse(consumer.hasConsumedQualifiedEventBeforeCompletion());
            assertFalse(consumer.hasConsumedDoubleQualifiedEventBeforeCompletion());
            assertFalse(consumer.hasConsumedAnyEventAfterCompletion());
            assertFalse(consumer.hasConsumedDefaultEventAfterCompletion());
            assertFalse(consumer.hasConsumedQualifiedEventAfterCompletion());
            assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterCompletion());
            assertFalse(consumer.hasConsumedAnyEventAfterFailure());
            assertFalse(consumer.hasConsumedDefaultEventAfterFailure());
            assertFalse(consumer.hasConsumedQualifiedEventAfterFailure());
            assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterFailure());
            assertFalse(consumer.hasConsumedAnyEventAfterSuccess());
            assertFalse(consumer.hasConsumedDefaultEventAfterSuccess());
            assertFalse(consumer.hasConsumedQualifiedEventAfterSuccess());
            assertFalse(consumer.hasConsumedDoubleQualifiedEventAfterSuccess());
        }).fireQualifiedAsync(new SomeEvent());
        consumer.waitForAsync();
        assertTrue(consumer.hasConsumedAnyEventAsync());
        assertFalse(consumer.hasConsumedDefaultEventAsync());
        assertTrue(consumer.hasConsumedQualifiedEventAsync());
    }
}
