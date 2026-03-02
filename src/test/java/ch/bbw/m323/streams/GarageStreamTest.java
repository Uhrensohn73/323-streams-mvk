package ch.bbw.m323.streams;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GarageStreamTest implements WithAssertions {

    Inventory inventory;

    record Inventory(List<Customer> products) {

        record Customer(String id, String customer, String email, List<Car> cars) {

            record Car(String brand, String price, Wheel wheels, Radio radio) {

                record Wheel(String brand, Integer amount) {
                }

                record Radio(Boolean ukw, Bluetooth bluetooth) {

                    record Bluetooth(Integer version, List<Standard> standards) {

                        record Standard(String codec, Boolean partial) {
                        }
                    }
                }
            }
        }
    }

    @BeforeEach
    void readJson() throws IOException {
        // TODO: change to "manynull.json" for a harder experience
        try (var in = GarageStreamTest.class.getClassLoader().getResourceAsStream("fewnull.json")) {
            inventory = new ObjectMapper().readValue(in, Inventory.class);
        }
    }

    @Test
    void aTest() {
		Function<Inventory.Customer, String> customerName = Inventory.Customer::customer;

        List<String> customersWith2PlusCars = streamOf(inventory.products())
                .filter(this::has2OrMoreCars)
                .map(customerName)
                .sorted()
                .toList();

        assertThat(customersWith2PlusCars).hasSizeBetween(10, 11);

        Predicate<Inventory.Customer.Car> hasUkwRadio = this::hasUkwRadio;

        long ukwCarsCount = streamOf(inventory.products())
                .flatMap(this::carsOfCustomer)
                .filter(hasUkwRadio)
                .count();

        assertThat(ukwCarsCount).isIn(8L, 16L);
    }

    private boolean has2OrMoreCars(Inventory.Customer customer) {
        return carsOfCustomer(customer).limit(2).count() >= 2;
    }

    private Stream<Inventory.Customer.Car> carsOfCustomer(Inventory.Customer customer) {
        return streamOf(customer.cars());
    }

    private boolean hasUkwRadio(Inventory.Customer.Car car) {
        return Stream.of(car)
                .map(Inventory.Customer.Car::radio)
                .filter(Objects::nonNull)
                .map(Inventory.Customer.Car.Radio::ukw)
                .anyMatch(Boolean.TRUE::equals);
    }

    private static <T> Stream<T> streamOf(List<T> list) {
        return list == null ? Stream.empty() : list.stream().filter(Objects::nonNull);
    }
}

