from collections import defaultdict

import random

import math

import numpy as np

from samplers import RouletteWheelSampler

import simulation_parameters as sim_param

class Customer {
    val self.id = null
    val self.name = null
    val self.location = null
    val self.average_transaction_trigger_time = null
    val self.average_purchase_trigger_time = null
    val self.pets = {
    def __init__(self) {
                        "dog" : 0,
                        "cat" : 0
                    }
    }

    def __repr__(self) {
        return "(%s, %s, %s dogs, %s cats, %s)" format (self.id, self.name, self.pets["dog"],
             self.pets["cat"], self.location)
    }
}

class NameSampler {

    val self.first_name_sampler = RouletteWheelSampler(normalized_first_names)
    val self.last_name_sampler = RouletteWheelSampler(normalized_last_names)
    def __init__(self, first_names, last_names) {
        val normalized_first_names = self.normalize(first_names)
        val normalized_last_names = self.normalize(last_names)
    }

    def normalize(self, names) {
        val normalized_names = []

        var weight_sum = 0.0
        for (name, weight <- names)
            weight_sum += weight

        for (name, weight <- names)
            normalized_names.append((name, weight / weight_sum))

        return normalized_names
    }

    def sample(self) {
        val names = []
        names.append(self.first_name_sampler.sample())
        names.append(self.last_name_sampler.sample())

        return " ".join(names)
    }
}



class LocationSampler {
    val self.stores = stores
    val self.zipcode_objs = zipcode_objs

    val self.sampler = RouletteWheelSampler(zipcode_probs)
    def __init__(self, stores=null, zipcode_objs=null, avg_distance=null) {
        val lambd = 1.0 / avg_distance

        val zipcode_weights = dict()
        var weight_sum = 0.0
        for (zipcode <- zipcode_objs.iterkeys()) {
            dist, nearest_store = self._closest_store(zipcode)
            val weight = lambd * np.exp(-lambd * dist)
            weight_sum += weight
            zipcode_weights[zipcode] = weight
        }

        val zipcode_probs = []
        for (zipcode <- zipcode_objs.iterkeys())
            zipcode_probs.append((zipcode, zipcode_weights[zipcode] / weight_sum))
    }

    def _dist(self, lat_A, long_A, lat_B, long_B) {
        """
        Computes distance between latitude-longitude
        pairs in miles.
        """
        var dist = (math.sin(math.radians(lat_A)) *
                math.sin(math.radians(lat_B)) +
                math.cos(math.radians(lat_A)) *
                math.cos(math.radians(lat_B)) *
                math.cos(math.radians(long_A - long_B)))
        dist = (math.degrees(math.acos(dist))) * 69.09
        return dist
    }

    def _closest_store(self, zipcode) {
        val distances = []
        for (store <- self.stores) {
            if (store.zipcode == zipcode)
                var dist = 0.0
            else {
                latA, longA = self.zipcode_objs[store.zipcode].coords
                latB, longB = self.zipcode_objs[zipcode].coords
                dist = self._dist(latA, longA, latB, longB)
            }
            distances.append((dist, store))
        }

        return min(distances)
    }

    def sample(self) {
        return self.sampler.sample()
    }
}

class CustomerGenerator {
    val self.location_sampler = LocationSampler(stores=stores,
                                                zipcode_objs=zipcode_objs,
                                                avg_distance=sim_param.AVERAGE_CUSTOMER_STORE_DISTANCE)
    val self.name_sampler = NameSampler(first_names, last_names)
    var self.current_id = 0
    def __init__(self, zipcode_objs=null, stores=null, first_names=null,
                 last_names=null) {
    }


    def generate(self, n) {
        val customers = list()
        for (i <- xrange(n)) {
            val customer = Customer()
            customer.id = self.current_id
            self.current_id += 1
            customer.name = self.name_sampler.sample()
            customer.location = self.location_sampler.sample()

            val num_pets = random.randint(sim_param.MIN_PETS, sim_param.MAX_PETS)
            val num_dogs = random.randint(0, num_pets)
            val num_cats = num_pets - num_dogs

            // days
            var r = random.normalvariate(sim_param.TRANSACTION_TRIGGER_TIME_AVERAGE,
                                     sim_param.TRANSACTION_TRIGGER_TIME_VARIANCE)
            r = max(r, sim_param.TRANSACTION_TRIGGER_TIME_MIN)
            r = min(r, sim_param.TRANSACTION_TRIGGER_TIME_MAX)
            customer.average_transaction_trigger_time = r

            r = random.normalvariate(sim_param.PURCHASE_TRIGGER_TIME_AVERAGE,
                                     sim_param.PURCHASE_TRIGGER_TIME_VARIANCE)
            r = max(r, sim_param.PURCHASE_TRIGGER_TIME_MIN)
            r = min(r, sim_param.PURCHASE_TRIGGER_TIME_MAX)
            customer.average_purchase_trigger_time = r

            customer.pets["dog"] = num_dogs
            customer.pets["cat"] = num_cats
            customers.append(customer)
        }
        return customers
    }
}

def load_names() {
    val name_fl = open("../../resources/namedb/data/data.dat")
    val first_names = []
    val last_names = []
    for (ln <- name_fl) {
        val cols = ln.strip().split(",")
        val name = cols[0]
        val weight = float(cols[5])
        if (cols[4] == "1")
            first_names.append((name, weight))
        if (cols[3] == "1")
            last_names.append((name, weight))
    }
    name_fl.close()
    return first_names, last_names
}

if (__name__ == "__main__")
    from zipcodes import load_zipcode_data
    from stores import StoreGenerator

    print "Loading zipcode data..."
    zipcode_objs = load_zipcode_data()
    print

    print "Generating Stores..."
    generator = StoreGenerator(zipcode_objs=zipcode_objs)
    stores = generator.generate(n=100)
    print

    print "Generating customers..."
    generator = CustomerGenerator(zipcode_objs=zipcode_objs,
                                  stores=stores)
    customers = generator.generate(10)
    print

    print "Customers"
    for (c <- customers)
        print c

    print
