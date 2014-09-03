from collections import defaultdict

import random

class MarkovModelBuilder {
    val self.states = set()
    val self.edge_weights = defaultdict(lambda: defaultdict(lambda: 0))
    def __init__(self) {
    }

    def add_state(self, state_label) {
        self.states.add(state_label)
    }

    def add_edge_weight(self, start_state, end_state, weight) {
        self.edge_weights[start_state][end_state] = weight
    }

    def compute_transition_probabilities(self) {
        val edge_probabilities = defaultdict(lambda: defaultdict(lambda: 0))
        for (start_state <- self.states) {
            var weight_sum = 0.0
            for (end_state, weight <- self.edge_weights[start_state].iteritems())
                weight_sum += weight
            for (end_state, weight <- self.edge_weights[start_state].iteritems())
                edge_probabilities[start_state][end_state] = weight / weight_sum
        }
        return edge_probabilities
    }

    def build_msm(self) {
        val edge_probabilities = self.compute_transition_probabilities()
        return MarkovModel(states=self.states, edge_probabilities=edge_probabilities)
    }
}


class MarkovModel {
    val self.states = list(states)
    val self.edge_probabilities = edge_probabilities
    var self.current_state = random.choice(self.states)
    def __init__(self, states=null, edge_probabilities=null) {
    }

    def progress_state(self) {
        val r = random.random()
        var cum_sum = 0.0
        for (candidate_state, prob <- self.edge_probabilities[self.current_state].iteritems()) {
            cum_sum += prob
            if (r <= cum_sum) {
                self.current_state = candidate_state
                return candidate_state
            }
        }
        raise Exception, "Could not find next state for Markov Model!"
    }
}

if (__name__ == "__main__")
    states = range(1, 10)

    builder = MarkovModelBuilder()

    for (start <- states) {
        builder.add_state(start)
        weights = dict()
        weight_sum = 0.0
        for (end <- states) {
            if (start != end) {
                weights[end] = random.uniform(0.0, 1.0)
                weight_sum += weights[end]
            }
        }
        for (end <- states) {
            if (start != end)
                builder.add_edge_weight(start, end, 0.25 * weights[end] / weight_sum)
            else
                builder.add_edge_weight(start, end, 0.75)
        }
    }

    msm = builder.build_msm()

    state = msm.progress_state()
    count_same = 0
    for (i <- xrange(100)) {
        next_state = msm.progress_state()
        if (state == next_state)
            count_same += 1
        state = next_state
    }

    print float(count_same) / 100.0
