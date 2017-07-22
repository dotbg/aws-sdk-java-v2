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

package software.amazon.awssdk.protocol.tests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import software.amazon.awssdk.AmazonServiceException;
import software.amazon.awssdk.SdkRequest;
import software.amazon.awssdk.SdkResponse;
import software.amazon.awssdk.auth.AwsCredentials;
import software.amazon.awssdk.auth.StaticCredentialsProvider;
import software.amazon.awssdk.client.builder.ClientBuilder;
import software.amazon.awssdk.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpFullResponse;
import software.amazon.awssdk.interceptor.ExecutionAttributes;
import software.amazon.awssdk.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.interceptor.ExecutionInterceptorException;
import software.amazon.awssdk.interceptor.context.AfterExecutionContext;
import software.amazon.awssdk.interceptor.context.AfterMarshallingContext;
import software.amazon.awssdk.interceptor.context.AfterTransmissionContext;
import software.amazon.awssdk.interceptor.context.AfterUnmarshallingContext;
import software.amazon.awssdk.interceptor.context.BeforeExecutionContext;
import software.amazon.awssdk.interceptor.context.BeforeMarshallingContext;
import software.amazon.awssdk.interceptor.context.BeforeTransmissionContext;
import software.amazon.awssdk.interceptor.context.BeforeUnmarshallingContext;
import software.amazon.awssdk.interceptor.context.FailedExecutionContext;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.protocolrestjson.ProtocolRestJsonAsyncClient;
import software.amazon.awssdk.services.protocolrestjson.ProtocolRestJsonClient;
import software.amazon.awssdk.services.protocolrestjson.model.AllTypesRequest;
import software.amazon.awssdk.services.protocolrestjson.model.AllTypesResponse;

/**
 * Verify that request handler hooks are behaving as expected.
 */
public class ExecutionInterceptorTest {
    @Rule
    public WireMockRule wireMock = new WireMockRule(0);

    private static final String ALL_TYPES_PATH = "/2016-03-11/allTypes";

    @Test
    public void successInterceptorMethodsCalledWithSyncClient() {
        // Given
        ExecutionInterceptor interceptor = mock(MessageUpdatingInterceptor.class, CALLS_REAL_METHODS);

        ProtocolRestJsonClient client = client(interceptor);
        AllTypesRequest request = AllTypesRequest.builder().build();
        stubFor(post(urlPathEqualTo(ALL_TYPES_PATH)).willReturn(aResponse().withStatus(200).withBody("")));

        // When
        AllTypesResponse result = client.allTypes(request);

        // Expect
        expectAllMethodsCalled(interceptor, request, result);
    }

    @Test
    public void successInterceptorMethodsCalledWithAsyncClient() throws ExecutionException, InterruptedException {
        // Given
        ExecutionInterceptor interceptor = mock(MessageUpdatingInterceptor.class, CALLS_REAL_METHODS);

        ProtocolRestJsonAsyncClient client = asyncClient(interceptor);
        AllTypesRequest request = AllTypesRequest.builder().build();
        stubFor(post(urlPathEqualTo(ALL_TYPES_PATH)).willReturn(aResponse().withStatus(200).withBody("")));

        // When
        AllTypesResponse result = client.allTypes(request).get();

        // Expect
        expectAllMethodsCalled(interceptor, request, result);
    }

    @Test
    public void serviceExceptionCallsOnFailureWithSyncClient() {
        // Given
        ExecutionInterceptor interceptor = mock(MessageUpdatingInterceptor.class, CALLS_REAL_METHODS);

        ProtocolRestJsonClient client = client(interceptor);
        AllTypesRequest request = AllTypesRequest.builder().build();

        // When
        assertThatExceptionOfType(AmazonServiceException.class).isThrownBy(() -> client.allTypes(request));

        // Expect
        expectServiceCallErrorMethodsCalled(interceptor);
    }

