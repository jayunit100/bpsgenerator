import random

import numpy as np

class ItemCategoryUsageSimulation {

    val self.daily_usage_rate = daily_usage_rate
    val self.amount_used_average = amount_used_average
    val self.amount_used_variance = amount_used_variance


    val self.trajectory = [(initial_time, initial_amount)]

    var self.time = initial_time
    var self.remaining_amount = initial_amount

    var self.ran_simulation = false
    def __init__(self, initial_amount=null, initial_time=null, daily_usage_rate=null, amount_used_average=null, amount_used_variance=null) {
        """
        daily_usage_rate is given in times/day -- used to determine when an item is used

        amount_used_average and amount_used_variance are given in units/day -- used to determine how
        much is used per usage.
        """
    }

    def _step(self) {
        """
        Simulate 1 step of usage.

        da/dt = -R(amount_used_average, amount_used_variance)

        timestep is determined from exponential distribution:
        f = \lambda \exp (-\lambda t) where \lambda = 1.0 / usage_rate
        \Delta t sampled from f

        Returns time after 1 step and remaining amount
        """

        // given in days since last usage
        val timestep = random.expovariate(self.daily_usage_rate)


        val r = random.normalvariate(0.0, 1.0)

        // given in units/day
        var usage_amount = self.amount_used_average * timestep + np.sqrt(self.amount_used_variance * timestep) * r

        // can't use a negative amount :)
        if (usage_amount < 0.0)
            usage_amount = 0.0

        self.remaining_amount -= min(usage_amount, self.remaining_amount)

        self.time += timestep

        self.trajectory.append((self.time, self.remaining_amount))
    }

    def simulate(self) {
        while (self.remaining_amount > 0.0)
            self._step()
        self.ran_simulation = true
    }

    def exhaustion_time(self) {
        if (!self.ran_simulation)
            raise Exception, "Cannot return exhaustion time before running simulation"
        return self.trajectory[-1][0]
    }

    def amount_at_time(self, time) {
        """
        Find amount remaining at given time.
        """
        previous_t, previous_amount = self.trajectory[0]
        for (t, amount <- self.trajectory[1:]) {
            if (t > time)
                break
            val previous_t = t
            val previous_amount = amount
        }
        return previous_amount
    }
}


class ItemCategorySimulation {

    val self.daily_usage_rate = item_category.daily_usage_rate
    val self.amount_used_average = item_category.base_amount_used_average * num_pets
    val self.amount_used_variance = item_category.base_amount_used_variance * num_pets

    val self.average_transaction_trigger_time = customer.average_transaction_trigger_time
    val self.average_purchase_trigger_time = customer.average_purchase_trigger_time

    var self.sim = null
    def __init__(self, item_category=null, customer=null) {
        """
        daily_usage_rate is given in times/day -- used to determine when an item is used

        amount_used_average and amount_used_variance are given in units/day -- used to determine how
        much is used per usage.
        """

        var num_pets = 0.0
        for (species <- item_category.species)
            num_pets += float(customer.pets[species])
    }

    def record_purchase(self, purchase_time, purchased_amount) {
        """
        Increase current amount, from a purchase.

        purchase_time -- given in seconds since start of model

        purchased_amount -- given in units
        """

        var total_amount = purchased_amount
        if (self.sim != null)
            total_amount += self.sim.amount_at_time(purchase_time)

        self.sim = ItemCategoryUsageSimulation(initial_amount=total_amount,
                                               initial_time=purchase_time,
                                               daily_usage_rate=self.daily_usage_rate,
                                               amount_used_average=self.amount_used_average,
                                               amount_used_variance=self.amount_used_variance)

        self.sim.simulate()
    }

    def exhaustion_time(self) {
        var exhaustion_time = 0.0
        if (self.sim != null)
            exhaustion_time = self.sim.exhaustion_time()
        return exhaustion_time
    }

    def get_remaining_amount(self, time) {
        if (self.sim == null)
            return 0.0
        return self.sim.amount_at_time(time)
    }

    def purchase_weight(self, time) {
        val remaining_time = max(self.exhaustion_time() - time, 0.0)
        val lambd = 1.0 / self.average_purchase_trigger_time
        return lambd * np.exp(-lambd * remaining_time)
    }

    def propose_transaction_time(self) {
        val lambd = 1.0 / self.average_transaction_trigger_time
        val time_until_transaction = random.expovariate(lambd)
        val transaction_time = max(self.exhaustion_time() - time_until_transaction, 0.0)
        return transaction_time
    }

    def choose_item_for_purchase(self) {
        return self.purchase_model.progress_state()
    }
}


if (__name__ == "__main__")
    sim = ItemCategoryUsageSimulation(initial_amount=30.0, initial_time=0.0, daily_usage_rate=1.0,
        amount_used_average=0.5, amount_used_variance=0.2)
    sim.simulate()

    for (time, amount <- sim.trajectory)
        print time, amount

    from products import load_products_json
    from customers import CustomerGenerator

    item_categories = load_products_json()

    customer = CustomerGenerator().generate(1)[0]
    print
    for (item_category <- item_categories.itervalues())
        sim = ExhaustibleItemCategorySimulation(item_category=item_category, customer=customer)
        for (i <- xrange(10))
            print sim.choose_item_for_purchase()
