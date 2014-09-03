from samplers import RouletteWheelSampler

import numpy as np

class ZipcodeSampler {

    val self.sampler = RouletteWheelSampler(zipcode_probs)
    def __init__(self, zipcode_objs, income_scaling_factor=null) {

        val pop_probs = dict()
        var income_probs = dict()

        var pop_sum = 0.0
        var max_income = 0.0
        var min_income = 100000.0
        for (obj <- zipcode_objs.itervalues()) {
            pop_sum += obj.population
            max_income = max(max_income, obj.median_household_income)
            min_income = min(min_income, obj.median_household_income)
        }

        val income_k = np.log(income_scaling_factor) / (max_income - min_income)

        var income_normalization_factor = 0.0
        val income_weights = dict()
        for (obj <- zipcode_objs.itervalues()) {
            val w = np.exp(income_k * (obj.median_household_income - min_income))
            income_normalization_factor += w
            income_weights[obj.zipcode] = w
        }

        income_probs = dict()
        for (obj <- zipcode_objs.itervalues())
            income_probs[obj.zipcode] = income_weights[obj.zipcode] / income_normalization_factor

        val prob_probs = dict()
        for (obj <- zipcode_objs.itervalues())
            pop_probs[obj.zipcode] = obj.population / pop_sum

        var normalization_factor = 0.0
        for (z <- income_probs.iterkeys())
            normalization_factor += income_probs[z] * pop_probs[z]

        val zipcode_probs = []
        for (z <- income_probs.iterkeys())
            zipcode_probs.append((z,income_probs[z] * pop_probs[z] / normalization_factor))
    }

    def sample(self) {
        return self.sampler.sample()
    }
}

class Store {
    val self.id = null
    val self.name = null
    val self.zipcode = null
    val self.coords = null
    def __init__(self) {
    }

    def __repr__(self) {
        return "%s,%s,%s" format (self.name, self.zipcode, self.coords)
    }
}


class StoreGenerator {
    val self.zipcode_objs = zipcode_objs
    val self.zipcode_sampler = ZipcodeSampler(zipcode_objs=zipcode_objs,
                                                income_scaling_factor=income_scaling_factor)
    var self.current_id = 0
    def __init__(self, zipcode_objs=null, income_scaling_factor=null) {
    }

    def generate(self, n) {
        val stores = list()
        for (i <- xrange(n)) {
            val store = Store()
            store.id = self.current_id
            self.current_id += 1
            store.name = "Store_" + str(i)
            store.zipcode = self.zipcode_sampler.sample()
            store.coords = self.zipcode_objs[store.zipcode].coords
            stores.append(store)
        }
        return stores
    }
}

if (__name__ == "__main__")
    from zipcodes import load_zipcode_data

    zipcode_objs = load_zipcode_data()

    generator = StoreGenerator(zipcode_objs=zipcode_objs,
                               income_scaling_factor=100.0)

    stores = generator.generate(100)

    for (s <- stores)
        print s