    @Test
    public void serviceExceptionCallsOnFailureWithAsyncClient() throws ExecutionException, InterruptedException {
        // Given
        ExecutionInterceptor interceptor = mock(MessageUpdatingInterceptor.class, CALLS_REAL_METHODS);

        ProtocolRestJsonAsyncClient client = asyncClient(interceptor);
        AllTypesRequest request = AllTypesRequest.builder().build();

        // When
        client.allTypes(request).exceptionally(t -> {
            assertThat(t).isInstanceOf(CompletionException.class);
            assertThat(t.getCause()).isInstanceOf(AmazonServiceException.class);
            return null;
        }).get();

        // Expect
        expectServiceCallErrorMethodsCalled(interceptor);
    }

    //
//    @Test
//    public void exceptionRequestHandlerMethodsCalled() {
//        // Given
//        ExecutionInterceptor interceptor = mock(ExecutionInterceptor.class, Mockito.CALLS_REAL_METHODS);
//        ProtocolRestJsonClient client = client(interceptor);
//        HeadOperationRequest request = HeadOperationRequest.builder().build();
//
//        // When
//        try {
//            client.headOperation(request);
//            fail();
//        } catch (Exception e) {
//            // Expected
//        }
//
//        // Expect
//        InOrder inOrder = Mockito.inOrder(interceptor);
//        inOrder.verify(interceptor).beforeExecution(any(), any()); // TODO: Validate input
//        inOrder.verify(interceptor).modifyRequest(any(), any());
//        inOrder.verify(interceptor).beforeMarshalling(any(), any());
//        inOrder.verify(interceptor).afterMarshalling(any(), any());
//        inOrder.verify(interceptor).modifyHttpRequest(any(), any());
//        inOrder.verify(interceptor).beforeTransmission(any(), any());
//        inOrder.verify(interceptor).onExecutionFailure(any(), any());
//        verifyNoMoreInteractions(interceptor);
//    }

//    @Test
//    public void clientUsesCopiedRequests() {
//        // Given
//        ExecutionInterceptor interceptor = mock(ExecutionInterceptor.class, Mockito.CALLS_REAL_METHODS);
//        ProtocolRestJsonClient client = client(interceptor);
//        AllTypesRequest inputRequest = AllTypesRequest.builder().build();
//        AllTypesRequest beforeExecutionRequest = AllTypesRequest.builder().build();
//        AllTypesRequest beforeMarshallingRequest = AllTypesRequest.builder().build();
//
//        when(interceptor.beforeExecution(eq(inputRequest))).thenReturn(beforeExecutionRequest);
//        when(interceptor.beforeMarshalling(eq(beforeExecutionRequest))).thenReturn(beforeMarshallingRequest);
//
//        stubFor(post(urlPathEqualTo(ALL_TYPES_PATH)).willReturn(aResponse().withStatus(200).withBody("")));
//
//        // When
//        AllTypesResult result = client.allTypes(inputRequest);
//
//        // Expect
//        InOrder inOrder = Mockito.inOrder(interceptor);
//        inOrder.verify(interceptor).beforeExecution(inputRequest);
//        inOrder.verify(interceptor).beforeMarshalling(beforeExecutionRequest);
//    }
//
//    @Test
//    public void clientPropagatesRequestHandlerValues() {
//        // Given
//        HandlerContextKey<String> contextKey = new HandlerContextKey<>("");
//        ExecutionInterceptor interceptor = mock(ExecutionInterceptor.class, Mockito.CALLS_REAL_METHODS);
//        ProtocolRestJsonClient client = client(interceptor);
//        AllTypesRequest request = AllTypesRequest.builder().build();
//
//        request.addHandlerContext(contextKey, "Value");
//        stubFor(post(urlPathEqualTo(ALL_TYPES_PATH)).willReturn(aResponse().withStatus(200).withBody("")));
//
//        // When
//        AllTypesResult result = client.allTypes(request);
//
//        // Expect
//        ArgumentCaptor<Request> httpRequestCaptor = ArgumentCaptor.forClass(Request.class);
//        verify(interceptor).afterResponse(httpRequestCaptor.capture(), any());
//        Request<?> httpRequest = httpRequestCaptor.getValue();
//        assertThat(httpRequest.getHandlerContext(contextKey), equalTo("Value"));
//    }

