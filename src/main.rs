mod split;

use crate::split::Canvas;
use colored::Colorize;
use image::{GenericImageView, ImageBuffer, Pixel, Rgba};
use rand::Rng;
use rand::rand_core::block;
use rand::random_bool;
use rand::random_range;
use rand::rng;
use rand::thread_rng;
use split::block::Block;
use std::cell::Cell;
use std::collections::VecDeque;
use std::f64::consts::PI;
use std::path::Path;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    // 示例用法
    let input_image = "./demo1.png"; // 输入图片路径
    let output_directory = "./output"; // 输出目录
    let blocks_count = 100; // 分割的区域数量

    // 读取输入图片
    let img = image::open(input_image)?;

    println!("开始用随机曲线分割图片...");
    let mut canvas = Canvas::new_with_size(img, 1u16);
    canvas.split(blocks_count)?;
    canvas.display();
    canvas.save(output_directory)?;
    // let blocks = split_image_with_curves(width, height, blocks_count);
    // split_image(&img, output_directory, blocks)?;
    println!("图片分割完成！");

    Ok(())
}
