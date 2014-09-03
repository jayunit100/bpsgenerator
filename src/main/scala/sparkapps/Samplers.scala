import random

class RouletteWheelSampler
    val self._wheel = []
    def __init__(self, values) {
        var end = 0.0
        for (x, w <- values) {
            end += w
            self._wheel.append((end, x))
        }
    }

    def sample(self)
        val r = random.random()
        for (end, x <- self._wheel) {
            if (r <= end)
                return x
        }
        // we should never get here since probabilities
        // should sum to 1
        raise Exception, "Could not pick a value!"
