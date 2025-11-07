package course.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import course.model.Course;
import course.model.CourseGroup;
import course.model.Student;
import course.model.StudentRegisteredCourse;

@WebServlet("/complete")
public class CompleteServlet extends HttpServlet {
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
            request.setAttribute("message", "登録対象の講義が見つかりませんでした。");
            request.getRequestDispatcher("/WEB-INF/error.jsp").forward(request, response);
            return;
        }

        @SuppressWarnings("unchecked")
        List<Course> allCourses = (List<Course>) request.getSession().getAttribute("allCourses");
        @SuppressWarnings("unchecked")
        List<CourseGroup> allGroups = (List<CourseGroup>) request.getSession().getAttribute("allGroups");

        List<Course> selectedCourses = new ArrayList<>();
        for (String code : selectedCodes) {
            for (Course c : allCourses) {
                if (c.getClassCode().equalsIgnoreCase(code)) {
                    selectedCourses.add(c);
                    break;
                }
            }
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            for (String code : selectedCodes) {
                Long count = em.createQuery(
                        "SELECT COUNT(s) FROM StudentCompletedCourse s WHERE s.studentId = :sid AND s.classCode = :cc",
                        Long.class)
                        .setParameter("sid", student.getId())
                        .setParameter("cc", code)
                        .getSingleResult();

                if (count == 0) {
                	StudentRegisteredCourse reg = new StudentRegisteredCourse();
                	reg.setStudentId(student.getId());
                	reg.setClassCode(code);
                	em.persist(reg);
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new ServletException(e);
        } finally {
            em.close();
        }

        request.setAttribute("selectedCourses", selectedCourses);
        request.setAttribute("allGroups", allGroups);
        request.getRequestDispatcher("/WEB-INF/complete.jsp").forward(request, response);
    }
}