    private void expectAllMethodsCalled(ExecutionInterceptor interceptor,
                                        SdkRequest inputRequest, SdkResponse outputResponse) {
        ArgumentCaptor<ExecutionAttributes> attributes = ArgumentCaptor.forClass(ExecutionAttributes.class);

        ArgumentCaptor<BeforeExecutionContext> beforeExecutionArg = ArgumentCaptor.forClass(BeforeExecutionContext.class);
        ArgumentCaptor<BeforeMarshallingContext> modifyRequestArg = ArgumentCaptor.forClass(BeforeMarshallingContext.class);
        ArgumentCaptor<BeforeMarshallingContext> beforeMarshallingArg = ArgumentCaptor.forClass(BeforeMarshallingContext.class);
        ArgumentCaptor<AfterMarshallingContext> afterMarshallingArg = ArgumentCaptor.forClass(AfterMarshallingContext.class);
        ArgumentCaptor<BeforeTransmissionContext> modifyHttpRequestArg = ArgumentCaptor.forClass(BeforeTransmissionContext.class);
        ArgumentCaptor<BeforeTransmissionContext> beforeTransmissionArg = ArgumentCaptor.forClass(BeforeTransmissionContext.class);
        ArgumentCaptor<AfterTransmissionContext> afterTransmissionArg = ArgumentCaptor.forClass(AfterTransmissionContext.class);
        ArgumentCaptor<BeforeUnmarshallingContext> modifyHttpResponseArg = ArgumentCaptor.forClass(BeforeUnmarshallingContext.class);
        ArgumentCaptor<BeforeUnmarshallingContext> beforeUnmarshallingArg = ArgumentCaptor.forClass(BeforeUnmarshallingContext.class);
        ArgumentCaptor<AfterUnmarshallingContext> afterUnmarshallingArg = ArgumentCaptor.forClass(AfterUnmarshallingContext.class);
        ArgumentCaptor<AfterExecutionContext> modifyResponseArg = ArgumentCaptor.forClass(AfterExecutionContext.class);
        ArgumentCaptor<AfterExecutionContext> afterExecutionArg = ArgumentCaptor.forClass(AfterExecutionContext.class);

        // Verify methods are called in the right order
        InOrder inOrder = Mockito.inOrder(interceptor);
        inOrder.verify(interceptor).beforeExecution(beforeExecutionArg.capture(), attributes.capture());
        inOrder.verify(interceptor).modifyRequest(modifyRequestArg.capture(), attributes.capture());
        inOrder.verify(interceptor).beforeMarshalling(beforeMarshallingArg.capture(), attributes.capture());
        inOrder.verify(interceptor).afterMarshalling(afterMarshallingArg.capture(), attributes.capture());
        inOrder.verify(interceptor).modifyHttpRequest(modifyHttpRequestArg.capture(), attributes.capture());
        inOrder.verify(interceptor).beforeTransmission(beforeTransmissionArg.capture(), attributes.capture());
        inOrder.verify(interceptor).afterTransmission(afterTransmissionArg.capture(), attributes.capture());
        inOrder.verify(interceptor).modifyHttpResponse(modifyHttpResponseArg.capture(), attributes.capture());
        inOrder.verify(interceptor).beforeUnmarshalling(beforeUnmarshallingArg.capture(), attributes.capture());
        inOrder.verify(interceptor).afterUnmarshalling(afterUnmarshallingArg.capture(), attributes.capture());
        inOrder.verify(interceptor).modifyResponse(modifyResponseArg.capture(), attributes.capture());
        inOrder.verify(interceptor).afterExecution(afterExecutionArg.capture(), attributes.capture());
        verifyNoMoreInteractions(interceptor);

        // Verify beforeExecution gets untouched request
        assertThat(beforeExecutionArg.getValue().request()).isSameAs(inputRequest);

        // Verify methods were given correct parameters
        validateArgs(beforeExecutionArg.getValue(), null);
        validateArgs(modifyRequestArg.getValue(), null);
        validateArgs(beforeMarshallingArg.getValue(), "1");
        validateArgs(afterMarshallingArg.getValue(), "1", null);
        validateArgs(modifyHttpRequestArg.getValue(), "1", null);
        validateArgs(beforeTransmissionArg.getValue(), "1", "2");
        validateArgs(afterTransmissionArg.getValue(), "1", "2", null);
        validateArgs(modifyHttpResponseArg.getValue(), "1", "2", null);
        validateArgs(beforeUnmarshallingArg.getValue(), "1", "2", "3");
        validateArgs(afterUnmarshallingArg.getValue(), "1", "2", "3", null);
        validateArgs(modifyResponseArg.getValue(), "1", "2", "3", null);
        validateArgs(afterExecutionArg.getValue(), "1", "2", "3", "4");

        // Verify afterExecution gets same response as the one returned by client
        assertThat(afterExecutionArg.getValue().response()).isSameAs(outputResponse);

        // Verify same execution attributes were used for all method calls
        assertThat(attributes.getAllValues()).containsOnly(attributes.getAllValues().get(0));
    }

