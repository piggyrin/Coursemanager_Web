package course.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import course.model.Course;
import course.model.CourseGroup;
import course.model.CoursePrerequisite;
import course.model.Student;
import course.model.StudentCompletedCourse;
import course.model.StudentRegisteredCourse;
import course.view.DisplayCourse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private EntityManagerFactory emf;

    @Override
    public void init() {
        emf = Persistence.createEntityManagerFactory("courseManagerPU");
    }

    @Override
    public void destroy() {
        if (emf != null) emf.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Student student = (Student) request.getSession().getAttribute("student");
        if (student == null) {
            response.sendRedirect("login");
            return;
        }

        EntityManager em = emf.createEntityManager();
        try {
            List<Course> allCourses = em.createQuery("SELECT c FROM Course c", Course.class).getResultList();
            List<CourseGroup> allGroups = em.createQuery("SELECT g FROM CourseGroup g", CourseGroup.class).getResultList();
            List<CoursePrerequisite> allPrereqs = em.createQuery("SELECT p FROM CoursePrerequisite p", CoursePrerequisite.class).getResultList();

            List<StudentRegisteredCourse> registered = em.createQuery(
                "SELECT r FROM StudentRegisteredCourse r WHERE r.studentId = :sid", StudentRegisteredCourse.class)
                .setParameter("sid", student.getId())
                .getResultList();

            List<StudentCompletedCourse> completed = em.createQuery(
                "SELECT c FROM StudentCompletedCourse c WHERE c.studentId = :sid", StudentCompletedCourse.class)
                .setParameter("sid", student.getId())
                .getResultList();

            Map<String, Course> codeMap = new HashMap<>();
            for (Course c : allCourses) {
                if (c != null && c.getClassCode() != null) {
                    codeMap.put(c.getClassCode(), c);
                }
            }

            Set<String> registeredCodes = registered.stream()
                    .map(StudentRegisteredCourse::getClassCode).collect(Collectors.toSet());
            Set<String> completedCodes = completed.stream()
                    .map(StudentCompletedCourse::getClassCode).collect(Collectors.toSet());

            List<Course> registeredCourses = registeredCodes.stream()
                    .map(codeMap::get).filter(Objects::nonNull).collect(Collectors.toList());
            List<Course> completedCourses = completedCodes.stream()
                    .map(codeMap::get).filter(Objects::nonNull).collect(Collectors.toList());

            Set<String> excludeSubjects = new HashSet<>();
            for (Course c : registeredCourses) excludeSubjects.add(c.getSubjectCode());
            for (Course c : completedCourses) excludeSubjects.add(c.getSubjectCode());

            Map<String, String> subjectNameMap = new HashMap<>();
            for (CourseGroup g : allGroups) {
                if (g != null && g.getSubjectCode() != null && g.getName() != null) {
                    subjectNameMap.put(g.getSubjectCode(), g.getName());
                }
            }

            List<DisplayCourse> availableCourses = new ArrayList<>();
            for (Course c : allCourses) {
                if (c == null || excludeSubjects.contains(c.getSubjectCode())) continue;
                String name = subjectNameMap.getOrDefault(c.getSubjectCode(), "（名称不明）");
                availableCourses.add(new DisplayCourse(
                        c.getClassCode(), c.getSubjectCode(), name,
                        c.getDayOfWeek(), c.getPeriod()
                ));
            }

            List<DisplayCourse> recommendedCourses = fetchRecommendations(student.getId());

            request.setAttribute("student", student);
            request.setAttribute("availableCourses", availableCourses);
            request.setAttribute("recommendedCourses", recommendedCourses);
            request.setAttribute("registeredCourses", registeredCourses);
            request.setAttribute("completedCourses", completedCourses);
            request.setAttribute("allGroups", allGroups);

            request.getSession().setAttribute("allCourses", allCourses);
            request.getSession().setAttribute("allGroups", allGroups);
            request.getSession().setAttribute("allPrereqs", allPrereqs);

            request.getRequestDispatcher("/WEB-INF/register.jsp").forward(request, response);

        } finally {
            em.close();
        }
    }

    private List<DisplayCourse> fetchRecommendations(String studentId) {
        List<DisplayCourse> result = new ArrayList<>();
        try {
            URL url = new URL("http://localhost:5005/recommend");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String json = "{\"student_id\": \"" + studentId + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("UTF-8"));
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String res = br.lines().collect(Collectors.joining());
                JSONObject jsonObj = new JSONObject(res);
                JSONArray arr = jsonObj.getJSONArray("recommended_courses");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    result.add(new DisplayCourse(
                            o.getString("class_code"),
                            "", // optional
                            o.getString("name"),
                            o.getString("day_of_week"),
                            o.getString("period"),
                            o.optString("priority_reason", ""),
                            o.optString("time_priority_reason", "")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
