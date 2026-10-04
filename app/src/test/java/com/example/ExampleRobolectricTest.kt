package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.PrankSoundType
import com.example.ui.navigation.PrankDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PrankMaster", appName)
  }

  @Test
  fun `verify prank sound types available`() {
    assertTrue(PrankSoundType.values().isNotEmpty())
    assertTrue(PrankSoundType.values().any { it.name == "AIR_HORN" })
    assertTrue(PrankSoundType.values().any { it.name == "FART_EPIC" })
  }

  @Test
  fun `verify prank destinations configured`() {
    assertEquals(8, PrankDestination.values().size)
  }
}
