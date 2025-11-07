<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>エラー</title>
</head>
<body>
    <h2 style="color:red;">エラーが発生しました</h2>
    <p>${message}</p>

    <form action="register" method="get">
        <button type="submit">戻る</button>
    </form>
</body>
</html>
