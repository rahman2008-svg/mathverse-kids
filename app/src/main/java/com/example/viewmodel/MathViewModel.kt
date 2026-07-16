package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class Screen {
    object Splash : Screen()
    object Welcome : Screen()
    object ProfileCreate : Screen()
    object Dashboard : Screen()
    object CompleteMathList : Screen()
    data class TopicQuiz(val topic: String, val level: String) : Screen()
    object MathGameWorld : Screen()
    data class ActiveGame(val gameType: GameType) : Screen()
    object SmartPractice : Screen()
    data class SmartPracticeSession(val mode: PracticeMode, val customParam: Int = 30) : Screen()
    object MathNotebook : Screen()
    object ParentDashboard : Screen()
    object TeacherMode : Screen()
    object RewardSystem : Screen()
    object WorksheetMode : Screen()
    object VoiceMath : Screen()
    object AboutDeveloper : Screen()
}

enum class GameType { ADVENTURE, ROCKET, OCEAN, RACE }
enum class PracticeMode { DAILY, WEAK_TOPIC, SPEED_TEST, MEMORY, REVISION }

data class QuizQuestion(
    val equation: String,
    val options: List<String>,
    val correctAnswer: String,
    val visualHint: String,
    val topic: String,
    val illustrationType: String = "text" // text, objects, shape, clock, fractional
)

data class FormulaItem(
    val title: String,
    val formula: String,
    val description: String,
    val illustration: String
)

data class AvatarAccessory(
    val id: String,
    val name: String,
    val category: String, // "Hat", "Glasses", "Dress", "Bag"
    val cost: Int,
    val icon: String // Visual emoji symbol representing the accessory
)

data class BadgeItem(
    val id: String,
    val title: String,
    val requirement: String,
    val icon: String,
    val unlocked: Boolean
)

class MathViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MathRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MathRepository(database.mathDao())
    }

    // DB States
    val userProfile = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val allMistakes = repository.allMistakes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allSavedProblems = repository.allSavedProblems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTeacherClasses = repository.allTeacherClasses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTeacherHomework = repository.allTeacherHomework.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allStudentProgress = repository.allStudentProgress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Navigation State
    val currentScreen = MutableStateFlow<Screen>(Screen.Splash)

    // Current Active Quiz/Game State
    val activeQuizQuestion = MutableStateFlow<QuizQuestion?>(null)
    val quizCorrectAnswered = MutableStateFlow<Boolean?>(null) // null = not answered, true = correct, false = wrong
    val selectedAnswer = MutableStateFlow<String?>(null)
    val streakCounter = MutableStateFlow(0)

    // Counting Game Specific
    val currentCountValue = MutableStateFlow(1)
    val targetCountValue = MutableStateFlow(5)

    // Math Adventure States
    val adventureLevelIndex = MutableStateFlow(0) // 0 to 6 (Forest, Castle, Desert, Snow, Volcano, Sky, Space)
    val adventureFeedback = MutableStateFlow<String?>(null)

    // Rocket Math States
    val rocketHeight = MutableStateFlow(10f) // height percentage (10% to 100%)

    // Ocean Math States
    val oceanFishesCaught = MutableStateFlow(0)

    // Car Race States
    val carRaceSpeed = MutableStateFlow(40) // km/h

    // Smart Practice States
    val practiceQuestionsLeft = MutableStateFlow(20)
    val practiceCorrectAnswers = MutableStateFlow(0)
    val practiceTotalQuestions = MutableStateFlow(20)
    val speedTestSecondsLeft = MutableStateFlow(60)
    val isPracticeSessionFinished = MutableStateFlow(false)
    val memorySequence = MutableStateFlow<List<Int>>(emptyList())
    val memoryUserInput = MutableStateFlow<List<Int>>(emptyList())
    val memoryGameState = MutableStateFlow("SHOWING") // "SHOWING", "INPUTTING", "SUCCESS", "FAIL"

    // Worksheet Generator State
    val generatedWorksheet = MutableStateFlow<List<String>>(emptyList())
    val worksheetDifficulty = MutableStateFlow("Easy")

    // Voice Math State
    val voiceMathInput = MutableStateFlow("")
    val voiceMathFeedback = MutableStateFlow("")

    // Sound effect trigger (Mocks vocal play and audio animations in UI)
    val uiToastMessage = MutableSharedFlow<String>()

    // Accessories definitions
    val accessoryShopList = listOf(
        AvatarAccessory("hat_wizard", "Wizard Hat", "Hat", 50, "🧙"),
        AvatarAccessory("hat_cap", "Cool Cap", "Hat", 25, "🧢"),
        AvatarAccessory("glasses_stars", "Star Glasses", "Glasses", 40, "⭐"),
        AvatarAccessory("glasses_cool", "Cool Shades", "Glasses", 30, "😎"),
        AvatarAccessory("dress_cape", "Hero Cape", "Dress", 80, "🧣"),
        AvatarAccessory("dress_bowtie", "Fancy Bowtie", "Dress", 20, "🎀"),
        AvatarAccessory("bag_backpack", "Adventure Pack", "Bag", 60, "🎒")
    )

    // Formulas
    val formulasList = listOf(
        FormulaItem("Rectangle Area", "Area = Length × Width", "The size of a flat surface inside a rectangle.", "📏 [ L ] × [ W ]"),
        FormulaItem("Triangle Area", "Area = ½ × Base × Height", "Half of the rectangle with same base and height.", "📐 ½ × B × H"),
        FormulaItem("Perimeter", "P = 2 × (Length + Width)", "The total distance around the outside of a shape.", "🔄 Sum of all sides"),
        FormulaItem("Circle Fraction", "Half = ½, Quarter = ¼", "Parts of a whole cookie or circle pie.", "🍕 Shaded portions"),
        FormulaItem("Addition Rule", "Parts make a Whole", "Add numbers together to find the sum total.", "➕ 2 + 3 = 5"),
        FormulaItem("Multiplication Table", "Repeated Addition", "6 × 7 means adding 6, seven times over.", "✖ 6 + 6 + 6...")
    )

    // Initial Database Populators
    fun initDefaultProfileIfNone() {
        viewModelScope.launch {
            val profile = repository.getProfile()
            if (profile == null) {
                // Populate default profile values
                repository.saveProfile(UserProfile(profileCreated = false))
            }
            // Populate simulated students for teacher mode
            if (repository.allStudentProgress.first().isEmpty()) {
                repository.addStudentProgress("Aarav Smith", "Class 1", 90, 45, 120)
                repository.addStudentProgress("Emma Watson", "Class 1", 95, 52, 150)
                repository.addStudentProgress("Li Wei", "Class 2", 85, 40, 95)
                repository.addStudentProgress("Zara Ahmed", "Class 2", 88, 38, 110)
            }
        }
    }

    // Profile Management
    fun createProfile(name: String, age: Int, className: String, avatar: String) {
        viewModelScope.launch {
            val existing = repository.getProfile() ?: UserProfile()
            val updated = existing.copy(
                name = name,
                age = age,
                className = className,
                avatarName = avatar,
                profileCreated = true,
                stars = 10,  // Start with bonus!
                coins = 20,
                xp = 50,
                level = 1
            )
            repository.saveProfile(updated)
            uiToastMessage.emit("Welcome to MathVerse, $name! 🌱")
            currentScreen.value = Screen.Dashboard
        }
    }

    fun updateAvatar(avatar: String) {
        viewModelScope.launch {
            val profile = repository.getProfile() ?: return@launch
            repository.saveProfile(profile.copy(avatarName = avatar))
            uiToastMessage.emit("Avatar changed to $avatar!")
        }
    }

    // Reward Store & Avatar Decoration
    fun buyAccessory(accessory: AvatarAccessory) {
        viewModelScope.launch {
            val profile = repository.getProfile() ?: return@launch
            if (profile.coins >= accessory.cost) {
                val currentUnlocked = profile.unlockedAccessories.split(",").filter { it.isNotEmpty() }.toMutableSet()
                currentUnlocked.add(accessory.id)
                val newCoins = profile.coins - accessory.cost
                val updated = profile.copy(
                    coins = newCoins,
                    unlockedAccessories = currentUnlocked.joinToString(",")
                )
                repository.saveProfile(updated)
                uiToastMessage.emit("Bought ${accessory.name}! 🎉")
            } else {
                uiToastMessage.emit("Not enough Coins! 🪙 Practice to earn more!")
            }
        }
    }

    fun equipAccessory(accessoryId: String) {
        viewModelScope.launch {
            val profile = repository.getProfile() ?: return@launch
            val updated = profile.copy(equippedAccessory = accessoryId)
            repository.saveProfile(updated)
            uiToastMessage.emit("Equipped successfully!")
        }
    }

    fun unequipAccessory() {
        viewModelScope.launch {
            val profile = repository.getProfile() ?: return@launch
            val updated = profile.copy(equippedAccessory = "")
            repository.saveProfile(updated)
            uiToastMessage.emit("Unequipped accessory!")
        }
    }

    // BADGES list dynamically calculated
    fun getBadges(profile: UserProfile?): List<BadgeItem> {
        val stars = profile?.stars ?: 0
        val coins = profile?.coins ?: 0
        val xp = profile?.xp ?: 0
        val level = profile?.level ?: 0

        return listOf(
            BadgeItem("badge_explorer", "🌱 Number Explorer", "Start learning journey", "🌱", true),
            BadgeItem("badge_calc", "🔢 Calculation Master", "Reach Level 3", "🔢", level >= 3),
            BadgeItem("badge_solver", "🧠 Problem Solver", "Solve 50+ Questions (500 XP)", "🧠", xp >= 500),
            BadgeItem("badge_champ", "🏆 Math Champion", "Collect 100 Stars", "🏆", stars >= 100),
            BadgeItem("badge_rich", "🪙 Math Tycoon", "Collect 100 Coins", "🪙", coins >= 100),
            BadgeItem("badge_perfect", "🔥 Streak Master", "Keep learning active", "🔥", profile?.streak ?: 0 >= 3)
        )
    }

    // Dynamic Math Question Generator Engine
    fun loadNextQuestion(topic: String, level: String) {
        quizCorrectAnswered.value = null
        selectedAnswer.value = null

        val r = Random
        val question = when (topic) {
            "Counting 1–1000" -> {
                val base = if (level == "Beginner") r.nextInt(1, 99) else r.nextInt(100, 990)
                val countType = r.nextInt(0, 3)
                if (countType == 0) {
                    val ans = base + 1
                    QuizQuestion(
                        equation = "What number comes after $base?",
                        options = listOf(ans.toString(), (base + 2).toString(), (base - 1).toString(), (base + 10).toString()).shuffled(),
                        correctAnswer = ans.toString(),
                        visualHint = "Count upward by 1: $base, then ...",
                        topic = topic
                    )
                } else if (countType == 1) {
                    val ans = base - 1
                    QuizQuestion(
                        equation = "What number comes before $base?",
                        options = listOf(ans.toString(), (base + 1).toString(), (base - 2).toString(), (base - 10).toString()).shuffled(),
                        correctAnswer = ans.toString(),
                        visualHint = "Count downward by 1: $base is preceded by...",
                        topic = topic
                    )
                } else {
                    val ans = base + 10
                    QuizQuestion(
                        equation = "What is $base + 10?",
                        options = listOf(ans.toString(), (base + 5).toString(), (base + 20).toString(), (base + 1).toString()).shuffled(),
                        correctAnswer = ans.toString(),
                        visualHint = "Add one to the tens column of $base.",
                        topic = topic
                    )
                }
            }
            "Number Recognition" -> {
                val num = r.nextInt(1, 20)
                QuizQuestion(
                    equation = "Which number is this? $num",
                    options = listOf(num.toString(), (num + r.nextInt(1, 3)).toString(), (num - r.nextInt(1, 3)).coerceAtLeast(0).toString(), (num + 5).toString()).distinct().shuffled().take(4),
                    correctAnswer = num.toString(),
                    visualHint = "Look at the digit $num and match it!",
                    topic = topic,
                    illustrationType = "objects"
                )
            }
            "Greater/Less" -> {
                val n1 = r.nextInt(1, 100)
                val n2 = r.nextInt(1, 100).let { if (it == n1) n1 + 2 else it }
                val isGreater = r.nextBoolean()
                val ans = if (isGreater) maxOf(n1, n2) else minOf(n1, n2)
                val symbol = if (isGreater) "GREATER" else "LESS"
                QuizQuestion(
                    equation = "Which number is $symbol? $n1 or $n2",
                    options = listOf(n1.toString(), n2.toString()),
                    correctAnswer = ans.toString(),
                    visualHint = "$ans is further along on the number line.",
                    topic = topic
                )
            }
            "Shapes" -> {
                val shapeList = listOf("Circle" to "🔴", "Square" to "🟧", "Triangle" to "🔺", "Rectangle" to "🟦")
                val item = shapeList.random()
                QuizQuestion(
                    equation = "Which shape is this? ${item.second}",
                    options = shapeList.map { it.first },
                    correctAnswer = item.first,
                    visualHint = "Count the sides! Circle has 0, Triangle 3, Square/Rectangle 4.",
                    topic = topic,
                    illustrationType = "shape"
                )
            }
            "Colors" -> {
                val colorsList = listOf("Red" to "🔴", "Blue" to "🔵", "Green" to "🟢", "Yellow" to "🟡")
                val item = colorsList.random()
                QuizQuestion(
                    equation = "What color is this balloon? ${item.second}",
                    options = colorsList.map { it.first },
                    correctAnswer = item.first,
                    visualHint = "Matches the primary paint colors!",
                    topic = topic
                )
            }
            "Addition" -> {
                val limit = if (level == "Beginner") 10 else if (level == "Primary") 50 else 150
                val n1 = r.nextInt(1, limit)
                val n2 = r.nextInt(1, limit)
                val ans = n1 + n2
                QuizQuestion(
                    equation = "$n1 + $n2",
                    options = listOf(ans.toString(), (ans + r.nextInt(1, 5)).toString(), (ans - r.nextInt(1, 4)).coerceAtLeast(0).toString(), (ans + 10).toString()).distinct().shuffled().take(4),
                    correctAnswer = ans.toString(),
                    visualHint = "Count $n1 dots plus $n2 more dots. Altogether it's $ans.",
                    topic = topic,
                    illustrationType = "objects"
                )
            }
            "Subtraction" -> {
                val limit = if (level == "Beginner") 10 else if (level == "Primary") 50 else 150
                val n1 = r.nextInt(5, limit)
                val n2 = r.nextInt(1, n1)
                val ans = n1 - n2
                QuizQuestion(
                    equation = "$n1 - $n2",
                    options = listOf(ans.toString(), (ans + r.nextInt(1, 5)).toString(), (ans - r.nextInt(1, 3)).coerceAtLeast(0).toString(), (ans + 7).toString()).distinct().shuffled().take(4),
                    correctAnswer = ans.toString(),
                    visualHint = "Start with $n1 items, cross out $n2 of them. $ans are left.",
                    topic = topic,
                    illustrationType = "objects"
                )
            }
            "Multiplication" -> {
                val maxLimit = if (level == "Primary") 9 else 12
                val n1 = r.nextInt(2, maxLimit)
                val n2 = r.nextInt(2, maxLimit)
                val ans = n1 * n2
                QuizQuestion(
                    equation = "$n1 × $n2",
                    options = listOf(ans.toString(), (ans + n1).toString(), (ans - n2).coerceAtLeast(0).toString(), (n1 * (n2 + 1) + 2).toString()).distinct().shuffled().take(4),
                    correctAnswer = ans.toString(),
                    visualHint = "$n1 added repeatedly $n2 times is $ans.",
                    topic = topic
                )
            }
            "Division" -> {
                val divisor = r.nextInt(2, 10)
                val quotient = r.nextInt(2, 10)
                val dividend = divisor * quotient
                QuizQuestion(
                    equation = "$dividend ÷ $divisor",
                    options = listOf(quotient.toString(), (quotient + 1).toString(), (quotient - 1).coerceAtLeast(1).toString(), (quotient + 3).toString()).distinct().shuffled().take(4),
                    correctAnswer = quotient.toString(),
                    visualHint = "How many groups of $divisor fit into $dividend? It fits $quotient times.",
                    topic = topic
                )
            }
            "Fractions" -> {
                val fractions = listOf("1/2" to "🌓 Half", "1/4" to "🍕 Quarter (1 of 4 parts)", "3/4" to "🍕 Three-Quarters (3 of 4 parts)")
                val choice = fractions.random()
                QuizQuestion(
                    equation = "What fraction is shaded in: ${choice.second}?",
                    options = listOf("1/2", "1/4", "3/4", "1/3"),
                    correctAnswer = choice.first,
                    visualHint = "Count shaded parts over the total pieces.",
                    topic = topic,
                    illustrationType = "fractional"
                )
            }
            "Geometry" -> {
                val geoType = r.nextInt(0, 3)
                if (geoType == 0) {
                    val w = r.nextInt(3, 8)
                    val h = r.nextInt(3, 8)
                    val ans = w * h
                    QuizQuestion(
                        equation = "Find the Area of a rectangle with length $w cm and width $h cm.",
                        options = listOf(ans.toString(), (w + h).toString(), (2 * (w + h)).toString(), (ans + 5).toString()).distinct().shuffled().take(4),
                        correctAnswer = ans.toString(),
                        visualHint = "Area = length × width. Multiply $w by $h.",
                        topic = topic
                    )
                } else if (geoType == 1) {
                    val side = r.nextInt(3, 10)
                    val ans = side * 4
                    QuizQuestion(
                        equation = "Find the Perimeter of a square with side length $side cm.",
                        options = listOf(ans.toString(), (side * side).toString(), (side * 2).toString(), (ans + 4).toString()).distinct().shuffled().take(4),
                        correctAnswer = ans.toString(),
                        visualHint = "Perimeter is the boundary: sum of all 4 equal sides. $side × 4.",
                        topic = topic
                    )
                } else {
                    QuizQuestion(
                        equation = "How many degrees are in a perfectly square right-angle corner?",
                        options = listOf("90°", "180°", "45°", "360°"),
                        correctAnswer = "90°",
                        visualHint = "An L-shape corner is exactly 90 degrees.",
                        topic = topic
                    )
                }
            }
            "Algebra Basics" -> {
                val x = r.nextInt(2, 12)
                val adder = r.nextInt(2, 10)
                val sum = x + adder
                QuizQuestion(
                    equation = "Solve for x:   x + $adder = $sum",
                    options = listOf(x.toString(), (x + 2).toString(), (x - 1).coerceAtLeast(1).toString(), (sum).toString()).distinct().shuffled().take(4),
                    correctAnswer = x.toString(),
                    visualHint = "Subtract $adder from $sum to isolate x.",
                    topic = topic
                )
            }
            "Percentage" -> {
                val base = listOf(40, 80, 100, 200).random()
                val pct = listOf(25, 50, 75).random()
                val ans = (base * pct) / 100
                QuizQuestion(
                    equation = "What is $pct% of $base?",
                    options = listOf(ans.toString(), (ans + 10).toString(), (ans / 2).toString(), (pct).toString()).distinct().shuffled().take(4),
                    correctAnswer = ans.toString(),
                    visualHint = "50% means half, 25% means a quarter, 75% is three-quarters.",
                    topic = topic
                )
            }
            "Money Math" -> {
                val bills = r.nextInt(2, 5)
                val coinVal = listOf(1, 5, 10).random()
                val ans = bills * coinVal
                QuizQuestion(
                    equation = "You have $bills coins of $$coinVal. How much money do you have in total?",
                    options = listOf("$$ans", "$${ans + coinVal}", "$${ans - coinVal}", "$$bills"),
                    correctAnswer = "$$ans",
                    visualHint = "Multiply the number of coins ($bills) by their value ($$coinVal).",
                    topic = topic
                )
            }
            "Time & Calendar" -> {
                val geoType = r.nextBoolean()
                if (geoType) {
                    val hr = r.nextInt(1, 12)
                    QuizQuestion(
                        equation = "If the short hour hand is pointing directly at $hr and long minute hand at 12, what time is it?",
                        options = listOf("$hr:00", "$hr:30", "12:00", "12:$hr"),
                        correctAnswer = "$hr:00",
                        visualHint = "The short hand shows the hour. When the long hand is at 12, it is exactly on the hour.",
                        topic = topic,
                        illustrationType = "clock"
                    )
                } else {
                    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    val dayIdx = r.nextInt(0, 5)
                    QuizQuestion(
                        equation = "If today is ${days[dayIdx]}, what day was yesterday?",
                        options = listOf(days[(dayIdx - 1 + 7) % 7], days[(dayIdx + 1) % 7], days[(dayIdx + 2) % 7], "Saturday"),
                        correctAnswer = days[(dayIdx - 1 + 7) % 7],
                        visualHint = "Yesterday is the day right before ${days[dayIdx]}.",
                        topic = topic
                    )
                }
            }
            else -> {
                QuizQuestion("2 + 2", listOf("3", "4", "5", "6"), "4", "Add 2 and 2 to make 4.", "Addition")
            }
        }
        activeQuizQuestion.value = question
    }

    // Checking Answers & Gamification Engine
    fun checkQuizAnswer(selected: String) {
        val currentQuestion = activeQuizQuestion.value ?: return
        selectedAnswer.value = selected

        if (selected == currentQuestion.correctAnswer) {
            quizCorrectAnswered.value = true
            streakCounter.value += 1

            // Award Rewards!
            viewModelScope.launch {
                val profile = repository.getProfile() ?: return@launch
                val xpGained = 15
                val starGained = 1
                val coinGained = 2
                val currentXp = profile.xp + xpGained
                val newLevel = (currentXp / 100) + 1 // Level up every 100 XP
                val levelUp = newLevel > profile.level

                val updatedProfile = profile.copy(
                    xp = currentXp,
                    stars = profile.stars + starGained,
                    coins = profile.coins + coinGained,
                    level = newLevel,
                    streak = maxOf(profile.streak, streakCounter.value)
                )
                repository.saveProfile(updatedProfile)

                if (levelUp) {
                    uiToastMessage.emit("⭐ LEVEL UP! You reached Level $newLevel! ⭐")
                } else {
                    uiToastMessage.emit("Correct! +$starGained ⭐ +$coinGained 🪙 +$xpGained XP")
                }
            }
        } else {
            quizCorrectAnswered.value = false
            streakCounter.value = 0

            // Log Mistake offline in DB
            viewModelScope.launch {
                repository.addMistake(
                    equation = currentQuestion.equation,
                    wrong = selected,
                    correct = currentQuestion.correctAnswer,
                    topic = currentQuestion.topic
                )
                uiToastMessage.emit("Incorrect. Try again! Hint available 💡")
            }
        }
    }

    // Toggle saved bookmarks for questions
    fun toggleSaveProblem(question: QuizQuestion) {
        viewModelScope.launch {
            val isSaved = repository.isProblemSaved(question.equation)
            if (isSaved) {
                // Delete
                val list = repository.allSavedProblems.first()
                val match = list.find { it.equation == question.equation }
                if (match != null) {
                    repository.deleteSavedProblem(match.id)
                    uiToastMessage.emit("Removed from Saved Problems!")
                }
            } else {
                repository.saveProblem(
                    equation = question.equation,
                    answer = question.correctAnswer,
                    topic = question.topic,
                    options = question.options
                )
                uiToastMessage.emit("Saved to Math Notebook! 🧠")
            }
        }
    }

    // Delete saved problem
    fun removeSavedProblem(id: Int) {
        viewModelScope.launch {
            repository.deleteSavedProblem(id)
            uiToastMessage.emit("Removed from Saved Problems!")
        }
    }

    // Clear mistakes history
    fun clearMistakes() {
        viewModelScope.launch {
            repository.clearMistakes()
            uiToastMessage.emit("Mistakes log cleared! 🧹")
        }
    }

    // Main counting activity
    fun initCountingActivity() {
        targetCountValue.value = Random.nextInt(3, 10)
        currentCountValue.value = 0
    }

    fun incrementCount() {
        if (currentCountValue.value < targetCountValue.value) {
            currentCountValue.value += 1
            if (currentCountValue.value == targetCountValue.value) {
                // Success
                viewModelScope.launch {
                    val profile = repository.getProfile() ?: return@launch
                    repository.saveProfile(profile.copy(stars = profile.stars + 1, coins = profile.coins + 2, xp = profile.xp + 10))
                    uiToastMessage.emit("Perfect Count! Earned 1 ⭐ and 2 🪙!")
                }
            }
        }
    }

    // Game Adventure Map Engine
    fun initAdventure() {
        adventureLevelIndex.value = 0
        adventureFeedback.value = null
        loadNextAdventureQuestion()
    }

    fun loadNextAdventureQuestion() {
        val levels = listOf("Counting 1–1000", "Number Recognition", "Greater/Less", "Addition", "Subtraction", "Multiplication", "Geometry")
        val currentTopic = levels[adventureLevelIndex.value]
        loadNextQuestion(currentTopic, if (adventureLevelIndex.value < 3) "Beginner" else "Primary")
    }

    fun answerAdventureQuestion(selected: String) {
        val q = activeQuizQuestion.value ?: return
        selectedAnswer.value = selected
        if (selected == q.correctAnswer) {
            adventureFeedback.value = "CORRECT"
            quizCorrectAnswered.value = true
            viewModelScope.launch {
                val profile = repository.getProfile() ?: return@launch
                repository.saveProfile(profile.copy(stars = profile.stars + 2, coins = profile.coins + 5, xp = profile.xp + 20))
                uiToastMessage.emit("Gate unlocked! Advance to next realm! 🏰")
            }
        } else {
            adventureFeedback.value = "WRONG"
            quizCorrectAnswered.value = false
            viewModelScope.launch {
                repository.addMistake(q.equation, selected, q.correctAnswer, q.topic)
                uiToastMessage.emit("Locked! Hint: ${q.visualHint}")
            }
        }
    }

    fun advanceAdventureLevel() {
        if (adventureLevelIndex.value < 6) {
            adventureLevelIndex.value += 1
            adventureFeedback.value = null
            loadNextAdventureQuestion()
        } else {
            // Completed map!
            viewModelScope.launch {
                val profile = repository.getProfile() ?: return@launch
                repository.saveProfile(profile.copy(stars = profile.stars + 10, coins = profile.coins + 20, xp = profile.xp + 100))
                uiToastMessage.emit("🏆 CONGRATULATIONS! You conquered Math Adventure! +10 Stars, +20 Coins")
            }
            currentScreen.value = Screen.Dashboard
        }
    }

    // Rocket Math Game
    fun initRocketMath() {
        rocketHeight.value = 10f
        loadNextQuestion("Addition", "Primary")
    }

    fun answerRocketQuestion(selected: String) {
        val q = activeQuizQuestion.value ?: return
        selectedAnswer.value = selected
        if (selected == q.correctAnswer) {
            quizCorrectAnswered.value = true
            rocketHeight.value = (rocketHeight.value + 20f).coerceAtMost(100f)
            viewModelScope.launch {
                val profile = repository.getProfile() ?: return@launch
                repository.saveProfile(profile.copy(stars = profile.stars + 1, coins = profile.coins + 2, xp = profile.xp + 10))
            }
            if (rocketHeight.value >= 100f) {
                viewModelScope.launch {
                    val profile = repository.getProfile() ?: return@launch
                    repository.saveProfile(profile.copy(stars = profile.stars + 5, coins = profile.coins + 10, xp = profile.xp + 50))
                    uiToastMessage.emit("🚀 ROCKET LAUNCHED INTO SPACE! Bonus +5 ⭐ +10 🪙")
                }
            }
        } else {
            quizCorrectAnswered.value = false
            rocketHeight.value = (rocketHeight.value - 15f).coerceAtLeast(10f)
            viewModelScope.launch {
                repository.addMistake(q.equation, selected, q.correctAnswer, q.topic)
            }
        }
    }

    // Ocean Math Game
    fun initOceanMath() {
        oceanFishesCaught.value = 0
        loadNextQuestion("Subtraction", "Primary")
    }

    fun answerOceanQuestion(selected: String) {
        val q = activeQuizQuestion.value ?: return
        selectedAnswer.value = selected
        if (selected == q.correctAnswer) {
            quizCorrectAnswered.value = true
            oceanFishesCaught.value += 1
            viewModelScope.launch {
                val profile = repository.getProfile() ?: return@launch
                repository.saveProfile(profile.copy(stars = profile.stars + 1, coins = profile.coins + 3, xp = profile.xp + 12))
                uiToastMessage.emit("🐟 Caught a Fish! Treasure earned +3 🪙")
            }
        } else {
            quizCorrectAnswered.value = false
            viewModelScope.launch {
                repository.addMistake(q.equation, selected, q.correctAnswer, q.topic)
            }
        }
    }

    // Car Math Race
    fun initCarRace() {
        carRaceSpeed.value = 40
        loadNextQuestion("Multiplication", "Primary")
    }

    fun answerCarRaceQuestion(selected: String) {
        val q = activeQuizQuestion.value ?: return
        selectedAnswer.value = selected
        if (selected == q.correctAnswer) {
            quizCorrectAnswered.value = true
            carRaceSpeed.value = (carRaceSpeed.value + 30).coerceAtMost(200)
            viewModelScope.launch {
                val profile = repository.getProfile() ?: return@launch
                repository.saveProfile(profile.copy(stars = profile.stars + 1, coins = profile.coins + 3, xp = profile.xp + 15))
                uiToastMessage.emit("🏎️ Zoom! Speed increased to ${carRaceSpeed.value} km/h!")
            }
        } else {
            quizCorrectAnswered.value = false
            carRaceSpeed.value = (carRaceSpeed.value - 20).coerceAtLeast(30)
            viewModelScope.launch {
                repository.addMistake(q.equation, selected, q.correctAnswer, q.topic)
                uiToastMessage.emit("Crash! Speed dropped!")
            }
        }
    }

    // Smart Practice Session Engine
    fun startSmartPractice(mode: PracticeMode) {
        practiceQuestionsLeft.value = 10
        practiceCorrectAnswers.value = 0
        practiceTotalQuestions.value = 10
        isPracticeSessionFinished.value = false
        speedTestSecondsLeft.value = 45 // 45 seconds for speed test

        when (mode) {
            PracticeMode.DAILY -> {
                loadNextQuestion("Addition", "Primary")
            }
            PracticeMode.WEAK_TOPIC -> {
                // Find weak topics dynamically from mistakes database
                viewModelScope.launch {
                    val mistakes = allMistakes.value
                    val topicCount = mistakes.groupBy { it.topic }.mapValues { it.value.size }
                    val weakest = topicCount.maxByOrNull { it.value }?.key ?: "Addition"
                    loadNextQuestion(weakest, "Primary")
                }
            }
            PracticeMode.SPEED_TEST -> {
                loadNextQuestion("Subtraction", "Primary")
            }
            PracticeMode.MEMORY -> {
                initMemoryGame()
            }
            PracticeMode.REVISION -> {
                viewModelScope.launch {
                    val mistakes = allMistakes.value
                    if (mistakes.isNotEmpty()) {
                        val m = mistakes.random()
                        activeQuizQuestion.value = QuizQuestion(
                            equation = m.equation,
                            options = listOf(m.correctAnswer, m.wrongAnswer, (m.correctAnswer.toIntOrNull()?.plus(3) ?: 10).toString(), (m.correctAnswer.toIntOrNull()?.minus(2) ?: 1).toString()).distinct().shuffled(),
                            correctAnswer = m.correctAnswer,
                            visualHint = "Revision of your past mistake on ${m.topic}",
                            topic = m.topic
                        )
                    } else {
                        // Fallback
                        loadNextQuestion("Addition", "Primary")
                    }
                }
            }
        }
    }

    fun submitPracticeAnswer(selected: String, mode: PracticeMode) {
        val q = activeQuizQuestion.value ?: return
        selectedAnswer.value = selected

        if (selected == q.correctAnswer) {
            quizCorrectAnswered.value = true
            practiceCorrectAnswers.value += 1
            viewModelScope.launch {
                val profile = repository.getProfile() ?: return@launch
                repository.saveProfile(profile.copy(stars = profile.stars + 1, coins = profile.coins + 2, xp = profile.xp + 10))
            }
        } else {
            quizCorrectAnswered.value = false
            viewModelScope.launch {
                repository.addMistake(q.equation, selected, q.correctAnswer, q.topic)
            }
        }

        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            practiceQuestionsLeft.value -= 1
            if (practiceQuestionsLeft.value <= 0 || (mode == PracticeMode.SPEED_TEST && speedTestSecondsLeft.value <= 0)) {
                finishPracticeSession(mode)
            } else {
                selectedAnswer.value = null
                quizCorrectAnswered.value = null
                if (mode == PracticeMode.REVISION) {
                    startSmartPractice(PracticeMode.REVISION)
                } else if (mode == PracticeMode.WEAK_TOPIC) {
                    startSmartPractice(PracticeMode.WEAK_TOPIC)
                } else {
                    loadNextQuestion(q.topic, "Primary")
                }
            }
        }
    }

    private fun finishPracticeSession(mode: PracticeMode) {
        isPracticeSessionFinished.value = true
        viewModelScope.launch {
            val score = practiceCorrectAnswers.value
            val total = practiceTotalQuestions.value
            val profile = repository.getProfile() ?: return@launch
            val extraStars = if (score == total) 5 else if (score >= total / 2) 2 else 0
            repository.saveProfile(profile.copy(
                stars = profile.stars + extraStars,
                coins = profile.coins + score,
                xp = profile.xp + (score * 10)
            ))
            uiToastMessage.emit("Session Finished! Score: $score/$total. Earned bonus +$extraStars ⭐!")
        }
    }

    // Memory Game Mechanics
    fun initMemoryGame() {
        val list = List(4) { Random.nextInt(1, 9) }
        memorySequence.value = list
        memoryUserInput.value = emptyList()
        memoryGameState.value = "SHOWING"

        viewModelScope.launch {
            kotlinx.coroutines.delay(3000) // Show for 3 seconds
            memoryGameState.value = "INPUTTING"
        }
    }

    fun enterMemoryNumber(num: Int) {
        if (memoryGameState.value != "INPUTTING") return
        val currentInputs = memoryUserInput.value.toMutableList()
        currentInputs.add(num)
        memoryUserInput.value = currentInputs

        val expected = memorySequence.value
        if (currentInputs.size == expected.size) {
            if (currentInputs == expected) {
                memoryGameState.value = "SUCCESS"
                practiceCorrectAnswers.value += 1
                viewModelScope.launch {
                    val profile = repository.getProfile() ?: return@launch
                    repository.saveProfile(profile.copy(stars = profile.stars + 2, coins = profile.coins + 5, xp = profile.xp + 15))
                    uiToastMessage.emit("Perfect Memory! +2 ⭐ +5 🪙")
                }
            } else {
                memoryGameState.value = "FAIL"
                viewModelScope.launch {
                    uiToastMessage.emit("Memory pattern mismatch!")
                }
            }
        }
    }

    // Worksheet Mode Options: Easy, Medium, Hard
    fun generateWorksheet(difficulty: String) {
        worksheetDifficulty.value = difficulty
        val topics = listOf("Addition", "Subtraction", "Multiplication", "Division")
        val questions = mutableListOf<String>()
        val r = Random

        val limit = when (difficulty) {
            "Easy" -> 10
            "Medium" -> 50
            else -> 100
        }

        for (i in 1..10) {
            val topic = topics.random()
            val n1 = r.nextInt(2, limit)
            val n2 = r.nextInt(2, limit)
            val line = when (topic) {
                "Addition" -> "Q$i)  $n1 + $n2 = ______"
                "Subtraction" -> "Q$i)  ${maxOf(n1, n2)} - ${minOf(n1, n2)} = ______"
                "Multiplication" -> "Q$i)  ${n1 % 12} × ${n2 % 12} = ______"
                else -> {
                    val div = r.nextInt(2, 10)
                    "Q$i)  ${div * (n1 % 10 + 1)} ÷ $div = ______"
                }
            }
            questions.add(line)
        }
        generatedWorksheet.value = questions
    }

    // Voice Math Simulated Speech Processing
    fun parseVoiceQuery(text: String) {
        voiceMathInput.value = text
        val cleaned = text.lowercase().trim()
        val numWords = mapOf(
            "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
            "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10
        )

        var operation = ""
        var num1 = 0
        var num2 = 0

        val words = cleaned.split(" ")
        for (word in words) {
            if (numWords.containsKey(word)) {
                if (num1 == 0) {
                    num1 = numWords[word]!!
                } else {
                    num2 = numWords[word]!!
                }
            }
            if (word == "plus" || word == "add" || word == "and") {
                operation = "Addition"
            }
            if (word == "minus" || word == "subtract") {
                operation = "Subtraction"
            }
            if (word == "times" || word == "multiply") {
                operation = "Multiplication"
            }
        }

        if (operation.isNotEmpty() && num1 != 0 && num2 != 0) {
            val correctAns = when (operation) {
                "Addition" -> num1 + num2
                "Subtraction" -> num1 - num2
                else -> num1 * num2
            }
            val operatorSign = when (operation) {
                "Addition" -> "+"
                "Subtraction" -> "-"
                else -> "×"
            }
            voiceMathFeedback.value = "Recognized: $num1 $operatorSign $num2. What is the answer?"
            activeQuizQuestion.value = QuizQuestion(
                equation = "$num1 $operatorSign $num2",
                options = listOf(correctAns.toString(), (correctAns + 2).toString(), (correctAns - 1).coerceAtLeast(0).toString(), (correctAns + 5).toString()).distinct().shuffled(),
                correctAnswer = correctAns.toString(),
                visualHint = "You spoken: '$text'. Double check details!",
                topic = operation
            )
        } else {
            // Check direct numbers
            val matchedNum = words.firstOrNull { numWords.containsKey(it) }
            if (matchedNum != null) {
                val nVal = numWords[matchedNum]!!
                voiceMathFeedback.value = "Recognized number: $nVal. Generating quick quiz!"
                loadNextQuestion("Number Recognition", "Beginner")
            } else {
                voiceMathFeedback.value = "Could not parse fully. Try saying 'Five plus three' or 'Six times two'!"
            }
        }
    }

    // Teacher tools
    fun addTeacherClass(name: String, code: String) {
        viewModelScope.launch {
            repository.createTeacherClass(name, code, studentCount = Random.nextInt(10, 25))
            uiToastMessage.emit("Class $name ($code) successfully created! 🏫")
        }
    }

    fun addTeacherHomework(code: String, title: String, desc: String, due: String, count: Int) {
        viewModelScope.launch {
            repository.createTeacherHomework(code, title, desc, due, count)
            uiToastMessage.emit("Homework '$title' assigned successfully!")
        }
    }

    fun generateNewTeacherQuiz() {
        viewModelScope.launch {
            uiToastMessage.emit("Generated new Quiz template for class assignments!")
        }
    }
}
