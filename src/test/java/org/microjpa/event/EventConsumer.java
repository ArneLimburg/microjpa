/*
 * Copyright 2021 - 2024 Arne Limburg
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
package org.microjpa.event;

import static jakarta.enterprise.event.TransactionPhase.AFTER_COMPLETION;
import static jakarta.enterprise.event.TransactionPhase.AFTER_FAILURE;
import static jakarta.enterprise.event.TransactionPhase.AFTER_SUCCESS;
import static jakarta.enterprise.event.TransactionPhase.BEFORE_COMPLETION;
import static jakarta.enterprise.event.TransactionPhase.IN_PROGRESS;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.inject.Default;

@ApplicationScoped
public class EventConsumer {

    private static final long ASYNC_TIMEOUT = 5L;

    private boolean consumedAnyEventInProgress;
    private boolean consumedAnyEventBeforeCompletion;
    private boolean consumedAnyEventAfterCompletion;
    private boolean consumedAnyEventAfterFailure;
    private boolean consumedAnyEventAfterSuccess;
    private boolean consumedAnyEventAsync;
    private boolean consumedDefaultEventInProgress;
    private boolean consumedDefaultEventBeforeCompletion;
    private boolean consumedDefaultEventAfterCompletion;
    private boolean consumedDefaultEventAfterFailure;
    private boolean consumedDefaultEventAfterSuccess;
    private boolean consumedDefaultEventAsync;
    private boolean consumedQualifiedEventInProgress;
    private boolean consumedQualifiedEventBeforeCompletion;
    private boolean consumedQualifiedEventAfterCompletion;
    private boolean consumedQualifiedEventAfterFailure;
    private boolean consumedQualifiedEventAfterSuccess;
    private boolean consumedQualifiedEventAsync;
    private boolean consumedDoubleQualifiedEventInProgress;
    private boolean consumedDoubleQualifiedEventBeforeCompletion;
    private boolean consumedDoubleQualifiedEventAfterCompletion;
    private boolean consumedDoubleQualifiedEventAfterFailure;
    private boolean consumedDoubleQualifiedEventAfterSuccess;
    private CountDownLatch latch;

    public boolean hasConsumedAnyEventInProgress() {
        return consumedAnyEventInProgress;
    }

    public boolean hasConsumedAnyEventBeforeCompletion() {
        return consumedAnyEventBeforeCompletion;
    }

    public boolean hasConsumedAnyEventAfterCompletion() {
        return consumedAnyEventAfterCompletion;
    }

    public boolean hasConsumedAnyEventAfterFailure() {
        return consumedAnyEventAfterFailure;
    }

    public boolean hasConsumedAnyEventAfterSuccess() {
        return consumedAnyEventAfterSuccess;
    }

    public boolean hasConsumedAnyEventAsync() {
        return consumedAnyEventAsync;
    }

    public boolean hasConsumedDefaultEventInProgress() {
        return consumedDefaultEventInProgress;
    }

    public boolean hasConsumedDefaultEventBeforeCompletion() {
        return consumedDefaultEventBeforeCompletion;
    }

    public boolean hasConsumedDefaultEventAfterCompletion() {
        return consumedDefaultEventAfterCompletion;
    }

    public boolean hasConsumedDefaultEventAfterFailure() {
        return consumedDefaultEventAfterFailure;
    }

    public boolean hasConsumedDefaultEventAfterSuccess() {
        return consumedDefaultEventAfterSuccess;
    }

    public boolean hasConsumedDefaultEventAsync() {
        return consumedDefaultEventAsync;
    }

    public boolean hasConsumedQualifiedEventInProgress() {
        return consumedQualifiedEventInProgress;
    }

    public boolean hasConsumedQualifiedEventBeforeCompletion() {
        return consumedQualifiedEventBeforeCompletion;
    }

    public boolean hasConsumedQualifiedEventAfterCompletion() {
        return consumedQualifiedEventAfterCompletion;
    }

    public boolean hasConsumedQualifiedEventAfterFailure() {
        return consumedQualifiedEventAfterFailure;
    }

    public boolean hasConsumedQualifiedEventAfterSuccess() {
        return consumedQualifiedEventAfterSuccess;
    }

    public boolean hasConsumedQualifiedEventAsync() {
        return consumedQualifiedEventAsync;
    }

    public boolean hasConsumedDoubleQualifiedEventInProgress() {
        return consumedDoubleQualifiedEventInProgress;
    }

    public boolean hasConsumedDoubleQualifiedEventBeforeCompletion() {
        return consumedDoubleQualifiedEventBeforeCompletion;
    }

    public boolean hasConsumedDoubleQualifiedEventAfterCompletion() {
        return consumedDoubleQualifiedEventAfterCompletion;
    }

    public boolean hasConsumedDoubleQualifiedEventAfterFailure() {
        return consumedDoubleQualifiedEventAfterFailure;
    }

    public boolean hasConsumedDoubleQualifiedEventAfterSuccess() {
        return consumedDoubleQualifiedEventAfterSuccess;
    }

    public void consumeAnyEventInProgress(@Observes(during = IN_PROGRESS) SomeEvent event) {
        consumedAnyEventInProgress = true;
    }

    public void consumeAnyEventBeforeCompletion(@Observes(during = BEFORE_COMPLETION) SomeEvent event) {
        consumedAnyEventBeforeCompletion = true;
    }

    public void consumeAnyEventAfterCompletion(@Observes(during = AFTER_COMPLETION) SomeEvent event) {
        consumedAnyEventAfterCompletion = true;
    }

    public void consumeAnyEventAfterFailure(@Observes(during = AFTER_FAILURE) SomeEvent event) {
        consumedAnyEventAfterFailure = true;
    }

    public void consumeAnyEventAfterSuccess(@Observes(during = AFTER_SUCCESS) SomeEvent event) {
        consumedAnyEventAfterSuccess = true;
    }

    public void consumeAnyEventAsync(@ObservesAsync SomeEvent event) {
        consumedAnyEventAsync = true;
        latch.countDown();
    }

    public void consumeDefaultEventInProgress(@Observes(during = IN_PROGRESS) @Default SomeEvent event) {
        consumedDefaultEventInProgress = true;
    }

    public void consumeDefaultEventBeforeCompletion(@Observes(during = BEFORE_COMPLETION) @Default SomeEvent event) {
        consumedDefaultEventBeforeCompletion = true;
    }

    public void consumeDefaultEventAfterCompletion(@Observes(during = AFTER_COMPLETION) @Default SomeEvent event) {
        consumedDefaultEventAfterCompletion = true;
    }

    public void consumeDefaultEventAfterFailure(@Observes(during = AFTER_FAILURE) @Default SomeEvent event) {
        consumedDefaultEventAfterFailure = true;
    }

    public void consumeDefaultEventAfterSuccess(@Observes(during = AFTER_SUCCESS) @Default SomeEvent event) {
        consumedDefaultEventAfterSuccess = true;
    }

    public void consumeDefaultEventAsync(@ObservesAsync @Default SomeEvent event) {
        consumedDefaultEventAsync = true;
        latch.countDown();
    }

    public void consumeQualifiedEventInProgress(@Observes(during = IN_PROGRESS) @SomeQualifier SomeEvent event) {
        consumedQualifiedEventInProgress = true;
    }

    public void consumeQualifiedEventBeforeCompletion(@Observes(during = BEFORE_COMPLETION) @SomeQualifier SomeEvent event) {
        consumedQualifiedEventBeforeCompletion = true;
    }

    public void consumeQualifiedEventAfterCompletion(@Observes(during = AFTER_COMPLETION) @SomeQualifier SomeEvent event) {
        consumedQualifiedEventAfterCompletion = true;
    }

    public void consumeQualifiedEventAfterFailure(@Observes(during = AFTER_FAILURE) @SomeQualifier SomeEvent event) {
        consumedQualifiedEventAfterFailure = true;
    }

    public void consumeQualifiedEventAfterSuccess(@Observes(during = AFTER_SUCCESS) @SomeQualifier SomeEvent event) {
        consumedQualifiedEventAfterSuccess = true;
    }

    public void consumeQualifiedEventAsync(@ObservesAsync @SomeQualifier SomeEvent event) {
        consumedQualifiedEventAsync = true;
        latch.countDown();
    }

    public void consumeDoubleQualifiedEventInProgress(@Observes(during = IN_PROGRESS) @SomeQualifier @AnotherQualifier SomeEvent event) {
        consumedDoubleQualifiedEventInProgress = true;
    }

    public void consumeDoubleQualifiedEventBeforeCompletion(
        @Observes(during = BEFORE_COMPLETION) @SomeQualifier @AnotherQualifier SomeEvent event) {

        consumedDoubleQualifiedEventBeforeCompletion = true;
    }

    public void consumeDoubleQualifiedEventAfterCompletion(
        @Observes(during = AFTER_COMPLETION) @SomeQualifier @AnotherQualifier SomeEvent event) {

        consumedDoubleQualifiedEventAfterCompletion = true;
    }

    public void consumeDoubleQualifiedEventAfterFailure(
        @Observes(during = AFTER_FAILURE) @SomeQualifier @AnotherQualifier SomeEvent event) {

        consumedDoubleQualifiedEventAfterFailure = true;
    }

    public void consumeDoubleQualifiedEventAfterSuccess(
        @Observes(during = AFTER_SUCCESS) @SomeQualifier @AnotherQualifier SomeEvent event) {

        consumedDoubleQualifiedEventAfterSuccess = true;
    }

    public void expect(int numberOfEvents) {
        latch = new CountDownLatch(numberOfEvents);
    }

    public void waitForAsync() throws InterruptedException {
        latch.await(ASYNC_TIMEOUT, TimeUnit.SECONDS);
    }
}
