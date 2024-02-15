package com.ecommerce.accountservice.domain;

import com.ecommerce.accountservice.exception.AccountException;
import com.ecommerce.accountservice.exception.ExceptionCode;
import com.ecommerce.accountservice.openfeign.OrderServiceClient;
import com.ecommerce.accountservice.web.dto.AccountDto;
import com.ecommerce.accountservice.openfeign.client.OrderDto;
import com.ecommerce.accountservice.web.dto.OrderListDto;
import com.ecommerce.accountservice.web.dto.SignUpDto;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final OrderServiceClient orderServiceClient;

    public AccountDto createAccount(SignUpDto signUpDto) {
        Account account = Account.fromAccountDto(signUpDto);
        account = accountRepository.save(account);
        return AccountDto.fromAccount(account);
    }

    public AccountDto getAccountById(Long userId) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ExceptionCode.NOT_FOUND_ACCOUNT));
        return AccountDto.fromAccount(account);
    }

    public OrderListDto getOrderList(Long userId, @Nullable Long cursorOrderId) {
        boolean isExist = accountRepository.existsById(userId);
        if(!isExist)
            throw new AccountException(ExceptionCode.NOT_FOUND_ACCOUNT);

        List<OrderDto> orderDtoList;
        if(cursorOrderId != null)
            orderDtoList = orderServiceClient.getOrdersByCursorOrderId(userId, cursorOrderId);
        else
            orderDtoList = orderServiceClient.getOrders(userId);

        return OrderListDto.fromUserIdAndOrderDtoList(userId, orderDtoList);
    }
}