    private void expectServiceCallErrorMethodsCalled(ExecutionInterceptor interceptor) {
        ArgumentCaptor<ExecutionAttributes> attributes = ArgumentCaptor.forClass(ExecutionAttributes.class);
        ArgumentCaptor<BeforeUnmarshallingContext> beforeUnmarshallingArg = ArgumentCaptor.forClass(BeforeUnmarshallingContext.class);
        ArgumentCaptor<FailedExecutionContext> failedExecutionArg = ArgumentCaptor.forClass(FailedExecutionContext.class);

        InOrder inOrder = Mockito.inOrder(interceptor);
        inOrder.verify(interceptor).beforeExecution(any(), attributes.capture());
        inOrder.verify(interceptor).modifyRequest(any(), attributes.capture());
        inOrder.verify(interceptor).beforeMarshalling(any(), attributes.capture());
        inOrder.verify(interceptor).afterMarshalling(any(), attributes.capture());
        inOrder.verify(interceptor).modifyHttpRequest(any(), attributes.capture());
        inOrder.verify(interceptor).beforeTransmission(any(), attributes.capture());
        inOrder.verify(interceptor).afterTransmission(any(), attributes.capture());
        inOrder.verify(interceptor).modifyHttpResponse(any(), attributes.capture());
        inOrder.verify(interceptor).beforeUnmarshalling(beforeUnmarshallingArg.capture(), attributes.capture());
        inOrder.verify(interceptor).onExecutionFailure(failedExecutionArg.capture(), attributes.capture());
        verifyNoMoreInteractions(interceptor);

        // Verify same execution attributes were used for all method calls
        assertThat(attributes.getAllValues()).containsOnly(attributes.getAllValues().get(0));

        // Verify HTTP response
        assertThat(beforeUnmarshallingArg.getValue().httpResponse().getStatusCode()).isEqualTo(404);

        // Verify failed execution parameters
        AllTypesRequest failedRequest = (AllTypesRequest) failedExecutionArg.getValue().request();

        assertThat(failedExecutionArg.getValue().exception()).isInstanceOf(AmazonServiceException.class);
        assertThat(failedRequest.stringMember()).isEqualTo("1");
        assertThat(failedExecutionArg.getValue().httpRequest()).hasValueSatisfying(httpRequest -> {
            assertThat(httpRequest.getFirstHeaderValue("Foo")).hasValue("2");
        });
        assertThat(failedExecutionArg.getValue().httpResponse()).hasValueSatisfying(httpResponse -> {
            assertThat(httpResponse.getFirstHeaderValue("Foo")).hasValue("3");
        });
        assertThat(failedExecutionArg.getValue().response()).isNotPresent();
    }

