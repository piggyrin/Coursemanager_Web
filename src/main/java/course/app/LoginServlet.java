package course.app;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import course.model.Student;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("courseManagerPU");
    }

    @Override
    public void destroy() {
        if (emf != null) emf.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String studentId = request.getParameter("studentId");
        String password = request.getParameter("password");

        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Student> q = em.createQuery(
                    "SELECT s FROM Student s WHERE s.id = :id AND s.password = :pw", Student.class);
            q.setParameter("id", studentId);
            q.setParameter("pw", password);
            Student student = q.getResultList().isEmpty() ? null : q.getSingleResult();

            if (student != null) {
                request.getSession().setAttribute("student", student);
                response.sendRedirect("register");
                return;
            } else {
                request.setAttribute("error", "学籍番号またはパスワードが違います。");
                request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
            }
        } finally {
            em.close();
        }
    }
}
