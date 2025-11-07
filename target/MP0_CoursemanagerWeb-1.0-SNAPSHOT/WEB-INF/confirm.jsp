<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>履修登録確認</title>
</head>
<body>
    <h1>履修登録確認</h1>

    <!-- 講義未選択時の表示 -->
    <c:if test="${empty selectedCourses}">
        <p style="color:red;">講義が選択されていません。</p>
        <form action="register" method="get">
            <button type="submit">戻る</button>
        </form>
    </c:if>

    <!-- 講義選択済みの場合 -->
    <c:if test="${not empty selectedCourses}">
        <p>以下の講義を登録します。よろしいですか？</p>
        <form action="complete" method="post">
            <table border="1">
                <tr>
                    <th>講義コード</th>
                    <th>科目名</th>
                    <th>曜日</th>
                    <th>時限</th>
                </tr>
                <c:forEach var="course" items="${selectedCourses}">
                    <c:set var="groupName" value="" />
                    <c:forEach var="group" items="${allGroups}">
                        <c:if test="${group.subjectCode == course.subjectCode}">
                            <c:set var="groupName" value="${group.name}" />
                        </c:if>
                    </c:forEach>
                    <tr>
                        <td>${course.classCode}</td>
                        <td>${groupName}</td>
                        <td>${course.dayOfWeek}</td>
                        <td>${course.period}</td>
                    </tr>
                    <input type="hidden" name="classCode" value="${course.classCode}" />
                </c:forEach>
            </table>
            <br>
            <button type="submit">登録を確定</button>
        </form>

        <form action="register" method="get" style="margin-top: 10px;">
            <button type="submit">戻る</button>
        </form>
    </c:if>
</body>
</html>