    private void validateArgs(BeforeExecutionContext context,
                              String expectedStringMemberValue) {
        AllTypesRequest request = (AllTypesRequest) context.request();
        assertThat(request.stringMember()).isEqualTo(expectedStringMemberValue);
    }

    private void validateArgs(AfterMarshallingContext context,
                              String expectedStringMemberValue, String expectedFooHeaderValue) {
        validateArgs(context, expectedStringMemberValue);
        assertThat(context.httpRequest().getFirstHeaderValue("Foo")).isEqualTo(Optional.ofNullable(expectedFooHeaderValue));
    }

    private void validateArgs(AfterTransmissionContext context,
                              String expectedStringMemberValue, String expectedFooHeaderValue,
                              String expectedResponseFooHeaderValue) {
        validateArgs(context, expectedStringMemberValue, expectedFooHeaderValue);
        assertThat(context.httpResponse().getFirstHeaderValue("Foo")).isEqualTo(Optional.ofNullable(expectedResponseFooHeaderValue));
    }

    private void validateArgs(AfterUnmarshallingContext context,
                              String expectedStringMemberValue, String expectedFooHeaderValue,
                              String expectedResponseFooHeaderValue, String expectedResponseStringMemberValue) {
        validateArgs(context, expectedStringMemberValue, expectedFooHeaderValue, expectedResponseFooHeaderValue);
        AllTypesResponse response = (AllTypesResponse) context.response();
        assertThat(response.stringMember()).isEqualTo(expectedResponseStringMemberValue);
    }

    private ProtocolRestJsonClient client(ExecutionInterceptor interceptor) {
        return initializeAndBuild(ProtocolRestJsonClient.builder(), interceptor);
    }

    private ProtocolRestJsonAsyncClient asyncClient(ExecutionInterceptor interceptor) {
        return initializeAndBuild(ProtocolRestJsonAsyncClient.builder(), interceptor);
    }

    private <T extends ClientBuilder<?, U>, U> U initializeAndBuild(T builder, ExecutionInterceptor interceptor) {
        return builder.region(Region.US_WEST_1)
                      .endpointOverride(URI.create("http://localhost:" + wireMock.port()))
                      .credentialsProvider(new StaticCredentialsProvider(new AwsCredentials("akid", "skid")))
                      .overrideConfiguration(ClientOverrideConfiguration.builder()
                                                                        .addExecutionInterceptor(interceptor)
                                                                        .build())
                      .build();
    }

    private static class MessageUpdatingInterceptor implements ExecutionInterceptor {
        @Override
        public SdkRequest modifyRequest(BeforeMarshallingContext execution, ExecutionAttributes executionAttributes)
                throws ExecutionInterceptorException {
            AllTypesRequest request = (AllTypesRequest) execution.request();
            return request.modify(b -> b.stringMember("1"));
        }

        @Override
        public SdkHttpFullRequest modifyHttpRequest(BeforeTransmissionContext execution, ExecutionAttributes executionAttributes)
                throws ExecutionInterceptorException {
            SdkHttpFullRequest httpRequest = execution.httpRequest();
            return httpRequest.modify(b -> b.header("Foo", "2"));
        }

        @Override
        public SdkHttpFullResponse modifyHttpResponse(BeforeUnmarshallingContext execution,
                                                      ExecutionAttributes executionAttributes)
                throws ExecutionInterceptorException {
            SdkHttpFullResponse httpResponse = execution.httpResponse();
            return httpResponse.modify(b -> b.addHeader("Foo", Collections.singletonList("3")));
        }

        @Override
        public SdkResponse modifyResponse(AfterExecutionContext execution, ExecutionAttributes executionAttributes)
                throws ExecutionInterceptorException {
            AllTypesResponse response = (AllTypesResponse) execution.response();
            return response.modify(b -> b.stringMember("4"));
        }
    }
}
