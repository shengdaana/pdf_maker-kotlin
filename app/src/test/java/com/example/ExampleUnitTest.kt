package com.example

import com.example.model.DeveloperSettings
import com.example.model.PageMarginOption
import com.example.model.PdfQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun developerSettings_defaultValues_areCorrect() {
    val settings = DeveloperSettings()
    assertEquals(PdfQuality.ALWAYS_ASK, settings.defaultQuality)
    assertEquals(PageMarginOption.BORDERED, settings.pageMargin)
    assertFalse(settings.autoCropOnImport)
    assertFalse(settings.simplifiedMode)
    assertEquals("PDF documents(pdf_maker)", settings.defaultSavePath)
    assertFalse(settings.highContrastMode)
  }

  @Test
  fun pdfQuality_presets_haveDescriptions() {
    assertEquals("Standard Quality", PdfQuality.STANDARD.displayName)
    assertEquals("Original Quality", PdfQuality.ORIGINAL.displayName)
    assertEquals("Always Ask", PdfQuality.ALWAYS_ASK.displayName)
  }

  @Test
  fun appScreen_viewGeneratedPdfs_exists() {
    val screen: com.example.model.AppScreen = com.example.model.AppScreen.ViewGeneratedPdfs
    assertEquals(com.example.model.AppScreen.ViewGeneratedPdfs, screen)
  }
}
