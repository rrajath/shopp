package com.rrajath.shopp

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.rrajath.shopp.data.db.ShoppDatabase
import com.rrajath.shopp.data.repository.ItemRepository
import com.rrajath.shopp.data.repository.LabelRepository
import com.rrajath.shopp.data.repository.PreferencesRepository
import com.rrajath.shopp.domain.SystemClock
import com.rrajath.shopp.domain.Uuidv7IdGenerator
import com.rrajath.shopp.usecases.CaptureItems
import com.rrajath.shopp.usecases.CompleteItem
import com.rrajath.shopp.usecases.DeleteLabel
import com.rrajath.shopp.usecases.EditTitle
import com.rrajath.shopp.usecases.MergeLabels
import com.rrajath.shopp.usecases.ReaddCompleted
import com.rrajath.shopp.usecases.RenameLabel
import com.rrajath.shopp.usecases.SetLabelColor
import com.rrajath.shopp.usecases.UndoComplete
import com.rrajath.shopp.widget.ShoppWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine

// Manual DI container. MainActivity and CaptureActivity both read this off
// the Application instance, so they share the exact same Room connection
// pool (and therefore the exact same use-case/parser code) — see TDD §1.2.
class AppContainer(app: Application) {
    val clock = SystemClock
    val idGenerator = Uuidv7IdGenerator

    val database: ShoppDatabase = ShoppDatabase.build(app)

    val itemRepository = ItemRepository(database.itemDao(), clock)
    val labelRepository = LabelRepository(database.labelDao(), clock, idGenerator)
    val preferencesRepository = PreferencesRepository(app.dataStore)

    val captureItems = CaptureItems(database, itemRepository, labelRepository, clock, idGenerator)
    val completeItem = CompleteItem(database, itemRepository)
    val undoComplete = UndoComplete(database, itemRepository)
    val editTitle = EditTitle(database, itemRepository, labelRepository)
    val renameLabel = RenameLabel(database, labelRepository)
    val setLabelColor = SetLabelColor(database, labelRepository)
    val mergeLabels = MergeLabels(database, itemRepository, labelRepository)
    val deleteLabel = DeleteLabel(database, itemRepository, labelRepository)
    val readdCompleted = ReaddCompleted(database, itemRepository, labelRepository)
}

private val Application.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ShoppApplication : Application() {
    lateinit var container: AppContainer
        private set

    // Lives for the process's whole lifetime -- backs the widget-refresh
    // collector below, independent of any single Activity/ViewModel scope.
    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Keeps the home-screen widget in sync even when its own Glance
        // session isn't currently active (e.g. it hasn't been redrawn in a
        // while) -- see docs/DESIGN_SYSTEM.md, "Home screen widget". The
        // widget's own provideContent also collects these flows directly
        // while a session *is* alive; this is the belt-and-suspenders path.
        applicationScope.launch {
            combine(
                container.itemRepository.observeActiveItems(),
                container.labelRepository.observeLabels(),
            ) { _, _ -> Unit }.collect {
                ShoppWidget().updateAll(this@ShoppApplication)
            }
        }
    }
}
