//! Core puzzle engine — grid-based image splitting via BFS seed expansion.

use image::{DynamicImage, GenericImageView};
use rand::Rng;
use serde::Serialize;
#[cfg(test)]
use image::{ImageBuffer, Rgba};
use std::vec;

// ── Data Types ──────────────────────────────────────────

// ── Serializable Output Types ───────────────────────────

#[derive(Debug, Clone, Serialize)]
pub struct PuzzlePieceData {
    pub id: String,
    /// Pixel rectangle in source image coordinates
    pub pixel_left: u32,
    pub pixel_top: u32,
    pub pixel_width: u32,
    pub pixel_height: u32,
    /// Grid positions (y, x) of blocks in this piece
    pub block_positions: Vec<BlockPosition>,
}

#[derive(Debug, Clone, Serialize)]
pub struct BlockPosition {
    pub y: u16,
    pub x: u16,
}

#[derive(Debug, Clone, Serialize)]
pub struct SplitResult {
    pub image_width: u32,
    pub image_height: u32,
    pub grid_cols: u32,
    pub grid_rows: u32,
    pub block_size: u32,
    pub pieces: Vec<PuzzlePieceData>,
}

// ── Engine ──────────────────────────────────────────────

pub struct PuzzleEngine {
    img: DynamicImage,
    img_width: u16,
    img_height: u16,
}

impl PuzzleEngine {
    /// Create a new engine from raw PNG/JPEG bytes.
    pub fn from_bytes(data: &[u8]) -> Result<Self, String> {
        let img = image::load_from_memory(data)
            .map_err(|e| format!("Failed to decode image: {}", e))?;
        let (w, h) = img.dimensions();
        Ok(PuzzleEngine {
            img,
            img_width: w as u16,
            img_height: h as u16,
        })
    }

    /// Split the image into irregular pieces.
    ///
    /// - `piece_count`: desired number of pieces
    /// - `block_size`: size of each grid cell in pixels (default 64)
    pub fn split(&self, piece_count: usize, block_size: u16) -> SplitResult {
        let cols = ((self.img_width as u32 + block_size as u32 - 1) / block_size as u32).max(1) as usize;
        let rows = ((self.img_height as u32 + block_size as u32 - 1) / block_size as u32).max(1) as usize;

        let actual_cols = cols;
        let actual_rows = rows;
        let cell_count = actual_cols * actual_rows;

        // 1. Random seeds. Sampling only the requested number avoids
        // shuffling every pixel in large images.
        let mut rng = rand::rng();
        let seed_count = piece_count.min(cell_count);
        if seed_count == 0 {
            return SplitResult {
                image_width: self.img_width as u32,
                image_height: self.img_height as u32,
                grid_cols: actual_cols as u32,
                grid_rows: actual_rows as u32,
                block_size: block_size as u32,
                pieces: Vec::new(),
            };
        }

        let mut owner: Vec<i32> = vec![-1; cell_count];
        let mut queue: Vec<usize> = vec![0; cell_count];
        let mut tail = 0usize;
        let mut head = 0usize;
        let mut selected = std::collections::HashSet::with_capacity(seed_count * 2);
        let mut piece_items: Vec<Vec<(u16, u16)>> = (0..seed_count).map(|_| Vec::new()).collect();
        let mut min_x = vec![u16::MAX; seed_count];
        let mut min_y = vec![u16::MAX; seed_count];
        let mut max_x = vec![0u16; seed_count];
        let mut max_y = vec![0u16; seed_count];

        for piece_index in 0..seed_count {
            let mut cell = rng.random_range(0..cell_count);
            while !selected.insert(cell) {
                cell = rng.random_range(0..cell_count);
            }
            owner[cell] = piece_index as i32;
            queue[tail] = cell;
            tail += 1;
            add_cell_to_piece(
                cell,
                actual_cols,
                piece_index,
                &mut piece_items,
                &mut min_x,
                &mut min_y,
                &mut max_x,
                &mut max_y,
            );
        }

        // 2. Multi-source BFS. Each pixel is visited at most once, so the
        // split cost is linear in image area.
        let mut remaining = cell_count - seed_count;
        while head < tail && remaining > 0 {
            let cell = queue[head];
            head += 1;
            let piece_index = owner[cell] as usize;
            let row = cell / actual_cols;
            let col = cell - row * actual_cols;
            let start_dir = rng.random_range(0..4usize);
            let mut claimed = 0usize;

            for step in 0..4usize {
                let direction = (start_dir + step) & 3;
                let next = match direction {
                    0 if col > 0 => Some(cell - 1),
                    1 if col + 1 < actual_cols => Some(cell + 1),
                    2 if row > 0 => Some(cell - actual_cols),
                    3 if row + 1 < actual_rows => Some(cell + actual_cols),
                    _ => None,
                };

                if let Some(next_cell) = next {
                    if owner[next_cell] < 0 {
                        owner[next_cell] = piece_index as i32;
                        queue[tail] = next_cell;
                        tail += 1;
                        add_cell_to_piece(
                            next_cell,
                            actual_cols,
                            piece_index,
                            &mut piece_items,
                            &mut min_x,
                            &mut min_y,
                            &mut max_x,
                            &mut max_y,
                        );
                        remaining -= 1;
                        claimed += 1;
                        if claimed == 2 { break; }
                    }
                }
            }
        }

        // 3. Assign any disconnected leftovers in a single linear pass.
        if remaining > 0 {
            let mut cursor = 0usize;
            for cell in 0..cell_count {
                if owner[cell] < 0 {
                    owner[cell] = cursor as i32;
                    add_cell_to_piece(
                        cell,
                        actual_cols,
                        cursor,
                        &mut piece_items,
                        &mut min_x,
                        &mut min_y,
                        &mut max_x,
                        &mut max_y,
                    );
                    cursor = (cursor + 1) % seed_count;
                }
            }
        }

        // 4. Build result
        let puzzle_pieces: Vec<PuzzlePieceData> = piece_items.into_iter().enumerate().map(|(i, items)| {
            let left = (min_x[i] as u32 * block_size as u32).min(self.img_width as u32);
            let top = (min_y[i] as u32 * block_size as u32).min(self.img_height as u32);
            let right = ((max_x[i] as u32 + 1) * block_size as u32).min(self.img_width as u32);
            let bottom = ((max_y[i] as u32 + 1) * block_size as u32).min(self.img_height as u32);
            PuzzlePieceData {
                id: format!("piece_{}", i),
                pixel_left: left,
                pixel_top: top,
                pixel_width: right.saturating_sub(left),
                pixel_height: bottom.saturating_sub(top),
                block_positions: items.into_iter().map(|(y, x)| BlockPosition { y, x }).collect(),
            }
        }).collect();

        SplitResult {
            image_width: self.img_width as u32,
            image_height: self.img_height as u32,
            grid_cols: actual_cols as u32,
            grid_rows: actual_rows as u32,
            block_size: block_size as u32,
            pieces: puzzle_pieces,
        }
    }

