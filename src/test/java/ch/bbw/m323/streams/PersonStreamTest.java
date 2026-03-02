package ch.bbw.m323.streams;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import ch.bbw.m323.streams.PersonStreamTest.Person.Country;
import ch.bbw.m323.streams.PersonStreamTest.Person.Gender;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

class PersonStreamTest implements WithAssertions {

	record Person(String name, int age, Gender gender, Country country) {

		enum Gender {
			MALE, FEMALE, NON_BINARY
		}

		record Country(String name, long population) {
		}

		public boolean isAdult() {
			return age >= 18;
		}
	}

	final Country france = new Country("France", 65_235_184L);

	final Country canada = new Country("Canada", 37_653_095L);

	final Country uk = new Country("United Kingdom", 67_791_734L);

	final List<Person> people = List.of(
			new Person("Brent", 50, Gender.MALE, canada),
			new Person("Luca", 22, Gender.MALE, canada),
			new Person("May", 12, Gender.FEMALE, france),
			new Person("Jojo", 23, Gender.NON_BINARY, uk),
			new Person("Maurice", 15, Gender.MALE, france),
			new Person("Alice", 15, Gender.FEMALE, france),
			new Person("Laurence", 22, Gender.MALE, france),
			new Person("Samantha", 67, Gender.FEMALE, canada));

	// tag::sample[]
	@Test
	void allNamesUppercase() { // Alle Namen UPPERCASE.
		// Dies ist eine Beispielimplementation, wie eine Lösung auszusehen hat.
		// Die Spielregel wurde eingehalten: nur ein `;` am Ende der Funktion
		assertThat(people.stream() // ein Stream<Person>
				.map(Person::name) // ein Stream<String> mit allen Namen. Dasselbe wie `.map(x -> x,name())`.
				.map(String::toUpperCase) // ein Stream<String> mit UPPERCASE-Namen
				.toList() // eine List<String>
		).containsExactly("BRENT", "LUCA", "MAY", "JOJO", "MAURICE", "ALICE", "LAURENCE", "SAMANTHA");
	}
	// end::sample[]

	// TODO: add all your own Testcases here

	@Test
	void namesWithMax4Letters() {
		assertThat(people.stream()
				.map(Person::name)
				.filter(n -> n.length() <= 4)
				.toList()
		).containsOnly("Luca", "May", "Jojo");
	}

	@Test
	void sumOfAllAges() {
		assertThat(people.stream()
				.mapToInt(Person::age)
				.sum()
		).isEqualTo(226);
	}

	@Test
	void ageOfOldestPerson() {
		assertThat(people.stream()
				.mapToInt(Person::age)
				.max()
				.orElseThrow()
		).isEqualTo(67);
	}

	@Test
	void allCanadianMen() {
		assertThat(people.stream()
				.filter(p -> p.gender() == Gender.MALE)
				.filter(p -> p.country().equals(canada))
				.toList()
		).hasSize(2).allSatisfy(p -> assertThat(p).isInstanceOf(Person.class));
	}

	@Test
	void allNamesJoinedWithUnderline() {
		assertThat(people.stream()
				.map(Person::name)
				.collect(Collectors.joining("_"))
		).hasSize(51).contains("_");
	}

	@Test
	void womenFromSmallCountriesUpToOneMillion() {
		assertThat(people.stream()
				.filter(p -> p.gender() == Gender.FEMALE)
				.filter(p -> p.country().population() <= 1_000_000L)
				.toList()
		).isEmpty();
	}

	@Test
	void maleNamesSortedByAge() {
		assertThat(people.stream()
				.filter(p -> p.gender() == Gender.MALE)
				.sorted(Comparator.comparingInt(Person::age))
				.map(Person::name)
				.toList()
		).containsExactly("Maurice", "Luca", "Laurence", "Brent");
	}

	@Test
	void secondOldestWoman() {
		assertThat(people.stream()
				.filter(p -> p.gender() == Gender.FEMALE)
				.sorted(Comparator.comparingInt(Person::age).reversed())
				.limit(2)
				.skip(1)
				.findFirst()
				.orElseThrow()
		).extracting(Person::name).isEqualTo("Alice");
	}
}
