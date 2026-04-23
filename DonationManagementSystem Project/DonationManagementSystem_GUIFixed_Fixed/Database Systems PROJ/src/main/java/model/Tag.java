package model;
import java.time.LocalDateTime;

public class Tag {
    public Tag(){}
    private String id;
    private String name;
    private String color;
    private LocalDateTime createdAt;

    public Tag(String id, String name, String color, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Used in ChoiceDialog — show just the tag name */
    @Override
    public String toString() {
        return name != null ? name : "(Unnamed Tag)";
    }
}
