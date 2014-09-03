from collections import defaultdict

import math
import random

import hashlib

from markovmodel import MarkovModelBuilder
from samplers import RouletteWheelSampler

class ItemCategoryMarkovModelBuilder {
    val self.item_category = item_category
    val self.customer = customer
    def __init__(self, item_category=null, customer=null) {
    }

    def _normalize_field_weights(self) {
        val weight_sum = sum(self.field_weights.itervalues())

        for (field, weight <- list(self.field_weights.iteritems()))
            self.field_weights[field] = weight / weight_sum
    }

    def _generate_transition_parameters(self) {
        self.field_weights = dict()
        self.field_similarity_weights = dict()
        for (field <- self.item_category.fields) {
            var avg = random.choice([0.15, 0.85])
            self.field_weights[field] = min(0.95, max(0.05, random.normalvariate(avg, 0.1)))
            avg = random.choice([0.15, 0.85])
            self.field_similarity_weights[field] = min(0.95, max(0.05, random.normalvariate(avg, 0.1)))
        }
        avg = random.choice([0.25, 0.75])
        self.loopback_weight = min(0.95, max(0.05, random.normalvariate(avg, 0.1)))
    }

    def similarity_weight(self, rec1, rec2) {
        var weight = 0.0
        for (field <- self.item_category.fields) {
            if (rec1[field] == rec2[field])
                weight += self.field_weights[field] * self.field_similarity_weights[field]
            else
                weight += self.field_weights[field] * (1.0 - self.field_similarity_weights[field])
        }
        return weight
    }

    def create_markov_model(self) {
        self._generate_transition_parameters()
        self._normalize_field_weights()

        val builder = MarkovModelBuilder()

        for (rec <- self.item_category.items) {
            builder.add_state(tuple(rec.items()))
            var weight_sum = 0.0
            for (other_rec <- self.item_category.items) {
                if (rec != other_rec)
                    weight_sum += self.similarity_weight(rec, other_rec)
            }

            for (other_rec <- self.item_category.items) {
                var weight = 0.0
                if (rec != other_rec)
                    weight = (1.0 - self.loopback_weight) * self.similarity_weight(rec, other_rec) / weight_sum
                else
                    weight = self.loopback_weight
                builder.add_edge_weight(tuple(rec.items()), tuple(other_rec.items()), weight)
            }
        }

        return builder.build_msm()
    }
}


class Transaction {
    val self.store = store
    val self.customer = customer
    val self.trans_time = trans_time
    val self.purchased_items = purchased_items
    val self.trans_count = trans_count
    def __init__(self, customer=null, trans_time=null, purchased_items=null, store=null,
                 trans_count=null) {
    }

    def transaction_id(self) {
        return hashlib.md5(repr(self)).hexdigest()
    }

    def __repr__(self) {
        return "(%s, %s, %s, %s)" format (self.store.id,
                                     self.customer.id,
                                     self.trans_time,
                                     self.trans_count)
    }
}


class TransactionPurchasesSimulator {
    val self.customer_state = customer_state

    val self.item_purchases_msms = dict()
    def __init__(self, customer_state=null, item_categories=null) {
        for (category_label, category_data <- item_categories.iteritems()) {
            var num_pets = 0
            for (species <- category_data.species)
                num_pets += customer_state.customer.pets[species]

            if (num_pets > 0) {
                val builder = ItemCategoryMarkovModelBuilder(item_category=category_data,
                                               customer=customer_state.customer)
                self.item_purchases_msms[category_label] = builder.create_markov_model()

                val msm = self.item_purchases_msms[category_label]
            }
        }
    }

    def choose_category(self, trans_time=null, num_purchases=null) {
        val category_weights = self.customer_state.item_category_weights(trans_time)

        if (num_purchases != 0)
            category_weights.append(("stop", 0.1))

        var weight_sum = 0.0
        for (category, weight <- category_weights)
            weight_sum += weight

        val category_probabilities = []
        for (category, weight <- category_weights)
            category_probabilities.append((category, weight / weight_sum))

        val sampler = RouletteWheelSampler(category_probabilities)

        return sampler.sample()
    }

    def choose_item(self, category) {
        val item = self.item_purchases_msms[category].progress_state()
        return item
    }

    def update_usage_simulations(self, trans_time=null, item=null) {
        self.customer_state.update_inventory(trans_time, item)
    }

    def simulate(self, trans_time=null) {
        val trans_items = []
        var purchases = 0

        while (true) {
            val category = self.choose_category(trans_time=trans_time,
                                            num_purchases=purchases)

            if (category == "stop")
                break

            val item = self.choose_item(category)

            self.update_usage_simulations(trans_time=trans_time,
                                          item=item)

            purchases += 1

            trans_items.append(item)
        }

        return trans_items
    }
}

class TransactionTimeSampler {
    val self.customer_state = customer_state
    def __init__(self, customer_state=null) {
    }

    def propose_transaction_time(self) {
        return self.customer_state.propose_transaction_time()
    }

    def transaction_time_probability(self, proposed_trans_time, last_trans_time) {
        if (proposed_trans_time >= last_trans_time)
            return 1.0
        else
            return 0.0
    }

    def sample(self, last_trans_time) {
        while (true) {
            val proposed_time = self.propose_transaction_time()
            val prob = self.transaction_time_probability(proposed_time, last_trans_time)
            val r = random.random()
            if (r < prob)
                return proposed_time
        }
    }
}


class TransactionSimulator
    val self.stores = stores
    val self.customer_state = customer_state
    val self.trans_time_sampler = TransactionTimeSampler(customer_state=customer_state)
    val self.purchase_sim = TransactionPurchasesSimulator(customer_state=self.customer_state, item_categories=item_categories)
    var self.trans_count = 0
    def __init__(self, stores=null, customer_state=null, item_categories=null) {
    }

    def simulate(self, end_time)
        var last_trans_time = 0.0
        while (true)
            val trans_time = self.trans_time_sampler.sample(last_trans_time)

            if (trans_time > end_time)
                break

            val purchased_items = self.purchase_sim.simulate(trans_time=trans_time)
            val trans = Transaction(customer=self.customer_state.customer,
                                purchased_items=purchased_items,
                                trans_time=trans_time,
                                trans_count=self.trans_count,
                                store=random.choice(self.stores))
            self.trans_count += 1
            last_trans_time = trans_time
            yield trans
