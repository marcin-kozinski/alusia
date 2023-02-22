package dev.kozinski.alusia

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class MainActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun launches() {
        assertEquals(Lifecycle.State.RESUMED, activityRule.scenario.state)
    }
}
