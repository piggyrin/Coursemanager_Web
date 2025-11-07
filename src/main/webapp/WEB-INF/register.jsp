<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
<style>
  table {
    border-collapse: collapse;
    margin-bottom: 20px;
    min-width: 600px;
    background: #fafcff;
    font-size: 15px;
  }
  th, td {
    border: 1px solid #bbb;
    padding: 8px 12px;
    text-align: center;
  }
  th {
    background: #dde9f7;
    color: #234;
  }
  tr:nth-child(even) {
    background: #f4f8fc;
  }
  tr:hover {
    background: #ffeabb !important;
    transition: 0.1s;
  }
  h2 {
    color: #34577c;
    margin-top: 32px;
    margin-bottom: 10px;
  }
</style>

    <title>履修登録</title>
</head>
<body>

<h2>
  ようこそ、${student.name} さん（学籍番号: ${student.id}／専門分野: ${student.specializedTags}）
</h2>

<h2>履修済みの講義</h2>

<c:if test="${not empty completedCourses}">
  <table border="1">
    <tr>
      <th>講義コード</th>
      <th>科目名</th>
      <th>曜日</th>
      <th>時限</th>
    </tr>
    <c:forEach var="c" items="${completedCourses}">
      <tr>
        <td>${c.classCode}</td>
        <td>
          <c:forEach var="g" items="${allGroups}">
            <c:if test="${g.subjectCode == c.subjectCode}">
              ${g.name}
            </c:if>
          </c:forEach>
        </td>
        <td>${c.dayOfWeek}</td>
        <td>${c.period}</td>
      </tr>
    </c:forEach>
  </table>
</c:if>
<c:if test="${empty completedCourses}">
  <p>現在、履修済みの講義はありません。</p>
</c:if>


<h2>履修中の講義</h2>

<c:if test="${not empty registeredCourses}">
  <form action="drop" method="post">
    <table border="1">
      <tr>
        <th>解除</th>
        <th>講義コード</th>
        <th>科目名</th>
        <th>曜日</th>
        <th>時限</th>
      </tr>
      <c:forEach var="c" items="${registeredCourses}">
        <tr>
          <td><input type="checkbox" name="classCode" value="${c.classCode}" /></td>
          <td>${c.classCode}</td>
          <td>
            <c:forEach var="g" items="${allGroups}">
              <c:if test="${g.subjectCode == c.subjectCode}">
                ${g.name}
              </c:if>
            </c:forEach>
          </td>
          <td>${c.dayOfWeek}</td>
          <td>${c.period}</td>
        </tr>
      </c:forEach>
    </table>
    <button type="submit">選択した講義を履修解除</button>
  </form>
</c:if>

<c:if test="${empty registeredCourses}">
  <p>現在、履修中の講義はありません。</p>
</c:if>

<c:if test="${not empty recommendedCourses}">
    <h2>あなたへのおすすめ講義</h2>
    <table border="1">
        <tr>
            <th>講義コード</th>
            <th>科目名</th>
            <th>曜日</th>
            <th>時限</th>
            <th>備考</th>
        </tr>
        <c:forEach var="rc" items="${recommendedCourses}">
            <tr>
                <td>${rc.classCode}</td>
                <td>${rc.name}</td>
                <td>${rc.dayOfWeek}</td>
                <td>${rc.period}</td>
                <td>${rc.priorityReason}・${rc.timePriorityReason}</td>
            </tr>
        </c:forEach>
    </table>
</c:if>


<h2>履修登録可能な講義一覧</h2>

<form action="confirm" method="post">
    <table border="1">
        <tr>
            <th>選択</th>
            <th>講義コード</th>
            <th>科目名</th>
            <th>曜日</th>
            <th>時限</th>
        </tr>
        <c:forEach var="course" items="${availableCourses}">
            <tr>
                <td><input type="checkbox" name="classCode" value="${course.classCode}"></td>
                <td>${course.classCode}</td>
                <td>${course.name}</td>
                <td>${course.dayOfWeek}</td>
                <td>${course.period}</td>
            </tr>
        </c:forEach>
        
        <c:if test="${not empty message}">
    		<p style="color:red;">${message}</p>
		</c:if>   
	
    </table>
    <br>
    <input type="submit" value="登録確認へ">
</form>

</body>
</html>
