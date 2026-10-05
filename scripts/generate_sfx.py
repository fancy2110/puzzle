#!/usr/bin/env python3
"""Generate gentle, kid-friendly sound effects as 16-bit mono WAV files.

Outputs into composeApp/src/commonMain/composeResources/files/audio/:
  sfx_select.wav   - soft short tone when picking a piece
  sfx_place.wav    - warm ascending two-note ding on correct placement
  sfx_wrong.wav    - gentle low two-note hint on a wrong target
  sfx_complete.wav - ascending arpeggio when a puzzle completes
  sfx_click.wav    - subtle UI click
"""

import math
import os
import wave

SAMPLE_RATE = 44100
OUT_DIR = os.path.join(
    os.path.dirname(__file__),
    "..",
    "composeApp/src/commonMain/composeResources/files/audio",
)


def tone(freq, duration, volume=0.5, decay=3.0, attack=0.006, harmonics=True):
    """Sine tone with fast attack and exponential decay."""
    samples = int(SAMPLE_RATE * duration)
    attack_samples = max(1, int(SAMPLE_RATE * attack))
    values = []
    for i in range(samples):
        t = i / SAMPLE_RATE
        env = min(1.0, i / attack_samples) * math.exp(-decay * t / duration)
        sample = math.sin(2 * math.pi * freq * t)
        if harmonics:
            sample += 0.35 * math.sin(2 * math.pi * 2 * freq * t)
            sample += 0.10 * math.sin(2 * math.pi * 3 * freq * t)
        values.append(volume * env * sample)
    return values


def mix_at(track, values, start_seconds):
    start = int(start_seconds * SAMPLE_RATE)
    end = start + len(values)
    if end > len(track):
        track.extend([0.0] * (end - len(track)))
    for i, value in enumerate(values):
        track[start + i] += value


def write_wav(name, track):
    peak = max((abs(v) for v in track), default=1.0)
    scale = 0.85 / peak if peak > 0 else 1.0

    path = os.path.normpath(os.path.join(OUT_DIR, name))
    with wave.open(path, "wb") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(SAMPLE_RATE)
        frames = bytearray()
        for value in track:
            pcm = int(max(-1.0, min(1.0, value * scale)) * 32767)
            frames += pcm.to_bytes(2, "little", signed=True)
        wav.writeframes(frames)
    print(f"wrote {path} ({len(track) / SAMPLE_RATE:.2f}s)")


def main():
    os.makedirs(os.path.normpath(OUT_DIR), exist_ok=True)

    # Subtle UI click — very short, soft high blip
    write_wav("sfx_click.wav", tone(1320, 0.06, volume=0.35, decay=14.0))

    # Piece select — one soft bell-like note (B5)
    write_wav("sfx_select.wav", tone(987.77, 0.16, volume=0.45, decay=2.8))

    # Correct placement — ascending G5 -> C6
    placed = []
    mix_at(placed, tone(783.99, 0.12, volume=0.45, decay=2.4), 0.0)
    mix_at(placed, tone(1046.50, 0.22, volume=0.45, decay=2.8), 0.09)
    write_wav("sfx_place.wav", placed)

    # Wrong target — gentle descending G4 -> E4, never harsh
    wrong = []
    mix_at(wrong, tone(392.00, 0.14, volume=0.38, decay=2.2), 0.0)
    mix_at(wrong, tone(329.63, 0.24, volume=0.38, decay=2.6), 0.12)
    write_wav("sfx_wrong.wav", wrong)

    # Puzzle complete — warm C5 E5 G5 C6 arpeggio
    complete = []
    mix_at(complete, tone(523.25, 0.20, volume=0.45, decay=2.6), 0.00)
    mix_at(complete, tone(659.25, 0.20, volume=0.45, decay=2.6), 0.15)
    mix_at(complete, tone(783.99, 0.22, volume=0.45, decay=2.6), 0.30)
    mix_at(complete, tone(1046.50, 0.60, volume=0.45, decay=2.2), 0.45)
    write_wav("sfx_complete.wav", complete)


if __name__ == "__main__":
    main()
