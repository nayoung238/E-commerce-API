package com.ecommerce.itemservice.item.repository;

import com.ecommerce.itemservice.item.entity.Item;
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
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @AfterEach
    void afterEach() {
        itemRepository.deleteAll();
    }

    @DisplayName("[아이템 조회 성공 테스트] 상품명으로 검색 시 상품명에 일치한 모든 상품 가져오기")
    @Test
    void find_all_items_test() {
        // given
        final String itemName = "apple";
        final int numberOfItems = 2;
        final long price = 1000L;
        IntStream.range(0, numberOfItems)
                .forEach(i -> {
                    Item item = Item.of(itemName, (long) i + 100, price);
                    itemRepository.save(item);
                });

        IntStream.range(0, numberOfItems)
            .forEach(i -> {
                Item item = Item.of("kiwi", (long) i + 100, price);
                itemRepository.save(item);
            });

        // when
        List<Item> items = itemRepository.findAllByName(itemName);

        // then
        assertThat(items)
                .hasSize(numberOfItems)
                .allMatch(item -> item.getName().equals(itemName))
                .extracting(Item::getName)
                .containsOnly(itemName);
    }

    @DisplayName("[아이템 조회 싪패 테스트] 존재하지 않은 상품명으로 조회 시 Optional.empty 반환")
    @Test
    void not_found_item_failed_test() {
        // given
        final String itemName = "apple";
        final int numberOfItems = 2;
        final long price = 1000L;
        IntStream.range(0, numberOfItems)
                .forEach(i -> {
                    Item item = Item.of(itemName, (long) i + 100, price);
                    itemRepository.save(item);
                });

        // when
        final String searchName = "kiwi";
        List<Item> items = itemRepository.findAllByName(searchName);

        // then
        assertThat(items).isEmpty();
    }

    @DisplayName("[in 절 쿼리 조회 테스트] 여러 조회 조건에 속하는 모든 상 조회")
    @Test
    void findAllBy_In_Test () {
        // given
        final long price = 1000L;
        final String itemName1 = "apple";
        final int numberOfItem1 = 2;

        final String itemName2 = "kiwi";
        final int numberOfItem2 = 5;

        List<Item> items = Stream.concat(
                IntStream.range(0, numberOfItem1)
                        .mapToObj(i -> Item.of(itemName1, (long) i + 100, price)),
                IntStream.range(0, numberOfItem2)
                        .mapToObj(i -> Item.of(itemName2, (long) i + 100, price)))
                        .toList();

        itemRepository.saveAll(items);

        // when
        List<Item> savedItems = itemRepository.findAllByNameIn(List.of(itemName1, itemName2));

        // then
        assertThat(savedItems)
                .hasSize(numberOfItem1 + numberOfItem2)
                .filteredOn(item -> item.getName().equals(itemName1))
                .hasSize(numberOfItem1);

        assertThat(savedItems).filteredOn(item -> item.getName().equals(itemName2))
                .hasSize(numberOfItem2);
    }

    @DisplayName("[like 쿼리 조회 테스트] 특정 문자열이 포함된 모든 객체 조회")
    @Test
    void findAllBy_Containing_Test() {
        // given
        final String itemName = "Apple";
        final long price = 1000L;

        final String countryOfOrigin1 = "domestic";
        final int numberOfItem1 = 2;
        List<Item> items = IntStream.range(0, numberOfItem1)
                .mapToObj(i -> Item.of(countryOfOrigin1 + itemName, (long) i + 100, price))
                .collect(Collectors.toList());

        final String countryOfOrigin2 = "foreign";
        final int numberOfItem2 = 3;

        items.addAll(IntStream.range(0, numberOfItem2)
                .mapToObj(i -> Item.of(countryOfOrigin2 + itemName, (long) i + 100, price))
                .toList());

        itemRepository.saveAll(items);

        // when
        List<Item> savedItems = itemRepository.findAllByNameContaining(itemName);

        // then
        assertThat(savedItems)
                .hasSize(numberOfItem1 + numberOfItem2)
                .extracting(Item::getName)
                .containsOnly(countryOfOrigin1 + itemName, countryOfOrigin2 + itemName);

        assertThat(savedItems)
                .filteredOn(item -> item.getName().equals(countryOfOrigin2 + itemName))
                .hasSize(numberOfItem2);
    }

    @DisplayName("[between 쿼리 조회 테스트] 지정된 가격 범위에 포함된 모든 상품 조회")
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
                        .mapToObj(i -> Item.of(itemName1, (long) i + 100, prices1.get(i))),
                IntStream.range(0, numberOfItem2)
                        .mapToObj(i -> Item.of(itemName2, (long) i + 100, prices2.get(i))))
                        .toList();

        itemRepository.saveAll(items);

        // when
        final long lowestPrice = 1000, highestPrice = 2000;
        List<Item> savedItems = itemRepository.findAllByPriceBetween(lowestPrice, highestPrice);

        // then
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

    @DisplayName("[like, between 쿼리 조회 테스트] 지정된 가격 범위에 포함되고 특정 문자열이 포함된 모든 상품 조회")
    @Test
    void findAllBy_Containing_and_Between_Test() {
        // given
        final String itemName1 = "apple";
        final int numberOfItem1 = 2;
        final List<Long> prices1 = List.of(800L, 1500L);
        List<Item> items = IntStream.range(0, numberOfItem1)
                .mapToObj(i -> Item.of(itemName1, (long) i + 100, prices1.get(i)))
                .collect(Collectors.toList());

        final String itemName2 = "kiwi";
        final int numberOfItem2 = 4;
        final List<Long> prices2 = List.of(1800L, 2000L, 800L, 300L);
        items.addAll(IntStream.range(0, numberOfItem2)
                .mapToObj(i -> Item.of(itemName2, (long) i + 100, prices2.get(i)))
                .toList());

        // when
        itemRepository.saveAll(items);

        // then
        final long lowestPrice = 800, highestPrice = 1500;
        List<Item> savedItems = itemRepository.findAllByNameContainingAndPriceBetween(itemName1, lowestPrice, highestPrice);
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