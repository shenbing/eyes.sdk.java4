
package com.applitools.eyes.metadata;

import com.applitools.eyes.RectangleSize;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "size"
})
public class Image {

    @JsonProperty("id")
    private String id;
    @JsonProperty("size")
    private RectangleSize size;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("size")
    public RectangleSize getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(RectangleSize size) {
        this.size = size;
    }

}
