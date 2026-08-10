package com.rrajath.milk

import android.app.Application
import com.rrajath.milk.data.db.ShoppDatabase
import com.rrajath.milk.data.repository.ItemRepository
import com.rrajath.milk.data.repository.LabelRepository
import com.rrajath.milk.domain.SystemClock
import com.rrajath.milk.domain.Uuidv7IdGenerator
import com.rrajath.milk.usecases.CaptureItems
import com.rrajath.milk.usecases.CompleteItem
import com.rrajath.milk.usecases.DeleteLabel
import com.rrajath.milk.usecases.EditTitle
import com.rrajath.milk.usecases.MergeLabels
import com.rrajath.milk.usecases.ReaddCompleted
import com.rrajath.milk.usecases.RenameLabel
import com.rrajath.milk.usecases.UndoComplete

// Manual DI container. MainActivity and CaptureActivity both read this off
// the Application instance, so they share the exact same Room connection
// pool (and therefore the exact same use-case/parser code) — see TDD §1.2.
class AppContainer(app: Application) {
    val clock = SystemClock
    val idGenerator = Uuidv7IdGenerator

    val database: ShoppDatabase = ShoppDatabase.build(app)

    val itemRepository = ItemRepository(database.itemDao(), clock)
    val labelRepository = LabelRepository(database.labelDao(), clock, idGenerator)

    val captureItems = CaptureItems(database, itemRepository, labelRepository, clock, idGenerator)
    val completeItem = CompleteItem(database, itemRepository)
    val undoComplete = UndoComplete(database, itemRepository)
    val editTitle = EditTitle(database, itemRepository, labelRepository)
    val renameLabel = RenameLabel(database, labelRepository)
    val mergeLabels = MergeLabels(database, itemRepository, labelRepository)
    val deleteLabel = DeleteLabel(database, itemRepository, labelRepository)
    val readdCompleted = ReaddCompleted(database, itemRepository, labelRepository, clock, idGenerator)
}

class MilkApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
