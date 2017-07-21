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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import software.amazon.awssdk.interceptor.context.DefaultFailedExecutionInterceptorContext;
import software.amazon.awssdk.interceptor.context.InterceptorContext;
import software.amazon.awssdk.utils.Validate;

public class ExecutionInterceptorChain {
    private List<ExecutionInterceptor> interceptors;

    public ExecutionInterceptorChain(List<ExecutionInterceptor> interceptors) {
        this.interceptors = new ArrayList<>(Validate.paramNotNull(interceptors, "interceptors"));
    }

    public void beforeExecution(InterceptorContext context, ExecutionAttributes executionAttributes) {
        tryOrThrowInterceptorException(() -> interceptors.forEach(i -> i.beforeExecution(context, executionAttributes)));
    }

    public InterceptorContext modifyRequest(InterceptorContext context, ExecutionAttributes executionAttributes) {
        return tryOrThrowInterceptorException(() -> {
            InterceptorContext result = context;
            for (ExecutionInterceptor interceptor : interceptors) {
                result = context.modify(b -> b.request(interceptor.modifyRequest(context, executionAttributes)));
                validateInterceptorResult(context, result, interceptor);
            }
            return result;
        });
    }

    public void beforeMarshalling(InterceptorContext context, ExecutionAttributes executionAttributes) {
        tryOrThrowInterceptorException(() -> interceptors.forEach(i -> i.beforeMarshalling(context, executionAttributes)));
    }

    public void afterMarshalling(InterceptorContext context, ExecutionAttributes executionAttributes) {
        tryOrThrowInterceptorException(() -> interceptors.forEach(i -> i.afterMarshalling(context, executionAttributes)));
    }

    public InterceptorContext modifyHttpRequest(InterceptorContext context,
                                                ExecutionAttributes executionAttributes) {
        return tryOrThrowInterceptorException(() -> {
            InterceptorContext result = context;
            for (ExecutionInterceptor interceptor : interceptors) {
                result = context.modify(b -> b.httpRequest(interceptor.modifyHttpRequest(context, executionAttributes)));
                validateInterceptorResult(context, result, interceptor);
            }
            return result;
        });
    }

    public void beforeTransmission(InterceptorContext context, ExecutionAttributes executionAttributes) {
        tryOrThrowInterceptorException(() -> interceptors.forEach(i -> i.beforeTransmission(context, executionAttributes)));
    }

    public void afterTransmission(InterceptorContext context, ExecutionAttributes executionAttributes) {
        tryOrThrowInterceptorException(() -> reverseForEach(i -> i.afterTransmission(context, executionAttributes)));
    }

    public InterceptorContext modifyHttpResponse(InterceptorContext context,
                                                 ExecutionAttributes executionAttributes) {
        return tryOrThrowInterceptorException(() -> {
            InterceptorContext result = context;
            for (int i = interceptors.size() - 1; i >= 0; i--) {
                result = context.toBuilder()
                                .httpResponse(interceptors.get(i).modifyHttpResponse(context, executionAttributes))
                                .build();
                validateInterceptorResult(context, result, interceptors.get(i));
            }
            return result;
        });
    }

    public void beforeUnmarshalling(InterceptorContext context, ExecutionAttributes executionAttributes) {
        tryOrThrowInterceptorException(() -> reverseForEach(i -> i.beforeUnmarshalling(context, executionAttributes)));
    }

    public void afterUnmarshalling(InterceptorContext context, ExecutionAttributes executionAttributes) {
        reverseForEach(i -> i.afterUnmarshalling(context, executionAttributes));
    }

    public InterceptorContext modifyResponse(InterceptorContext context, ExecutionAttributes executionAttributes) {
        return tryOrThrowInterceptorException(() -> {
            InterceptorContext result = context;
            for (int i = interceptors.size() - 1; i >= 0; i--) {
                result = context.toBuilder()
                                .response(interceptors.get(i).modifyResponse(context, executionAttributes))
                                .build();
                validateInterceptorResult(context, result, interceptors.get(i));
            }
            return result;
        });
    }

    public void afterExecution(InterceptorContext context, ExecutionAttributes executionAttributes) {
        tryOrThrowInterceptorException(() -> reverseForEach(i -> i.afterExecution(context, executionAttributes)));
    }

    public void onExecutionFailure(DefaultFailedExecutionInterceptorContext context, ExecutionAttributes executionAttributes) {
        tryOrThrowInterceptorException(() -> interceptors.forEach(i -> i.onExecutionFailure(context, executionAttributes)));
    }

    private void validateInterceptorResult(InterceptorContext originalContext, InterceptorContext newContext,
                                           ExecutionInterceptor interceptor) {
        Validate.validState(newContext != null,
                            "Request interceptor '%s' returned null from its modifyRequest interceptor.",
                            interceptor);
        Validate.isInstanceOf(originalContext.request().getClass(), newContext.request(),
                              "Request interceptor '%s' returned '%s' from modifyRequest, but '%s' was expected.",
                              interceptor, newContext.request().getClass(), originalContext.request().getClass());
    }

    private <T> T tryOrThrowInterceptorException(Supplier<T> function) {
        try {
            return function.get();
        } catch (ExecutionInterceptorException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ExecutionInterceptorException("An exception was raised by an execution interceptor.", e);
        }
    }

    private void tryOrThrowInterceptorException(Runnable function) {
        tryOrThrowInterceptorException(() -> {
            function.run();
            return null;
        });
    }

    private void reverseForEach(Consumer<ExecutionInterceptor> action) {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            action.accept(interceptors.get(i));
        }
    }
}
