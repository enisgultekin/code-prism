package io.github.codeprism

import com.intellij.lang.Language
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLKeyValue

class YamlIndentAnnotatorTest : BasePlatformTestCase() {
  fun `test yaml language id is registered`() {
    assertEquals("yaml", Language.findLanguageByID("yaml")?.id)
  }

  fun `test mapping keys are parsed and their indentation determines depth`() {
    val file = PsiFileFactory.getInstance(project).createFileFromText(
      "scopes.yaml",
      YAMLFileType.YML,
      "root:\n  child:\n    grandchild: value\n",
    )
    val keys = PsiTreeUtil.collectElementsOfType(file, YAMLKeyValue::class.java).toList()

    assertEquals(listOf("root", "child", "grandchild"), keys.map { it.key?.text })
    assertEquals(listOf(0, 1, 2), keys.map(YamlIndentAnnotator()::depthFor))
  }
}
