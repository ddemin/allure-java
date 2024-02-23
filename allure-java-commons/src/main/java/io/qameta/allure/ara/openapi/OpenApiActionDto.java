package io.qameta.allure.ara.openapi;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.qameta.allure.ara.AbstractActionDto;
import io.swagger.v3.oas.models.PathItem;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static io.qameta.allure.ara.AbstractActionDto.TYPE_OPENAPI;


@JsonTypeName(TYPE_OPENAPI)
public class OpenApiActionDto extends AbstractActionDto {

    private String uri;
    private String path;
    private PathItem pathItem;

    public OpenApiActionDto() {
        super(TYPE_OPENAPI);
    }

    public OpenApiActionDto(String uri, String path, PathItem pathItem) {
        super(TYPE_OPENAPI);
        this.uri = uri;
        this.path = path;
        this.pathItem = pathItem;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public PathItem getPathItem() {
        return pathItem;
    }

    public void setPathItem(PathItem pathItem) {
        this.pathItem = pathItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        OpenApiActionDto that = (OpenApiActionDto) o;

        return new EqualsBuilder().append(uri, that.uri).append(path, that.path).append(pathItem, that.pathItem).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uri).append(path).append(pathItem).toHashCode();
    }

    @Override
    public String toString() {
        return "HttpOpenApiActionDto{" +
                "url=" + uri +
                ", path='" + path + '\'' +
                ", pathItem=" + pathItem +
                '}';
    }
}
