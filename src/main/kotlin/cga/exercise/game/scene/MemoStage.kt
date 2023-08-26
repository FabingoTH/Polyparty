package cga.exercise.game.scene

enum class MemoStage {
    FLOWER, RAKE, LILY, WIN, DONE
}

// Done ist quasi ein IDLE state in dem nichts mehr passiert.
// Dieser wurde eingefügt, damit man sich nicht ewig fortbewegen kann, while die if Bedingungen true sind