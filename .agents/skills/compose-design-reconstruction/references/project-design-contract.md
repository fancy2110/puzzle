# Project Design Contract

## Project stack

- Compose Multiplatform: `1.10.3`
- Kotlin: `2.3.21`
- Material3: `1.10.0-alpha05`
- Targets: Android and iOS
- Primary source set: `composeApp/src/commonMain/kotlin`

## Design sources

- Complete componentized specification: `design/fragma/fragma-design-spec.html`
- Tokens: `design/fragma/design-tokens.json`
- Editable vector components: `design/fragma/components/*.svg`
- Product annotations: `design/storybook-v2-ui-annotations.md`
- Reference concept images: `design/mockups/*.png`

Use SVG assets as editable design sources, then reproduce them with cross-platform Canvas/ImageVector components. This project's Android Compose Resources runtime throws for SVG `painterResource`; do not place or load SVG files from `composeResources/drawable`. Use PNG/WebP only for genuine bitmap artwork, not for interactive controls.

## Screen contract

### Home

- Safe-area-aware top bar with brand mark, title, subtitle, and settings icon.
- Five-page `HorizontalPager`; adjacent pages remain partially visible.
- Each page contains an image region and story-copy region in one card.
- Pager indicator reflects the real current page.
- Piece count is continuous from 10 to 300.
- Primary and secondary actions preserve fixed heights.

### Puzzle

- Visible vector back button pauses/returns according to game state.
- Center progress plaque contains a book icon and `已拼 x/y`.
- Timer plaque contains a clock icon and updates once per second.
- Book/canvas decoration surrounds but does not resize the true board image box.
- A target highlights only when dragged-piece overlap reaches 50%.
- Placed content shows only the irregular piece, never the full image.
- Piece tray is horizontally scrollable and selected state does not change layout size.

## Pixel parity rules

- Primary frame: 390 x 844 dp-equivalent portrait.
- Geometry tolerance: 2dp for major edges and repeated gaps.
- Typography may use platform font fallback, but size, weight, line height, alignment, and max lines must match.
- Keep card aspect ratios and illustration framing stable.
- Use `ContentScale.Crop` only where the design crops; use `Fit` for inspectable artwork and pieces.
- Apply system insets exactly once at the page root.

## Validation matrix

1. Android compile: `./gradlew :composeApp:compileDebugKotlinAndroid`
2. iOS compile: `./gradlew :composeApp:compileKotlinIosSimulatorArm64`
3. Portrait design frame: 390 x 844
4. Compact portrait: approximately 360 x 720
5. Landscape smoke test
6. Interactions: pager swipe, settings, start, back, pause/resume, timer, piece select, drag/drop, completion
7. Visual states: default, selected, wrong drop, paused, loading, completed, error

## Current implementation gaps

Close these before claiming design parity:

- Copy or convert `design/fragma/components/*.svg` into Compose Resources; current screens do not consume the supplied vectors.
- Replace text glyph icons such as `⚙`, `✣`, `◆`, pause emoji, and checkmarks with vector resources or Canvas paths.
- Move repeated radii, padding, typography, elevations, and component sizes out of `MenuScreen.kt` and `GameScreen.kt` into design tokens/component defaults.
- Add preview or screenshot baselines for Home and Puzzle at 390 x 844 and compact portrait dimensions.
- Compare actual rendered pages against the Fragma artboard; compilation alone is not visual validation.
- Verify the SVG/Compose resource pipeline on both Android and iOS before replacing Canvas fallbacks.

## Official references

- Android agent workflow and Skills announcement: https://developer.android.com/blog/posts/android-cli-build-android-apps-3x-faster-using-any-agent?hl=zh-cn
- Official Android Skills repository: https://github.com/android/skills
- Official Compose Styles skill: https://github.com/android/skills/blob/main/jetpack-compose/theming/styles/SKILL.md
- Official adaptive Compose skill: https://github.com/android/skills/blob/main/jetpack-compose/adaptive/SKILL.md

Compatibility note: the official Styles skill is experimental, requires newer alpha Compose APIs, and does not support Material component Styles. Reuse its baseline/component-style/screenshot-validation workflow without importing its experimental API into this project.
