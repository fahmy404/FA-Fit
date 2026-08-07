package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.api.ChallengeService
import com.example.api.UserService
import com.example.api.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// --- Data Models ---

data class AppState(
    val caloriesConsumed: Int = 0,
    val calorieGoal: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val challenges: List<Challenge> = emptyList(),
    val isDarkTheme: Boolean = true,
    val selectedDiet: Int = 0,
    val zigzagEnabled: Boolean = false,
    val adaptiveTDEEEnabled: Boolean = true,
    val exercises: List<Exercise> = emptyList(),
    val isProfileSetup: Boolean = false,
    val isProfileLoading: Boolean = true,
    val name: String = "",
    val profileImage: String = "",
    val favoriteMeals: List<com.example.api.MealAnalysisResult> = emptyList(),
    // Water tracking
    val waterConsumed: Int = 0,   // in cups (250ml each)
    val waterGoal: Int = 8,       // default 8 cups
    // Calendar
    val selectedDate: String = todayDateString(),
    // Workout schedule: date -> list of exercises
    val workoutSchedule: Map<String, List<Exercise>> = emptyMap(),
    // User weight for water goal calculation
    val userWeight: Int = 70,
    // Points
    val totalPoints: Int = 0,
    val userLevel: Int = 1
)

data class DailyLog(
    val date: String = "",
    val caloriesConsumed: Int = 0,
    val waterConsumed: Int = 0
)

data class Challenge(
    val id: Int,
    val title: String,
    val timeLeft: String,
    val rank: Int,
    val code: String = "",
    val type: String = "calories", // "calories", "workouts", "points"
    val targetValue: Int = 0
)

data class Exercise(
    val id: Int,
    val name: String,
    val targets: String,
    val sets: List<ExerciseSet>
)

data class ExerciseSet(
    val setNum: Int,
    val weight: Int,
    val reps: Int,
    val isDone: Boolean
)

data class WorkoutDay(
    val dayName: String,
    val dayKey: String, // e.g. "SAT", "SUN", etc.
    val exercises: List<Exercise> = emptyList()
)

fun todayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

