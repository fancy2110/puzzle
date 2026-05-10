use colored::{Color, Colorize};
use image::{DynamicImage, GenericImage, Pixel, Rgba32FImage};
use image::{GenericImageView, ImageBuffer, Rgba};
use rand::Rng;
use rand::rand_core::block;
use rand::random_bool;
use rand::random_range;
use rand::rng;
use rand::thread_rng;
use raqote::{
    DrawTarget,
    PathBuilder,
    Point,
    SolidSource,
    Source, // raqote 核心类型
};
use std::collections::VecDeque;
use std::f64::consts::PI;
use std::fmt::Display;
use std::io::{Error, ErrorKind};
use std::path::Path;

use crate::split::block::Block;
use crate::split::block::Position;
use crate::split::{Canvas, Rect};

// 定义区域边界（矩形，简化处理）
#[derive(Debug, Clone)]
pub struct Piece {
    //block position
    pixels: Rect,
    //pixels, ref to canvas blocks
    blocks: Rect,
    items: Vec<Position>,
}

impl Piece {
    pub fn new(block: &Block) -> Self {
        let pos = &block.position;
        let x = pos.x();
        let y = pos.y();
        Piece {
            pixels: block.rect.clone(),
            blocks: Rect {
                left: x,
                top: y,
                right: x,
                bottom: y,
            },
            items: vec![pos.clone()],
        }
    }

    // 计算区域面积
    fn area(&self) -> usize {
        self.width() * self.height()
    }

    // 计算区域宽度
    fn width(&self) -> usize {
        let (width, _) = self.pixels.size();
        width as usize
    }

    // 计算区域高度
    pub fn height(&self) -> usize {
        let (_, height) = self.pixels.size();
        height as usize
    }

    pub fn draw(&self, canvas: &Canvas) -> Result<ImageBuffer<Rgba<u8>, Vec<u8>>, Error> {
        let (height, width) = self.pixels.size();
        let width = width as u32;
        let height = height as u32;
        let mut target = ImageBuffer::new(width, height);
        let blocks = &canvas.blocks;
        let image = &canvas.img;

        let mut position: Option<(u32, u32)> = None;
        println!(
            "draw image to split blocks, Pixels:{:?}, blocks:{:?}",
            self.pixels.size(),
            self.blocks.size()
        );
        self.display();

        let _blocks = self.render_blocks();
        for row in _blocks {
            for item in row {
                let x = item.x() as usize;
                let y = item.y() as usize;
                let block = &blocks[y][x];
                if position == None {
                    position = Some((block.rect.left as u32, block.rect.top as u32));
                    println!("Begin postion, {}, {},position:{:?}", x, y, position);
                }

                if !item.is_taken() {
                    continue;
                }

                if let Some((start_x, start_y)) = position {
                    let Rect {
                        left,
                        right,
                        top,
                        bottom,
                    } = block.rect;

                    for s_x in left..right {
                        for s_y in top..bottom {
                            let source_x = s_x as u32;
                            let source_y = s_y as u32;
                            let p = image.get_pixel(source_x, source_y);
                            target.put_pixel(source_x - start_x, source_y - start_y, p);
                        }
                    }
                }
            }
        }
        Ok(target)
    }

    pub fn add_block(&mut self, block: &Block) {
        let pos = block.position.clone();
        let x = pos.x();
        let y = pos.y();
        self.blocks.expand(x, y, x, y);
        self.items.push(pos);

        let rect = block.rect;
        let Rect {
            left,
            right,
            top,
            bottom,
        } = rect;
        self.pixels.expand(left, top, right, bottom);
    }

    fn render_blocks(&self) -> Vec<Vec<Position>> {
        let (col, row) = self.blocks.size();
        let start_x = self.blocks.left;
        let start_y = self.blocks.top;

        let mut blocks: Vec<Vec<Position>> = vec![];
        for y in 0..col {
            let mut rows: Vec<Position> = vec![];
            for x in 0..row {
                let postion = Position::new(start_y + y as u16, start_x + x as u16);
                rows.push(postion);
            }
            blocks.push(rows);
        }

        for ele in &self.items {
            let x = (ele.x() - self.blocks.left) as usize;
            let y = (ele.y() - self.blocks.top) as usize;
            blocks[y][x] = ele.clone();
        }
        blocks
    }

    pub fn display(&self) {
        let mut colored_block_szie = 0;
        let blocks = self.render_blocks();
        for row in blocks {
            let mut line = String::from("");
            for item in row {
                let holder = if item.is_taken() {
                    colored_block_szie += 1;
                    "[1]".color(Color::Blue)
                } else {
                    "[0]".color(Color::Black)
                };
                line.push_str(holder.to_string().as_str());
            }
            println!("{}", line);
        }
        println!("colored block size: {}", colored_block_szie);
    }
}

impl Display for Piece {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "Pixels:{:?}, with blocks: {:?}",
            self.pixels, self.blocks
        )
    }
}
