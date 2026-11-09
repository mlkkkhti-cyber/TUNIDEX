package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CarCatalog
import com.example.model.CarTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Tunidex", appName)
  }

  @Test
  fun `verify car catalog integrity across all 5 tiers`() {
    val cars = CarCatalog.cars
    assertTrue(cars.size >= 30)

    CarTier.entries.forEach { tier ->
      val carsInTier = CarCatalog.getCarsByTier(tier)
      assertTrue("Tier ${tier.name} must have cars", carsInTier.isNotEmpty())
    }

    val r34 = CarCatalog.getCarById("skyline_r34_gtr")
    assertNotNull(r34)
    assertEquals("Nissan", r34?.brand)

    val gt3rs = CarCatalog.getCarById("porsche_911_gt3_rs_992")
    assertNotNull(gt3rs)
    assertEquals(9000, gt3rs?.redlineRpm)
  }
}
