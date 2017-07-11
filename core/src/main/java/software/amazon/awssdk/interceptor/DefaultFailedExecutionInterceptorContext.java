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
        implements ToCopyableBuilder<DefaultFailedExecutionInterceptorContext.Builder, DefaultFailedExecutionInterceptorContext> {
    private SdkRequest request;
    private SdkHttpFullRequest httpRequest;
    private SdkHttpFullResponse httpResponse;
    private SdkResponse response;
    private Exception exception;

    private DefaultFailedExecutionInterceptorContext(Builder builder) {
        this.request = Validate.paramNotNull(builder.request, "request");
        this.httpRequest = builder.httpRequest;
        this.httpResponse = builder.httpResponse;
        this.response = builder.response;
        this.exception = Validate.paramNotNull(builder.exception, "exception");
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Builder toBuilder() {
        return new Builder(this);
    }

    public SdkRequest request() {
        return request;
    }

    public Optional<SdkHttpFullRequest> httpRequest() {
        return Optional.ofNullable(httpRequest);
    }

    public Optional<SdkHttpFullResponse> httpResponse() {
        return Optional.ofNullable(httpResponse);
    }

    public Optional<SdkResponse> response() {
        return Optional.ofNullable(response);
    }

    public Exception exception() {
        return exception;
    }

    @NotThreadSafe
    public static final class Builder implements CopyableBuilder<Builder, DefaultFailedExecutionInterceptorContext> {
        private SdkRequest request;
        private SdkHttpFullRequest httpRequest;
        private SdkHttpFullResponse httpResponse;
        private SdkResponse response;
        private Exception exception;

        private Builder() {
            super();
        }

        private Builder(DefaultFailedExecutionInterceptorContext context) {
            this.request = context.request;
            this.httpRequest = context.httpRequest;
            this.httpResponse = context.httpResponse;
            this.response = context.response;
            this.exception = context.exception;
        }

        public Builder request(SdkRequest request) {
            this.request = request;
            return this;
        }

        public Builder httpRequest(SdkHttpFullRequest httpRequest) {
            this.httpRequest = httpRequest;
            return this;
        }

        public Builder httpResponse(SdkHttpFullResponse httpResponse) {
            this.httpResponse = httpResponse;
            return this;
        }

        public Builder response(SdkResponse response) {
            this.response = response;
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
