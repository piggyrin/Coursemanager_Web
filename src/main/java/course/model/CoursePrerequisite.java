package course.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
@Entity
@Table(name = "course_prerequisites")
public class CoursePrerequisite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(name = "subject_code")
    private String subjectCode;

    @Column(name = "prerequisite_subject_code")
    private String prerequisiteSubjectCode;

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getPrerequisiteSubjectCode() { return prerequisiteSubjectCode; }
    public void setPrerequisiteSubjectCode(String prerequisiteSubjectCode) { this.prerequisiteSubjectCode = prerequisiteSubjectCode; }
}
