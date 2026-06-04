package com.example.sportrecord.model

/** 运动项目定义 */
data class SportDef(
    val key: String,
    val name: String,
    val icon: String,
    val met: Double = 1.0,
    val measureType: String = "duration",  // duration | count | both
    val kcalPerUnit: Double = 0.0,
    val caloriePerKm: Double = 0.0
)

object SportsData {
    val allSports = listOf(
        // 有氧
        SportDef("walking",      "🚶 步行",         "🚶",  met=3.0, measureType="duration", caloriePerKm=0.62),
        SportDef("running",      "🏃 跑步(慢跑)",    "🏃",  met=8.0, measureType="duration", caloriePerKm=0.97),
        SportDef("running_fast", "🏃 跑步(快跑)",    "🏃", met=12.0, measureType="duration", caloriePerKm=1.03),
        SportDef("swimming",     "🏊 游泳",         "🏊",  met=7.0, measureType="duration"),
        SportDef("cycling",      "🚴 骑行",         "🚴",  met=6.0, measureType="duration", caloriePerKm=0.38),
        SportDef("hiking",       "🥾 徒步",         "🥾",  met=5.3, measureType="duration", caloriePerKm=0.53),

        // 球类
        SportDef("basketball",   "🏀 篮球",         "🏀",  met=6.5, measureType="duration"),
        SportDef("football",     "⚽ 足球",         "⚽",  met=7.0, measureType="duration"),
        SportDef("badminton",    "🏸 羽毛球",       "🏸",  met=5.5, measureType="duration"),
        SportDef("table_tennis", "🏓 乒乓球",       "🏓",  met=4.0, measureType="duration"),
        SportDef("tennis",       "🎾 网球",         "🎾",  met=7.3, measureType="duration"),

        // 力量
        SportDef("fitness",      "💪 健身",         "💪",  met=5.0, measureType="duration"),
        SportDef("sit_up",       "🦵 仰卧起坐",     "🦵",  met=3.8, measureType="count", kcalPerUnit=0.5),
        SportDef("push_up",      "💪 俯卧撑",       "💪",  met=3.8, measureType="count", kcalPerUnit=0.6),
        SportDef("plank",        "🧘 平板支撑",     "🧘",  met=3.0, measureType="count", kcalPerUnit=0.4),
        SportDef("squat",        "🦿 深蹲",         "🦿",  met=5.0, measureType="count", kcalPerUnit=0.5),
        SportDef("pull_up",      "🏋 引体向上",     "🏋️", met=3.8, measureType="count", kcalPerUnit=0.7),
        SportDef("jump_rope",    "🪢 跳绳",         "🪢",  met=10.0, measureType="both", kcalPerUnit=0.15),

        // 柔韧
        SportDef("yoga",         "🧘 瑜伽",         "🧘",  met=2.5, measureType="duration"),
        SportDef("dancing",      "💃 跳舞",         "💃",  met=4.8, measureType="duration")
    )

    fun getByKey(key: String) = allSports.find { it.key == key }
}

/** 热量计算引擎 */
object CalorieEngine {
    /** 计时: kcal = MET × weight(kg) × duration(h) */
    fun byDuration(met: Double, weightKg: Int, minutes: Int): Int {
        if (minutes <= 0) return 0
        return (met * weightKg * (minutes / 60.0)).toInt()
    }
    /** 计次: kcal = count × perUnit */
    fun byCount(count: Int, perUnit: Double): Int = (count * perUnit).toInt()
    /** 距离: kcal = dist(km) × weight(kg) × perKm */
    fun byDistance(km: Double, weightKg: Int, perKm: Double): Int =
        (km * weightKg * perKm).toInt()
}
