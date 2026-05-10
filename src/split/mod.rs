pub mod block;
pub mod image;

use std::{
    io::{Error, ErrorKind},
    ops::Range,
    vec,
};

use ::image::{DynamicImage, GenericImageView};
use colored::{Color, Colorize};
use rand::{Rng, seq::SliceRandom};

use crate::split::{
    block::{Block, Position},
    image::Piece,
};

pub struct Canvas {
    img: DynamicImage,
    blocks: Vec<Vec<Block>>,
    pieces: Vec<Piece>,
    size: u16,
}

#[derive(Debug, Clone, Copy)]
pub struct Rect {
    left: u16,
    top: u16,
    right: u16,
    bottom: u16,
}

impl Rect {
    // 计算区域宽度
    // (height * width) 对应矩阵很坐标位置
    #[inline]
    pub fn size(&self) -> (u16, u16) {
        (self.bottom - self.top + 1, self.right - self.left + 1)
    }

    #[inline]
    // 计算区域面积
    pub fn h_range(&self) -> Range<usize> {
        self.left as usize..self.right as usize
    }

    #[inline]
    // 计算区域面积
    pub fn v_range(&self) -> Range<usize> {
        self.top as usize..self.bottom as usize
    }

    #[inline]
    pub fn expand(&mut self, left: u16, top: u16, right: u16, bottom: u16) {
        if left < self.left {
            self.left = left;
        }
        if top < self.top {
            self.top = top;
        }
        if right > self.right {
            self.right = right;
        }
        if bottom > self.bottom {
            self.bottom = bottom;
        }
    }
}

impl Canvas {
    const DEFAULT_SIZE: u16 = 8 * 8;

    pub fn new(img: DynamicImage) -> Self {
        Self::new_with_size(img, Self::DEFAULT_SIZE)
    }

    pub fn new_with_size(img: DynamicImage, size: u16) -> Self {
        Self {
            img,
            blocks: vec![],
            pieces: vec![],
            size,
        }
    }

    pub fn split(&mut self, count: usize) -> Result<bool, Error> {
        let (w, h) = self.img.dimensions();
        let w = w as u16;
        let h = h as u16;

        let size = self.size;
        let w_count = w / size + 1;
        let h_count = h / size + 1;
        for y in 0..h_count {
            let mut line_blocks: Vec<Block> = vec![];
            let top = (y * size).min(h);
            let bottom = (top + size).min(h);
            if top >= bottom {
                break;
            }

            for x in 0..w_count {
                let left = (x * size).min(w);
                let right = (left + size).min(w);
                if left >= right {
                    break;
                }

                let rect = Rect {
                    left: left,
                    right: right,
                    top: top,
                    bottom: bottom,
                };

                let pos = Position::new(y, x);
                let block = Block::new(pos, rect);
                line_blocks.push(block);
            }
            self.blocks.push(line_blocks);
        }

        self.generate_random_blocks(count);
        self.pieces = self.color_blocks(count);

        println!(
            "split size: h{} * w{}, pieces: {}",
            self.blocks.len(),
            self.blocks[0].len(),
            self.pieces.len()
        );
        Ok(true)
    }

    fn generate_random_blocks(&mut self, count: usize) {
        let mut queue: Vec<&mut Block> = vec![];
        for row in self.blocks.iter_mut() {
            for item in row.iter_mut() {
                queue.push(item);
            }
        }

        let mut rng = rand::rng();
        queue.shuffle(&mut rng);

        let mut taken_count = 0;
        while !queue.is_empty() && taken_count < count {
            let index = rng.random_range(0..queue.len());
            let item = queue.remove(index);
            item.taken();
            item.color = Color::Blue;
            taken_count += 1;
        }
    }

