package tv.projectivy.plugin.wallpaperprovider.bingwallpaper.models;

public class WallpaperItem {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String thumbnailUrl;
    private String contentType; // "image" or "video"
    private long timestamp;

    public WallpaperItem() {
        this.contentType = "image";
        this.timestamp = System.currentTimeMillis();
    }

    public WallpaperItem(String id, String title, String description, 
                         String imageUrl, String thumbnailUrl) {
        this(id, title, description, imageUrl, thumbnailUrl, "image");
    }

    public WallpaperItem(String id, String title, String description, 
                         String imageUrl, String thumbnailUrl, String contentType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.contentType = contentType;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isVideo() {
        return "video".equalsIgnoreCase(contentType);
    }

    @Override
    public String toString() {
        return "WallpaperItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", contentType='" + contentType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}