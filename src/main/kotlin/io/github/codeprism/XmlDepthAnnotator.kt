package io.github.codeprism

import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType

/**
 * Colors only the local-name part of an XML element's opening and closing tag.
 * Prefixes, namespace declarations, attributes, values, text, comments, and CDATA retain
 * the IDE's normal highlighting. Direct XML_NAME tokens also make incomplete tags harmless.
 */
class XmlDepthAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    val tag = element as? XmlTag ?: return
    val color = DepthPalette.colorFor(depthFor(tag), EditorColorsManager.getInstance().globalScheme)

    tagNameRanges(tag).forEach { range ->
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(range)
        .enforcedTextAttributes(TextAttributes().apply { foregroundColor = color })
        .create()
    }
  }

  /** Root element is depth 0; each enclosing element advances the shared palette by one. */
  internal fun depthFor(tag: XmlTag): Int = generateSequence(tag.parent) { it.parent }
    .count { it is XmlTag }

  /**
   * XML_NAME nodes directly owned by a tag are its opening/closing element names. Attribute
   * names live under attribute nodes, so they are intentionally excluded. For `p:item`, only
   * `item` is ranged; the prefix and colon remain under the IDE XML highlighter.
   */
  internal fun tagNameRanges(tag: XmlTag): List<TextRange> = tag.node.getChildren(null)
    .asSequence()
    .filter { it.elementType == XmlTokenType.XML_NAME }
    .map { node ->
      val prefixEnd = node.text.lastIndexOf(':') + 1
      TextRange(node.startOffset + prefixEnd, node.startOffset + node.textLength)
    }
    .filter { !it.isEmpty }
    .toList()
}
