package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val age: Int = 6,
    val className: String = "Class 1",
    val avatarName: String = "Boy",
    val stars: Int = 0,
    val coins: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 1,
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val unlockedAvatars: String = "Boy,Girl,Cat",
    val unlockedAccessories: String = "",
    val equippedAccessory: String = "",
    val profileCreated: Boolean = false
)

@Entity(tableName = "mistake_history")
data class MistakeHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val equation: String,
    val wrongAnswer: String,
    val correctAnswer: String,
    val topic: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_problems")
data class SavedProblem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val equation: String,
    val answer: String,
    val topic: String,
    val options: String, // comma-separated options
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "teacher_classes")
data class TeacherClass(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val className: String,
    val classCode: String,
    val studentCount: Int = 0
)

@Entity(tableName = "teacher_homework")
data class TeacherHomework(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val classCode: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val questionCount: Int
)

@Entity(tableName = "student_progress")
data class StudentProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentName: String,
    val className: String,
    val accuracy: Int, // percentage
    val completedCount: Int,
    val stars: Int
)

@Dao
interface MathDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    // Mistake History
    @Query("SELECT * FROM mistake_history ORDER BY timestamp DESC")
    fun getAllMistakesFlow(): Flow<List<MistakeHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: MistakeHistory)

    @Query("DELETE FROM mistake_history")
    suspend fun clearMistakes()

    // Saved Problems
    @Query("SELECT * FROM saved_problems ORDER BY timestamp DESC")
    fun getAllSavedProblemsFlow(): Flow<List<SavedProblem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedProblem(problem: SavedProblem)

    @Query("DELETE FROM saved_problems WHERE id = :id")
    suspend fun deleteSavedProblem(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_problems WHERE equation = :equation)")
    suspend fun isProblemSaved(equation: String): Boolean

    // Teacher classes
    @Query("SELECT * FROM teacher_classes ORDER BY id DESC")
    fun getAllTeacherClassesFlow(): Flow<List<TeacherClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherClass(teacherClass: TeacherClass)

    // Teacher homework
    @Query("SELECT * FROM teacher_homework ORDER BY id DESC")
    fun getAllTeacherHomeworkFlow(): Flow<List<TeacherHomework>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherHomework(homework: TeacherHomework)

    // Student progress simulation
    @Query("SELECT * FROM student_progress ORDER BY stars DESC")
    fun getAllStudentProgressFlow(): Flow<List<StudentProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentProgress(progress: StudentProgress)
}

@Database(
    entities = [
        UserProfile::class,
        MistakeHistory::class,
        SavedProblem::class,
        TeacherClass::class,
        TeacherHomework::class,
        StudentProgress::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mathDao(): MathDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mathverse_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class MathRepository(private val mathDao: MathDao) {
    val userProfile: Flow<UserProfile?> = mathDao.getUserProfileFlow()
    val allMistakes: Flow<List<MistakeHistory>> = mathDao.getAllMistakesFlow()
    val allSavedProblems: Flow<List<SavedProblem>> = mathDao.getAllSavedProblemsFlow()
    val allTeacherClasses: Flow<List<TeacherClass>> = mathDao.getAllTeacherClassesFlow()
    val allTeacherHomework: Flow<List<TeacherHomework>> = mathDao.getAllTeacherHomeworkFlow()
    val allStudentProgress: Flow<List<StudentProgress>> = mathDao.getAllStudentProgressFlow()

    suspend fun getProfile(): UserProfile? = mathDao.getUserProfile()
    suspend fun saveProfile(profile: UserProfile) = mathDao.insertOrUpdateProfile(profile)

    suspend fun addMistake(equation: String, wrong: String, correct: String, topic: String) {
        mathDao.insertMistake(MistakeHistory(equation = equation, wrongAnswer = wrong, correctAnswer = correct, topic = topic))
    }

    suspend fun clearMistakes() = mathDao.clearMistakes()

    suspend fun saveProblem(equation: String, answer: String, topic: String, options: List<String>) {
        mathDao.insertSavedProblem(
            SavedProblem(
                equation = equation,
                answer = answer,
                topic = topic,
                options = options.joinToString(",")
            )
        )
    }

    suspend fun deleteSavedProblem(id: Int) = mathDao.deleteSavedProblem(id)
    suspend fun isProblemSaved(equation: String): Boolean = mathDao.isProblemSaved(equation)

    suspend fun createTeacherClass(className: String, classCode: String, studentCount: Int = 0) {
        mathDao.insertTeacherClass(TeacherClass(className = className, classCode = classCode, studentCount = studentCount))
    }

    suspend fun createTeacherHomework(classCode: String, title: String, description: String, dueDate: String, questionCount: Int) {
        mathDao.insertTeacherHomework(
            TeacherHomework(
                classCode = classCode,
                title = title,
                description = description,
                dueDate = dueDate,
                questionCount = questionCount
            )
        )
    }

    suspend fun addStudentProgress(studentName: String, className: String, accuracy: Int, completedCount: Int, stars: Int) {
        mathDao.insertStudentProgress(
            StudentProgress(
                studentName = studentName,
                className = className,
                accuracy = accuracy,
                completedCount = completedCount,
                stars = stars
            )
        )
    }
}
