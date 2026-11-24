package com.example.ui.components

import android.graphics.Rect as AndroidRect
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

private const val MENU_ID_COPY = 0
private const val MENU_ID_PASTE = 1
private const val MENU_ID_CUT = 2
private const val MENU_ID_SELECT_ALL = 3

/**
 * Lifecycle-aware [TextToolbar] that guards [ActionMode.TYPE_FLOATING] creation and disposal
 * against re-entrant `finish()` calls, detached DecorViews, and window focus transitions
 * that otherwise trigger `Destroying unexpected ActionMode instance of TYPE_FLOATING`.
 */
class SafeAndroidTextToolbar(private val view: View) : TextToolbar {

    private var actionMode: ActionMode? = null
    private var isFinishingActionMode = false
    private var isCreatingActionMode = false

    private var currentRect: Rect = Rect.Zero
    private var onCopyRequested: (() -> Unit)? = null
    private var onPasteRequested: (() -> Unit)? = null
    private var onCutRequested: (() -> Unit)? = null
    private var onSelectAllRequested: (() -> Unit)? = null

    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
        private set

    private val actionModeCallback = object : ActionMode.Callback2() {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            if (mode == null || menu == null) return false
            if (onCopyRequested != null) {
                menu.add(0, MENU_ID_COPY, MENU_ID_COPY, android.R.string.copy)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }
            if (onPasteRequested != null) {
                menu.add(0, MENU_ID_PASTE, MENU_ID_PASTE, android.R.string.paste)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }
            if (onCutRequested != null) {
                menu.add(0, MENU_ID_CUT, MENU_ID_CUT, android.R.string.cut)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }
            if (onSelectAllRequested != null) {
                menu.add(0, MENU_ID_SELECT_ALL, MENU_ID_SELECT_ALL, android.R.string.selectAll)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            if (mode == null || menu == null) return false
            var changed = false
            changed = syncMenuItem(menu, MENU_ID_COPY, android.R.string.copy, onCopyRequested != null) || changed
            changed = syncMenuItem(menu, MENU_ID_PASTE, android.R.string.paste, onPasteRequested != null) || changed
            changed = syncMenuItem(menu, MENU_ID_CUT, android.R.string.cut, onCutRequested != null) || changed
            changed = syncMenuItem(menu, MENU_ID_SELECT_ALL, android.R.string.selectAll, onSelectAllRequested != null) || changed
            return changed
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                MENU_ID_COPY -> onCopyRequested?.invoke()
                MENU_ID_PASTE -> onPasteRequested?.invoke()
                MENU_ID_CUT -> onCutRequested?.invoke()
                MENU_ID_SELECT_ALL -> onSelectAllRequested?.invoke()
                else -> return false
            }
            hide()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            if (actionMode === mode) {
                actionMode = null
            }
            status = TextToolbarStatus.Hidden
        }

        override fun onGetContentRect(mode: ActionMode?, view: View?, outRect: AndroidRect?) {
            outRect?.set(
                currentRect.left.toInt(),
                currentRect.top.toInt(),
                currentRect.right.toInt(),
                currentRect.bottom.toInt()
            )
        }
    }

    private fun syncMenuItem(menu: Menu, id: Int, titleRes: Int, shouldExist: Boolean): Boolean {
        val existing = menu.findItem(id)
        return when {
            shouldExist && existing == null -> {
                menu.add(0, id, id, titleRes).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                true
            }
            !shouldExist && existing != null -> {
                menu.removeItem(id)
                true
            }
            else -> false
        }
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        currentRect = rect
        this.onCopyRequested = onCopyRequested
        this.onPasteRequested = onPasteRequested
        this.onCutRequested = onCutRequested
        this.onSelectAllRequested = onSelectAllRequested

        if (!view.isAttachedToWindow || !view.hasWindowFocus()) {
            hide()
            return
        }

        val current = actionMode
        if (current == null) {
            if (isCreatingActionMode || isFinishingActionMode) return
            isCreatingActionMode = true
            try {
                val created = view.startActionMode(actionModeCallback, ActionMode.TYPE_FLOATING)
                actionMode = created
                status = if (created != null) TextToolbarStatus.Shown else TextToolbarStatus.Hidden
            } catch (_: Throwable) {
                actionMode = null
                status = TextToolbarStatus.Hidden
            } finally {
                isCreatingActionMode = false
            }
        } else {
            status = TextToolbarStatus.Shown
            try {
                current.invalidate()
                current.invalidateContentRect()
            } catch (_: Throwable) {
                actionMode = null
                status = TextToolbarStatus.Hidden
            }
        }
    }

    override fun hide() {
        status = TextToolbarStatus.Hidden
        val current = actionMode ?: return
        if (isFinishingActionMode || isCreatingActionMode) return
        actionMode = null
        if (!view.isAttachedToWindow) {
            return
        }
        isFinishingActionMode = true
        try {
            current.finish()
        } catch (_: Throwable) {
            // DecorView may already have cleaned up the floating mode during teardown.
        } finally {
            isFinishingActionMode = false
        }
    }

    fun onWindowDetached() {
        status = TextToolbarStatus.Hidden
        actionMode = null
    }
}

@Composable
fun ProvideSafeTextToolbar(content: @Composable () -> Unit) {
    val view = LocalView.current
    val safeToolbar = remember(view) { SafeAndroidTextToolbar(view) }

    DisposableEffect(view, safeToolbar) {
        val attachListener = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = Unit
            override fun onViewDetachedFromWindow(v: View) {
                safeToolbar.onWindowDetached()
            }
        }
        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (!hasFocus) {
                safeToolbar.hide()
            }
        }
        view.addOnAttachStateChangeListener(attachListener)
        view.viewTreeObserver.addOnWindowFocusChangeListener(focusListener)

        onDispose {
            view.removeOnAttachStateChangeListener(attachListener)
            if (view.viewTreeObserver.isAlive) {
                view.viewTreeObserver.removeOnWindowFocusChangeListener(focusListener)
            }
            safeToolbar.hide()
        }
    }

    CompositionLocalProvider(LocalTextToolbar provides safeToolbar) {
        content()
    }
}
