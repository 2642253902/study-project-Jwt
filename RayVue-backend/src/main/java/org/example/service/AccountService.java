package org.example.service;

import com.baomidou.mybatisplus.spring.service.IService;
import org.example.entity.dto.Account;
import org.example.entity.vo.request.EmailRegisterVo;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {

    Account findAccountByUsernameOrEmail(String text);

    String registerEmailVerifyCode(String type, String email, String ip);

    String registerEmailAccount(EmailRegisterVo emailRegisterVo);

}
