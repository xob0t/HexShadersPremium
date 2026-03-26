package ru.serjik.preferences

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.serjik.preferences.controllers.*

abstract class PreferenceController {
    var preferenceEntry: PreferenceEntry? = null
        internal set

    var context: Context? = null
        internal set

    var view: View? = null
        internal set

    private var density: Float = -1.0f

    protected fun dp(px: Int): Int = ((px * density) + 0.5f).toInt()

    protected abstract fun createView(params: Array<String>): View

    companion object {
        /** Applies a dark shadow to all TextViews in the hierarchy for readability over shader previews. */
        private fun applyTextShadow(view: View) {
            if (view is TextView) {
                view.setTextColor(Color.WHITE)
                view.setShadowLayer(6f, 0f, 0f, Color.BLACK)
            }
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    applyTextShadow(view.getChildAt(i))
                }
            }
        }


        fun create(typeName: String, params: Array<String>, entry: PreferenceEntry, context: Context): PreferenceController {
            val controller = when (typeName) {
                "Range" -> RangeController()
                "Integer" -> IntegerController()
                "CheckBox" -> CheckBoxController()
                "RGB" -> RGBController()
                "Copyright" -> CopyrightController()
                "Separator" -> SeparatorController()
                else -> throw IllegalArgumentException("Unknown controller type: $typeName")
            }
            controller.preferenceEntry = entry
            controller.context = context
            controller.density = context.resources.displayMetrics.density
            controller.view = controller.createView(params)
            controller.view?.let { applyTextShadow(it) }
            return controller
        }
    }
}
