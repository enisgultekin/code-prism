package io.github.codeprism

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsScheme
import java.awt.Color

/** Shared palette lookup for structural annotators. */
internal object DepthPalette {
  fun colorFor(depth: Int, scheme: EditorColorsScheme): Color {
    val palette = CodePrismSettings.getInstance().colors()
    if (palette.isNotEmpty()) return Color.decode(palette[depth % palette.size])

    return scheme.getAttributes(DefaultLanguageHighlighterColors.KEYWORD)?.foregroundColor
      ?: scheme.defaultForeground
  }
}
