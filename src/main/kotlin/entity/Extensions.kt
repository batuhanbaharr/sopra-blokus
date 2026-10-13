package entity

import entity.BlockType.*
import entity.ColorType.*
import entity.GameMode.*
import entity.Rotation.*

/**
 * This class extends all the entitys that are used in [service.network.NetworkService]
 * and [service.network.GameNetworkClient] and converts them to the ntf version and back.
 * */
object Extensions {

    /**
     * Provide a NTFBlockType representation of BlockType
     *
     * @return the type of the block
     * */
    fun BlockType.toNTFBlockType() : edu.udo.cs.sopra.ntf.BlockType = when (this) {
        I5 -> edu.udo.cs.sopra.ntf.BlockType.I5
        L5 -> edu.udo.cs.sopra.ntf.BlockType.L5
        U5 -> edu.udo.cs.sopra.ntf.BlockType.U5
        Z5 -> edu.udo.cs.sopra.ntf.BlockType.Z5
        T5 -> edu.udo.cs.sopra.ntf.BlockType.T5
        X5 -> edu.udo.cs.sopra.ntf.BlockType.X5
        W5 -> edu.udo.cs.sopra.ntf.BlockType.W5
        V5 -> edu.udo.cs.sopra.ntf.BlockType.V5
        F5 -> edu.udo.cs.sopra.ntf.BlockType.F5
        P5 -> edu.udo.cs.sopra.ntf.BlockType.P5
        Y5 -> edu.udo.cs.sopra.ntf.BlockType.Y5
        N5 -> edu.udo.cs.sopra.ntf.BlockType.N5
        O1 -> edu.udo.cs.sopra.ntf.BlockType.O1
        I2 -> edu.udo.cs.sopra.ntf.BlockType.I2
        I3 -> edu.udo.cs.sopra.ntf.BlockType.I3
        I4 -> edu.udo.cs.sopra.ntf.BlockType.I4
        T4 -> edu.udo.cs.sopra.ntf.BlockType.T4
        L4 -> edu.udo.cs.sopra.ntf.BlockType.L4
        Z4 -> edu.udo.cs.sopra.ntf.BlockType.Z4
        O4 -> edu.udo.cs.sopra.ntf.BlockType.O4
        V3 -> edu.udo.cs.sopra.ntf.BlockType.V3
    }

    /**
     * Provide a NTFColor representation of Color
     *
     * @return the color of the block
     * */
    fun ColorType.toNTFColor(): edu.udo.cs.sopra.ntf.Color = when (this) {
        //Funktionen müssen auf die Netzwerk funktionen gemapped werden
        BLUE -> edu.udo.cs.sopra.ntf.Color.BLUE
        YELLOW -> edu.udo.cs.sopra.ntf.Color.YELLOW
        RED -> edu.udo.cs.sopra.ntf.Color.RED
        GREEN -> edu.udo.cs.sopra.ntf.Color.GREEN
        else -> error("Unknown color type: $this")
    }

    /**
     * Provide a NTFRotation representation of Rotation
     *
     * @return the rotation of the block
     * */
    fun Rotation.toNTFRotation() : edu.udo.cs.sopra.ntf.Rotation = when (this) {
        //Funktionen müssen auf die Netzwerk funktionen gemapped werden
        Rotation.NONE -> edu.udo.cs.sopra.ntf.Rotation.NONE
        NINETY -> edu.udo.cs.sopra.ntf.Rotation.NINETY
        ONEEIGHTY -> edu.udo.cs.sopra.ntf.Rotation.ONEHUNDREDANDEIGHTY
        TWOSEVENTY -> edu.udo.cs.sopra.ntf.Rotation.TWOHUNDREDANDSEVENTY
    }

    /**
     * Provide a NTFGameMode representation of GameMode
     *
     * @return the game mode
     * */
    fun GameMode.toNTFGameMode(): edu.udo.cs.sopra.ntf.GameMode = when(this) {
        FOUR_PLAYER -> edu.udo.cs.sopra.ntf.GameMode.FOUR_PLAYER
        THREE_PLAYER -> edu.udo.cs.sopra.ntf.GameMode.THREE_PLAYER
        TWO_PLAYER -> edu.udo.cs.sopra.ntf.GameMode.TWO_PLAYER
        TWO_PLAYER_SMALL -> edu.udo.cs.sopra.ntf.GameMode.TWO_PLAYER_SMALL
    }


    /**
     * Provides a BlockType representation on NTFBlockType
     *
     * @return the type of the block
     * */
    fun edu.udo.cs.sopra.ntf.BlockType.toBlockType() : BlockType = when (this) {
        edu.udo.cs.sopra.ntf.BlockType.I5 -> I5
        edu.udo.cs.sopra.ntf.BlockType.L5->L5
        edu.udo.cs.sopra.ntf.BlockType.U5-> U5
        edu.udo.cs.sopra.ntf.BlockType.Z5-> Z5
        edu.udo.cs.sopra.ntf.BlockType.T5->T5
        edu.udo.cs.sopra.ntf.BlockType.X5 -> X5
        edu.udo.cs.sopra.ntf.BlockType.W5->W5
        edu.udo.cs.sopra.ntf.BlockType.V5 -> V5
        edu.udo.cs.sopra.ntf.BlockType.F5 -> F5
        edu.udo.cs.sopra.ntf.BlockType.P5 ->P5
        edu.udo.cs.sopra.ntf.BlockType.Y5 ->Y5
        edu.udo.cs.sopra.ntf.BlockType.N5 ->N5
        edu.udo.cs.sopra.ntf.BlockType.O1 ->O1
        edu.udo.cs.sopra.ntf.BlockType.I2 ->I2
        edu.udo.cs.sopra.ntf.BlockType.I3 ->I3
        edu.udo.cs.sopra.ntf.BlockType.I4 ->I4
        edu.udo.cs.sopra.ntf.BlockType.T4 ->T4
        edu.udo.cs.sopra.ntf.BlockType.L4 ->L4
        edu.udo.cs.sopra.ntf.BlockType.Z4 ->Z4
        edu.udo.cs.sopra.ntf.BlockType.O4 ->O4
        edu.udo.cs.sopra.ntf.BlockType.V3 ->V3
    }

    /**
     * Provide a ColorType representation of NTFColor
     *
     * @return the color of the block
     * */
    fun edu.udo.cs.sopra.ntf.Color.toColorType() : ColorType = when (this) {
        edu.udo.cs.sopra.ntf.Color.BLUE -> BLUE
        edu.udo.cs.sopra.ntf.Color.YELLOW -> YELLOW
        edu.udo.cs.sopra.ntf.Color.RED -> RED
        edu.udo.cs.sopra.ntf.Color.GREEN -> GREEN
    }

    /**
     * Provide a GameMode representation of NTFGameMode
     *
     * @return the game mode
     * */
    fun edu.udo.cs.sopra.ntf.GameMode.toGameMode() : GameMode = when(this) {
        edu.udo.cs.sopra.ntf.GameMode.FOUR_PLAYER -> FOUR_PLAYER
        edu.udo.cs.sopra.ntf.GameMode.THREE_PLAYER -> THREE_PLAYER
        edu.udo.cs.sopra.ntf.GameMode.TWO_PLAYER -> TWO_PLAYER
        edu.udo.cs.sopra.ntf.GameMode.TWO_PLAYER_SMALL -> TWO_PLAYER_SMALL
    }
}