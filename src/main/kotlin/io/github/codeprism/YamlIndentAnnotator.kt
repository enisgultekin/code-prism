package io.github.codeprism

import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLKeyValue

/**
 * Deliberately works from the key's line prefix instead of walking sibling maps.
 * That keeps every annotation O(line length) and avoids YAML tree-wide recalculation.
 */
/**
 * An annotator is intentionally used instead of a syntax highlighter: indentation
 * depth is PSI/document dependent, while a syntax highlighter only sees lexer tokens.
 */
class YamlIndentAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    val keyValue = element as? YAMLKeyValue ?: return
    val key = keyValue.key ?: return
    val color = DepthPalette.colorFor(depthFor(keyValue), EditorColorsManager.getInstance().globalScheme)

    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
      .range(key.textRange)
      .enforcedTextAttributes(TextAttributes().apply { foregroundColor = color })
      .create()
  }

  internal fun depthFor(keyValue: YAMLKeyValue): Int = indentationBefore(keyValue) / INDENT_WIDTH

  private fun indentationBefore(keyValue: YAMLKeyValue): Int {
    val text = keyValue.containingFile.viewProvider.document?.charsSequence ?: return 0
    var offset = keyValue.textRange.startOffset - 1
    var indentation = 0
    while (offset >= 0 && text[offset] != '\n' && text[offset] != '\r') offset--
    offset++
    while (offset < text.length && (text[offset] == ' ' || text[offset] == '\t')) {
      indentation += if (text[offset] == '\t') INDENT_WIDTH else 1
      offset++
    }
    return indentation
  }

  private companion object {
    const val INDENT_WIDTH = 2
  }
}
