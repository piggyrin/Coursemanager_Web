package course.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "course_groups")
public class CourseGroup {
    @Id
    @Column(name = "subject_code")
    private String subjectCode;

    private String name;
    private Integer credit;
    private String tags;

    // getter/setter
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getCredit() { return credit; }
    public void setCredit(Integer credit) { this.credit = credit; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
