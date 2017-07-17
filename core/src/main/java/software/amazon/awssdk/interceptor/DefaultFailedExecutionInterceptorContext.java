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

package software.amazon.awssdk.interceptor;

import java.util.Optional;
import software.amazon.awssdk.SdkRequest;
import software.amazon.awssdk.SdkResponse;
import software.amazon.awssdk.annotation.NotThreadSafe;
import software.amazon.awssdk.annotation.SdkInternalApi;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpFullResponse;
import software.amazon.awssdk.utils.Validate;
import software.amazon.awssdk.utils.builder.CopyableBuilder;
import software.amazon.awssdk.utils.builder.ToCopyableBuilder;

@SdkInternalApi
public class DefaultFailedExecutionInterceptorContext
        implements ToCopyableBuilder<DefaultFailedExecutionInterceptorContext.Builder, DefaultFailedExecutionInterceptorContext>,
                   FailedExecutionContext {
    private final DefaultInterceptorContext interceptorContext;
    private final Exception exception;

    private DefaultFailedExecutionInterceptorContext(Builder builder) {
        this.interceptorContext = Validate.paramNotNull(builder.interceptorContext, "interceptorContext");
        this.exception = Validate.paramNotNull(builder.exception, "exception");
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public SdkRequest request() {
        return interceptorContext.request();
    }

    @Override
    public Optional<SdkHttpFullRequest> httpRequest() {
        return Optional.ofNullable(interceptorContext.httpRequest());
    }

    @Override
    public Optional<SdkHttpFullResponse> httpResponse() {
        return Optional.ofNullable(interceptorContext.httpResponse());
    }

    @Override
    public Optional<SdkResponse> response() {
        return Optional.ofNullable(interceptorContext.response());
    }

    @Override
    public Exception exception() {
        return exception;
    }

    @NotThreadSafe
    public static final class Builder implements CopyableBuilder<Builder, DefaultFailedExecutionInterceptorContext> {
        private DefaultInterceptorContext interceptorContext;
        private Exception exception;

        private Builder() {
            super();
        }

        private Builder(DefaultFailedExecutionInterceptorContext context) {
            this.interceptorContext = context.interceptorContext;
            this.exception = context.exception;
        }

        public Builder interceptorContext(DefaultInterceptorContext interceptorContext) {
            this.interceptorContext = interceptorContext;
            return this;
        }

        public Builder exception(Exception exception) {
            this.exception = exception;
            return this;
        }

        @Override
        public DefaultFailedExecutionInterceptorContext build() {
            return new DefaultFailedExecutionInterceptorContext(this);
        }
    }
}