    /// Extract a piece's pixel data from the source image as raw RGBA bytes.
    /// Save pieces as individual PNG files to the given directory.
    /// Returns the list of saved file paths (relative to output_dir).
    /// Each piece image is masked — only the piece's own blocks are visible,
    /// the rest of the bounding box is transparent.
    pub fn save_pieces(
        &self,
        pieces: &[PuzzlePieceData],
        block_size: u32,
        output_dir: &std::path::Path,
    ) -> Result<Vec<String>, String> {
        use image::{ImageBuffer, Rgba, RgbaImage};

        std::fs::create_dir_all(output_dir)
            .map_err(|e| format!("Failed to create output dir: {}", e))?;

        let mut saved = Vec::with_capacity(pieces.len());

        for piece in pieces {
            let w = piece.pixel_width;
            let h = piece.pixel_height;
            let left = piece.pixel_left;
            let top = piece.pixel_top;

            // Build a block-ownership mask for fast lookup
            let mut owned = std::collections::HashSet::new();
            for bp in &piece.block_positions {
                owned.insert((bp.y as u32, bp.x as u32));
            }

            // Create transparent RGBA buffer
            let mut img: RgbaImage = ImageBuffer::new(w, h);

            for py in 0..h {
                for px in 0..w {
                    let source_x = left + px;
                    let source_y = top + py;
                    let gx = source_x / block_size;
                    let gy = source_y / block_size;

                    if owned.contains(&(gy, gx)) {
                        let pixel = self.img.get_pixel(source_x, source_y);
                        let alpha = edge_alpha(
                            source_x,
                            source_y,
                            block_size,
                            &owned,
                            self.img_width as u32,
                            self.img_height as u32,
                        );
                        img.put_pixel(px, py, Rgba([pixel[0], pixel[1], pixel[2], alpha]));
                    } else {
                        img.put_pixel(px, py, Rgba([0, 0, 0, 0])); // transparent
                    }
                }
            }

            let filename = format!("{}.png", piece.id);
            let filepath = output_dir.join(&filename);
            img.save(&filepath)
                .map_err(|e| format!("Failed to save {}: {}", filename, e))?;

            saved.push(filename);
        }

        Ok(saved)
    }

    /// Extract raw RGBA pixel data for a single piece (legacy FFI).
    pub fn extract_piece_pixels(
        &self,
        piece: &PuzzlePieceData,
    ) -> Vec<u8> {
        let w = piece.pixel_width;
        let h = piece.pixel_height;
        let left = piece.pixel_left;
        let top = piece.pixel_top;
        let mut buf = Vec::with_capacity((w * h * 4) as usize);
        for py in top..top + h {
            for px in left..left + w {
                let pixel = self.img.get_pixel(px, py);
                buf.push(pixel[0]);
                buf.push(pixel[1]);
                buf.push(pixel[2]);
                buf.push(pixel[3]);
            }
        }
        buf
    }
}

