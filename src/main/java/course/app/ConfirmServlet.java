package course.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import course.model.Course;
import course.model.CourseGroup;
import course.model.Student;

@WebServlet("/confirm")
public class ConfirmServlet extends HttpServlet {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Student student = (Student) request.getSession().getAttribute("student");
        if (student == null) {
            response.sendRedirect("login");
            return;
        }

        String[] selectedCodes = request.getParameterValues("classCode");
        if (selectedCodes == null || selectedCodes.length == 0) {
            request.setAttribute("message", "講義が選択されていません。");
            request.getRequestDispatcher("/WEB-INF/error.jsp").forward(request, response);
            return;
        }

        @SuppressWarnings("unchecked")
        List<Course> allCourses = (List<Course>) request.getSession().getAttribute("allCourses");
        @SuppressWarnings("unchecked")
        List<CourseGroup> allGroups = (List<CourseGroup>) request.getSession().getAttribute("allGroups");

        if (allCourses == null || allGroups == null) {
            response.sendRedirect("register");
            return;
        }

        String studentId = student.getId();
        EntityManager em = emf.createEntityManager();

        try {
            List<String> registered = em.createQuery(
                    "SELECT s.classCode FROM StudentRegisteredCourse s WHERE s.studentId = :sid", String.class)
                    .setParameter("sid", studentId)
                    .getResultList();

            List<String> completed = em.createQuery(
                    "SELECT s.classCode FROM StudentCompletedCourse s WHERE s.studentId = :sid", String.class)
                    .setParameter("sid", studentId)
                    .getResultList();

            //展示用
            List<Course> selectedCourses = new ArrayList<>();
            for (String code : selectedCodes) {
                for (Course c : allCourses) {
                    if (c.getClassCode().equalsIgnoreCase(code)) {
                        selectedCourses.add(c);
                        break;
                    }
                }
            }

            // === 1. 同一科目 & 時間割重複チェック
            Set<String> usedSubjects = new HashSet<>();
            Set<String> occupiedSlots = new HashSet<>();
            for (Course c : allCourses) {
                if (registered.contains(c.getClassCode())) {
                    usedSubjects.add(c.getSubjectCode());
                    for (String p : c.getPeriod().split(",")) {
                        String slot = (c.getDayOfWeek() + "-" + p).replaceAll("\\s+", "");
                        occupiedSlots.add(slot);
                    }
                }
            }

            // === 2. 前提科目チェック：registered + completed
            for (Course c : selectedCourses) {
                List<String> prereqSubjects = em.createQuery(
                        "SELECT p.prerequisiteSubjectCode FROM CoursePrerequisite p WHERE p.subjectCode = :sub",
                        String.class)
                        .setParameter("sub", c.getSubjectCode())
                        .getResultList();

                for (String prereq : prereqSubjects) {
                    boolean ok = false;
                    for (Course cc : allCourses) {
                        if (cc.getSubjectCode().equals(prereq)) {
                            String code = cc.getClassCode();
                            if (registered.contains(code) || completed.contains(code)) {
                                ok = true;
                                break;
                            }
                        }
                    }
                    if (!ok) {
                        request.setAttribute("message", "前提科目未履修: " + prereq);
                        request.getRequestDispatcher("/WEB-INF/error.jsp").forward(request, response);
                        return;
                    }
                }
            }

            // === 3. 各講義について重複チェック
            for (Course c : selectedCourses) {
                if (usedSubjects.contains(c.getSubjectCode())) {
                    request.setAttribute("message", "同じ科目を複数登録しようとしています: " + c.getSubjectCode());
                    request.getRequestDispatcher("/WEB-INF/error.jsp").forward(request, response);
                    return;
                }

                for (String p : c.getPeriod().split(",")) {
                    String slot = (c.getDayOfWeek() + "-" + p).replaceAll("\\s+", "");
                    if (occupiedSlots.contains(slot)) {
                        request.setAttribute("message", "時間割が重複しています: " + c.getClassCode());
                        request.getRequestDispatcher("/WEB-INF/error.jsp").forward(request, response);
                        return;
                    }
                }

                usedSubjects.add(c.getSubjectCode());
                for (String p : c.getPeriod().split(",")) {
                    String slot = (c.getDayOfWeek() + "-" + p).replaceAll("\\s+", "");
                    occupiedSlots.add(slot);
                }
            }

            request.setAttribute("selectedCourses", selectedCourses);
            request.setAttribute("allGroups", allGroups);
            request.getRequestDispatcher("/WEB-INF/confirm.jsp").forward(request, response);

        } finally {
            em.close();
        }
    }
}
