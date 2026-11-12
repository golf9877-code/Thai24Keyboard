package com.antigravity.thai24keyboard

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.antigravity.thai24keyboard.ui.KeyboardRootView

class ThaiKeyboardService : InputMethodService() {

    private val keyboardLifecycleOwner = KeyboardLifecycleOwner()
    private var currentImeOptions: Int = EditorInfo.IME_ACTION_NONE

    override fun onCreate() {
        super.onCreate()
        keyboardLifecycleOwner.onCreate()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentImeOptions = info?.imeOptions ?: EditorInfo.IME_ACTION_NONE
        keyboardLifecycleOwner.onResume()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        keyboardLifecycleOwner.onPause()
    }

    override fun onCreateInputView(): View {
        keyboardLifecycleOwner.onStart()

        return ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)

            // Setup lifecycle owners so Compose can render inside Service safely
            setViewTreeLifecycleOwner(keyboardLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(keyboardLifecycleOwner)
            setViewTreeViewModelStoreOwner(keyboardLifecycleOwner)

            setContent {
                KeyboardRootView(
                    inputConnection = currentInputConnection,
                    imeOptions = currentImeOptions,
                    onSwitchIme = {
                        switchInputMethod()
                    },
                    onClose = {
                        requestHideSelf(0)
                    }
                )
            }
        }
    }

    private fun switchInputMethod() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            switchToNextInputMethod(false)
        } else {
            @Suppress("DEPRECATION")
            imm.showInputMethodPicker()
        }
    }

    override fun onDestroy() {
        keyboardLifecycleOwner.onDestroy()
        super.onDestroy()
    }
}
