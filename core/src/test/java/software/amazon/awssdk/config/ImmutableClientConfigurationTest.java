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

package software.amazon.awssdk.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.Test;
import software.amazon.awssdk.auth.AwsCredentialsProvider;
import software.amazon.awssdk.auth.DefaultCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.internal.auth.NoOpSignerProvider;
import software.amazon.awssdk.metrics.RequestMetricCollector;
import software.amazon.awssdk.retry.v2.RetryPolicy;
import software.amazon.awssdk.retry.v2.RetryPolicyContext;

/**
 * Validate the functionality of {@link ImmutableClientConfiguration}.
 */
@SuppressWarnings("deprecation") // Intentional use of deprecated class
public class ImmutableClientConfigurationTest {
    private static final NoOpSignerProvider SIGNER_PROVIDER = new NoOpSignerProvider();
    private static final AwsCredentialsProvider CREDENTIALS_PROVIDER = new DefaultCredentialsProvider();
    private static final URI ENDPOINT = URI.create("https://www.example.com");
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
    private static final SdkHttpClient SYNC_HTTP_CLIENT = mock(SdkHttpClient.class);
    private static final SdkAsyncHttpClient ASYNC_HTTP_CLIENT = mock(SdkAsyncHttpClient.class);
    private static final ExecutionInterceptor EXECUTION_INTERCEPTOR = new ExecutionInterceptor() {
    };
    private static final RetryPolicy RETRY_POLICY = new RetryPolicy() {
        @Override
        public long computeDelayBeforeNextRetry(RetryPolicyContext context) {
            return 0;
        }

        @Override
        public boolean shouldRetry(RetryPolicyContext context) {
            return false;
        }
    };

    @Test
    public void immutableSyncConfigurationMatchesMutableConfiguration() {
        assertThat(new ImmutableSyncClientConfiguration(new InitializedSyncConfiguration()))
                .isEqualToComparingFieldByField(new InitializedSyncConfiguration());
    }

    @Test
    public void immutableAsyncConfigurationMatchesMutableConfiguration() {
        assertThat(new ImmutableAsyncClientConfiguration(new InitializedAsyncConfiguration()))
                .isEqualToComparingFieldByField(new InitializedAsyncConfiguration());
    }

    private static class InitializedSyncConfiguration extends InitializedConfiguration implements SyncClientConfiguration {

        @Override
        public SdkHttpClient httpClient() {
            return SYNC_HTTP_CLIENT;
        }
    }

    private static class InitializedAsyncConfiguration extends InitializedConfiguration implements AsyncClientConfiguration {
        @Override
        public ScheduledExecutorService asyncExecutorService() {
            return EXECUTOR_SERVICE;
        }

        @Override
        public SdkAsyncHttpClient asyncHttpClient() {
            return ASYNC_HTTP_CLIENT;
        }
    }

    private static class InitializedConfiguration implements ClientConfiguration {

        @Override
        public ClientOverrideConfiguration overrideConfiguration() {
            return ClientOverrideConfiguration.builder()
                                              .httpRequestTimeout(Duration.ofSeconds(2))
                                              .totalExecutionTimeout(Duration.ofSeconds(4))
                                              .gzipEnabled(true)
                                              .addAdditionalHttpHeader("header", "value")
                                              .requestMetricCollector(RequestMetricCollector.NONE)
                                              .advancedOption(AdvancedClientOption.USER_AGENT_PREFIX, "userAgentPrefix")
                                              .advancedOption(AdvancedClientOption.USER_AGENT_SUFFIX, "userAgentSuffix")
                                              .advancedOption(AdvancedClientOption.SIGNER_PROVIDER, SIGNER_PROVIDER)
                                              .retryPolicy(RETRY_POLICY)
                                              .addExecutionInterceptor(EXECUTION_INTERCEPTOR)
                                              .build();
        }

        @Override
        public AwsCredentialsProvider credentialsProvider() {
            return CREDENTIALS_PROVIDER;
        }

        @Override
        public URI endpoint() {
            return ENDPOINT;
        }

    }
}
