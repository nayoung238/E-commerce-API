package com.nayoung.itemservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

    NOT_FOUND_ITEM(HttpStatus.BAD_REQUEST, "존재하지 않는 상품입니다."),
    ALREADY_EXIST_ITEM(HttpStatus.BAD_REQUEST, "이미 존재하는 상품입니다."),
    NOT_FOUND_ORDER_DETAILS(HttpStatus.NOT_FOUND, "주문 내역이 없습니다."),
    NOT_FOUND_ITEM_UPDATE_LOG(HttpStatus.NOT_FOUND, "업데이트 기록이 없습니다."),
    DUPLICATE_NAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 이름입니다."),
    INSUFFICIENT_STOCK_EXCEPTION(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),

    NO_MATCHING_DISCOUNT_CODE(HttpStatus.BAD_REQUEST, "매칭되는 할인이 없습니다."),

    NOT_FOUND_SHOP(HttpStatus.BAD_REQUEST, "존재하지 않는 상점입니다."),
    ALREADY_EXIST_SHOP(HttpStatus.BAD_REQUEST, "이미 존재하는 상점입니다."),
    ALREADY_EXIST_SHOP_NAME(HttpStatus.BAD_REQUEST, "사용 중인 상점 이름입니다."),
    NON_SERVICE_LOCATION(HttpStatus.BAD_REQUEST, "서비스를 지원하지 않는 지역입니다."),
    UNAUTHORIZED_MANAGER(HttpStatus.BAD_REQUEST, "권한없는 매니저입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
