package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
//        return userMapper.selectById(id);
        //Redis重构此方法
        User user = getCache(id);
        if(user == null){
            user = initCache(id);
        }
        return user;
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        //空值处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空！");
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空！");
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg","该账号已存在！");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg","该邮箱已存在！");
            return map;
        }

        //注册用户,即写入数据库
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.MD5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //http://localhost:8080/community/activation/{id}/code
        String url = domain + contextPath + "/activation/" + user.getId() +"/"+ user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }

    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

    //登录功能
    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object> map = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空~");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空~");
            return map;
        }

        //合法性验证,查库，比较账号存在否,是否激活,在比较密码
        User user = userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","该账号不存在~");
            return map;
        }
        if(user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活~");
            return map;
        }
        password = CommunityUtil.MD5(password+user.getSalt());
        if(!password.equals(user.getPassword())){
            map.put("passwordMsg","密码不对哦~");
            return map;
        }

        //成功，生成凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds*1000));
        //存入数据库
//        loginTicketMapper.insertLoginTicket(loginTicket);

        //存入redis
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    //退出登录
    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    //查询凭证
    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    //更新用户头像
    public int updateHeader(int userId,String headerUrl){
//        return userMapper.updateHeader(userId,headerUrl);
        int rows = userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;
    }

    // 修改密码
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword, String passwordAgain) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空!");
            return map;
        }

        // 验证原始密码
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.MD5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "原密码输入有误!");
            return map;
        }

        //两次输入新密码不一致
        if(!newPassword.equals(passwordAgain)){
            map.put("notMatchMsg","两次输入的密码不一致!");
            return map;
        }

        // 更新密码
        newPassword = CommunityUtil.MD5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);
        clearCache(userId);

        return map;
    }

    //忘记密码
    public Map<String,Object> resetPassword(String email,String password){
        Map<String,Object> map = new HashMap<>();
        //空值判断
        if(StringUtils.isBlank(email)){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        //验证邮箱
        User user = userMapper.selectByEmail(email);
        if(user == null){
            map.put("emailMsg","邮箱未被注册");
            return map;
        }

        //重置密码
        password = CommunityUtil.MD5(password+user.getSalt());
        userMapper.updatePassword(user.getId(),password);
        clearCache(user.getId());

        map.put("user",user);
        return map;
    }

    public User findUserByName(String name){
        return userMapper.selectByName(name);
    }

    /* 1.优先从缓存中取值
       2.取不到时初始化缓存数据
       3.数据变更时删除缓存
     */
        // 第1步
        private User getCache(int userId) {
            String redisKey = RedisKeyUtil.getUserKey(userId);
            return (User) redisTemplate.opsForValue().get(redisKey);
        }

        //第2步
        private User initCache(int userId){
            User user = userMapper.selectById(userId);
            String redisKey = RedisKeyUtil.getUserKey(userId);
            redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
            return user;
        }

        //第3步
        private void clearCache(int userId){
            String redisKey = RedisKeyUtil.getUserKey(userId);
            redisTemplate.delete(redisKey);
        }
}
