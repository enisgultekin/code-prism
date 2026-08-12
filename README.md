# Code Prism

Code Prism is an IntelliJ Platform plugin for IntelliJ IDEA and Rider that colors YAML mapping keys by their indentation depth. It intentionally does one small thing: it paints key tokens only, so values, comments, inspections, and the active IDE theme remain readable.

The plugin is represented in the IDE Plugin Manager by the Chromatic Arc logo, with light and dark theme SVG variants packaged in `META-INF`.

## Features

- YAML only (`.yaml` / `.yml`), via the platform YAML PSI. The descriptor binds to
  YAML's registered language ID, `yaml` (lowercase).
- A repeating, configurable pastel color palette based on two-space indentation levels.
- Palette recovery: an empty or invalid saved palette is restored to the pastel defaults.
- Application-level settings at **Settings/Preferences | Tools | Code Prism**.
- Lightweight annotator: indentation is read only from each key line; no whole-file traversal or background indexing.

## Requirements and compatibility

- JDK 25. The included Gradle wrapper uses Gradle 9.0.0.
- Built for IntelliJ IDEA Ultimate 2026.2 (platform build 262), including IU-262.8665.337.
- The plugin has no language-specific JVM APIs and depends only on the bundled YAML plugin, so the same ZIP can be installed in compatible IntelliJ IDEA editions in that build range.

## Develop

```bash
./gradlew runIde
```

This starts a sandbox IntelliJ IDEA instance. Open a YAML file and adjust the ordered palette under **Settings/Preferences | Tools | Code Prism**. Click a visible color swatch, then use **Change selected…** (or double-click it) to open the picker; add, remove, or move a selected color as needed. **Reset defaults** restores the pastel starting palette. List order determines the indentation-depth sequence; a change is applied after the settings dialog is confirmed. Existing comma- or semicolon-separated palette settings are read automatically and saved in the current canonical format; blank or invalid palettes are repaired to the defaults.

## Build and test

```bash
./gradlew test buildPlugin
./gradlew verifyPlugin
```

`buildPlugin` creates `code-prism-<version>.zip` in `build/distributions/`. `verifyPlugin` validates the explicit IntelliJ IDEA Ultimate 2026.2 target. The first invocation downloads IDE artifacts and can take several minutes.

The current local build is **0.2.2**. This patch replaces the Settings page's deferred binding implementation with a synchronous IntelliJ `SearchableConfigurable` lifecycle, so the palette editor is constructed immediately when its page is selected.

## Palette migration

Code Prism uses the plugin ID `io.github.codeprism`. It is installed alongside the earlier plugin ID `io.github.yamlscopecolors`, rather than replacing it; uninstall the earlier plugin before installing Code Prism, since both register YAML annotators. To preserve an existing user palette, the persistent-state name and storage file intentionally remain `YamlScopeColorsSettings` and `yamlScopeColors.xml`. These are compatibility identifiers only; Code Prism is the project, package, and distribution name.

## Design notes

The annotator receives a `YAMLKeyValue` and colors only its key range. Depth is derived from leading whitespace (`tab = two spaces`) and uses `depth % palette.size`; this keeps the work bounded by one line per key and prevents highlighting a parent scope from overwriting normal YAML syntax colors.
