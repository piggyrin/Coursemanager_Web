from flask import Flask, request, jsonify
import sqlite3
import pandas as pd

app = Flask(__name__)

# === 1. 全局数据预加载 ===
DB_PATH = r"E:\pleiades\2025-03\workspace\MP0_CoursemanagerWeb\data\MP0_dummy.db"

def load_data():
    conn = sqlite3.connect(DB_PATH)
    df_students = pd.read_sql("SELECT * FROM students", conn)
    df_groups = pd.read_sql("SELECT * FROM course_groups", conn)
    df_courses = pd.read_sql("SELECT * FROM courses WHERE day_of_week != 'その他' AND period != 'その他'", conn)
    conn.close()
    return df_students, df_groups, df_courses

df_students, df_groups, df_courses = load_data()

# === 2. 推荐核心函数 ===
def recommend_courses(student_id, top_n=10):
    row = df_students[df_students["student_id"] == student_id]
    if row.empty:
        return []

    student_tags = row.iloc[0]["specialized_tags"].split(",") if row.iloc[0]["specialized_tags"] else []

    conn = sqlite3.connect(DB_PATH)
    df_registered = pd.read_sql(
        f"SELECT class_code FROM student_registered_courses WHERE student_id = '{student_id}'", conn
    )
    registered_codes = set(df_registered["class_code"].tolist())
    df_completed = pd.read_sql(
        f"SELECT class_code FROM student_completed_courses WHERE student_id = '{student_id}'", conn
    )
    completed_codes = set(df_completed["class_code"].tolist())

    # 合并所有已修/已注册课程的 subject_code
    taken_subjects = set(
        df_courses[df_courses["class_code"].isin(registered_codes | completed_codes)]["subject_code"].unique()
    )

    # 已注册课程的 {day_of_week: [period, ...]}
    registered_on_day = {}
    for _, rc in df_courses[df_courses["class_code"].isin(registered_codes)].iterrows():
        day = rc["day_of_week"]
        periods = [int(p.strip()) for p in str(rc["period"]).split(",")]
        if day not in registered_on_day:
            registered_on_day[day] = []
        registered_on_day[day].extend(periods)

    # 只推荐专业相关 group 里的 subject_code
    def matches_interest(row):
        tags = row["tags"].split(",") if row["tags"] else []
        return any(tag.strip() in student_tags for tag in tags)
    df_group_matched = df_groups[df_groups.apply(matches_interest, axis=1)]
    matched_subjects = df_group_matched["subject_code"].unique()

    # 时限冲突判定
    def is_slot_available(row):
        periods = str(row["period"]).split(",")
        day = row["day_of_week"]
        for p in periods:
            p = int(p.strip())
            if day in registered_on_day and p in registered_on_day[day]:
                return False
        return True
    df_courses["is_time_ok"] = df_courses.apply(is_slot_available, axis=1)

    df_candidates = df_courses[
        df_courses["subject_code"].isin(matched_subjects) &
        ~df_courses["subject_code"].isin(taken_subjects) &
        df_courses["is_time_ok"]
    ]

    # 前提过滤
    def has_prereq_satisfied(class_code):
        row = df_courses[df_courses["class_code"] == class_code]
        if row.empty:
            return False
        subject_code = row.iloc[0]["subject_code"]
        sql = "SELECT prerequisite_subject_code FROM course_prerequisites WHERE subject_code = ?"
        prereqs = pd.read_sql(sql, conn, params=(subject_code,))
        if prereqs.empty:
            return True
        for _, pre in prereqs.iterrows():
            pre_sub = pre["prerequisite_subject_code"]
            exist = not df_courses[
                (df_courses["subject_code"] == pre_sub) &
                (df_courses["class_code"].isin(registered_codes | completed_codes))
            ].empty
            if not exist:
                return False
        return True
    df_candidates = df_candidates[df_candidates["class_code"].apply(has_prereq_satisfied)]

    # ====== TAG 优先级 ======
    def get_priority(row):
        tags = row["tags"].split(",") if row["tags"] else []
        if any(tag.strip() in student_tags for tag in tags):
            return 1
        elif "GEN" in [t.strip() for t in tags]:
            return 2
        else:
            return 3

    # ====== 时限优先级 ======
    def get_time_priority(row):
        day = row["day_of_week"]
        periods = sorted([int(p.strip()) for p in str(row["period"]).split(",")])
        reg = sorted(set(registered_on_day.get(day, [])))
        if not reg:
            return 5  # 该日没课，放最后
        # 1. 空コマ: 在两课之间
        for p in periods:
            for i in range(len(reg) - 1):
                if reg[i] < p < reg[i+1]:
                    return 1
        # 2. 中间（不是刚好夹缝，但在区间内）
        for p in periods:
            if len(reg) >= 2 and min(reg) < p < max(reg):
                return 2
        # 3. 紧跟在现有课后
        for p in periods:
            if (p - 1) in reg:
                return 3
        # 4. 紧接在现有课前
        for p in periods:
            if (p + 1) in reg:
                return 4
        # 5. 其它空时段
        return 5

    # ====== 合并排序和推荐理由 ======
    df_result = pd.merge(df_candidates, df_groups, on="subject_code", how="left")
    if df_result.empty:
        conn.close()
        return []

    df_result["priority"] = df_result.apply(get_priority, axis=1)
    df_result["time_priority"] = df_result.apply(get_time_priority, axis=1)
    df_result = df_result.sort_values(by=["priority", "time_priority", "name"])

    def explain_priority(priority):
        return {1: "専門分野", 2: "共通科目", 3: "その他"}.get(priority, "")

    def explain_time_priority(tp):
        return {
            1: "挟みコマ",
            2: "中間コマ",
            3: "後続コマ",
            4: "前続コマ",
            5: "空きコマ"
        }.get(tp, "")

    df_result["priority_reason"] = df_result["priority"].apply(explain_priority)
    df_result["time_priority_reason"] = df_result["time_priority"].apply(explain_time_priority)

    out_cols = [
        "class_code", "name", "day_of_week", "period", "tags",
        "priority", "priority_reason", "time_priority", "time_priority_reason"
    ]
    result = df_result[out_cols].head(top_n)
    conn.close()
    return result.to_dict(orient="records")

# === 3. Flask 路由 ===
@app.route('/recommend', methods=['POST'])
def recommend_api():
    data = request.json
    student_id = data.get("student_id")
    if not student_id:
        return jsonify({"error": "student_id required"}), 400
    recommendations = recommend_courses(student_id)
    return jsonify({"recommended_courses": recommendations})

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5005)
