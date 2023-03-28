package com.h.common.message;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * @author Lin
 */
@XStreamAlias("Image")
public class Image {

    @XStreamAlias("MediaId")
    private String mediaId;

    public Image(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }
}
