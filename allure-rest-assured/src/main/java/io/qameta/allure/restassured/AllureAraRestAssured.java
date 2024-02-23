/*
 *  Copyright 2019 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure.restassured;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.ara.ARAttachmentDto;
import io.qameta.allure.ara.ExceptionReactionDto;
import io.qameta.allure.ara.AbstractReactionDto;
import io.qameta.allure.ara.openapi.OpenApiActionDto;
import io.qameta.allure.ara.openapi.OpenApiReactionDto;
import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Allure logger filter for Rest-assured.
 */
public class AllureAraRestAssured implements OrderedFilter {

    private static final String PREFIX_FILE_MIME_TYPE = "application/octet-stream";

    private final AllureLifecycle lifecycle;
    private final ObjectWriter objectWriter;

    public AllureAraRestAssured() {
        this.lifecycle = Allure.getLifecycle();
        this.objectWriter = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writerWithDefaultPrettyPrinter();
    }

    @Override
    public Response filter(final FilterableRequestSpecification requestSpec,
                           final FilterableResponseSpecification responseSpec,
                           final FilterContext filterContext) {
        AbstractReactionDto reactionDto = null;
        try {
            final Response restAssuredResponse = filterContext.next(requestSpec, responseSpec);

            reactionDto = new OpenApiReactionDto(
                    restAssuredResponse.statusCode(),
                    buildApiResponses(restAssuredResponse)
            );

            return restAssuredResponse;
        } catch (Throwable tr) {
            reactionDto = new ExceptionReactionDto(
                    tr.getMessage(),
                    Base64.getEncoder().encodeToString(
                            ExceptionUtils.getStackTrace(tr).getBytes(UTF_8)
                    )
            );
            throw new RuntimeException(tr);
        } finally {
            try {
                final ARAttachmentDto arAttachmentDto = new ARAttachmentDto(
                        new OpenApiActionDto(
                                requestSpec.getBaseUri(),
                                parseUri(requestSpec).getPath(),
                                buildPathItem(requestSpec)
                        ),
                        reactionDto
                );
                attachAra(requestSpec, arAttachmentDto);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    private void attachAra(
            final FilterableRequestSpecification requestSpec,
            final ARAttachmentDto arAttachmentDto
    ) throws JsonProcessingException {
        lifecycle.addAttachment(
                "Interaction - " + requestSpec.getURI(),
                "application/json",
                ".json",
                objectWriter.writeValueAsString(arAttachmentDto).getBytes(UTF_8)
        );
    }

    private ApiResponses buildApiResponses(Response restAssuredResponse) {
        final Map<String, Header> headersMap = convertMapToHeadersMap(restAssuredResponse.headers());
        final ApiResponse apiResponse = new ApiResponse();
        apiResponse.setHeaders(headersMap);
        apiResponse.setDescription(restAssuredResponse.statusLine());

        final Example responseBodyExample;
        if (restAssuredResponse.body() != null) {
            responseBodyExample = new Example().value(
                    Base64.getEncoder().encode(
                            restAssuredResponse.asString().getBytes(UTF_8)
                    )
            );
        } else {
            responseBodyExample = null;
        }

        final MediaType mediaType = new MediaType();
        mediaType.setExample(responseBodyExample);
        if (StringUtils.isNoneBlank(restAssuredResponse.getContentType())) {
            apiResponse.setContent(
                    new Content().addMediaType(
                            restAssuredResponse.getContentType(),
                            mediaType
                    )
            );
        }

        final ApiResponses apiResponses = new ApiResponses();
        apiResponses.addApiResponse(
                String.valueOf(restAssuredResponse.statusCode()),
                apiResponse
        );
        return apiResponses;
    }

    private PathItem buildPathItem(FilterableRequestSpecification requestSpec) {
        final Operation openApiOperation = new Operation();

        final List<Parameter> parameters = new LinkedList<>();
        requestSpec.getHeaders().forEach(
                header -> {
                    parameters.add(
                            new Parameter()
                                    .in(ParameterIn.HEADER.toString())
                                    .name(header.getName())
                                    .schema(new StringSchema())
                                    .example(header.getValue())
                    );
                }
        );

        requestSpec.getQueryParams().forEach(
                (k, v) -> {
                    parameters.add(
                            new Parameter()
                                    .in(ParameterIn.QUERY.toString())
                                    .name(k)
                                    .schema(new StringSchema())
                                    .example(v)
                    );
                }
        );

        requestSpec.getNamedPathParams().forEach(
                (k, v) -> {
                    parameters.add(
                            new Parameter()
                                    .in(ParameterIn.PATH.toString())
                                    .name(k)
                                    .schema(new StringSchema())
                                    .example(v)
                    );
                }
        );

        requestSpec.getCookies().forEach(
                cookie -> {
                    parameters.add(
                            new Parameter()
                                    .in(ParameterIn.COOKIE.toString())
                                    .name(cookie.getName())
                                    .schema(new StringSchema())
                                    .example(cookie.getValue())
                    );
                }
        );

        openApiOperation.setParameters(parameters);

        try {
            final Example bodyExample;
            if (requestSpec.getBody() != null) {
                bodyExample = new Example().value(
                        Base64.getEncoder().encode(
                                objectWriter.writeValueAsString(
                                        requestSpec.getBody()
                                ).getBytes(UTF_8)
                        )
                );
                MediaType mediaType = new MediaType();
                mediaType.setExample(bodyExample);
                openApiOperation.setRequestBody(
                        new RequestBody().content(
                                new Content().addMediaType(
                                        requestSpec.getContentType(),
                                        mediaType
                                )
                        )
                );
            } else if (requestSpec.getMultiPartParams() != null && !requestSpec.getMultiPartParams().isEmpty()) {
                enrichWithMultiPartSpecs(requestSpec, openApiOperation);
            } else {

                final String contentTypeHeader = requestSpec.getHeaders().getValue("Content-Type");
                if (StringUtils.isNoneBlank(contentTypeHeader)) {
                    openApiOperation.setRequestBody(
                            new RequestBody().content(
                                    new Content().addMediaType(
                                            requestSpec.getContentType(),
                                            new MediaType()
                                    )
                            )
                    );
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        final PathItem pathItem = new PathItem();
        pathItem.operation(
                PathItem.HttpMethod.valueOf(requestSpec.getMethod()),
                openApiOperation
        );

        return pathItem;
    }

    private void enrichWithMultiPartSpecs(
            final FilterableRequestSpecification requestSpec,
            final Operation openApiOperation
    ) {
        final ObjectSchema partsSchema = new ObjectSchema();

        final HashMap<String, Encoding> partsEncoding = new HashMap<>();

        final MediaType mediaType = new MediaType();
        mediaType.schema(partsSchema);
        mediaType.encoding(partsEncoding);

        openApiOperation.setRequestBody(
                new RequestBody().content(
                        new Content().addMediaType("multipart/form-data", mediaType)
                )
        );

        requestSpec.getMultiPartParams().forEach(
                multipart -> {
                    final Schema partSchema;
                    final Example partExample = new Example();
                    if (multipart.hasFileName() && multipart.getMimeType().startsWith(PREFIX_FILE_MIME_TYPE)) {
                        partSchema = new BinarySchema();
                        partExample.description(multipart.getFileName());
                    } else {
                        partSchema = new StringSchema();
                        if (multipart.getContent() != null) {
                            try {
                                partSchema.setExample(
                                        objectWriter.canSerialize(multipart.getContent().getClass())
                                        ? Base64.getEncoder().encode(
                                        objectWriter.writeValueAsBytes(multipart.getContent())
                                )
                                        : String.valueOf(multipart.getContent()).getBytes(UTF_8)
                                );
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    partsSchema.addProperty(
                            multipart.getControlName(),
                            partSchema
                    );
                    partsEncoding.computeIfAbsent(
                            multipart.getControlName(),
                            k -> new Encoding().headers(convertMapToHeadersMap(multipart.getHeaders()))
                    );
                }
        );
    }

    private URI parseUri(FilterableRequestSpecification requestSpec) {
        try {
            return new URI(requestSpec.getURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Header> convertMapToHeadersMap(Headers headers) {
        final Map<String, Header> headersAsMap = new LinkedHashMap<>();
        headers.forEach(
                h -> {
                    headersAsMap.computeIfAbsent(
                            h.getName(),
                            name -> {
                                Header header = new Header();
                                header.setExample(
                                        new Example().value(h.getValue())
                                );
                                return header;
                            }
                    );
                }
        );
        return headersAsMap;
    }

    private Map<String, Header> convertMapToHeadersMap(Map<String, String> headers) {
        final Map<String, Header> headersAsMap = new LinkedHashMap<>();
        headers.forEach(
                (hk, hv) -> {
                    headersAsMap.computeIfAbsent(
                            hk,
                            name -> {
                                Header header = new Header();
                                header.setExample(
                                        new Example().value(hv)
                                );
                                return header;
                            }
                    );
                }
        );
        return headersAsMap;
    }

}
