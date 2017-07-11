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

import java.util.List;
import software.amazon.awssdk.annotation.NotThreadSafe;
import software.amazon.awssdk.annotation.SdkProtectedApi;
import software.amazon.awssdk.auth.AwsCredentials;
import software.amazon.awssdk.auth.AwsCredentialsProvider;
import software.amazon.awssdk.auth.Signer;
import software.amazon.awssdk.interceptor.ExecutionAttributes;
import software.amazon.awssdk.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.internal.auth.NoOpSignerProvider;
import software.amazon.awssdk.internal.http.timers.client.ClientExecutionAbortTrackerTask;
import software.amazon.awssdk.metrics.spi.AwsRequestMetrics;
import software.amazon.awssdk.runtime.auth.SignerProvider;
import software.amazon.awssdk.runtime.auth.SignerProviderContext;
import software.amazon.awssdk.util.AwsRequestMetricsFullSupport;
import software.amazon.awssdk.utils.Validate;

/**
 * @NotThreadSafe This class should only be accessed by a single thread and be used throughout
 *                a single request lifecycle.
 */
@NotThreadSafe
@SdkProtectedApi
public class ExecutionContext {
    private final AwsRequestMetrics awsRequestMetrics;
    private final List<ExecutionInterceptor> executionInterceptors;
    private final ExecutionAttributes executionAttributes;
    private final SignerProvider signerProvider;

    private boolean retryCapacityConsumed;

    /**
     * Optional credentials to enable the runtime layer to handle signing requests (and resigning on
     * retries).
     */
    private AwsCredentialsProvider credentialsProvider;

    private ClientExecutionAbortTrackerTask clientExecutionTrackerTask;

    private ExecutionContext(final Builder builder) {
        this.executionInterceptors = Validate.paramNotNull(builder.executionInterceptors, "executionInterceptors");
        this.executionAttributes = Validate.paramNotNull(builder.executionAttributes, "executionAttributes");
        this.awsRequestMetrics = builder.useRequestMetrics ? new AwsRequestMetricsFullSupport() : new AwsRequestMetrics();
        this.signerProvider = Validate.paramNotNull(builder.signerProvider, "signerProvider");
    }

    public static ExecutionContext.Builder builder() {
        return new ExecutionContext.Builder();
    }

    public List<ExecutionInterceptor> executionInterceptors() {
        return executionInterceptors;
    }

    public ExecutionAttributes executionAttributes() {
        return executionAttributes;
    }

    public AwsRequestMetrics awsRequestMetrics() {
        return awsRequestMetrics;
    }

    /**
     * There is in general no need to set the signer in the execution context, since the signer for
     * each request may differ depending on the URI of the request. The exception is S3 where the
     * signer is currently determined only when the S3 client is constructed. Hence the need for
     * this method. We may consider supporting a per request level signer determination for S3 later
     * on.
     */
    @Deprecated
    public void setSigner(Signer signer) {
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
     * Passes in the provided {@link SignerProviderContext} into a {@link SignerProvider} and returns
     * a {@link Signer} instance.
     */
    public Signer getSigner(SignerProviderContext context) {
        return signerProvider.getSigner(context);
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

    public SignerProvider getSignerProvider() {
        return signerProvider;
    }

    public static class Builder {

        private boolean useRequestMetrics;
        private List<ExecutionInterceptor> executionInterceptors;
        private ExecutionAttributes executionAttributes;
        private SignerProvider signerProvider = new NoOpSignerProvider();

        private Builder() {
        }

        public Builder withUseRequestMetrics(boolean withUseRequestMetrics) {
            this.useRequestMetrics = withUseRequestMetrics;
            return this;
        }

        public Builder executionInterceptors(List<ExecutionInterceptor> executionInterceptors) {
            this.executionInterceptors = executionInterceptors;
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
