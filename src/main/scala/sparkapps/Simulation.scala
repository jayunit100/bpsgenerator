from customers import CustomerGenerator
from customers import load_names
from customer_simulation import CustomerState
import simulation_parameters as sim_param
from stores import StoreGenerator
import products
from transactions import TransactionSimulator
from zipcodes import load_zipcode_data


class Simulator {
    def __init__(self) {
        ()
    }

    def load_data(self) {
        self.item_categories = products.load_products_json()
        self.zipcode_objs = load_zipcode_data()
        self.first_names, self.last_names = load_names()
    }

    def generate_stores(self, num=null) {
        val generator = StoreGenerator(zipcode_objs=self.zipcode_objs,
                                   income_scaling_factor=sim_param.STORE_INCOME_SCALING_FACTOR)
        self.stores = generator.generate(n=num)
    }

    def generate_customers(self, num=null) {
        val generator = CustomerGenerator(zipcode_objs=self.zipcode_objs,
                                      stores=self.stores,
                                      first_names=self.first_names,
                                      last_names=self.last_names)
        self.customers = generator.generate(num)
    }

    def generate_transactions(self, end_time=null) {
        for (customer <- self.customers) {
            val state = CustomerState(item_categories=self.item_categories,
                    customer=customer)
            val trans_sim = TransactionSimulator(stores=self.stores,
                                             customer_state=state,
                                             item_categories=self.item_categories)
            for (trans <- trans_sim.simulate(end_time))
                yield trans
        }
    }
}

class TransactionItemWriter {
    val self.fl = open(filename, "w")
    def __init__(self, filename=null) {
    }

    def append(self, trans) {
        for (item <- trans.purchased_items) {
            val item = dict(item)
            if ("food" in item["category"]) {
                var item_str = "%s:%s:%s:%s" format (item["category"], item["brand"], item["flavor"],
                     item["size"])
            }
            else if ("poop bags" == item["category"]) {
                item_str = "%s:%s:%s:%s" format (item["category"], item["brand"], item["color"],
                     item["size"])
            }
            else
                item_str = "%s:%s:%s" format (item["category"], item["brand"], item["size"])

            self.fl.write("%s,%s\n" format (trans.transaction_id(),
                                  item_str))
        }
    }
    def close(self) {
        self.fl.close()
    }
}


class CustomerWriter {
    val self.fl = open(filename, "w")
    def __init__(self, filename=null) {
    }

    def append(self, customer) {
        val string = "%s,%s,%s\n" format (customer.id,
                                 customer.name,
                                 customer.location)

        self.fl.write(string)
    }

    def close(self) {
        self.fl.close()
    }
}

class StoreWriter {
    val self.fl = open(filename, "w")
    def __init__(self, filename=null) {
    }

    def append(self, store) {
        val string = "%s,%s\n" format (store.id, store.zipcode)
        self.fl.write(string)
    }

    def close(self) {
        self.fl.close()
    }
}


class TransactionWriter {
    val self.fl = open(filename, "w")
    def __init__(self, filename=null) {
    }

    def append(self, trans) {
            val values = [
                trans.transaction_id(),
                trans.store.id,
                trans.customer.id,
                trans.trans_time,
                ]
            val string = ",".join(map(str, values)) + "\n"
            self.fl.write(string)
    }

    def close(self) {
        self.fl.close()
    }
}

def driver() {
    val sim = Simulator()
    val item_writer = TransactionItemWriter(filename="transaction_items.txt")
    val trans_writer = TransactionWriter(filename="transactions.txt")
    val store_writer = StoreWriter(filename="stores.txt")
    val customer_writer = CustomerWriter(filename="customers.txt")

    print "Loading data..."
    sim.load_data()

    print "Generating stores..."
    sim.generate_stores(num=10)

    for (store <- sim.stores)
        store_writer.append(store)
    store_writer.close()

    print "Generating customers..."
    sim.generate_customers(num=100)

    for (customer <- sim.customers)
        customer_writer.append(customer)
    customer_writer.close()

    print "Generating transactions..."
    for (trans <- sim.generate_transactions(end_time=365.0*5.0)) {
        trans_writer.append(trans)
        item_writer.append(trans)
    }

    print

    trans_writer.close()
    item_writer.close()
}

if (__name__ == "__main__")
    driver()
