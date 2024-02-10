package io.qameta.allure.okhttp3;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.ara.ARAttachmentDto;
import io.qameta.allure.ara.ExceptionReactionDto;
import io.qameta.allure.ara.IReactionDto;
import io.qameta.allure.ara.http.OpenApiActionDto;
import io.qameta.allure.ara.http.OpenApiReactionDto;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import okhttp3.*;
import okio.Buffer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AllureAraOkHttp3 implements Interceptor {

    private static final String DEFAULT_VALUE_NOT_FOUND_HEADER = "";
    private static final String DEFAULT_CONTENT_TYPE = "text/plain";
    private static final String PREFIX_UNKNOWN_PART = "part-";
    private static final String DELIMITER_CONTENT_DISPOSITION = ";";
    private static final String PREFIX_UNNAMED_PATH_PART = "segment-";

    private final AllureLifecycle lifecycle;
    private final ObjectWriter objectWriter;

    public AllureAraOkHttp3() {
        this.lifecycle = Allure.getLifecycle();
        this.objectWriter = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writerWithDefaultPrettyPrinter();
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        final Request request = chain.request();

        IReactionDto reactionDto = null;
        try {
            final Response response = chain.proceed(request);
            reactionDto = new OpenApiReactionDto(
                    response.code(),
                    buildApiResponses(response)
            );
        } catch (Throwable tr) {
            reactionDto = new ExceptionReactionDto(
                    tr.getClass().getTypeName(),
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
                                StringUtils.substringBefore(request.url().redact(), "/..."),
                                URLDecoder.decode(request.url().encodedPath(), UTF_8.name()),
                                buildPathItem(request)
                        ),
                        reactionDto
                );
                attachAra(request, arAttachmentDto);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        }
        return null;
    }

    private PathItem buildPathItem(final Request request) throws IOException {
        final Operation openApiOperation = new Operation();

        final List<Parameter> parameters = new LinkedList<>();
        request.headers().toMultimap().forEach(
                (k, vs) -> {
                    parameters.add(
                            new Parameter()
                                    .in(ParameterIn.HEADER.toString())
                                    .name(k)
                                    .schema(new StringSchema())
                                    .example(vs.stream().findFirst().orElse(DEFAULT_VALUE_NOT_FOUND_HEADER))
                    );
                }
        );

        request.url().queryParameterNames().forEach(
                qname -> {
                    parameters.add(
                            new Parameter()
                                    .in(ParameterIn.QUERY.toString())
                                    .name(qname)
                                    .schema(new StringSchema())
                                    .example(request.url().queryParameterValues(qname).stream().findFirst())
                    );
                }
        );

        // TODO Named parameters support ?
        final AtomicInteger segmentNum = new AtomicInteger(0);
        request.url().pathSegments().forEach(
                psegment -> {
                    parameters.add(
                            new Parameter()
                                    .in(ParameterIn.PATH.toString())
                                    .name(PREFIX_UNNAMED_PATH_PART + segmentNum.incrementAndGet())
                                    .schema(new StringSchema())
                                    .example(psegment)
                    );
                }
        );

        // TODO Cookie support ?

        openApiOperation.setParameters(parameters);

        if (request.body() instanceof MultipartBody) {
            enrichWithMultiPartSpecs(request, openApiOperation);
        } else if (request.body() instanceof FormBody) {
            enrichWithFormDataSpecs((FormBody) request.body(), openApiOperation);
        } else {
            final MediaType mediaType = new MediaType();
            if (request.body() != null) {
                final String rawBody = readRequestBody(request.body());
                final Example bodyExample = new Example().value(
                        Base64.getEncoder().encode(rawBody.getBytes(UTF_8))
                );
                mediaType.setExample(bodyExample);
            }

            final String contentTypeHeader = request.header("Content-Type");
            final String contentType = request.body() == null
                    ? contentTypeHeader
                    : request.body().contentType().toString();
            if (StringUtils.isNoneBlank(contentType)) {
                openApiOperation.setRequestBody(
                        new io.swagger.v3.oas.models.parameters.RequestBody().content(
                                new Content().addMediaType(
                                        contentType,
                                        mediaType
                                )
                        )
                );
            }
        }

        final PathItem pathItem = new PathItem();
        pathItem.operation(
                PathItem.HttpMethod.valueOf(request.method()),
                openApiOperation
        );

        return pathItem;
    }

    private void enrichWithFormDataSpecs(final FormBody body, final Operation openApiOperation) {
        final ObjectSchema partsSchema = new ObjectSchema();
        final MediaType mediaType = new MediaType();
        mediaType.schema(partsSchema);

        partsSchema.properties(new LinkedHashMap<>());

        openApiOperation.setRequestBody(
                new RequestBody().content(
                        new Content().addMediaType("application/x-www-form-urlencoded", mediaType)
                )
        );

        final int formsCnt = body.size();
        for (int i = 0; i < formsCnt; i++) {
            partsSchema.getProperties().put(
                    body.name(i),
                    new StringSchema()
            );
        }
    }

    private void enrichWithMultiPartSpecs(final okhttp3.Request request, final Operation openApiOperation) {
        final okhttp3.MultipartBody body = (MultipartBody) request.body();
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

        final AtomicInteger partNumAtomic = new AtomicInteger(0);
        body.parts().forEach(
                part -> {
                    final Schema partSchema;
                    final Example partExample = new Example();

                    final String disposition = part.headers() == null
                            ? ""
                            : part.headers().get("Content-Disposition");
                    final String type = StringUtils.substringBefore(disposition, DELIMITER_CONTENT_DISPOSITION);
                    final String name = StringUtils.substringBetween(disposition, "name=", DELIMITER_CONTENT_DISPOSITION);
                    final String filename = StringUtils.substringBetween(disposition, "filename=", DELIMITER_CONTENT_DISPOSITION);

                    if (StringUtils.isNoneBlank(filename) || isBinaryResponseBody(part)) {
                        partSchema = new BinarySchema();
                    } else {
                        partSchema = new StringSchema();
                        try {
                            final String partRawBody = readRequestBody(part.body());
                            if (StringUtils.isNoneBlank(partRawBody)) {
                                partSchema.setExample(
                                        Base64.getEncoder().encodeToString(
                                                partRawBody.getBytes(UTF_8)
                                        )
                                );
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    final int partNum = partNumAtomic.incrementAndGet();
                    partsSchema.addProperty(
                            StringUtils.firstNonBlank(
                                    name, filename, type, PREFIX_UNKNOWN_PART + partNum
                            ),
                            partSchema
                    );
                    partsEncoding.computeIfAbsent(
                            StringUtils.firstNonBlank(
                                    name, filename, type, PREFIX_UNKNOWN_PART + partNum
                            ),
                            k -> new Encoding().headers(convertMapToHeadersMap(part.headers()))
                    );
                }
        );
    }

    private boolean isBinaryResponseBody(final MultipartBody.Part part) {
        return part.body() != null
                && part.body().contentType() != null
                && part.body().contentType().toString().equalsIgnoreCase("application/octet-stream");
    }

    private ApiResponses buildApiResponses(final Response okHttpResponse) throws IOException {
        final Map<String, Header> headersMap = convertMapToHeadersMap(okHttpResponse.headers());
        final ApiResponse apiResponse = new ApiResponse();
        apiResponse.setHeaders(headersMap);
        apiResponse.setDescription(okHttpResponse.message());

        final ResponseBody responseBody = okHttpResponse.body();
        final String contentType;
        final Example responseBodyExample;
        final String contentTypeHeader = okHttpResponse.header("Content-Type");
        if (responseBody != null) {
            responseBodyExample = new Example().value(
                    Base64.getEncoder().encode(
                            responseBody.string().getBytes(UTF_8)
                    )
            );
            contentType = responseBody.contentType() == null
                    ? contentTypeHeader
                    : responseBody.contentType().toString();
        } else {
            responseBodyExample = null;
            contentType = contentTypeHeader;
        }

        final MediaType mediaType = new MediaType();
        mediaType.setExample(responseBodyExample);
        apiResponse.setContent(
                new Content().addMediaType(
                        StringUtils.firstNonBlank(contentType, DEFAULT_CONTENT_TYPE),
                        mediaType
                )
        );

        final ApiResponses apiResponses = new ApiResponses();
        apiResponses.addApiResponse(
                String.valueOf(okHttpResponse.code()),
                apiResponse
        );
        return apiResponses;
    }


    private Map<String, Header> convertMapToHeadersMap(final Headers headers) {
        final Map<String, Header> headersAsMap = new HashMap<>();
        if (headers == null) {
            return headersAsMap;
        }

        headers.toMultimap().forEach(
                (k, vs) -> {
                    headersAsMap.computeIfAbsent(
                            k,
                            name -> {
                                final Header header = new Header();
                                header.setExample(
                                        new Example().value(vs.stream()
                                                .findFirst()
                                                .orElse(DEFAULT_VALUE_NOT_FOUND_HEADER))
                                );
                                return header;
                            }
                    );
                }
        );
        return headersAsMap;
    }

    private void attachAra(
            final Request request,
            final ARAttachmentDto arAttachmentDto
    ) throws JsonProcessingException {
        lifecycle.addAttachment(
                "Interaction - " + request.url(),
                "application/json",
                ".json",
                objectWriter.writeValueAsString(arAttachmentDto).getBytes(UTF_8)
        );
    }

    private String readRequestBody(final okhttp3.RequestBody requestBody) throws IOException {
        final Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);
        return buffer.readString(StandardCharsets.UTF_8);
    }

}
