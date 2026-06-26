---
name: compose-design-reconstruction
description: Restore annotated Figma/Fragma-style designs in this Compose Multiplatform Android/iOS app. Use when implementing or reviewing screens, components, icons, SVG/vector assets, spacing, system bars, pager behavior, responsive layouts, interaction states, animations, or visual parity against design/fragma artifacts and screenshots.
---

# Compose Design Reconstruction

Implement designs as a measured system, not an approximate drawing. Preserve interaction and coordinate logic while achieving visual parity on Android and iOS.

## Required Context

1. Read `references/project-design-contract.md`.
2. Inspect `design/fragma/design-tokens.json`, `design/fragma/fragma-design-spec.html`, and relevant SVG components.
3. Inspect existing Compose theme, primitives, screen state, and navigation before editing.
4. Treat design annotations as the source of truth. Record any unavoidable platform deviation.

## Workflow

### 1. Establish the baseline

- Identify the exact design frame and target screen.
- Record frame size, safe areas, major bounds, gaps, radii, typography, colors, elevation, and states.
- Map each visual group to one composable with stable dimensions.
- Reuse SVG/vector assets. Do not substitute emoji, Unicode symbols, or generic Material icons when a supplied asset exists.
- Capture a baseline render or screenshot before changing an existing screen when tooling permits.

### 2. Build the design system first

- Move colors, dimensions, typography, shapes, and elevations into theme tokens or component defaults.
- Build primitives before screens: background, icon button, plaque, story card, slider panel, book canvas, piece tray.
- Keep geometry in one coordinate system. Use explicit `Dp`, aspect ratios, constraints, and stable slots.
- Do not introduce experimental Compose Styles APIs in this project unless explicitly requested; the official Styles skill requires alpha APIs and does not style Material components.

### 3. Compose the screen

- Match the design hierarchy and order exactly.
- Use `WindowInsets.safeDrawing` for system bars.
- Use real `HorizontalPager` for story pages and synchronize page state with the view model.
- Keep decorative layers separate from interactive hit regions.
- For the puzzle board, preserve the image box used for drag/drop coordinate conversion. Decorations must never alter target rectangles.
- Keep selected, pressed, loading, empty, error, paused, and completed states layout-stable.
- Avoid text glyphs for back, clock, settings, book, image, or theme icons. Use supplied vectors or Canvas paths.

### 4. Adapt without redesigning

- Phone portrait is the primary parity target.
- Compact-height layouts may reduce vertical gaps and text lines, but must preserve hierarchy and component proportions.
- Landscape may change composition, not visual language.
- Verify text scaling, long Chinese copy, safe areas, and minimum touch targets.
- Use adaptive layout changes only when constraints require them; do not stretch fixed-format cards or artwork arbitrarily.

### 5. Validate visually

- Build Android and iOS simulator targets.
- Render the target screen at the design frame and at one compact phone size.
- Compare bounds, spacing, clipping, typography, colors, icons, and shadows against the design.
- Prefer screenshot tests or Compose Preview screenshot tests when available.
- Iterate until major geometry is within 2dp and repeated spacing is consistent.
- Confirm selection and animations do not resize parents or shift adjacent content.
- Test system back, visible back control, timer updates, pager swipes, drag/drop, pause overlay, and completion state.

## Completion Gate

Do not call the work complete unless all are true:

- Android and iOS Kotlin compilation pass.
- Design assets are used or a documented vector-equivalent implementation exists.
- The screen respects safe drawing insets.
- Pager, back button, timer, selection, and drag/drop behavior work.
- No overlap, clipping, page jitter, or selected-state reflow is visible.
- A visual comparison has been performed, or the inability to render has been explicitly reported.

## Official Basis

This workflow follows the Android Skills approach: ground the agent with task-specific `SKILL.md` instructions, centralize component styles, establish screenshot baselines, compare visual output, then run builds and UI tests. See the project contract for source links and compatibility notes.
