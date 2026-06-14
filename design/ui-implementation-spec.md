# Puzzle App UI Implementation Spec

This spec converts the generated mockups in `design/mockups/` into programmatic UI rules for Compose Multiplatform.

## Visual Direction

- Mood: quiet, tactile, premium, isometric puzzle world.
- Composition: large scenic object first, UI controls second.
- Chrome: minimal, soft surfaces, low contrast outlines, clear primary action.
- Avoid: emoji UI, demo-looking cards, default Material purple, heavy gradients, dense copy.

## Design Tokens

### Colors

| Token | Hex | Usage |
| --- | --- | --- |
| `Mist` | `#F7F2EA` | App background |
| `Cloud` | `#FFF9F0` | Cards, trays, dialogs |
| `Stone` | `#D9CBB6` | Borders and architectural surfaces |
| `StoneDark` | `#7A6750` | Primary text |
| `Muted` | `#8F806E` | Secondary text |
| `Teal` | `#7CAEA5` | Selected controls, drop targets |
| `TealDark` | `#356D6A` | Primary semantic color |
| `Coral` | `#E98166` | Main CTA |
| `CoralDark` | `#B85B44` | CTA pressed/outline |
| `Gold` | `#CDA45A` | Completion and accent strokes |
| `ErrorSoft` | `#C76A5A` | Wrong placement feedback |

### Dimensions

- Page padding: `18dp`
- Compact page padding: `10dp`
- Card radius: `18dp`
- Control radius: `24dp`
- Small icon button: `46dp`
- Primary button height: `58dp`
- Secondary button height: `50dp`
- Game tray height: `140dp`
- Board border width: `2dp`

### Typography

- Brand / hero title: `34sp`, medium serif-like feeling where available; fallback bold.
- Page title: `24sp`, bold.
- Section title: `15sp`, bold.
- Body: `13sp`.
- Caption: `12sp`.
- Game metric pill: `16sp`, medium/bold.

Compose does not currently define custom fonts, so use weight, color, spacing and layout to approximate the mockups.

## Component Rules

### App Background

- Use `Mist`.
- Add faint isometric blocks through simple layered translucent rectangles when useful.
- Do not use full-screen dark gradients.

### Primary Button

- Fill `Coral`.
- Text white, bold, 18-22sp depending on page.
- Rounded pill, 24dp radius.
- Height 58dp.

### Secondary Button

- Fill `Cloud`.
- Border `Stone`.
- Text `StoneDark`.
- Height 50dp.

### Icon Button

- Fill `Cloud`.
- Border `Stone` at 1dp.
- Size 44-48dp.
- Use plain text symbols only as fallback; prefer native vector icons later.

### Preview Pedestal

- Image area should feel mounted on a stone pedestal.
- Use stacked rounded surfaces: shadow card -> image frame -> label plaque.
- Keep image large; controls should not dominate.

## Screen Specs

### Home

- Top: centered brand `Puzzle`; settings icon at top-right.
- Main: large framed preview image on pedestal occupying 52-58% of available height.
- Overlay/plaque: theme name and selected difficulty chip.
- Difficulty selector: 4 equal pill segments.
- CTA: `开始游戏`, full width, coral.
- Secondary actions: `换主题`, `换图片`, icon-like circular surfaces with labels.

### Theme Selection

- Top: back icon left, title `选择画面` centered.
- Grid: two columns.
- Cards: tall floating cards, image thumbnail 70% card height, label and piece chip below.
- Selected card: coral/gold glow border and check mark badge.
- Bottom: primary `开始这张`, secondary text `确认选择`.

### Gameplay

- Top: back/pause controls left, progress pill center, timer pill right.
- Board: large stone-framed board. Reference image alpha `0.35-0.45`.
- Placed pieces: actual bitmap fragments, visible above reference image.
- Drop target: teal glow, not red.
- Tray: cloud surface at bottom with stable height 140dp; pieces retain image aspect.
- Hint: `点击或拖动`, small, muted, below tray.
- Wrong placement: soft text `再试试` with coral color, no emoji.

### Completion

- Top: home/back icon, title `完成拼图`.
- Main: restored image large, glowing frame on pedestal.
- Stats: three columns: time, pieces, complete percentage.
- Primary: `再来一局`.
- Secondary: `换个主题` or return menu action.
- Celebration: subtle; avoid noisy full-screen confetti.

## Interaction Requirements

- Home start -> generating -> playing.
- Theme card tap updates selected theme, confirm returns.
- Gameplay supports tap-select and drag-to-place.
- Back during game opens pause dialog.
- Completion stops timer and locks board.
- All failures show retry and home actions.

