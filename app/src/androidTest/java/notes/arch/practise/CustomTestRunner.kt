package notes.arch.practise

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * A custom [AndroidJUnitRunner] used to replace the application used in tests with a
 * [TestTodoApplication].
 */
class CustomTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, TestTodoApplication::class.java.name, context)
    }
}