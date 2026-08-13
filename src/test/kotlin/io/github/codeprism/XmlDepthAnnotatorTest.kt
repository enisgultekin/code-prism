package io.github.codeprism

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class XmlDepthAnnotatorTest : BasePlatformTestCase() {
  fun `test nested XML tags use element depth and range local opening and closing names`() {
    val file = PsiFileFactory.getInstance(project).createFileFromText(
      "scopes.xml",
      XmlFileType.INSTANCE,
      "<root attr=\"keep\"><ns:child xmlns:ns=\"urn:test\"><leaf><![CDATA[data]]></leaf></ns:child></root>",
    )
    val annotator = XmlDepthAnnotator()
    val tags = PsiTreeUtil.collectElementsOfType(file, XmlTag::class.java).toList()

    assertEquals(listOf("root", "ns:child", "leaf"), tags.map { it.name })
    assertEquals(listOf(0, 1, 2), tags.map(annotator::depthFor))
    assertEquals(
      listOf("root", "root", "child", "child", "leaf", "leaf"),
      tags.flatMap(annotator::tagNameRanges).map { file.text.substring(it.startOffset, it.endOffset) },
    )
  }

  fun `test incomplete XML tag is safe`() {
    val file = PsiFileFactory.getInstance(project).createFileFromText(
      "incomplete.xml",
      XmlFileType.INSTANCE,
      "<root><ns:child",
    )
    val tag = PsiTreeUtil.findChildOfType(file, XmlTag::class.java) ?: error("XML tag was not parsed")

    assertTrue(XmlDepthAnnotator().tagNameRanges(tag).all { it.length > 0 })
  }
}
