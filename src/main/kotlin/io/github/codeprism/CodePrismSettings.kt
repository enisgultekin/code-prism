package io.github.codeprism

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "YamlScopeColorsSettings", storages = [Storage("yamlScopeColors.xml")])
@Service(Service.Level.APP)
class CodePrismSettings : PersistentStateComponent<CodePrismSettings> {
  /**
   * Persisted as comma-separated CSS-style RGB colours. Semicolon-separated values are accepted
   * when loading settings saved by earlier versions and are rewritten in the canonical format.
   */
  var palette: String = serializePalette(DEFAULT_PALETTE)

  override fun getState(): CodePrismSettings = this

  override fun loadState(state: CodePrismSettings) {
    XmlSerializerUtil.copyBean(state, this)
    palette = serializePalette(colors())
  }

  /** A blank or invalid palette is repaired to the visible default palette. */
  fun colors(): List<String> = parsePalette(palette).ifEmpty { DEFAULT_PALETTE }

  companion object {
    private val HEX_COLOR = Regex("#[0-9a-fA-F]{6}")
    val DEFAULT_PALETTE = listOf("#4F8CC9", "#7A9E3A", "#B070C0", "#C77D3A", "#3B9D9D", "#B85C72")

    fun parsePalette(value: String): List<String> =
      value.split(',', ';').map { it.trim() }.filter { it.matches(HEX_COLOR) }

    fun serializePalette(colors: List<String>): String = colors.joinToString(",")

    fun getInstance(): CodePrismSettings =
      ApplicationManager.getApplication().getService(CodePrismSettings::class.java)
  }
}
