package org.ditto.sexyimage.model;

import com.google.common.base.Strings;
import lombok.Data;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.ditto.sexyimage.common.grpc.ImageType;

import java.io.Serializable;
import java.util.Objects;

/**
 * @see [https://www.javacodegeeks.com/2017/07/apache-ignite-spring-data.html]
 */
@Data
public class Image implements Serializable {

    @QuerySqlField(index = true)
    private String url;
    @QuerySqlField
    private String infoUrl;
    @QuerySqlField
    private String title;
    @QuerySqlField
    private String desc;
    @QuerySqlField(index = true)
    private ImageType type;
    @QuerySqlField
    private long created;
    @QuerySqlField(index = true)
    private long lastUpdated;
    @QuerySqlField(index = true)
    private boolean active;
    @QuerySqlField(index = true)
    private boolean toprank;
    @QuerySqlField(index = true)
    private long visitCount;


    public Image() {
    }

    private Image(String url, String infoUrl, String title, String desc, ImageType type, long created, long lastUpdated, boolean active, boolean toprank, long visitCount) {
        this.url = url;
        this.infoUrl = infoUrl;
        this.title = title;
        this.desc = desc;
        this.type = type;
        this.created = created;
        this.lastUpdated = lastUpdated;
        this.active = active;
        this.toprank = toprank;
        this.visitCount = visitCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String url;
        private String infoUrl;
        private String title;
        private String desc;
        private ImageType type;//NORMAL,SEXY,PORN
        private long created;
        private long lastUpdated;
        private boolean active;
        private boolean toprank;
        private int visitCount;

        Builder() {
        }

        public Image build() {
            String missing = "";
            if (Strings.isNullOrEmpty(url)) {
                missing += " url";
            }
            if (Strings.isNullOrEmpty(infoUrl)) {
                missing += " infoUrl";
            }
            if (Objects.isNull(type)) {
                missing += " type";
            }
            if (lastUpdated < 1) {
                missing += " lastUpdated";
            }
            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            Image image = new Image(  url,   infoUrl,   title,   desc,  type,  created, lastUpdated,   active, toprank,  visitCount);
            return image;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setInfoUrl(String infoUrl) {
            this.infoUrl = infoUrl;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDesc(String desc) {
            this.desc = desc;
            return this;
        }

        public Builder setType(ImageType type) {
            this.type = type;
            return this;
        }

        public Builder setCreated(long created) {
            this.created = created;
            return this;
        }

        public Builder setLastUpdated(long lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public Builder setActive(boolean active) {
            this.active = active;
            return this;
        }

        public Builder setToprank(boolean toprank) {
            this.toprank = toprank;
            return this;
        }

        public Builder setVisitCount(int visitCount) {
            this.visitCount = visitCount;
            return this;
        }


    }
}