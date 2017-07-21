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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
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
import software.amazon.awssdk.interceptor.context.AfterExecutionContext;
import software.amazon.awssdk.interceptor.context.AfterMarshallingContext;
import software.amazon.awssdk.interceptor.context.AfterTransmissionContext;
import software.amazon.awssdk.interceptor.context.AfterUnmarshallingContext;
import software.amazon.awssdk.interceptor.context.BeforeExecutionContext;
import software.amazon.awssdk.interceptor.context.BeforeMarshallingContext;
import software.amazon.awssdk.interceptor.context.BeforeTransmissionContext;
import software.amazon.awssdk.interceptor.context.BeforeUnmarshallingContext;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.protocolrestjson.ProtocolRestJsonAsyncClient;
import software.amazon.awssdk.services.protocolrestjson.ProtocolRestJsonClient;
import software.amazon.awssdk.services.protocolrestjson.model.AllTypesRequest;
import software.amazon.awssdk.services.protocolrestjson.model.AllTypesResponse;
import software.amazon.awssdk.services.protocolrestjson.model.HeadOperationRequest;

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
        ExecutionInterceptor interceptor = mock(ExecutionInterceptorImpl.class, CALLS_REAL_METHODS);
        ProtocolRestJsonClient client = client(interceptor);
        AllTypesRequest request = AllTypesRequest.builder().build();

        stubFor(post(urlPathEqualTo(ALL_TYPES_PATH)).willReturn(aResponse().withStatus(200).withBody("")));

        // When
        AllTypesResponse result = client.allTypes(request);

        // Expect
        expectAllMethodsCalled(interceptor);
    }

    @Test
    public void successInterceptorMethodsCalledWithAsyncClient() throws ExecutionException, InterruptedException {
        // Given
        ExecutionInterceptor interceptor = mock(ExecutionInterceptorImpl.class, CALLS_REAL_METHODS);
        ProtocolRestJsonAsyncClient client = asyncClient(interceptor);
        AllTypesRequest request = AllTypesRequest.builder().build();

        stubFor(post(urlPathEqualTo(ALL_TYPES_PATH)).willReturn(aResponse().withStatus(200).withBody("")));

        // When
        AllTypesResponse result = client.allTypes(request).get();

        // Expect
        expectAllMethodsCalled(interceptor);
    }

    @Test
    public void exceptionRequestHandlerMethodsCalled() {
        // Given
        ExecutionInterceptor interceptor = mock(ExecutionInterceptor.class, Mockito.CALLS_REAL_METHODS);
        ProtocolRestJsonClient client = client(interceptor);
        HeadOperationRequest request = HeadOperationRequest.builder().build();

        // When
        try {
            client.headOperation(request);
            fail();
        } catch (Exception e) {
            // Expected
        }

        // Expect
        InOrder inOrder = Mockito.inOrder(interceptor);
        inOrder.verify(interceptor).beforeExecution(any(), any()); // TODO: Validate input
        inOrder.verify(interceptor).modifyRequest(any(), any());
        inOrder.verify(interceptor).beforeMarshalling(any(), any());
        inOrder.verify(interceptor).afterMarshalling(any(), any());
        inOrder.verify(interceptor).modifyHttpRequest(any(), any());
        inOrder.verify(interceptor).beforeTransmission(any(), any());
        inOrder.verify(interceptor).onExecutionFailure(any(), any());
        verifyNoMoreInteractions(interceptor);
    }

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

    private SdkRequest CHANGED_REQUEST = AllTypesRequest.builder().build();
    private SdkHttpFullRequest CHANGED_HTTP_REQUEST = SdkHttpFullRequest.builder().build();
    private SdkHttpFullResponse CHANGED_HTTP_RESPONSE = SdkHttpFullResponse.builder().build();
    private SdkResponse CHANGED_RESPONSE = AllTypesResponse.builder().build();

    private void expectAllMethodsCalled(ExecutionInterceptor interceptor,
                                        SdkRequest request, SdkResponse response, ExecutionAttributes attributes) {
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

        InOrder inOrder = Mockito.inOrder(interceptor);
        inOrder.verify(interceptor).beforeExecution(beforeExecutionArg.capture(), eq(attributes));
        inOrder.verify(interceptor).modifyRequest(modifyRequestArg.capture(), eq(attributes));
        inOrder.verify(interceptor).beforeMarshalling(beforeMarshallingArg.capture(), eq(attributes));
        inOrder.verify(interceptor).afterMarshalling(afterMarshallingArg.capture(), eq(attributes));
        inOrder.verify(interceptor).modifyHttpRequest(modifyHttpRequestArg.capture(), eq(attributes));
        inOrder.verify(interceptor).beforeTransmission(beforeTransmissionArg.capture(), eq(attributes));
        inOrder.verify(interceptor).afterTransmission(afterTransmissionArg.capture(), eq(attributes));
        inOrder.verify(interceptor).modifyHttpResponse(modifyHttpResponseArg.capture(), eq(attributes));
        inOrder.verify(interceptor).beforeUnmarshalling(beforeUnmarshallingArg.capture(), eq(attributes));
        inOrder.verify(interceptor).afterUnmarshalling(afterUnmarshallingArg.capture(), eq(attributes));
        inOrder.verify(interceptor).modifyResponse(modifyResponseArg.capture(), eq(attributes));
        inOrder.verify(interceptor).afterExecution(afterExecutionArg.capture(), eq(attributes));
        verifyNoMoreInteractions(interceptor);
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

    private static class ExecutionInterceptorImpl implements ExecutionInterceptor {
    }
}
