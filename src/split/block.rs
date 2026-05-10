use colored::Color;

use crate::split::Rect;

#[derive(Debug, Clone, Copy)]
pub struct Position {
    _data: u32,
}

/**
 * is the same as the other position.
 * */
impl PartialEq for Position {
    fn eq(&self, other: &Self) -> bool {
        let left = self._data & Self::BITS_TAKEN_FLAG;
        let right = other._data & Self::BITS_TAKEN_FLAG;
        left == right
    }
}

impl Default for Position {
    fn default() -> Self {
        Position { _data: 0 }
    }
}

impl Position {
    const BITS_TAKEN_FLAG: u32 = 1 << 31;
    const BITS_VALUE: u32 = 0b0111111111111111;

    #[inline]
    pub fn new(y: u16, x: u16) -> Self {
        let data = Self::copy_with_x(0, x);
        let data = Self::copy_with_y(data, y);
        let data = data & !Self::BITS_TAKEN_FLAG;
        Position { _data: data }
    }

    #[inline]
    fn copy_with_x(data: u32, value: u16) -> u32 {
        if value as u32 > Self::BITS_VALUE {
            panic!("x coordinate out of bounds");
        }
        (data & !Self::BITS_VALUE) | (value as u32 & Self::BITS_VALUE)
    }

    #[inline]
    fn copy_with_y(data: u32, value: u16) -> u32 {
        if value as u32 > Self::BITS_VALUE {
            panic!("x coordinate out of bounds");
        }

        (data & !(Self::BITS_VALUE << 16)) | ((value as u32 & Self::BITS_VALUE) << 16)
    }

    #[inline]
    pub fn x(&self) -> u16 {
        (self._data & Self::BITS_VALUE) as u16
    }

    #[inline]
    pub fn y(&self) -> u16 {
        ((self._data >> 16) & Self::BITS_VALUE) as u16
    }

    pub fn set_x(&mut self, x: u16) {
        self._data = Self::copy_with_x(self._data, x);
    }

    pub fn set_y(&mut self, y: u16) {
        self._data = Self::copy_with_y(self._data, y);
    }

    pub fn set_taken(&mut self, taken: bool) {
        let flag = if taken { Self::BITS_TAKEN_FLAG } else { 0 };
        self._data = (self._data & !Self::BITS_TAKEN_FLAG) | flag;
    }

    #[inline]
    pub fn is_taken(&self) -> bool {
        self._data & Self::BITS_TAKEN_FLAG != 0
    }

    #[inline]
    pub fn move_to(&mut self, target: &Position) {
        self._data = target._data
    }

    pub fn left(&self) -> Option<Self> {
        let x = self.x();
        if x <= 0 {
            None
        } else {
            let data = Self::copy_with_x(self._data, x - 1);
            Some(Position { _data: data })
        }
    }

    pub fn right(&self, max: u16) -> Option<Self> {
        let x = self.x();
        if x >= max {
            None
        } else {
            let data = Self::copy_with_y(self._data, x + 1);
            Some(Position { _data: data })
        }
    }

    pub fn up(&self) -> Option<Self> {
        let y = self.y();
        if y <= 0 {
            None
        } else {
            let data = Self::copy_with_y(self._data, y - 1);
            Some(Position { _data: data })
        }
    }

    pub fn down(&self, max: u16) -> Option<Self> {
        let y = self.y();
        if y >= max {
            None
        } else {
            let data = Self::copy_with_y(self._data, y + 1);
            Some(Position { _data: data })
        }
    }
}

// 点结构体
#[derive(Debug, Clone, Copy)]
pub struct Block {
    pub position: Position,
    pub rect: Rect,
    pub color: Color,
}

impl Block {
    pub fn new(pos: Position, rect: Rect) -> Self {
        Block {
            position: pos,
            rect: rect,
            color: Color::Black,
        }
    }

    pub fn taken(&mut self) {
        self.position.set_taken(true);
    }

    pub fn is_taken(&self) -> bool {
        self.position.is_taken()
    }
}

impl PartialEq for Block {
    fn eq(&self, other: &Self) -> bool {
        self.position == other.position
    }
}

impl Default for Block {
    fn default() -> Self {
        Self {
            position: Position::default(),
            rect: Rect {
                left: 0,
                right: 0,
                top: 0,
                bottom: 0,
            },
            color: Color::Black,
        }
    }
}
