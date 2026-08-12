package io.github.codeprism

import com.intellij.testFramework.EdtTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ThrowableRunnable
import java.awt.Component
import java.awt.Container
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JList

class CodePrismConfigurableTest : BasePlatformTestCase() {
  fun `test settings page creates default palette and controls on the EDT`() {
    val configurable = CodePrismConfigurable()
    lateinit var component: JComponent

    EdtTestUtil.runInEdtAndWait<RuntimeException>(ThrowableRunnable { component = configurable.createComponent() })
    try {
      val paletteList = allComponents(component).filterIsInstance<JList<*>>().singleOrNull()
        ?: error("Palette list was not created")
      assertEquals(CodePrismSettings.DEFAULT_PALETTE.size, paletteList.model.size)
      assertEquals(
        setOf("Add color…", "Change selected…", "Remove selected", "Move up", "Move down", "Reset defaults"),
        allComponents(component).filterIsInstance<JButton>().mapNotNull { it.text }.filter { it.isNotBlank() }.toSet(),
      )
    } finally {
      EdtTestUtil.runInEdtAndWait<RuntimeException>(ThrowableRunnable { configurable.disposeUIResources() })
    }
  }

  private fun allComponents(component: Component): List<Component> = buildList {
    add(component)
    (component as? Container)?.components?.forEach { addAll(allComponents(it)) }
  }
}
