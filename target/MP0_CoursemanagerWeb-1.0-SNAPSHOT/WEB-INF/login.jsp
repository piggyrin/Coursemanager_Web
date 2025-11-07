<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>ログイン</title>
</head>
<body>
    <h1>履修登録システム - ログイン</h1>

    <c:if test="${not empty error}">
        <p style="color:red;">${error}</p>
    </c:if>

    <form action="login" method="post">
        学籍番号: <input type="text" name="studentId" required><br>
        パスワード: <input type="password" name="password" required><br>
        <button type="submit">ログイン</button>
    </form>
</body>
</html>
