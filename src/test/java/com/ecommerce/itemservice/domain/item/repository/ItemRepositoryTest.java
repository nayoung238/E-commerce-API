package com.ecommerce.itemservice.domain.item.repository;

import com.ecommerce.itemservice.BaseServiceTest;
import com.ecommerce.itemservice.domain.item.Item;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ItemRepositoryTest extends BaseServiceTest {

    @Autowired
    private ItemRepository itemRepository;

    @AfterEach
    void afterEach() {
        itemRepository.deleteAll();
    }

    @DisplayName("Non-unique 한 아이템 이름에 해당하는 모든 상품 조회")
    @Test
    void non_unique_값에_해당하는_모든_상품_조회() {
        // given & when
        final String itemName = "apple";
        final int numberOfItems = 2;
        final long price = 1000L;
        IntStream.range(0, numberOfItems)
                .forEach(i -> {
                    Item item = getItem(itemName, (long) i + 100, price);
                    itemRepository.save(item);
                });


        // then
        List<Item> items = itemRepository.findAllByName(itemName);
        assertThat(items)
                .hasSize(numberOfItems)
                .allMatch(item -> item.getName().equals(itemName));
//                .extracting(Item::getName)
//                .containsOnly(itemName);
    }

    @DisplayName("존재하지 않은 값으로 조회 시 값이 반환되면 안 됨")
    @Test
    void 존재하지_않은_값으로_조회() {
        // given & when
        final String itemName = "apple";
        final int numberOfItems = 2;
        final long price = 1000L;
        IntStream.range(0, numberOfItems)
                .forEach(i -> {
                    Item item = getItem(itemName, (long) i + 100, price);
                    itemRepository.save(item);
                });

        // then
        final String searchName = "kiwi";
        List<Item> items = itemRepository.findAllByName(searchName);
        assertThat(items).isEmpty();
    }

    @DisplayName("여러 조회 조건에 속하는 모든 객체 조회")
    @Test
    void findAllBy_In_Test () {
        // given
        final String itemName1 = "apple";
        final int numberOfItem1 = 2;
        final long price = 1000L;

        final String itemName2 = "kiwi";
        final int numberOfItem2 = 5;

        List<Item> items = Stream.concat(
                IntStream.range(0, numberOfItem1)
                        .mapToObj(i -> getItem(itemName1, (long) i + 100, price)),
                IntStream.range(0, numberOfItem2)
                        .mapToObj(i -> getItem(itemName2, (long) i + 100, price)))
                        .toList();

        // when
        itemRepository.saveAll(items);

        // then
        List<Item> savedItems = itemRepository.findAllByNameIn(List.of(itemName1, itemName2));
        assertThat(savedItems)
                .hasSize(numberOfItem1 + numberOfItem2)
                .filteredOn(item -> item.getName().equals(itemName1))
                .hasSize(numberOfItem1);

        assertThat(savedItems).filteredOn(item -> item.getName().equals(itemName2))
                .hasSize(numberOfItem2);
    }

    @DisplayName("특정 문자열이 포함된 모든 객체 조회")
    @Test
    void findAllBy_Containing_Test() {
        // given
        final String itemName = "Apple";
        final long price = 1000L;

        // 아이템명: domesticApple
        final String countryOfOrigin1 = "domestic";
        final int numberOfItem1 = 2;
        List<Item> items = IntStream.range(0, numberOfItem1)
                .mapToObj(i -> getItem(countryOfOrigin1 + itemName, (long) i + 100, price))
                .collect(Collectors.toList());

        // 아이템명: foreignApple
        final String countryOfOrigin2 = "foreign";
        final int numberOfItem2 = 3;

        items.addAll(IntStream.range(0, numberOfItem2)
                .mapToObj(i -> getItem(countryOfOrigin2 + itemName, (long) i + 100, price))
                .toList());

        // when
        itemRepository.saveAll(items);

        // then
        List<Item> savedItems = itemRepository.findAllByNameContaining(itemName);
        assertThat(savedItems)
                .hasSize(numberOfItem1 + numberOfItem2)
                .extracting(Item::getName)
                .containsOnly(countryOfOrigin1 + itemName, countryOfOrigin2 + itemName);

        assertThat(savedItems)
                .filteredOn(item -> item.getName().equals(countryOfOrigin2 + itemName))
                .hasSize(numberOfItem2);
    }

    @DisplayName("지정된 가격 범위에 포함된 모든 상품 조회")
    @Test
    void findAllBy_Between_Test() {
        // given
        final String itemName1 = "apple";
        final int numberOfItem1 = 2;
        final List<Long> prices1 = List.of(800L, 2000L);

        final String itemName2 = "kiwi";
        final int numberOfItem2 = 3;
        final List<Long> prices2 = List.of(1800L, 2000L, 12000L);

        List<Item> items = Stream.concat(
                IntStream.range(0, numberOfItem1)
                        .mapToObj(i -> getItem(itemName1, (long) i + 100, prices1.get(i))),
                IntStream.range(0, numberOfItem2)
                        .mapToObj(i -> getItem(itemName2, (long) i + 100, prices2.get(i))))
                        .toList();

        // when
        itemRepository.saveAll(items);

        // then
        final long lowestPrice = 1000, highestPrice = 2000;
        List<Item> savedItems = itemRepository.findAllByPriceBetween(lowestPrice, highestPrice);
        long count = Stream.concat(prices1.stream(), prices2.stream())
                .filter(price -> price >= lowestPrice && price <= highestPrice)
                .count();
        assertThat(savedItems.size()).isEqualTo(count);

        assertThat(savedItems)
                .extracting(Item::getPrice)
                .allMatch(p -> p >= lowestPrice && p <= highestPrice);

        assertThat(savedItems)
                .extracting(Item::getName)
                .containsOnly(itemName1, itemName2);
    }

    @DisplayName("지정된 가격 범위에 포함되고 특정 문자열이 포함된 모든 상품 조회")
    @Test
    void findAllBy_Containing_and_Between_Test() {
        // given
        final String itemName1 = "apple";
        final int numberOfItem1 = 2;
        final List<Long> prices1 = List.of(800L, 1500L);
        List<Item> items = IntStream.range(0, numberOfItem1)
                .mapToObj(i -> getItem(itemName1, (long) i + 100, prices1.get(i)))
                .collect(Collectors.toList());

        final String itemName2 = "kiwi";
        final int numberOfItem2 = 4;
        final List<Long> prices2 = List.of(1800L, 2000L, 800L, 300L);
        items.addAll(IntStream.range(0, numberOfItem2)
                .mapToObj(i -> getItem(itemName2, (long) i + 100, prices2.get(i)))
                .toList());

        // when
        itemRepository.saveAll(items);

        // then
        final long lowestPrice = 800, highestPrice = 1500;
        List<Item> savedItems = itemRepository.findALLByNameContainingAndPriceBetween(itemName1, lowestPrice, highestPrice);
        long count = prices1.stream()
                .filter(price -> price >= lowestPrice && price <= highestPrice)
                .count();
        assertThat(savedItems)
                .hasSize((int) count)
                .extracting(Item::getPrice)
                .allMatch(p -> p >= lowestPrice && p <= highestPrice);

        assertThat(savedItems)
                .extracting(Item::getName)
                .containsOnly(itemName1);
    }
}