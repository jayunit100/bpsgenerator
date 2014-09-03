from item_simulations import ItemCategorySimulation

class CustomerState {
    val self.customer = customer

    val self.item_sims = dict()
    def __init__(self, item_categories=null, customer=null) {
        for (category, model <- item_categories.iteritems()) {
            var num_pets = 0
            for (species <- model.species)
                num_pets += customer.pets[species]
            if (num_pets > 0) {
                self.item_sims[category] = ItemCategorySimulation(item_category=model,
                    customer=customer)
            }
        }
    }

    def propose_transaction_time(self) {
        val transaction_times = []
        for (category, sim <- self.item_sims.iteritems()) {
            val time = sim.propose_transaction_time()
            transaction_times.append(time)
        }

        return min(transaction_times)
    }

    def item_category_weights(self, current_time) {
        val weights = []
        for (category_name, sim <- self.item_sims.iteritems())
            weights.append((category_name, sim.purchase_weight(current_time)))
        return weights
    }

    def update_inventory(self, time, item) {
        item = dict(item)
        val category = item["category"]
        val amount = item["size"]
        val sim = self.item_sims[category]
        sim.record_purchase(time, amount)
    }

    def get_inventory_amounts(self, time) {
        val amounts = {}
        for (category, sim <- self.item_sims.iteritems()) {
            val remaining_amount = sim.get_remaining_amount(time)
            amounts[category] = remaining_amount
        }
        return amounts
    }

    def __repr__(self) {
        return "(%s, %s dogs, %s cats)" format (self.customer.name, self.customer.pets["dog"], self.customer.pets["cat"])
    }
}

if (__name__ == "__main__")
    from products import load_products_json
    from customers import CustomerGenerator

    item_categories = load_products_json()

    customer = CustomerGenerator().generate(1)[0]

    customer_sim = CustomerState(item_categories=item_categories,
        customer=customer)
