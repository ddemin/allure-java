package io.qameta.allure.restassured.ara;

import java.util.Objects;

public class ExampleResponse {

    private Boolean success;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ExampleResponse) obj;
        return Objects.equals(this.success, that.success);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success);
    }

    @Override
    public String toString() {
        return "ExampleResponse[" +
                "success=" + success + ']';
    }

}
