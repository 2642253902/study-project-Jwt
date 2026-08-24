package org.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.entity.dto.Account;
import org.example.entity.vo.request.ConfirmResetVO;
import org.example.entity.vo.request.EmailRegisterVO;
import org.example.entity.vo.request.EmailResetVO;
import org.example.mapper.AccountMapper;
import org.example.service.AccountService;
import org.example.utils.Const;
import org.example.utils.FlowUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    @Resource
    PasswordEncoder passwordEncoder;

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

    @Override
    public String registerEmailAccount(EmailRegisterVO emailRegisterVo) {
        String email = emailRegisterVo.getEmail();
        String username = emailRegisterVo.getUsername();
        String key = Const.VERIFY_EMAIL_DATA + email;
        String code = stringRedisTemplate.opsForValue().get(key);
        if (code == null) {
            return "请先获取验证码";
        }
        if (!code.equals(emailRegisterVo.getCode())) {
            return "验证码错误，请重新输入";
        }
        if (this.existAccountByEmail(email)) {
            return "该邮箱已注册，请直接登录";
        }
        if (this.existAccountByUsername(username)) {
            return "该用户名已被占用，请更换用户名";
        }
        String password = passwordEncoder.encode(emailRegisterVo.getPassword());
        Account account = new Account(null, username, password, email, "USER", new Date());
        if (this.save(account)) {
            stringRedisTemplate.delete(key);
            return null;
        } else {
            return "注册失败，请稍后再试";
        }
    }

    @Override
    public String resetConfirm(ConfirmResetVO resetVO) {
        String email = resetVO.getEmail();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + resetVO.getEmail());
        if (code == null) {
            return "请先获取验证码";
        }
        if (!code.equals(resetVO.getCode())) {
            return "验证码错误，请重新输入";
        }
        return null;
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO resetVO) {
        String email = resetVO.getEmail();
        String verify = this.resetConfirm(new ConfirmResetVO(email, resetVO.getCode()));
        if (verify != null) {
            return verify;
        }
        String password = passwordEncoder.encode(resetVO.getPassword());
        boolean update = this.update().eq("email", email).set("password", password).update();
        if (update) {
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
            return null;
        }
        return null;
    }

    private boolean existAccountByEmail(String email) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email", email));
    }

    private boolean existAccountByUsername(String username) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username", username));
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
