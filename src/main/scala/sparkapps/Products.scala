from collections import defaultdict

import random

import json

class ItemCategory {

    val self.category_label = category
    val self.fields = fields
    val self.items = items
    val self.species = species
    val self.trigger_transaction = trigger_transaction
    val self.daily_usage_rate = daily_usage_rate
    val self.base_amount_used_average = base_amount_used_average
    val self.base_amount_used_variance = base_amount_used_average
    val self.transaction_trigger_rate = transaction_trigger_rate
    val self.transaction_purchase_rate = transaction_purchase_rate
    def __init__(self, category=null, fields=null, items=null, species=null, trigger_transaction=false,
            daily_usage_rate=null, base_amount_used_average=null,
            base_amount_used_variance=null,
            transaction_trigger_rate=null,
            transaction_purchase_rate=null) {
    }
}



def load_products_json() {
    val category_fl = open("product_categories.json")
    val product_categories = json.load(category_fl)
    category_fl.close()

    val item_category_objects = dict()
    for (category <- product_categories)
        item_category_objects[category["category"]] = ItemCategory(**category)

    return item_category_objects
}

if (__name__ == "__main__")
    product_cateogry_objects = load_products_json()
