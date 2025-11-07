<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <meta charset="UTF-8">
    <title>登録完了</title>
</head>
<body>
	<h2>登録完了</h2>
	<p>以下の講義が正常に登録されました。</p>
	
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
	    </c:forEach>
	</table>


    <form action="register" method="get">
        <button type="submit">履修登録画面に戻る</button>
    </form>
</body>
</html>
