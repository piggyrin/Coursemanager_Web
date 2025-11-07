package course.view;

public class DisplayCourse {
    private String classCode;
    private String subjectCode;
    private String name;
    private String dayOfWeek;
    private String period;


    private String priorityReason;
    private String timePriorityReason;

    public DisplayCourse(String classCode, String subjectCode, String name, String dayOfWeek, String period) {
        this.classCode = classCode;
        this.subjectCode = subjectCode;
        this.name = name;
        this.dayOfWeek = dayOfWeek;
        this.period = period;
    }


    public DisplayCourse(String classCode, String subjectCode, String name, String dayOfWeek, String period,
                         String priorityReason, String timePriorityReason) {
        this.classCode = classCode;
        this.subjectCode = subjectCode;
        this.name = name;
        this.dayOfWeek = dayOfWeek;
        this.period = period;
        this.priorityReason = priorityReason;
        this.timePriorityReason = timePriorityReason;
    }

    public String getClassCode() { return classCode; }
    public String getSubjectCode() { return subjectCode; }
    public String getName() { return name; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getPeriod() { return period; }


    public String getPriorityReason() { return priorityReason; }
    public void setPriorityReason(String priorityReason) { this.priorityReason = priorityReason; }

    public String getTimePriorityReason() { return timePriorityReason; }
    public void setTimePriorityReason(String timePriorityReason) { this.timePriorityReason = timePriorityReason; }
}