fn edge_alpha(
    source_x: u32,
    source_y: u32,
    block_size: u32,
    owned: &std::collections::HashSet<(u32, u32)>,
    image_width: u32,
    image_height: u32,
) -> u8 {
    let has_transparent_neighbor =
        !is_owned_pixel(source_x as i64 - 1, source_y as i64, block_size, owned, image_width, image_height)
        || !is_owned_pixel(source_x as i64 + 1, source_y as i64, block_size, owned, image_width, image_height)
        || !is_owned_pixel(source_x as i64, source_y as i64 - 1, block_size, owned, image_width, image_height)
        || !is_owned_pixel(source_x as i64, source_y as i64 + 1, block_size, owned, image_width, image_height);

    if has_transparent_neighbor { 140 } else { 255 }
}

fn is_owned_pixel(
    x: i64,
    y: i64,
    block_size: u32,
    owned: &std::collections::HashSet<(u32, u32)>,
    image_width: u32,
    image_height: u32,
) -> bool {
    if x < 0 || y < 0 || x >= image_width as i64 || y >= image_height as i64 {
        return false;
    }
    let block_x = x as u32 / block_size;
    let block_y = y as u32 / block_size;
    owned.contains(&(block_y, block_x))
}

fn add_cell_to_piece(
    cell: usize,
    columns: usize,
    piece_index: usize,
    piece_items: &mut [Vec<(u16, u16)>],
    min_x: &mut [u16],
    min_y: &mut [u16],
    max_x: &mut [u16],
    max_y: &mut [u16],
) {
    let row = (cell / columns) as u16;
    let col = (cell - (cell / columns) * columns) as u16;
    piece_items[piece_index].push((row, col));
    if col < min_x[piece_index] { min_x[piece_index] = col; }
    if row < min_y[piece_index] { min_y[piece_index] = row; }
    if col > max_x[piece_index] { max_x[piece_index] = col; }
    if row > max_y[piece_index] { max_y[piece_index] = row; }
}

// ── Tests ───────────────────────────────────────────────

#[cfg(test)]
mod tests {
    use super::*;

    fn make_test_image(w: u32, h: u32) -> Vec<u8> {
        let mut img: ImageBuffer<Rgba<u8>, Vec<u8>> = ImageBuffer::new(w, h);
        for y in 0..h {
            for x in 0..w {
                img.put_pixel(x, y, Rgba([
                    (x % 256) as u8,
                    (y % 256) as u8,
                    ((x + y) % 256) as u8,
                    255,
                ]));
            }
        }
        let mut buf = Vec::new();
        img.write_to(
            &mut std::io::Cursor::new(&mut buf),
            image::ImageFormat::Png,
        ).unwrap();
        buf
    }

    #[test]
    fn test_split_produces_correct_count() {
        let png = make_test_image(800, 600);
        let engine = PuzzleEngine::from_bytes(&png).unwrap();
        let result = engine.split(6, 64);
        assert_eq!(result.pieces.len(), 6);
    }

    #[test]
    fn test_split_covers_all_blocks() {
        let png = make_test_image(800, 600);
        let engine = PuzzleEngine::from_bytes(&png).unwrap();
        let result = engine.split(12, 64);
        let total_blocks = (result.grid_cols * result.grid_rows) as usize;
        let covered: usize = result.pieces.iter().map(|p| p.block_positions.len()).sum();
        assert_eq!(covered, total_blocks);
    }

    #[test]
    fn test_split_no_overlap() {
        let png = make_test_image(800, 600);
        let engine = PuzzleEngine::from_bytes(&png).unwrap();
        let result = engine.split(20, 64);
        let mut seen = std::collections::HashSet::new();
        for piece in &result.pieces {
            for bp in &piece.block_positions {
                assert!(seen.insert((bp.y, bp.x)), "Overlap at ({}, {})", bp.y, bp.x);
            }
        }
    }

    #[test]
    fn test_grid_coverage() {
        // Verify that grid blocks cover the entire image.
        // Bug: cols/rows used floor division, missing rightmost/bottommost edge blocks.
        let png = make_test_image(800, 600);
        let engine = PuzzleEngine::from_bytes(&png).unwrap();
        let result = engine.split(6, 64);

        let expected_cols = (800u32 + 64 - 1) / 64; // ceiling division
        let expected_rows = (600u32 + 64 - 1) / 64;
        println!("Expected grid: {}x{}", expected_cols, expected_rows);
        println!("Actual grid:   {}x{}", result.grid_cols, result.grid_rows);

        // The current bug: grid_cols = 12, but should be 13 (800/64 = 12.5 → 13)
        // This means 32px of width and 24px of height are excluded from the puzzle
        assert_eq!(result.grid_cols, expected_cols,
            "grid_cols should cover full image width (ceiling division)");
        assert_eq!(result.grid_rows, expected_rows,
            "grid_rows should cover full image height (ceiling division)");
    }

