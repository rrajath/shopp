package com.rrajath.milk

import android.app.Application
import com.rrajath.milk.data.db.ShoppDatabase
import com.rrajath.milk.data.repository.ItemRepository
import com.rrajath.milk.data.repository.LabelRepository
import com.rrajath.milk.domain.SystemClock
import com.rrajath.milk.domain.Uuidv7IdGenerator

// Manual DI container. MainActivity and CaptureActivity both read this off
// the Application instance, so they share the exact same Room connection
// pool (and therefore the exact same use-case/parser code) — see TDD §1.2.
class AppContainer(app: Application) {
    val clock = SystemClock
    val idGenerator = Uuidv7IdGenerator

    val database: ShoppDatabase = ShoppDatabase.build(app)

    val itemRepository = ItemRepository(database.itemDao(), clock)
    val labelRepository = LabelRepository(database.labelDao(), clock, idGenerator)
}

class MilkApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