// --- ViewModel ---

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val challengeService = ChallengeService()
    private val userService = UserService()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        loadProfile()
        loadChallenges()
        loadDailyLog(todayDateString())
        loadFavoriteMeals()
        loadWorkoutForDate(todayDateString())
    }

    fun reloadAllData() {
        val today = todayDateString()
        loadProfile()
        loadChallenges()
        loadDailyLog(today)
        loadFavoriteMeals()
        loadWorkoutForDate(today)
    }

    fun loadProfile() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                val profile = userService.getProfile()
                if (profile != null) {
                    // Calculate water goal: weight * 30ml / 250ml per cup
                    val waterGoalCups = maxOf(6, (profile.weight * 30) / 250)
                    _state.update {
                        it.copy(
                            isProfileSetup = true,
                            isProfileLoading = false,
                            name = profile.name,
                            calorieGoal = profile.calorieGoal,
                            profileImage = user.photoUrl?.toString() ?: "",
                            userWeight = profile.weight,
                            waterGoal = waterGoalCups
                        )
                    }
                    // Reload daily data now that we have user
                    val today = todayDateString()
                    loadDailyLog(today)
                    loadFavoriteMeals()
                    loadWorkoutForDate(today)
                } else {
                    _state.update {
                        it.copy(
                            isProfileSetup = false,
                            isProfileLoading = false,
                            name = user.displayName ?: "",
                            profileImage = user.photoUrl?.toString() ?: ""
                        )
                    }
                }
            } else {
                _state.update { it.copy(isProfileLoading = false) }
            }
        }
    }

    fun saveProfile(profile: UserProfile, onSuccess: () -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            userService.saveProfile(profile)
        }
        val waterGoalCups = maxOf(6, (profile.weight * 30) / 250)
        _state.update {
            it.copy(
                isProfileSetup = true,
                name = profile.name,
                calorieGoal = profile.calorieGoal,
                userWeight = profile.weight,
                waterGoal = waterGoalCups
            )
        }
        onSuccess()
    }

    fun loadChallenges() {
        viewModelScope.launch {
            val fetchedChallenges = challengeService.getActiveChallenges()
            if (fetchedChallenges.isNotEmpty()) {
                _state.update { it.copy(challenges = fetchedChallenges) }
            }
        }
    }

    // ---- Daily Log (Calories + Water per day) ----

    fun loadDailyLog(date: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                val doc = db.collection("users").document(userId)
                    .collection("dailyLogs").document(date).get().await()
                if (doc.exists()) {
                    _state.update {
                        it.copy(
                            caloriesConsumed = doc.getLong("caloriesConsumed")?.toInt() ?: 0,
                            waterConsumed = doc.getLong("waterConsumed")?.toInt() ?: 0,
                            protein = doc.getLong("protein")?.toInt() ?: 0,
                            carbs = doc.getLong("carbs")?.toInt() ?: 0,
                            fat = doc.getLong("fat")?.toInt() ?: 0,
                            selectedDate = date
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            caloriesConsumed = 0,
                            waterConsumed = 0,
                            protein = 0,
                            carbs = 0,
                            fat = 0,
                            selectedDate = date
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveDailyLog() {
        val userId = auth.currentUser?.uid ?: return
        val date = _state.value.selectedDate
        val data = hashMapOf(
            "caloriesConsumed" to _state.value.caloriesConsumed,
            "waterConsumed" to _state.value.waterConsumed,
            "protein" to _state.value.protein,
            "carbs" to _state.value.carbs,
            "fat" to _state.value.fat,
            "calorieGoal" to _state.value.calorieGoal,
            "date" to date
        )
        db.collection("users").document(userId)
            .collection("dailyLogs").document(date).set(data)
    }

    fun selectDate(date: String) {
        loadDailyLog(date)
    }

    // ---- Calories ----

    fun addCalories(amount: Int) {
        _state.update { it.copy(caloriesConsumed = it.caloriesConsumed + amount) }
        saveDailyLog()
    }

    fun addMacros(protein: Int, carbs: Int, fat: Int) {
        _state.update {
            it.copy(
                protein = it.protein + protein,
                carbs = it.carbs + carbs,
                fat = it.fat + fat
            )
        }
        saveDailyLog()
    }

    fun resetDailyCalories() {
        _state.update {
            it.copy(
                caloriesConsumed = 0,
                protein = 0,
                carbs = 0,
                fat = 0
            )
        }
        saveDailyLog()
    }

    // ---- Water ----

    fun addWater(cups: Int = 1) {
        _state.update { it.copy(waterConsumed = (it.waterConsumed + cups).coerceAtMost(it.waterGoal + 4)) }
        saveDailyLog()
    }

    fun removeWater() {
        _state.update { it.copy(waterConsumed = (it.waterConsumed - 1).coerceAtLeast(0)) }
        saveDailyLog()
    }

    fun setWaterGoal(cups: Int) {
        _state.update { it.copy(waterGoal = cups) }
    }

    // ---- Favorites ----

    fun addFavoriteMeal(meal: com.example.api.MealAnalysisResult) {
        _state.update { it.copy(favoriteMeals = it.favoriteMeals + meal) }
        saveFavoriteMeal(meal)
    }

    private fun saveFavoriteMeal(meal: com.example.api.MealAnalysisResult) {
        val userId = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "name" to meal.name,
            "calories" to meal.calories,
            "protein" to meal.protein,
            "carbs" to meal.carbs,
            "fat" to meal.fat
        )
        db.collection("users").document(userId)
            .collection("favorites").add(data)
    }

    fun loadFavoriteMeals() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val docs = db.collection("users").document(userId)
                    .collection("favorites").get().await()
                val meals = docs.map { doc ->
                    com.example.api.MealAnalysisResult(
                        calories = doc.getLong("calories")?.toInt() ?: 0,
                        protein = doc.getLong("protein")?.toInt() ?: 0,
                        carbs = doc.getLong("carbs")?.toInt() ?: 0,
                        fat = doc.getLong("fat")?.toInt() ?: 0,
                        name = doc.getString("name") ?: ""
                    )
                }
                _state.update { it.copy(favoriteMeals = meals) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ---- Challenges ----

    fun createChallenge(title: String, type: String = "calories", targetValue: Int = 0, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val generatedCode = "CH" + (1000..9999).random().toString()
            val newChallenge = Challenge(
                id = _state.value.challenges.size + 1,
                title = title,
                timeLeft = "7 أيام متبقية",
                rank = 1,
                code = generatedCode,
                type = type,
                targetValue = targetValue
            )
            challengeService.createChallenge(newChallenge)
            _state.update { it.copy(challenges = it.challenges + newChallenge) }
            onSuccess(generatedCode)
        }
    }

    fun joinChallenge(code: String) {
        viewModelScope.launch {
            val challenge = challengeService.joinChallenge(code)
            if (challenge != null) {
                _state.update { it.copy(challenges = it.challenges + challenge) }
            }
        }
    }

    // ---- Gym / Exercises ----

    fun addExercise(name: String, target: String) {
        val date = _state.value.selectedDate
        val currentExercises = _state.value.workoutSchedule[date] ?: emptyList()
        val newExercise = Exercise(
            id = (currentExercises.maxOfOrNull { it.id } ?: 0) + 1,
            name = name,
            targets = target,
            sets = listOf(ExerciseSet(1, 0, 0, false))
        )
        val updatedSchedule = _state.value.workoutSchedule.toMutableMap()
        updatedSchedule[date] = currentExercises + newExercise
        _state.update { it.copy(workoutSchedule = updatedSchedule, exercises = updatedSchedule[date] ?: emptyList()) }
        saveWorkoutToFirestore(date, updatedSchedule[date] ?: emptyList())
    }

    fun deleteExercise(id: Int) {
        val date = _state.value.selectedDate
        val updatedSchedule = _state.value.workoutSchedule.toMutableMap()
        updatedSchedule[date] = (updatedSchedule[date] ?: emptyList()).filter { it.id != id }
        _state.update { it.copy(workoutSchedule = updatedSchedule, exercises = updatedSchedule[date] ?: emptyList()) }
        saveWorkoutToFirestore(date, updatedSchedule[date] ?: emptyList())
    }

    fun loadWorkoutForDate(date: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(userId)
                    .collection("workouts").document(date).get().await()
                if (doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val rawList = doc.get("exercises") as? List<Map<String, Any>> ?: emptyList()
                    val exercises = rawList.map { exMap ->
                        @Suppress("UNCHECKED_CAST")
                        val rawSets = exMap["sets"] as? List<Map<String, Any>> ?: emptyList()
                        Exercise(
                            id = (exMap["id"] as? Long)?.toInt() ?: 0,
                            name = exMap["name"] as? String ?: "",
                            targets = exMap["targets"] as? String ?: "",
                            sets = rawSets.map { setMap ->
                                ExerciseSet(
                                    setNum = (setMap["setNum"] as? Long)?.toInt() ?: 0,
                                    weight = (setMap["weight"] as? Long)?.toInt() ?: 0,
                                    reps = (setMap["reps"] as? Long)?.toInt() ?: 0,
                                    isDone = setMap["isDone"] as? Boolean ?: false
                                )
                            }
                        )
                    }
                    val updatedSchedule = _state.value.workoutSchedule.toMutableMap()
                    updatedSchedule[date] = exercises
                    _state.update { it.copy(workoutSchedule = updatedSchedule, exercises = exercises) }
                } else {
                    val updatedSchedule = _state.value.workoutSchedule.toMutableMap()
                    updatedSchedule[date] = emptyList()
                    _state.update { it.copy(workoutSchedule = updatedSchedule, exercises = emptyList()) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveWorkoutToFirestore(date: String, exercises: List<Exercise>) {
        val userId = auth.currentUser?.uid ?: return
        val exercisesData = exercises.map { ex ->
            hashMapOf(
                "id" to ex.id,
                "name" to ex.name,
                "targets" to ex.targets,
                "sets" to ex.sets.map { s ->
                    hashMapOf(
                        "setNum" to s.setNum,
                        "weight" to s.weight,
                        "reps" to s.reps,
                        "isDone" to s.isDone
                    )
                }
            )
        }
        db.collection("users").document(userId)
            .collection("workouts").document(date)
            .set(hashMapOf("exercises" to exercisesData))
    }

    fun updateExerciseSet(exerciseId: Int, setNum: Int, weight: Int, reps: Int) {
        val date = _state.value.selectedDate
        val updatedSchedule = _state.value.workoutSchedule.toMutableMap()
        updatedSchedule[date] = (updatedSchedule[date] ?: emptyList()).map { ex ->
            if (ex.id == exerciseId) {
                ex.copy(sets = ex.sets.map { set ->
                    if (set.setNum == setNum) set.copy(weight = weight, reps = reps) else set
                })
            } else ex
        }
        _state.update { it.copy(workoutSchedule = updatedSchedule, exercises = updatedSchedule[date] ?: emptyList()) }
        saveWorkoutToFirestore(date, updatedSchedule[date] ?: emptyList())
    }

    fun addExerciseSet(exerciseId: Int) {
        val date = _state.value.selectedDate
        val updatedSchedule = _state.value.workoutSchedule.toMutableMap()
        updatedSchedule[date] = (updatedSchedule[date] ?: emptyList()).map { ex ->
            if (ex.id == exerciseId) {
                val nextSetNum = (ex.sets.maxOfOrNull { it.setNum } ?: 0) + 1
                ex.copy(sets = ex.sets + ExerciseSet(nextSetNum, 0, 0, false))
            } else ex
        }
        _state.update { it.copy(workoutSchedule = updatedSchedule, exercises = updatedSchedule[date] ?: emptyList()) }
        saveWorkoutToFirestore(date, updatedSchedule[date] ?: emptyList())
    }

    fun addSetToExercise(exerciseId: Int) = addExerciseSet(exerciseId)

    fun toggleExerciseSet(exerciseId: Int, setNum: Int) {
        val date = _state.value.selectedDate
        val updatedSchedule = _state.value.workoutSchedule.toMutableMap()
        updatedSchedule[date] = (updatedSchedule[date] ?: emptyList()).map { ex ->
            if (ex.id == exerciseId) {
                ex.copy(sets = ex.sets.map { s ->
                    if (s.setNum == setNum) s.copy(isDone = !s.isDone) else s
                })
            } else ex
        }
        _state.update { it.copy(workoutSchedule = updatedSchedule, exercises = updatedSchedule[date] ?: emptyList()) }
        saveWorkoutToFirestore(date, updatedSchedule[date] ?: emptyList())
    }

    // ---- Theme ----

    fun toggleTheme(isDark: Boolean) {
        _state.update { it.copy(isDarkTheme = isDark) }
    }

    // ---- Diet Settings ----

    fun selectDiet(index: Int) {
        _state.update { it.copy(selectedDiet = index) }
    }

    fun toggleZigzag(enabled: Boolean) {
        _state.update { it.copy(zigzagEnabled = enabled) }
    }

    fun toggleAdaptive(enabled: Boolean) {
        _state.update { it.copy(adaptiveTDEEEnabled = enabled) }
    }

    // ---- Logout ----

    fun logout() {
        auth.signOut()
        // Reset all state completely so next login starts fresh
        _state.value = AppState(isProfileLoading = false)
    }

    // Calculate real progress for a challenge based on actual app data
    fun getChallengeProgress(challenge: Challenge): Int {
        val s = _state.value
        return when (challenge.type) {
            "calories" -> s.caloriesConsumed
            "workouts" -> s.workoutSchedule.values.sumOf { exList ->
                exList.sumOf { ex -> ex.sets.count { it.isDone } }
            }
            "volume" -> s.workoutSchedule.values.sumOf { exList ->
                exList.sumOf { ex -> ex.sets.sumOf { s -> if (s.isDone) s.weight * s.reps else 0 } }
            }
            else -> 0
        }
    }
}