    #[test]
    fn test_split_demo1_png() {
        // Integration test with the actual demo image
        let path = concat!(env!("CARGO_MANIFEST_DIR"), "/../../demo1.png");
        let data = match std::fs::read(path) {
            Ok(d) => d,
            Err(e) => {
                println!("Skipping demo1 test: {}", e);
                return;
            }
        };
        let engine = PuzzleEngine::from_bytes(&data).unwrap();
        let (w, h) = (engine.img_width, engine.img_height);
        println!("demo1.png: {}x{} pixels", w, h);

        let result = engine.split(12, 64);
        println!("Grid: {}x{} ({} blocks)", result.grid_cols, result.grid_rows,
            result.grid_cols * result.grid_rows);
        println!("Pieces: {}", result.pieces.len());

        // Verify full coverage
        let expected_cols = (w as u32 + 64 - 1) / 64;
        let expected_rows = (h as u32 + 64 - 1) / 64;
        assert_eq!(result.grid_cols, expected_cols,
            "Grid cols {} != expected {}", result.grid_cols, expected_cols);
        assert_eq!(result.grid_rows, expected_rows,
            "Grid rows {} != expected {}", result.grid_rows, expected_rows);

        // Verify all blocks covered
        let total: usize = result.pieces.iter().map(|p| p.block_positions.len()).sum();
        assert_eq!(total, (result.grid_cols * result.grid_rows) as usize,
            "Block coverage: {} vs {}", total, result.grid_cols * result.grid_rows);

        // Verify no overlap
        let mut seen = std::collections::HashSet::new();
        for p in &result.pieces {
            for bp in &p.block_positions {
                assert!(seen.insert((bp.y, bp.x)),
                    "Overlap at ({}, {})", bp.y, bp.x);
            }
        }

        // Verify pieces are non-empty
        for (i, p) in result.pieces.iter().enumerate() {
            assert!(!p.block_positions.is_empty(),
                "Piece {} has no blocks", i);
            assert!(p.pixel_width > 0, "Piece {} has zero width", i);
            assert!(p.pixel_height > 0, "Piece {} has zero height", i);
        }

        // Print piece summary
        for (i, p) in result.pieces.iter().enumerate() {
            println!("  piece_{}: {}x{}px at ({},{}), {} blocks",
                i, p.pixel_width, p.pixel_height,
                p.pixel_left, p.pixel_top,
                p.block_positions.len());
        }
    }

    #[test]
    fn test_grid_coverage_various_sizes() {
        for (w, h, bs) in [
            (800, 600, 64),
            (1024, 1024, 128),
            (750, 500, 64),
            (100, 100, 33),
        ] {
            let png = make_test_image(w, h);
            let engine = PuzzleEngine::from_bytes(&png).unwrap();
            let result = engine.split(4, bs as u16);

            let expected_cols = (w + bs - 1) / bs;
            let expected_rows = (h + bs - 1) / bs;
            assert_eq!(result.grid_cols, expected_cols,
                "{}x{} @ bs={}: col mismatch", w, h, bs);
            assert_eq!(result.grid_rows, expected_rows,
                "{}x{} @ bs={}: row mismatch", w, h, bs);
        }
    }

    #[test]
    fn test_extract_pixels() {
        let png = make_test_image(200, 200);
        let engine = PuzzleEngine::from_bytes(&png).unwrap();
        let result = engine.split(4, 64);
        for piece in &result.pieces {
            let pixels = engine.extract_piece_pixels(piece);
            let expected = (piece.pixel_width * piece.pixel_height * 4) as usize;
            assert_eq!(pixels.len(), expected);
        }
    }

    #[test]
    fn test_split_1024_square_into_1000_pixel_pieces_under_10_seconds() {
        let engine = PuzzleEngine {
            img: DynamicImage::ImageRgba8(ImageBuffer::new(1024, 1024)),
            img_width: 1024,
            img_height: 1024,
        };

        let started = std::time::Instant::now();
        let result = engine.split(1000, 1);
        let elapsed = started.elapsed();
        let covered: usize = result.pieces.iter().map(|p| p.block_positions.len()).sum();

        assert_eq!(result.pieces.len(), 1000);
        assert_eq!(covered, 1024 * 1024);
        assert!(
            elapsed.as_secs_f32() < 10.0,
            "expected split under 10s, actual {:?}",
            elapsed
        );
    }
}
