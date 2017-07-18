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

package software.amazon.awssdk.services.dynamodb;

import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.test.AwsIntegrationTestBase;

public class ExecutionInterceptorIntegrationTest extends AwsIntegrationTestBase {

// TODO: Protocol test this stuff
//    private static DynamoDBClient ddb;
//
//    private ExecutionInterceptor mockExecutionInterceptor;
//
//    @Before
//    public void setupFixture() {
//        mockExecutionInterceptor = spy(new ExecutionInterceptor() {
//        });
//        ddb = DynamoDBClient.builder()
//                .credentialsProvider(CREDENTIALS_PROVIDER_CHAIN)
//                .overrideConfiguration(ClientOverrideConfiguration.builder().addExecutionInterceptor(mockExecutionInterceptor).build())
//                .build();
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        ddb.close();
//    }
//
//    @Test
//    public void successfulRequest_InvokesAllSuccessCallbacks() {
//        ddb.listTables(ListTablesRequest.builder().build());
//
//        verify(mockExecutionInterceptor).beforeMarshalling(any(AmazonWebServiceRequest.class));
//        verify(mockExecutionInterceptor).beforeRequest(any(SdkHttpFullRequest.class));
//        verify(mockExecutionInterceptor).beforeUnmarshalling(any(SdkHttpFullRequest.class), any(HttpResponse.class));
//        verify(mockExecutionInterceptor).afterResponse(any(SdkHttpFullRequest.class), any(Response.class));
//    }
//
//    @Test
//    public void successfulRequest_BeforeMarshalling_ReplacesOriginalRequest() {
//        ListTablesRequest originalRequest = ListTablesRequest.builder().build();
//        ListTablesRequest spiedRequest = spy(originalRequest);
//        when(mockExecutionInterceptor.beforeMarshalling(eq(originalRequest))).thenReturn(spiedRequest);
//
//        ddb.listTables(originalRequest);
//
//        verify(mockExecutionInterceptor).beforeMarshalling(any(AmazonWebServiceRequest.class));
//        // Asserts that the request is actually replaced with what's returned by beforeMarshalling
//        verify(spiedRequest).exclusiveStartTableName();
//    }
//
//    @Test
//    public void failedRequest_InvokesAllErrorCallbacks() {
//        try {
//            ddb.describeTable(DescribeTableRequest.builder().tableName("some-nonexistent-table-name").build());
//        } catch (AmazonServiceException expected) {
//            // Ignored or expected.
//        }
//
//        // Before callbacks should always be called
//        verify(mockExecutionInterceptor).beforeMarshalling(any(AmazonWebServiceRequest.class));
//        verify(mockExecutionInterceptor).beforeRequest(any(SdkHttpFullRequest.class));
//        verify(mockExecutionInterceptor).afterError(any(SdkHttpFullRequest.class), any(Response.class), any(Exception.class));
//    }
//
//    /**
//     * Asserts that changing the {@link HttpResponse} during the beforeUnmarshalling callback has an
//     * affect on the final unmarshalled response
//     */
//    @Test
//    public void beforeUnmarshalling_ModificationsToHttpResponse_AreReflectedInUnmarshalling() {
//        final String injectedTableName = "SomeInjectedTableName";
//        ExecutionInterceptor executionInterceptor = new ExecutionInterceptor() {
//            @Override
//            public SdkHttpFullResponse modifyHttpResponse(AfterTransmissionContext execution,
//                                                          ExecutionAttributes executionAttributes) {
//                try {
//                    String newContent = "{\"TableNames\":[\"" + injectedTableName + "\"]}";
//                    return execution.httpResponse()
//                                    .toBuilder()
//                                    .content(new StringInputStream(newContent))
//                                    .addHeader("Content-Length", Collections.singletonList(String.valueOf(newContent.length())))
//                                    .build();
//                } catch (UnsupportedEncodingException e) {
//                    throw new UncheckedIOException(e);
//                }
//
//            }
//        };
//        DynamoDBClient ddb = DynamoDBClient.builder()
//                                           .credentialsProvider(CREDENTIALS_PROVIDER_CHAIN)
//                                           .overrideConfiguration(ClientOverrideConfiguration.builder()
//                                                                                             .addExecutionInterceptor(executionInterceptor)
//                                                                                             .build())
//                                           .build();
//
//        ListTablesResponse result = ddb.listTables(ListTablesRequest.builder().build());
//        // Assert that the unmarshalled response contains our injected table name and not the actual
//        // list of tables
//        assertThat(result.tableNames().toArray(new String[0]), arrayContaining(injectedTableName));
//    }

}
