package io.qameta.allure.retrofit2;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import okhttp3.Request;
import retrofit2.Invocation;
import retrofit2.http.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AllureAraOkHttp3Retrofit extends AllureAraOkHttp3 {

    @Override
    protected String extractPath(Request request) {
        return Arrays.stream(request.tag(Invocation.class).method().getAnnotations())
                .map(methodAnn -> {
                            final String rawPath;
                            if (methodAnn.annotationType().equals(GET.class)) {
                                rawPath = ((GET) methodAnn).value();
                            } else if (methodAnn.annotationType().equals(POST.class)) {
                                rawPath = ((POST) methodAnn).value();
                            } else if (methodAnn.annotationType().equals(PUT.class)) {
                                rawPath = ((PUT) methodAnn).value();
                            } else if (methodAnn.annotationType().equals(PATCH.class)) {
                                rawPath = ((PATCH) methodAnn).value();
                            } else if (methodAnn.annotationType().equals(DELETE.class)) {
                                rawPath = ((DELETE) methodAnn).value();
                            } else if (methodAnn.annotationType().equals(HEAD.class)) {
                                rawPath = ((HEAD) methodAnn).value();
                            } else if (methodAnn.annotationType().equals(OPTIONS.class)) {
                                rawPath = ((OPTIONS) methodAnn).value();
                            } else if (methodAnn.annotationType().equals(HTTP.class)) {
                                rawPath = ((HTTP) methodAnn).path();
                            } else {
                                return null;
                            }
                            return rawPath.startsWith("/") ? rawPath : ("/" + rawPath);
                        }
                )
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(
                        () -> {
                            try {
                                return URLDecoder.decode(request.url().encodedPath(), UTF_8.name());
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
    }

    @Override
    protected void enrichByPathParameters(Request request, List<Parameter> parameters) {
        final java.lang.reflect.Parameter[] methodParams = request.tag(Invocation.class).method().getParameters();
        for (int i = 0; i < methodParams.length; i++) {
            if (methodParams[i].isAnnotationPresent(Path.class)) {
                parameters.add(
                        new PathParameter()
                                .name(methodParams[i].getAnnotation(Path.class).value())
                                .example(String.valueOf(request.tag(Invocation.class).arguments().get(i)))
                );
            }
        }
    }

}
