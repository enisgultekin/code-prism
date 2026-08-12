package io.github.codeprism

import kotlin.test.Test
import kotlin.test.assertEquals

class CodePrismSettingsTest {
  @Test
  fun `accepts only six digit hex colors`() {
    val settings = CodePrismSettings().apply { palette = "#abcdef, blue, #123456" }
    assertEquals(listOf("#abcdef", "#123456"), settings.colors())
  }

  @Test
  fun `accepts legacy semicolon separated palette values`() {
    val settings = CodePrismSettings().apply { palette = "#4F8CC9; #7A9E3A; invalid" }
    assertEquals(listOf("#4F8CC9", "#7A9E3A"), settings.colors())
    assertEquals("#4F8CC9,#7A9E3A", CodePrismSettings.serializePalette(settings.colors()))
  }

  @Test
  fun `repairs an empty palette to the visible defaults`() {
    val settings = CodePrismSettings().apply { palette = "" }
    assertEquals(CodePrismSettings.DEFAULT_PALETTE, settings.colors())
  }
}
