package entity

/**
 * Enum to distinguish between the 21 possible block types in Blokus.
 *
 * Each color has one set of 21 pieces: one 1-square piece (ONE),
 * one 2-square piece (TWO), two 3-square pieces (THREE, CROOKEDTHREE),
 * five 4-square pieces (SHORTI, SHORTL, SHORTT, SHORTZ, SQUARE),
 * and twelve 5-square pieces (I, L, U, Z, T, X, W, V, F, P, Y, N).
 * -> Called differently here.
 */
enum class BlockType {
    I5, L5, U5, Z5, T5, X5, W5, V5, F5, P5, Y5, N5,
    O1, I2, I3,
    I4, T4, L4, Z4,
    O4, V3, ;
}