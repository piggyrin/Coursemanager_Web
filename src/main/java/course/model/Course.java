package course.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @Column(name = "class_code")
    private String classCode;

    @Column(name = "subject_code")
    private String subjectCode;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    private String period;

    // getter/setter
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
}
