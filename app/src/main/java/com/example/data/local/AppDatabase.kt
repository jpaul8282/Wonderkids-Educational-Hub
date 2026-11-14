package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Badge
import com.example.data.model.ChildProfile
import com.example.data.model.DailyMission
import com.example.data.model.GameCategory
import com.example.data.model.GameSession
import com.example.data.model.ParentSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChildProfile::class,
        GameSession::class,
        Badge::class,
        DailyMission::class,
        ParentSettings::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kidsDao(): KidsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wonderkids_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.kidsDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: KidsDao) {
            // Seed default parent settings
            dao.saveParentSettings(
                ParentSettings(
                    id = 1,
                    parentName = "Sarah Jenkins",
                    parentEmail = "sarah.j@example.com",
                    pinCode = "1234",
                    dailyScreenLimitMinutes = 45,
                    soundEffectsEnabled = true,
                    backgroundMusicEnabled = true,
                    mathGateEnabled = true,
                    bedtimeReminderEnabled = true,
                    bedtimeHour = 20,
                    bedtimeMinute = 0
                )
            )

            // Seed kids
            val kid1Id = dao.insertChild(
                ChildProfile(
                    name = "Leo",
                    age = 6,
                    avatarId = "lion",
                    gradeLevel = "Kindergarten",
                    totalStars = 48,
                    currentStreakDays = 4,
                    lastPlayedTimestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                    todayPlayedMinutes = 18,
                    dailyGoalMinutes = 25,
                    isCurrentActive = true
                )
            )

            val kid2Id = dao.insertChild(
                ChildProfile(
                    name = "Maya",
                    age = 9,
                    avatarId = "astronaut",
                    gradeLevel = "3rd Grade",
                    totalStars = 115,
                    currentStreakDays = 7,
                    lastPlayedTimestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                    todayPlayedMinutes = 24,
                    dailyGoalMinutes = 30,
                    isCurrentActive = false
                )
            )

            // Seed Badges for Leo
            seedBadgesForChild(dao, kid1Id)
            // Seed Badges for Maya
            seedBadgesForChild(dao, kid2Id)

            val todayEpochDay = System.currentTimeMillis() / (1000L * 60 * 60 * 24)
            seedDailyMissionsForChild(dao, kid1Id, todayEpochDay)
            seedDailyMissionsForChild(dao, kid2Id, todayEpochDay)

            // Unlock some initial badges
            dao.unlockBadge(kid1Id, "FIRST_GAME", System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3)
            dao.unlockBadge(kid1Id, "MATH_STARTER", System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2)
            dao.unlockBadge(kid1Id, "STREAK_3", System.currentTimeMillis() - 1000 * 60 * 60 * 24)

            dao.unlockBadge(kid2Id, "FIRST_GAME", System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7)
            dao.unlockBadge(kid2Id, "MATH_STARTER", System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 6)
            dao.unlockBadge(kid2Id, "WORD_MASTER", System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 5)
            dao.unlockBadge(kid2Id, "MEMORY_GENIUS", System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 4)
            dao.unlockBadge(kid2Id, "STREAK_3", System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3)
            dao.unlockBadge(kid2Id, "STREAK_7", System.currentTimeMillis() - 1000 * 60 * 60 * 24)

            // Seed some past sessions for rich parent analytics
            val now = System.currentTimeMillis()
            val dayMs = 24L * 60 * 60 * 1000

            dao.insertGameSession(
                GameSession(
                    childId = kid1Id,
                    gameCategory = GameCategory.MATH.name,
                    gameTitle = "Addition Blast",
                    score = 100,
                    totalQuestions = 5,
                    correctAnswers = 5,
                    durationSeconds = 120,
                    starsEarned = 5,
                    timestamp = now - (15 * 60 * 1000)
                )
            )
            dao.insertGameSession(
                GameSession(
                    childId = kid1Id,
                    gameCategory = GameCategory.SPELLING.name,
                    gameTitle = "Jungle Phonics",
                    score = 80,
                    totalQuestions = 5,
                    correctAnswers = 4,
                    durationSeconds = 150,
                    starsEarned = 4,
                    timestamp = now - (60 * 60 * 1000)
                )
            )
            dao.insertGameSession(
                GameSession(
                    childId = kid1Id,
                    gameCategory = GameCategory.MEMORY.name,
                    gameTitle = "Animal Pair Safari",
                    score = 100,
                    totalQuestions = 6,
                    correctAnswers = 6,
                    durationSeconds = 95,
                    starsEarned = 5,
                    timestamp = now - dayMs
                )
            )
            dao.insertGameSession(
                GameSession(
                    childId = kid1Id,
                    gameCategory = GameCategory.SCIENCE.name,
                    gameTitle = "Curious Planet Trivia",
                    score = 75,
                    totalQuestions = 4,
                    correctAnswers = 3,
                    durationSeconds = 140,
                    starsEarned = 3,
                    timestamp = now - (dayMs * 2)
                )
            )
            dao.insertGameSession(
                GameSession(
                    childId = kid1Id,
                    gameCategory = GameCategory.SHAPES.name,
                    gameTitle = "Color Magic Lab",
                    score = 100,
                    totalQuestions = 5,
                    correctAnswers = 5,
                    durationSeconds = 85,
                    starsEarned = 5,
                    timestamp = now - (dayMs * 3)
                )
            )
        }

        fun getStandardBadgesForChild(childId: Long): List<Badge> = listOf(
            // Learning Module Completion Badges
            Badge(
                childId = childId,
                badgeCode = "MODULE_MATH_GRAD",
                title = "Math Galaxy Graduate",
                description = "Completed a full learning session in Math Galaxy!",
                iconEmoji = "🚀",
                category = "MODULES",
                rewardStars = 15,
                moduleName = "Math Galaxy",
                requirementDescription = "Complete 1 Math Galaxy learning session"
            ),
            Badge(
                childId = childId,
                badgeCode = "MODULE_WORD_GRAD",
                title = "Word Explorer Graduate",
                description = "Completed a full learning session in Word Explorer!",
                iconEmoji = "📚",
                category = "MODULES",
                rewardStars = 15,
                moduleName = "Word Explorer",
                requirementDescription = "Complete 1 Word Explorer vocabulary session"
            ),
            Badge(
                childId = childId,
                badgeCode = "MODULE_MEMORY_GRAD",
                title = "Memory Safari Graduate",
                description = "Completed a full concentration session in Memory Safari!",
                iconEmoji = "🧠",
                category = "MODULES",
                rewardStars = 15,
                moduleName = "Memory Safari",
                requirementDescription = "Complete 1 Memory Safari focus session"
            ),
            Badge(
                childId = childId,
                badgeCode = "MODULE_SCIENCE_GRAD",
                title = "Science Lab Graduate",
                description = "Completed a full curiosity session in Science Trivia!",
                iconEmoji = "🔬",
                category = "MODULES",
                rewardStars = 15,
                moduleName = "Science Trivia",
                requirementDescription = "Complete 1 Science & Nature trivia session"
            ),
            Badge(
                childId = childId,
                badgeCode = "MODULE_SHAPES_GRAD",
                title = "Shape Kingdom Graduate",
                description = "Completed a full geometry session in Shape Kingdom!",
                iconEmoji = "🎨",
                category = "MODULES",
                rewardStars = 15,
                moduleName = "Shape Kingdom",
                requirementDescription = "Complete 1 Color & Shapes session"
            ),
            Badge(
                childId = childId,
                badgeCode = "MODULE_ALL_FIVE",
                title = "Curriculum Conqueror",
                description = "Completed learning modules across all 5 core subjects!",
                iconEmoji = "👑",
                category = "MODULES",
                rewardStars = 30,
                moduleName = "All 5 Modules",
                requirementDescription = "Complete sessions in Math, Words, Memory, Science & Shapes"
            ),
            Badge(
                childId = childId,
                badgeCode = "MODULE_PERFECT_100",
                title = "Flawless Scholar",
                description = "Achieved 100% accuracy in a learning module challenge!",
                iconEmoji = "💯",
                category = "MODULES",
                rewardStars = 20,
                moduleName = "Any Module",
                requirementDescription = "Score 100% in any educational module"
            ),
            Badge(
                childId = childId,
                badgeCode = "MODULES_COUNT_5",
                title = "Module Explorer",
                description = "Completed 5 learning module sessions total!",
                iconEmoji = "🌟",
                category = "MODULES",
                rewardStars = 25,
                moduleName = "General",
                requirementDescription = "Complete 5 module sessions total"
            ),
            Badge(
                childId = childId,
                badgeCode = "MODULES_COUNT_10",
                title = "Academic Olympian",
                description = "Completed 10 learning module sessions with excellence!",
                iconEmoji = "🏅",
                category = "MODULES",
                rewardStars = 40,
                moduleName = "General",
                requirementDescription = "Complete 10 module sessions total"
            ),
            // Core Subject Mastery Badges
            Badge(childId = childId, badgeCode = "FIRST_GAME", title = "First Steps", description = "Completed your first educational adventure!", iconEmoji = "🚀", category = "GENERAL", rewardStars = 10, moduleName = "Any", requirementDescription = "Play any learning game"),
            Badge(childId = childId, badgeCode = "MATH_STARTER", title = "Math Explorer", description = "Solved 10 math puzzles correctly!", iconEmoji = "🔢", category = "MATH", rewardStars = 15, moduleName = "Math Galaxy", requirementDescription = "Solve 10 math problems"),
            Badge(childId = childId, badgeCode = "MATH_WIZARD", title = "Galaxy Mathematician", description = "Scored 100% in a Math Galaxy mission!", iconEmoji = "⚡", category = "MATH", rewardStars = 20, moduleName = "Math Galaxy", requirementDescription = "Score 100% in Math Galaxy"),
            Badge(childId = childId, badgeCode = "WORD_MASTER", title = "Spelling Champ", description = "Mastered spelling challenges with high accuracy!", iconEmoji = "📚", category = "SPELLING", rewardStars = 20, moduleName = "Word Explorer", requirementDescription = "Solve 4+ spelling questions correctly"),
            Badge(childId = childId, badgeCode = "MEMORY_GENIUS", title = "Memory Mastermind", description = "Found all memory pairs without any misses!", iconEmoji = "🧩", category = "MEMORY", rewardStars = 20, moduleName = "Memory Safari", requirementDescription = "Score 100% in Memory Safari"),
            Badge(childId = childId, badgeCode = "SCIENCE_EXPLORER", title = "Junior Scientist", description = "Discovered amazing nature & science facts!", iconEmoji = "🔬", category = "SCIENCE", rewardStars = 15, moduleName = "Science Trivia", requirementDescription = "Complete a Science Trivia session"),
            Badge(childId = childId, badgeCode = "SHAPE_ARTIST", title = "Geometry Hero", description = "Identified all magical colors and shapes!", iconEmoji = "🎨", category = "SHAPES", rewardStars = 15, moduleName = "Shape Kingdom", requirementDescription = "Score 100% in Shape Kingdom"),
            Badge(childId = childId, badgeCode = "STREAK_3", title = "3-Day Fire Streak", description = "Learned 3 days in a row!", iconEmoji = "🔥", category = "STREAK", rewardStars = 20, moduleName = "Habits", requirementDescription = "Maintain a 3-day learning streak"),
            Badge(childId = childId, badgeCode = "STREAK_7", title = "Super Weekly Hero", description = "Maintained a 7-day learning streak!", iconEmoji = "👑", category = "STREAK", rewardStars = 35, moduleName = "Habits", requirementDescription = "Maintain a 7-day learning streak"),
            Badge(childId = childId, badgeCode = "STAR_50", title = "Star Collector", description = "Earned 50 shiny learning stars!", iconEmoji = "⭐", category = "GENERAL", rewardStars = 25, moduleName = "Milestone", requirementDescription = "Accumulate 50 total stars"),
            Badge(childId = childId, badgeCode = "STAR_100", title = "Constellation Master", description = "Earned 100 golden stars across all games!", iconEmoji = "🌟", category = "GENERAL", rewardStars = 50, moduleName = "Milestone", requirementDescription = "Accumulate 100 total stars"),
            // Daily Mission Badges
            Badge(childId = childId, badgeCode = "DAILY_MATH_HERO", title = "Cosmic Calculator", description = "Mastered a daily Math Galaxy mission!", iconEmoji = "🔢", category = "MISSION", rewardStars = 10, moduleName = "Math Galaxy", requirementDescription = "Complete daily Math quest"),
            Badge(childId = childId, badgeCode = "DAILY_WORD_EXPLORER", title = "Word Enchanter", description = "Mastered a daily Word Explorer mission!", iconEmoji = "📖", category = "MISSION", rewardStars = 10, moduleName = "Word Explorer", requirementDescription = "Complete daily Word quest"),
            Badge(childId = childId, badgeCode = "DAILY_MEMORY_SAFARI", title = "Safari Scout", description = "Completed a daily Memory Safari mission!", iconEmoji = "🦁", category = "MISSION", rewardStars = 10, moduleName = "Memory Safari", requirementDescription = "Complete daily Memory quest"),
            Badge(childId = childId, badgeCode = "DAILY_SCIENCE_GENIUS", title = "Nature Prodigy", description = "Completed a daily Science & Trivia mission!", iconEmoji = "🔬", category = "MISSION", rewardStars = 10, moduleName = "Science Trivia", requirementDescription = "Complete daily Science quest"),
            Badge(childId = childId, badgeCode = "DAILY_SHAPE_CREATOR", title = "Pattern Wizard", description = "Completed a daily Color & Shapes mission!", iconEmoji = "🎨", category = "MISSION", rewardStars = 10, moduleName = "Shape Kingdom", requirementDescription = "Complete daily Shapes quest"),
            Badge(childId = childId, badgeCode = "MISSION_CHAMPION", title = "Daily All-Star Hero", description = "Completed all 3 daily missions in a single day!", iconEmoji = "🎖️", category = "MISSION", rewardStars = 25, moduleName = "Daily Quests", requirementDescription = "Complete all daily missions in one day")
        )

        suspend fun seedBadgesForChild(dao: KidsDao, childId: Long) {
            dao.insertBadges(getStandardBadgesForChild(childId))
        }

        suspend fun ensureBadgesForChild(dao: KidsDao, childId: Long) {
            val existing = dao.getBadgesForChildDirect(childId)
            if (existing.isEmpty()) {
                seedBadgesForChild(dao, childId)
            } else {
                val existingCodes = existing.map { it.badgeCode }.toSet()
                val missing = getStandardBadgesForChild(childId).filter { it.badgeCode !in existingCodes }
                if (missing.isNotEmpty()) {
                    dao.insertBadges(missing)
                }
            }
        }

        suspend fun seedDailyMissionsForChild(dao: KidsDao, childId: Long, dateEpochDay: Long) {
            val existing = dao.getDailyMissionsDirect(childId, dateEpochDay)
            if (existing.isEmpty()) {
                val missions = listOf(
                    DailyMission(
                        childId = childId,
                        missionCode = "MISSION_MATH_DAILY",
                        title = "Math Galaxy Quest",
                        description = "Complete 1 Math Galaxy session to boost your mental math power!",
                        iconEmoji = "🚀",
                        category = "MATH",
                        targetCount = 1,
                        currentProgress = 0,
                        isCompleted = false,
                        isRewardClaimed = false,
                        rewardBadgeCode = "DAILY_MATH_HERO",
                        rewardBadgeTitle = "Cosmic Calculator",
                        rewardBadgeIcon = "🔢",
                        rewardBadgeDescription = "Awarded for completing the Daily Math Galaxy quest!",
                        rewardStars = 10,
                        dateEpochDay = dateEpochDay
                    ),
                    DailyMission(
                        childId = childId,
                        missionCode = "MISSION_WORD_DAILY",
                        title = "Spelling Jungle Adventure",
                        description = "Spell words correctly in Word Explorer to unlock secret vocabulary!",
                        iconEmoji = "📚",
                        category = "SPELLING",
                        targetCount = 1,
                        currentProgress = 0,
                        isCompleted = false,
                        isRewardClaimed = false,
                        rewardBadgeCode = "DAILY_WORD_EXPLORER",
                        rewardBadgeTitle = "Word Enchanter",
                        rewardBadgeIcon = "📖",
                        rewardBadgeDescription = "Awarded for conquering the Daily Spelling challenge!",
                        rewardStars = 10,
                        dateEpochDay = dateEpochDay
                    ),
                    DailyMission(
                        childId = childId,
                        missionCode = "MISSION_MEMORY_DAILY",
                        title = "Memory Safari Scout",
                        description = "Find all matching animal pairs in Memory Safari without giving up!",
                        iconEmoji = "🧩",
                        category = "MEMORY",
                        targetCount = 1,
                        currentProgress = 0,
                        isCompleted = false,
                        isRewardClaimed = false,
                        rewardBadgeCode = "DAILY_MEMORY_SAFARI",
                        rewardBadgeTitle = "Safari Scout",
                        rewardBadgeIcon = "🦁",
                        rewardBadgeDescription = "Awarded for sharp focus and memory mastery today!",
                        rewardStars = 10,
                        dateEpochDay = dateEpochDay
                    )
                )
                dao.insertDailyMissions(missions)
            }
        }
    }
}
