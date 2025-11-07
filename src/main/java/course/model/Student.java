package course.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @Column(name = "student_id")
    private String id;

    private String name;

    @Column(name = "specialized_tags")
    private String specializedTags;

    private String password;


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecializedTags() { return specializedTags; }
    public void setSpecializedTags(String specializedTags) { this.specializedTags = specializedTags; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
