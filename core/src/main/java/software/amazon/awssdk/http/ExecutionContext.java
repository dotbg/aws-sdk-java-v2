/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.http;

import software.amazon.awssdk.annotation.NotThreadSafe;
import software.amazon.awssdk.annotation.SdkProtectedApi;
import software.amazon.awssdk.auth.AwsCredentials;
import software.amazon.awssdk.auth.AwsCredentialsProvider;
import software.amazon.awssdk.interceptor.context.DefaultInterceptorContext;
import software.amazon.awssdk.interceptor.ExecutionAttributes;
import software.amazon.awssdk.interceptor.ExecutionInterceptorChain;
import software.amazon.awssdk.internal.http.timers.client.ClientExecutionAbortTrackerTask;
import software.amazon.awssdk.metrics.spi.AwsRequestMetrics;
import software.amazon.awssdk.runtime.auth.SignerProvider;
import software.amazon.awssdk.utils.Validate;
import software.amazon.awssdk.utils.builder.CopyableBuilder;
import software.amazon.awssdk.utils.builder.ToCopyableBuilder;

/**
 * @NotThreadSafe This class should only be accessed by a single thread and be used throughout
 *                a single request lifecycle.
 */
@NotThreadSafe
@SdkProtectedApi
public class ExecutionContext implements ToCopyableBuilder<ExecutionContext.Builder, ExecutionContext> {
    private final AwsRequestMetrics awsRequestMetrics;
    private final SignerProvider signerProvider;
    private DefaultInterceptorContext interceptorContext;
    private final ExecutionInterceptorChain interceptorChain;
    private final ExecutionAttributes executionAttributes;

    private boolean retryCapacityConsumed;

    /**
     * Optional credentials to enable the runtime layer to handle signing requests (and resigning on
     * retries).
     */
    private AwsCredentialsProvider credentialsProvider;

    private ClientExecutionAbortTrackerTask clientExecutionTrackerTask;

    private ExecutionContext(final Builder builder) {
        this.awsRequestMetrics = Validate.paramNotNull(builder.awsRequestMetrics, "awsRequestMetrics");
        this.signerProvider = Validate.paramNotNull(builder.signerProvider, "signerProvider");
        this.interceptorContext = Validate.paramNotNull(builder.interceptorContext, "interceptorContext");
        this.interceptorChain = Validate.paramNotNull(builder.interceptorChain, "interceptorChain");
        this.executionAttributes = Validate.paramNotNull(builder.executionAttributes, "executionAttributes");
    }

    public static ExecutionContext.Builder builder() {
        return new ExecutionContext.Builder();
    }

    public DefaultInterceptorContext interceptorContext() {
        return interceptorContext;
    }

    public ExecutionContext interceptorContext(DefaultInterceptorContext interceptorContext) {
        // TODO: this shouldn't be mutable
        this.interceptorContext = interceptorContext;
        return this;
    }

    public ExecutionInterceptorChain interceptorChain() {
        return interceptorChain;
    }

    public ExecutionAttributes executionAttributes() {
        return executionAttributes;
    }

    public AwsRequestMetrics awsRequestMetrics() {
        return awsRequestMetrics;
    }

    /**
     * Returns whether retry capacity was consumed during this request lifecycle.
     * This can be inspected to determine whether capacity should be released if a retry succeeds.
     *
     * @return true if retry capacity was consumed
     */
    public boolean retryCapacityConsumed() {
        return retryCapacityConsumed;
    }

    /**
     * Marks that a retry during this request lifecycle has consumed retry capacity.  This is inspected
     * when determining if capacity should be released if a retry succeeds.
     */
    public void markRetryCapacityConsumed() {
        this.retryCapacityConsumed = true;
    }

    /**
     * Returns the credentials provider used for fetching the credentials. The credentials fetched
     * is used for signing the request. If there is no credential provider, then the runtime will
     * not attempt to sign (or resign on retries) requests.
     *
     * @return the credentials provider to fetch {@link AwsCredentials}
     */
    public AwsCredentialsProvider getCredentialsProvider() {
        return this.credentialsProvider;
    }

    /**
     * Sets the credentials provider used for fetching the credentials. The credentials fetched is
     * used for signing the request. If there is no credential provider, then the runtime will not
     * attempt to sign (or resign on retries) requests.
     *
     * @param credentialsProvider
     *            the credentials provider to fetch {@link AwsCredentials}
     */
    public void setCredentialsProvider(AwsCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public ClientExecutionAbortTrackerTask getClientExecutionTrackerTask() {
        return clientExecutionTrackerTask;
    }

    public void setClientExecutionTrackerTask(ClientExecutionAbortTrackerTask clientExecutionTrackerTask) {
        this.clientExecutionTrackerTask = clientExecutionTrackerTask;
    }

    public SignerProvider signerProvider() {
        return signerProvider;
    }

    @Override
    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder implements CopyableBuilder<Builder, ExecutionContext> {
        private DefaultInterceptorContext interceptorContext;
        private ExecutionInterceptorChain interceptorChain;
        private ExecutionAttributes executionAttributes;
        private SignerProvider signerProvider;
        private AwsRequestMetrics awsRequestMetrics;

        private Builder() {
        }

        public Builder(ExecutionContext executionContext) {
            this.awsRequestMetrics = executionContext.awsRequestMetrics;
            this.signerProvider = executionContext.signerProvider;
            this.interceptorContext = executionContext.interceptorContext;
            this.interceptorChain = executionContext.interceptorChain;
            this.executionAttributes = executionContext.executionAttributes;
        }

        public Builder interceptorContext(DefaultInterceptorContext interceptorContext) {
            this.interceptorContext = interceptorContext;
            return this;
        }

        public Builder awsRequestMetrics(AwsRequestMetrics awsRequestMetrics) {
            this.awsRequestMetrics = awsRequestMetrics;
            return this;
        }

        public Builder interceptorChain(ExecutionInterceptorChain interceptorChain) {
            this.interceptorChain = interceptorChain;
            return this;
        }

        public Builder executionAttributes(ExecutionAttributes executionAttributes) {
            this.executionAttributes = executionAttributes;
            return this;
        }

        public Builder signerProvider(SignerProvider signerProvider) {
            this.signerProvider = signerProvider;
            return this;
        }

        public ExecutionContext build() {
            return new ExecutionContext(this);
        }

    }

}
