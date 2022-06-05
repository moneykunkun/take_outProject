package com.qk.reggie.controller;

import com.qk.reggie.common.BaseContext;
import com.qk.reggie.common.R;
import com.qk.reggie.entity.AddressBook;
import com.qk.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}",addressBook);

        //调用业务层方法
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }


}
