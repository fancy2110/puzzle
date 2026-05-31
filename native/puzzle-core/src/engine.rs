//! Core puzzle engine — grid-based image splitting via BFS seed expansion.

use image::{DynamicImage, GenericImageView};
use rand::seq::SliceRandom;
use serde::Serialize;
#[cfg(test)]
use image::{ImageBuffer, Rgba};
use std::vec;

// ── Data Types ──────────────────────────────────────────

/// Grid position (y, x) with an occupancy flag.
#[derive(Debug, Clone, Copy)]
struct Position {
    data: u32,
}

impl Position {
    const TAKEN_FLAG: u32 = 1 << 31;
    const VALUE_MASK: u32 = 0x7FFF;

    #[inline]
    fn new(y: u16, x: u16) -> Self {
        let data = (x as u32 & Self::VALUE_MASK)
                 | ((y as u32 & Self::VALUE_MASK) << 16);
        Position { data }
    }

    #[inline]
    fn x(&self) -> u16 { (self.data & Self::VALUE_MASK) as u16 }
    #[inline]
    fn y(&self) -> u16 { ((self.data >> 16) & Self::VALUE_MASK) as u16 }
    #[inline]
    fn is_taken(&self) -> bool { self.data & Self::TAKEN_FLAG != 0 }
    fn set_taken(&mut self) { self.data |= Self::TAKEN_FLAG; }
}

/// Pixel-coordinate rectangle.
#[derive(Debug, Clone, Copy)]
struct PixelRect {
    left: u16,
    top: u16,
    right: u16,
    bottom: u16,
}

impl PixelRect {
    fn expand(&mut self, l: u16, t: u16, r: u16, b: u16) {
        if l < self.left { self.left = l; }
        if t < self.top { self.top = t; }
        if r > self.right { self.right = r; }
        if b > self.bottom { self.bottom = b; }
    }
    fn width(&self) -> u16 { self.right - self.left }
    fn height(&self) -> u16 { self.bottom - self.top }
}

/// An individual grid block (one cell).
#[derive(Debug, Clone)]
struct Block {
    pos: Position,
    rect: PixelRect,
}

impl Block {
    fn new(y: u16, x: u16, rect: PixelRect) -> Self {
        Block { pos: Position::new(y, x), rect }
    }
    fn is_taken(&self) -> bool { self.pos.is_taken() }
    fn take(&mut self) { self.pos.set_taken(); }
}

/// One irregular puzzle piece — a collection of grid blocks.
#[derive(Debug, Clone)]
struct Piece {
    /// Pixel boundary (for cropping the source image)
    pixel_rect: PixelRect,
    /// Grid coordinate boundary
    block_rect: (u16, u16, u16, u16), // (min_y, min_x, max_y, max_x)
    /// All block positions belonging to this piece
    items: Vec<(u16, u16)>, // (y, x)
}

impl Piece {
    fn new(block: &Block) -> Self {
        let x = block.pos.x();
        let y = block.pos.y();
        Piece {
            pixel_rect: block.rect,
            block_rect: (y, x, y, x),
            items: vec![(y, x)],
        }
    }

    fn add_block(&mut self, block: &Block) {
        let x = block.pos.x();
        let y = block.pos.y();
        // Expand block bounds
        self.block_rect.0 = self.block_rect.0.min(y);
        self.block_rect.1 = self.block_rect.1.min(x);
        self.block_rect.2 = self.block_rect.2.max(y);
        self.block_rect.3 = self.block_rect.3.max(x);
        self.items.push((y, x));
        // Expand pixel bounds
        self.pixel_rect.expand(
            block.rect.left, block.rect.top,
            block.rect.right, block.rect.bottom,
        );
    }
}

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

        // 1. Build grid
        let mut grid: Vec<Vec<Block>> = Vec::with_capacity(rows);
        for y in 0..rows {
            let mut row_blocks = Vec::with_capacity(cols);
            let top = (y as u16 * block_size).min(self.img_height);
            let bottom = (top + block_size).min(self.img_height);
            if top >= bottom { break; }

            for x in 0..cols {
                let left = (x as u16 * block_size).min(self.img_width);
                let right = (left + block_size).min(self.img_width);
                if left >= right { break; }

                let rect = PixelRect { left, top, right, bottom };
                row_blocks.push(Block::new(y as u16, x as u16, rect));
            }
            grid.push(row_blocks);
        }

        let actual_cols = grid.first().map(|r| r.len()).unwrap_or(0);
        let actual_rows = grid.len();

        // 2. Random seeds
        let mut rng = rand::rng();
        let mut positions: Vec<(usize, usize)> = Vec::new();
        for y in 0..actual_rows {
            for x in 0..actual_cols {
                positions.push((y, x));
            }
        }
        positions.shuffle(&mut rng);
        let seed_count = piece_count.min(positions.len());

        let mut pieces: Vec<Piece> = Vec::with_capacity(seed_count);
        for i in 0..seed_count {
            let (y, x) = positions[i];
            grid[y][x].take();
            pieces.push(Piece::new(&grid[y][x]));
        }

        // 3. BFS expand (single round, each piece claims up to 2 neighbours)
        // Directions: left, right, up, down
        let dirs: [(isize, isize); 4] = [(0, -1), (0, 1), (-1, 0), (1, 0)];

        loop {
            let mut queue: Vec<(usize, usize, usize)> = Vec::new(); // (y, x, piece_index)

            for (pi, piece) in pieces.iter().enumerate() {
                for &(py, px) in &piece.items {
                    let mut dir_indices: Vec<usize> = (0..4).collect();
                    dir_indices.shuffle(&mut rng);

                    let mut claimed = 0;
                    for &di in dir_indices.iter().take(2) {
                        if claimed >= 2 { break; }
                        let (dy, dx) = dirs[di];
                        let ny = py as isize + dy;
                        let nx = px as isize + dx;
                        if ny >= 0 && ny < actual_rows as isize
                        && nx >= 0 && nx < actual_cols as isize {
                            let (ny, nx) = (ny as usize, nx as usize);
                            if !grid[ny][nx].is_taken() {
                                grid[ny][nx].take();
                                queue.push((ny, nx, pi));
                                claimed += 1;
                            }
                        }
                    }
                }
            }

            if queue.is_empty() { break; }

            for (y, x, pi) in queue {
                pieces[pi].add_block(&grid[y][x]);
            }
        }

        // 4. Handle orphan blocks
        for y in 0..actual_rows {
            for x in 0..actual_cols {
                if !grid[y][x].is_taken() {
                    grid[y][x].take();
                    let pick = rand::random_range(0..pieces.len());
                    pieces[pick].add_block(&grid[y][x]);
                }
            }
        }

        // 5. Build result
        let puzzle_pieces: Vec<PuzzlePieceData> = pieces.iter().enumerate().map(|(i, p)| {
            PuzzlePieceData {
                id: format!("piece_{}", i),
                pixel_left: p.pixel_rect.left as u32,
                pixel_top: p.pixel_rect.top as u32,
                pixel_width: p.pixel_rect.width() as u32,
                pixel_height: p.pixel_rect.height() as u32,
                block_positions: p.items.iter().map(|&(y, x)| BlockPosition { y, x }).collect(),
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
                    let gx = (left + px) / block_size;
                    let gy = (top + py) / block_size;

                    if owned.contains(&(gy, gx)) {
                        let pixel = self.img.get_pixel(left + px, top + py);
                        img.put_pixel(px, py, Rgba([pixel[0], pixel[1], pixel[2], 255]));
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
}