    fn color_blocks(&mut self, count: usize) -> Vec<Piece> {
        let rng = &mut rand::rng();
        let mut colors = Self::generate_colors(count);
        println!("color blocks, colors:{}", colors.len());

        struct Item {
            x: usize,
            y: usize,
            piece: usize,
        }

        impl Item {
            fn left(&self) -> Option<Self> {
                if self.x <= 0 {
                    None
                } else {
                    Some(Item {
                        x: self.x - 1,
                        y: self.y,
                        piece: self.piece,
                    })
                }
            }

            fn right(&self, max: usize) -> Option<Self> {
                if self.x >= max {
                    None
                } else {
                    Some(Item {
                        x: self.x + 1,
                        y: self.y,
                        piece: self.piece,
                    })
                }
            }

            fn up(&self) -> Option<Self> {
                if self.y <= 0 {
                    None
                } else {
                    Some(Item {
                        x: self.x,
                        y: self.y - 1,
                        piece: self.piece,
                    })
                }
            }

            fn down(&self, max: usize) -> Option<Self> {
                if self.y >= max {
                    None
                } else {
                    Some(Item {
                        x: self.x,
                        y: self.y + 1,
                        piece: self.piece,
                    })
                }
            }
        }

        let blocks = &mut self.blocks;
        let mut pieces: Vec<Piece> = vec![];
        let mut empty_items: Vec<Item> = vec![];
        for (y, row) in blocks.iter_mut().enumerate() {
            for (x, item) in row.iter_mut().enumerate() {
                if item.is_taken()
                    && let Some(color) = colors.pop()
                {
                    let picece = Piece::new(item);
                    let job = Item {
                        x,
                        y,
                        piece: pieces.len(),
                    };
                    empty_items.push(job);
                    pieces.push(picece);
                    item.color = color;
                }
            }
        }

        empty_items.shuffle(rng);
        let h_count = blocks.len();
        let w_count = blocks[0].len();

        loop {
            let mut queue: Vec<Item> = vec![];
            for job in empty_items {
                let block = &mut blocks[job.y][job.x];
                let picece = &mut pieces[job.piece];
                let color = block.color.clone();
                let mut directions = vec![
                    job.left(),
                    job.right(w_count - 1),
                    job.up(),
                    job.down(h_count - 1),
                ];
                directions.shuffle(rng);

                let mut taken_count = 0;
                let mut empty_count = 0;
                for direction in directions {
                    if let Some(direction) = direction {
                        let item = &mut blocks[direction.y][direction.x];
                        if !item.is_taken() {
                            if taken_count < 2 {
                                taken_count += 1;
                                item.color = color.clone();
                                item.taken();

                                picece.add_block(item);
                                queue.push(direction);
                            } else {
                                empty_count += 1;
                            }
                        }
                    }
                }

                if empty_count > 0 {
                    queue.push(job);
                }
            }

            if queue.is_empty() {
                break;
            }
            queue.shuffle(rng);
            empty_items = queue;
        }
        pieces
    }

    pub fn display(&self) {
        let mut colored_block_szie = 0;
        for row in self.blocks.iter() {
            let mut line = String::from("");
            for item in row {
                let holder = if item.is_taken() {
                    colored_block_szie += 1;
                    "[1]".color(item.color.clone())
                } else {
                    "[0]".color(item.color.clone())
                };
                line.push_str(holder.to_string().as_str());
            }
            println!("{}", line);
        }
        println!("colored block size: {}", colored_block_szie);
    }

    fn generate_colors(count: usize) -> Vec<Color> {
        let mut colors = vec![];
        let mut base_count = 1usize;
        loop {
            if base_count.pow(3) > count {
                break;
            }
            base_count += 1;
        }
        let step = 255 / base_count;
        println!("generate colors by step:{}, base:{}", step, base_count);
        for r in (0..=255).step_by(step) {
            for g in (0..=255).step_by(step) {
                for b in (0..=255).step_by(step) {
                    colors.push(Color::TrueColor { r, g, b });
                }
            }
        }
        colors.shuffle(&mut rand::rng());
        colors
    }

    pub fn save(&self, output_dir: &str) -> Result<(), Error> {
        for (index, piece) in self.pieces.iter().enumerate() {
            let filename = format!("{}/piece_{}.png", output_dir, index);
            if let Ok(image) = piece.draw(self) {
                image.save(filename.clone()).unwrap();
                println!("generate image:{}", filename);
            } else {
                println!("generate image failed:{}", filename);
            }
        }
        Ok(())
    }
}
