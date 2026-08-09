package org.example.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.entity.dto.Account;
import org.example.mapper.AccountMapper;
import org.example.service.AccountService;
import org.example.utils.Const;
import org.example.utils.FlowUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    FlowUtils flowUtils;

    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Account account = findAccountByUsernameOrEmail(username);
        if (account == null) {
            throw new UsernameNotFoundException("用户名或邮箱不存在");
        }
        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    public Account findAccountByUsernameOrEmail(String text) {
        return this.query()
                .eq("username", text)
                .or()
                .eq("email", text)
                .one();
    }

    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) {
            if (!this.verifyLimit(ip)) {
                return "请求过于频繁，请稍后再试";
            }
            Random random = new Random();
            String code = String.valueOf(random.nextInt(100000, 999999));
            Map<String, Object> data = Map.of(
                    "type", type,
                    "email", email,
                    "code", code
            );
            amqpTemplate.convertAndSend("mailQueue", data);
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        }
    }

    private boolean verifyLimit(String ip) {
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return flowUtils.limitOnceCheck(key, 60);
    }

}
