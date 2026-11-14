package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.KidsDao
import com.example.data.model.Badge
import com.example.data.model.ChildProfile
import com.example.data.model.DailyMission
import com.example.data.model.GameCategory
import com.example.data.model.GameSession
import com.example.data.model.ParentSettings
import com.example.data.model.SubjectMastery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class KidsRepository(private val dao: KidsDao) {

    val allChildren: Flow<List<ChildProfile>> = dao.getAllChildren()
    val activeChild: Flow<ChildProfile?> = dao.getActiveChild()
    val allRecentSessions: Flow<List<GameSession>> = dao.getAllRecentSessions()
    val parentSettings: Flow<ParentSettings?> = dao.getParentSettings()

    fun getTodayEpochDay(): Long = System.currentTimeMillis() / (1000L * 60 * 60 * 24)

    fun getSessionsForChild(childId: Long): Flow<List<GameSession>> =
        dao.getSessionsForChild(childId)

    fun getBadgesForChild(childId: Long): Flow<List<Badge>> =
        dao.getBadgesForChild(childId)

    fun getDailyMissionsForChild(childId: Long): Flow<List<DailyMission>> {
        return dao.getDailyMissionsForChild(childId, getTodayEpochDay())
    }

    suspend fun ensureDailyMissions(childId: Long) {
        AppDatabase.seedDailyMissionsForChild(dao, childId, getTodayEpochDay())
    }

    suspend fun setActiveChild(childId: Long) {
        dao.clearActiveChildren()
        dao.setActiveChild(childId)
        ensureDailyMissions(childId)
    }

    suspend fun addChild(child: ChildProfile): Long {
        val newId = dao.insertChild(child)
        // Seed badges for the new child
        AppDatabase.seedBadgesForChild(dao, newId)
        // Seed daily missions for the new child
        AppDatabase.seedDailyMissionsForChild(dao, newId, getTodayEpochDay())
        return newId
    }

    suspend fun updateChild(child: ChildProfile) {
        dao.updateChild(child)
    }

    suspend fun deleteChild(childId: Long) {
        dao.deleteChild(childId)
        val children = dao.getAllChildren().firstOrNull() ?: emptyList()
        if (children.isNotEmpty() && children.none { it.isCurrentActive }) {
            dao.setActiveChild(children.first().id)
        }
    }

    suspend fun saveParentSettings(settings: ParentSettings) {
        dao.saveParentSettings(settings)
    }

    suspend fun recordGameCompletion(
        childId: Long,
        gameCategory: GameCategory,
        gameTitle: String,
        score: Int,
        totalQuestions: Int,
        correctAnswers: Int,
        durationSeconds: Int,
        starsEarned: Int
    ): List<Badge> {
        val now = System.currentTimeMillis()
        val minutesSpent = maxOf(1, durationSeconds / 60)

        // Insert session record
        dao.insertGameSession(
            GameSession(
                childId = childId,
                gameCategory = gameCategory.name,
                gameTitle = gameTitle,
                score = score,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                durationSeconds = durationSeconds,
                starsEarned = starsEarned,
                timestamp = now
            )
        )

        // Increment stars and playtime
        dao.incrementChildStarsAndPlaytime(
            childId = childId,
            stars = starsEarned,
            minutesToAdd = minutesSpent,
            timestamp = now
        )

        // Update Daily Missions progress
        val todayEpochDay = getTodayEpochDay()
        ensureDailyMissions(childId)
        val activeMissions = dao.getDailyMissionsDirect(childId, todayEpochDay)
        for (mission in activeMissions) {
            val matchesCategory = mission.category.equals(gameCategory.name, ignoreCase = true) ||
                    mission.category.equals("ALL", ignoreCase = true)
            if (matchesCategory && !mission.isCompleted) {
                val newProgress = (mission.currentProgress + 1).coerceAtMost(mission.targetCount)
                val isNowCompleted = newProgress >= mission.targetCount
                dao.updateMissionProgress(
                    missionId = mission.id,
                    progress = newProgress,
                    isCompleted = isNowCompleted
                )
            }
        }

        // Check if all today's missions are completed
        val updatedMissions = dao.getDailyMissionsDirect(childId, todayEpochDay)
        if (updatedMissions.isNotEmpty() && updatedMissions.all { it.isCompleted }) {
            dao.unlockBadge(childId, "MISSION_CHAMPION", now)
        }

        // Ensure standard badges are present in Room database
        AppDatabase.ensureBadgesForChild(dao, childId)

        val newlyUnlockedBadges = mutableListOf<Badge>()

        suspend fun attemptUnlock(code: String) {
            val badge = dao.getBadgeByCode(childId, code) ?: return
            if (!badge.isUnlocked) {
                val updatedRows = dao.unlockBadgeIfNotUnlocked(childId, code, now)
                if (updatedRows > 0) {
                    if (badge.rewardStars > 0) {
                        dao.incrementChildStarsAndPlaytime(
                            childId = childId,
                            stars = badge.rewardStars,
                            minutesToAdd = 0,
                            timestamp = now
                        )
                    }
                    newlyUnlockedBadges.add(badge.copy(isUnlocked = true, unlockedAtTimestamp = now))
                }
            }
        }

        // 1. General First Game
        attemptUnlock("FIRST_GAME")

        // 2. Specific Learning Module Completion Badges
        when (gameCategory) {
            GameCategory.MATH -> attemptUnlock("MODULE_MATH_GRAD")
            GameCategory.SPELLING -> attemptUnlock("MODULE_WORD_GRAD")
            GameCategory.MEMORY -> attemptUnlock("MODULE_MEMORY_GRAD")
            GameCategory.SCIENCE -> attemptUnlock("MODULE_SCIENCE_GRAD")
            GameCategory.SHAPES -> attemptUnlock("MODULE_SHAPES_GRAD")
        }

        // 3. Score & Accuracy Module Badges
        if (score == 100) {
            attemptUnlock("MODULE_PERFECT_100")
        }
        if (score == 100 && gameCategory == GameCategory.MATH) {
            attemptUnlock("MATH_WIZARD")
        }
        if (gameCategory == GameCategory.SPELLING && correctAnswers >= 4) {
            attemptUnlock("WORD_MASTER")
        }
        if (gameCategory == GameCategory.MEMORY && score == 100) {
            attemptUnlock("MEMORY_GENIUS")
        }
        if (gameCategory == GameCategory.SCIENCE) {
            attemptUnlock("SCIENCE_EXPLORER")
        }
        if (gameCategory == GameCategory.SHAPES && score == 100) {
            attemptUnlock("SHAPE_ARTIST")
        }

        // 4. Module Session Milestones & All-5-Curriculum Conqueror
        val totalSessions = dao.getCompletedSessionCountForChild(childId)
        if (totalSessions >= 5) {
            attemptUnlock("MODULES_COUNT_5")
        }
        if (totalSessions >= 10) {
            attemptUnlock("MODULES_COUNT_10")
        }

        val completedCategories = dao.getDistinctCompletedCategories(childId)
        val requiredCoreCategories = listOf("MATH", "SPELLING", "MEMORY", "SCIENCE", "SHAPES")
        if (completedCategories.containsAll(requiredCoreCategories)) {
            attemptUnlock("MODULE_ALL_FIVE")
        }

        // 5. Star and Streak Milestones
        val child = dao.getChildById(childId)
        if (child != null) {
            if (child.totalStars >= 50) {
                attemptUnlock("STAR_50")
            }
            if (child.totalStars >= 100) {
                attemptUnlock("STAR_100")
            }
            if (child.currentStreakDays >= 3) {
                attemptUnlock("STREAK_3")
            }
            if (child.currentStreakDays >= 7) {
                attemptUnlock("STREAK_7")
            }
        }

        return newlyUnlockedBadges
    }

    suspend fun claimMissionReward(childId: Long, missionId: Long): DailyMission? {
        val now = System.currentTimeMillis()
        val todayEpochDay = getTodayEpochDay()
        val missions = dao.getDailyMissionsDirect(childId, todayEpochDay)
        val mission = missions.find { it.id == missionId } ?: return null

        if (!mission.isRewardClaimed) {
            // Mark claimed
            dao.markMissionRewardClaimed(missionId)

            // Award reward stars
            dao.incrementChildStarsAndPlaytime(
                childId = childId,
                stars = mission.rewardStars,
                minutesToAdd = 0,
                timestamp = now
            )

            // Unlock the corresponding virtual badge
            dao.unlockBadge(childId, mission.rewardBadgeCode, now)

            // Check if all missions are completed, unlock all-star hero
            val refreshed = dao.getDailyMissionsDirect(childId, todayEpochDay)
            if (refreshed.isNotEmpty() && refreshed.all { it.isCompleted }) {
                dao.unlockBadge(childId, "MISSION_CHAMPION", now)
            }

            return mission.copy(isRewardClaimed = true)
        }
        return mission
    }

    suspend fun computeSubjectMastery(childId: Long): List<SubjectMastery> {
        val list = mutableListOf<SubjectMastery>()
        for (cat in GameCategory.values()) {
            val sessions = dao.getSessionsByChildAndCategory(childId, cat.name)
            val totalPlayed = sessions.size
            val totalQuestions = sessions.sumOf { it.totalQuestions }
            val correct = sessions.sumOf { it.correctAnswers }
            val accuracy = if (totalQuestions > 0) (correct * 100) / totalQuestions else 85
            val starsWon = sessions.sumOf { it.starsEarned }
            val level = when {
                totalPlayed >= 5 && accuracy >= 80 -> "Mastery Level 3 ⭐⭐⭐"
                totalPlayed >= 2 && accuracy >= 60 -> "Explorer Level 2 ⭐⭐"
                else -> "Starter Level 1 ⭐"
            }
            list.add(
                SubjectMastery(
                    category = cat,
                    totalPlayed = totalPlayed,
                    accuracyPercentage = accuracy,
                    starsWon = starsWon,
                    recommendedLevel = level
                )
            )
        }
        return list
    }
}
