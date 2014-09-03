def read_income_data(flname) {
    val fl = open(flname)
    //skip headers
    next(fl)
    next(fl)
    val zipcode_incomes = dict()
    for (ln <- fl) {
        val cols = ln.strip().split(",")
        // zipcodes in column 3 in the format "ZCTA5 XXXXX"
        val zipcode = cols[2].split()[1]
        try {
            val median_household_income = int(cols[5])
            zipcode_incomes[zipcode] = median_household_income
        }
        catch {
            // some records do not contain incomes
            ()
        }
    }
    fl.close()
    return zipcode_incomes
}

def read_population_data(flname) {
        val fl = open(flname)
        val pop_data = dict()
        // skip headers
        next(fl)
        for (ln <- fl) {
                if (ln.strip() == "")
                        continue
                zipcode, pop = ln.split(",")
                val pop = int(pop)
        }
        // remove duplicates.  keep largest pop values
                if (pop_data contains zipcode)
                        pop_data[zipcode] = max(pop_data[zipcode], pop)
                else
                        pop_data[zipcode] = pop
        fl.close()
        return pop_data
}

def read_zipcode_coords(flname) {
    val fl = open(flname)
    // skip header
    next(fl)
    val zipcode_coords = dict()
    for (ln <- fl) {
        val cols = ln.split(", ")
        val zipcode = cols[0][1:-1] // remove double-quote marks
        val lat = float(cols[2][1:-1])
        val long = float(cols[3][1:-1])
        zipcode_coords[zipcode] = (lat, long)
    }
    fl.close()
    return zipcode_coords
}

class ZipcodeData {
    val self.zipcode = zipcode
    val self.median_household_income = median_household_income
    val self.population = population
    val self.coords = coords
    def __init__(self, zipcode=null, median_household_income=null,
                 population=null, coords=null) {
    }
}

def load_zipcode_data() {
    val zipcode_incomes = read_income_data("../../resources/ACS_12_5YR_S1903/ACS_12_5YR_S1903_with_ann.csv")
    val zipcode_pop = read_population_data("../../resources/population_data.csv")
    val zipcode_coords = read_zipcode_coords("../../resources/zips.csv")

    val all_zipcodes = set(zipcode_incomes.keys()).intersection(set(zipcode_pop.keys())).intersection(set(zipcode_coords))

    val zipcode_objects = dict()
    for (z <- all_zipcodes) {
        val obj = ZipcodeData(zipcode=z,
                          median_household_income=zipcode_incomes[z],
                          population=zipcode_pop[z],
                          coords=zipcode_coords[z])
        zipcode_objects[z] = obj
    }

    return zipcode_objects
}


if (__name__ == "__main__")
    zipcode_objects = load_zipcode_data()

    print zipcode_objects.length
