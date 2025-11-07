package course.app;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import course.model.Student;

@WebServlet("/drop")
public class DropServlet extends HttpServlet {
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

        String[] codes = request.getParameterValues("classCode");
        if (codes == null || codes.length == 0) {
            response.sendRedirect("register");
            return;
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            for (String code : codes) {
                em.createQuery("DELETE FROM StudentRegisteredCourse s WHERE s.studentId = :sid AND s.classCode = :cc")
                        .setParameter("sid", student.getId())
                        .setParameter("cc", code)
                        .executeUpdate();
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new ServletException(e);
        } finally {
            em.close();
        }

        response.sendRedirect("register");
    }
}
