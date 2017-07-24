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

package software.amazon.awssdk.handlers;

import software.amazon.awssdk.RequestConfig;
import software.amazon.awssdk.ServiceAdvancedConfiguration;
import software.amazon.awssdk.annotation.ReviewBeforeRelease;
import software.amazon.awssdk.auth.AwsCredentials;
import software.amazon.awssdk.auth.Signer;
import software.amazon.awssdk.interceptor.ExecutionAttribute;
import software.amazon.awssdk.interceptor.ExecutionInterceptor;

/**
 * AWS-specific attributes attached to the execution. This information is available to {@link ExecutionInterceptor}s and
 * {@link Signer}s.
 */
public class AwsExecutionAttributes {
    /**
     * The key under which the request credentials are set.
     */
    public static final ExecutionAttribute<AwsCredentials> AWS_CREDENTIALS = new ExecutionAttribute<>();

    /**
     * The key under which the request config is stored.
     */
    @ReviewBeforeRelease("RequestConfig feels pretty internal. Can we just expose parts of it?")
    public static final ExecutionAttribute<RequestConfig> REQUEST_CONFIG = new ExecutionAttribute<>();

    /**
     * The key under which the service name is stored.
     */
    public static final ExecutionAttribute<String> SERVICE_NAME = new ExecutionAttribute<>();

    /**
     * The key under which the time offset (for clock skew correction) is stored.
     */
    public static final ExecutionAttribute<Integer> TIME_OFFSET = new ExecutionAttribute<>();

    /**
     * Handler context key for advanced configuration.
     */
    public static final ExecutionAttribute<ServiceAdvancedConfiguration> SERVICE_ADVANCED_CONFIG = new ExecutionAttribute<>();
}
