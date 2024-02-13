package com.ecommerce.itemservice.domain.discount;
//
//import com.nayoung.itemservice.domain.item.Item;
//import com.nayoung.itemservice.exception.DiscountException;
//import com.nayoung.itemservice.exception.ExceptionCode;
//import com.nayoung.itemservice.web.dto.ItemDto;
//
//import java.util.Objects;
//
//public enum DiscountCode {
//
//    NONE(0), SILVER(5), GOLD(10);
//
//    public final int percentage;
//
//    DiscountCode(int percentage) {
//        this.percentage = percentage;
//    }
//
//    public static DiscountCode getDiscountCode(String customerRating) {
//        if(Objects.equals(customerRating, "UNQUALIFIED")) return DiscountCode.NONE;
//        if(Objects.equals(customerRating, "WELCOME")) return DiscountCode.NONE;
//        if(Objects.equals(customerRating, "SILVER")) return DiscountCode.SILVER;
//        if(Objects.equals(customerRating, "GOLD")) return DiscountCode.GOLD;
//        throw new DiscountException(ExceptionCode.NO_MATCHING_DISCOUNT_CODE);
//    }
//
//    public static ItemDto applyDiscountByItem(Item item, DiscountCode customerDiscountCode) {
//        ItemDto response = ItemDto.fromItem(item);
//        response.setPrice(getDiscountedPrice(response.getPrice(), item.getDiscountPercentage(), customerDiscountCode));
//        return response;
//    }
//
//    public static ItemDto applyDiscountByItemDto(ItemDto itemDto, DiscountCode customerDiscountCode) {
//        Long discountedPrice = getDiscountedPrice(itemDto.getPrice(), itemDto.getDiscountPercentage(), customerDiscountCode);
//        itemDto.setDiscountedPrice(discountedPrice);
//        return itemDto;
//    }
//
//    private static Long getDiscountedPrice(long price, int discountPercentage, DiscountCode customerDiscountCode) {
//        if(customerDiscountCode.percentage <= discountPercentage) {
//            return price * (100 - discountPercentage) / 100;
//        }
//        return price * (100 - customerDiscountCode.percentage) / 100;
//    }
//}
