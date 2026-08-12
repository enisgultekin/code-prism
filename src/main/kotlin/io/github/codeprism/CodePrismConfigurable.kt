package io.github.codeprism

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.components.JBList
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JColorChooser
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ListCellRenderer
import javax.swing.ListSelectionModel
import javax.swing.UIManager

/**
 * Kept deliberately synchronous: Settings creates Configurable components on the EDT and expects
 * createComponent() to return a complete Swing tree without deferred panel bindings.
 */
class CodePrismConfigurable : SearchableConfigurable {
  private val settings = CodePrismSettings.getInstance()
  private var paletteEditor: PaletteEditor? = null

  override fun getId(): String = "io.github.codeprism.settings"

  override fun getDisplayName(): String = "Code Prism"

  override fun createComponent(): JComponent {
    val editor = PaletteEditor().also { it.setColors(settings.colors()) }
    paletteEditor = editor
    return JPanel(BorderLayout(0, 8)).apply {
      border = BorderFactory.createEmptyBorder(8, 0, 0, 0)
      add(JLabel("Palette colors"), BorderLayout.NORTH)
      add(editor, BorderLayout.CENTER)
      add(
        JLabel(
          "Configure the depth palette for YAML mapping keys. Code Prism currently supports YAML and is designed to grow toward language-aware structural coloring in future releases.",
        ),
        BorderLayout.SOUTH,
      )
    }
  }

  override fun isModified(): Boolean = paletteEditor?.colors() != null && paletteEditor?.colors() != settings.colors()

  override fun apply() {
    val colors = paletteEditor?.colors() ?: return
    settings.palette = CodePrismSettings.serializePalette(colors.ifEmpty { CodePrismSettings.DEFAULT_PALETTE })
  }

  override fun reset() {
    paletteEditor?.setColors(settings.colors())
  }

  override fun disposeUIResources() {
    paletteEditor = null
  }
}

/** Renders an ordered swatch rather than exposing the persisted HEX value as visible UI text. */
private class PaletteCellRenderer : JPanel(BorderLayout(8, 0)), ListCellRenderer<String> {
  private val position = JLabel()
  private val swatch = JPanel()

  init {
    isOpaque = true
    border = BorderFactory.createEmptyBorder(4, 8, 4, 8)
    position.preferredSize = Dimension(32, 34)
    swatch.preferredSize = Dimension(220, 34)
    swatch.minimumSize = Dimension(72, 34)
    add(position, BorderLayout.WEST)
    add(swatch, BorderLayout.CENTER)
  }

  override fun getListCellRendererComponent(
    list: JList<out String>,
    value: String,
    index: Int,
    isSelected: Boolean,
    cellHasFocus: Boolean,
  ): Component {
    val foreground = if (isSelected) list.selectionForeground else list.foreground
    background = if (isSelected) list.selectionBackground else list.background
    position.text = "${index + 1}."
    position.foreground = foreground
    swatch.background = Color.decode(value)
    swatch.border = BorderFactory.createLineBorder(
      if (isSelected) foreground else UIManager.getColor("Component.borderColor") ?: Color.GRAY,
      if (isSelected) 2 else 1,
    )
    toolTipText = "Palette color ${index + 1}: $value"
    // List renderers are reused before Swing creates an AccessibleContext for this panel.
    // Do not dereference the nullable backing field while Settings measures list rows.
    getAccessibleContext()?.accessibleName = "Palette color ${index + 1}: $value"
    return this
  }
}

private class PaletteEditor : JPanel(BorderLayout(8, 0)) {
  private val model = DefaultListModel<String>()
  private val list = JBList(model).apply {
    selectionMode = ListSelectionModel.SINGLE_SELECTION
    accessibleContext.accessibleName = "Palette colors"
    cellRenderer = PaletteCellRenderer()
    fixedCellHeight = 42
  }
  private val addButton = JButton("Add color…")
  private val updateButton = JButton("Change selected…")
  private val removeButton = JButton("Remove selected")
  private val moveUpButton = JButton("Move up")
  private val moveDownButton = JButton("Move down")
  private val resetButton = JButton("Reset defaults")
  private val controls = JPanel().apply {
    layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS)
    preferredSize = Dimension(CONTROL_WIDTH, 222)
    minimumSize = Dimension(CONTROL_WIDTH, 222)
  }

  init {
    border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
    preferredSize = Dimension(524, 222)
    add(JScrollPane(list).apply { preferredSize = Dimension(360, 222) }, BorderLayout.CENTER)
    add(controls, BorderLayout.EAST)
    addControl(addButton)
    addControl(updateButton)
    addControl(removeButton)
    controls.add(Box.createVerticalStrut(8))
    addControl(moveUpButton)
    addControl(moveDownButton)
    controls.add(Box.createVerticalStrut(8))
    addControl(resetButton)

    addButton.addActionListener { chooseColor("Add palette color", DEFAULT_COLOR)?.let(::addColor) }
    updateButton.addActionListener { updateSelectedColor() }
    removeButton.addActionListener {
      val index = list.selectedIndex
      if (index >= 0) {
        model.remove(index)
        list.selectedIndex = (index.coerceAtMost(model.size - 1)).coerceAtLeast(0)
      }
    }
    moveUpButton.addActionListener { moveSelected(-1) }
    moveDownButton.addActionListener { moveSelected(1) }
    resetButton.addActionListener { setColors(CodePrismSettings.DEFAULT_PALETTE) }
    list.addListSelectionListener { updateButtons() }
    list.addMouseListener(object : MouseAdapter() {
      override fun mouseClicked(event: MouseEvent) {
        if (event.clickCount == 2 && list.selectedIndex >= 0) updateSelectedColor()
      }
    })
    updateButtons()
  }

  fun setColors(colors: List<String>) {
    model.clear()
    colors.forEach(model::addElement)
    if (model.size > 0) list.selectedIndex = 0
    updateButtons()
  }

  fun colors(): List<String> = List(model.size) { model[it] }

  private fun addControl(button: JButton) {
    button.alignmentX = Component.LEFT_ALIGNMENT
    button.maximumSize = Dimension(CONTROL_WIDTH, button.preferredSize.height)
    controls.add(button)
  }

  private fun addColor(color: String) {
    model.addElement(color)
    list.selectedIndex = model.size - 1
  }

  private fun updateSelectedColor() {
    val index = list.selectedIndex
    if (index >= 0) chooseColor("Change selected palette color", model[index])?.let { model[index] = it }
  }

  private fun moveSelected(direction: Int) {
    val from = list.selectedIndex
    val to = from + direction
    if (from < 0 || to !in 0 until model.size) return
    val color = model.remove(from)
    model.add(to, color)
    list.selectedIndex = to
  }

  private fun chooseColor(title: String, current: String): String? =
    JColorChooser.showDialog(this, title, Color.decode(current), true)?.let {
      "#%02X%02X%02X".format(it.red, it.green, it.blue)
    }

  private fun updateButtons() {
    val index = list.selectedIndex
    val hasSelection = index >= 0
    updateButton.isEnabled = hasSelection
    removeButton.isEnabled = hasSelection
    moveUpButton.isEnabled = index > 0
    moveDownButton.isEnabled = index >= 0 && index < model.size - 1
  }

  private companion object {
    const val CONTROL_WIDTH = 156
    val DEFAULT_COLOR = CodePrismSettings.DEFAULT_PALETTE.first()
  }
}
