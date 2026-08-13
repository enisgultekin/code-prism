# Code Prism

Code Prism is a structural-coloring plugin for IntelliJ Platform IDEs. In this release, it colors YAML mapping keys and XML element names by structural depth with a configurable palette, making nested configuration easier to scan without overpowering values, comments, inspections, or the active IDE theme.

Code Prism is intentionally focused today: YAML mapping keys and XML element names are the current supported scopes. Its longer-term direction is language-aware structural coloring for nested source structures, but additional language support is not part of v1.0.0.

The plugin is represented in the IDE Plugin Manager by the Chromatic Arc logo, with light and dark theme SVG variants packaged in `META-INF`.

## Features

- YAML mapping keys (`.yaml` / `.yml`), via the platform YAML PSI. The descriptor binds to
  YAML's registered language ID, `yaml` (lowercase); colors repeat by two-space indentation depth.
- XML element local names (`.xml`): opening and closing names share their nesting-depth color.
  Attribute names and values, text, comments, CDATA, and namespace prefixes/declarations retain IDE syntax colors.
- One repeating, configurable pastel palette shared by both structural views.
- Palette recovery: an empty or invalid saved palette is restored to the pastel defaults.
- Application-level settings at **Settings/Preferences | Tools | Code Prism**.
- Lightweight annotator: indentation is read only from each key line; no whole-file traversal or background indexing.

## Product direction

Code Prism starts with precise, low-noise YAML and XML experiences and a palette model that makes structural depth immediately visible. Future releases may extend the same language-aware approach to additional source-file structures where depth and nesting improve readability. Those extensions are prospective; the current plugin supports YAML mapping keys and XML element names only.

## Requirements and compatibility

- JDK 21. The included Gradle wrapper uses Gradle 9.0.0.
- Built against IntelliJ IDEA Ultimate 2025.1 (platform build 251) and verified for the 251–262 platform range, including IntelliJ IDEA Ultimate 2026.2.
- The packaged plugin declares `since-build="251"` and `until-build="262.*"`; the same ZIP supports compatible IDEs in those build lines. Compatibility with build 263 and later will be added only after testing.
- The plugin uses stable platform XML PSI and the bundled YAML plugin; it has no dependencies on additional language plugins.

## Develop

```bash
./gradlew runIde
```

This starts a sandbox IntelliJ IDEA instance. Open a YAML or XML file and adjust the ordered palette under **Settings/Preferences | Tools | Code Prism**. Click a visible color swatch, then use **Change selected…** (or double-click it) to open the picker; add, remove, or move a selected color as needed. **Reset defaults** restores the pastel starting palette. List order determines the structural-depth sequence; a change is applied after the settings dialog is confirmed. Existing comma- or semicolon-separated palette settings are read automatically and saved in the current canonical format; blank or invalid palettes are repaired to the defaults.

## Build and test

```bash
./gradlew test buildPlugin
./gradlew verifyPlugin
```

`buildPlugin` creates `code-prism-<version>.zip` in `build/distributions/`. `verifyPlugin` validates IntelliJ IDEA Ultimate 2025.1, 2025.2, 2025.3, 2026.1, and 2026.2 targets. The first invocation downloads IDE artifacts and can take several minutes.

The current local build is **1.0.0**, the first stable release under independent semantic versioning. This one ZIP supports IntelliJ Platform builds 251–262; compatibility with later build lines is intentionally not claimed until verified. The Settings page uses a synchronous IntelliJ `SearchableConfigurable` lifecycle, so the palette editor is constructed immediately when its page is selected.

## Palette migration

Code Prism uses the plugin ID `io.github.codeprism`. It is installed alongside the earlier plugin ID `io.github.yamlscopecolors`, rather than replacing it; uninstall the earlier plugin before installing Code Prism, since both register YAML annotators. To preserve an existing user palette, the persistent-state name and storage file intentionally remain `YamlScopeColorsSettings` and `yamlScopeColors.xml`. These are compatibility identifiers only; Code Prism is the project, package, and distribution name.

## Design notes

The YAML annotator receives a `YAMLKeyValue` and colors only its key range. Depth is derived from leading whitespace (`tab = two spaces`) and uses `depth % palette.size`; this keeps the work bounded by one line per key and prevents highlighting a parent scope from overwriting normal YAML syntax colors. The XML annotator receives an `XmlTag`, counts enclosing tags, and ranges only directly owned `XML_NAME` tokens; it colors the local name of opening/closing tags while leaving XML syntax and attributes untouched.
