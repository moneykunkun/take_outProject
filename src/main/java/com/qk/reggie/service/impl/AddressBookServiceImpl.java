package com.qk.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qk.reggie.entity.AddressBook;
import com.qk.reggie.mapper.AddressBookMapper;
import com.qk.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}