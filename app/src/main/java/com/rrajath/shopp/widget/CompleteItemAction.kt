package com.rrajath.shopp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.rrajath.shopp.ShoppApplication

val ItemIdKey = ActionParameters.Key<String>("itemId")

// Tapping an unchecked item in the widget completes it immediately via the
// same use case the app's own FAB/list flow uses (user decision -- see
// docs/plan: no interim "checked" visual, the row just disappears once the
// widget recomposes off the now-updated active-items Flow).
class CompleteItemAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val itemId = parameters[ItemIdKey] ?: return
        val container = (context.applicationContext as ShoppApplication).container
        container.completeItem(itemId)
        ShoppWidget().update(context, glanceId)
    }
}
