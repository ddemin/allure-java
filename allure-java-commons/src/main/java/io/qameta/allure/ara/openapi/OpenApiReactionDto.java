package io.qameta.allure.ara.openapi;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.qameta.allure.ara.AbstractReactionDto;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static io.qameta.allure.ara.AbstractActionDto.TYPE_OPENAPI;

@JsonTypeName(TYPE_OPENAPI)
public class OpenApiReactionDto extends AbstractReactionDto {

    private int code;
    private ApiResponses response;

    public OpenApiReactionDto() {
        super(TYPE_OPENAPI);
    }

    public OpenApiReactionDto(int code, ApiResponses response) {
        super(TYPE_OPENAPI);
        this.code = code;
        this.response = response;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public ApiResponses getResponse() {
        return response;
    }

    public void setResponse(ApiResponses response) {
        this.response = response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        OpenApiReactionDto that = (OpenApiReactionDto) o;

        return new EqualsBuilder().append(code, that.code).append(response, that.response).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(code).append(response).toHashCode();
    }

    @Override
    public String toString() {
        return "HttpOpenApiReactionDto{" +
                "code=" + code +
                ", response=" + response +
                '}';
    }
}
